package com.slickdev.resume_analyzer.service.ai.Gemini;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.errors.ApiException;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Tool;
import com.google.genai.types.UrlContext;
import com.slickdev.resume_analyzer.entities.ResumeAnalysis;
import com.slickdev.resume_analyzer.entities.ResumeData;
import com.slickdev.resume_analyzer.exception.GeminiQuotaException;
import com.slickdev.resume_analyzer.exception.ServiceUnavailableException;
import com.slickdev.resume_analyzer.reponses.JobMatchResponse;
import com.slickdev.resume_analyzer.service.AiService;
import com.slickdev.resume_analyzer.service.constants.ServiceConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService implements AiService {
    
    private final GeminiAvailability geminiAvailability;
    private final Client geminiClient;
    
    public ResumeData parseResume(String resumeContent) {

        GenerateContentConfig config = 
            GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .candidateCount(1)
                .responseSchema(ServiceConstants.PARSING_SCHEMA)
                .build();

        String response = sendGeminiReq(config, String.format(ServiceConstants.RESUME_PARSING_PROMPT, resumeContent), "Resume Parsing");

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ResumeData resumeData = objectMapper.readValue(response, ResumeData.class);
            return resumeData;
        } catch (Exception e) {
            log.warn("Object mapper was unable to interpret resume analysis to object");
            e.printStackTrace();
        }
        return null;
    }

    public ResumeAnalysis analyzeResume(String resumeContent, String jobDescription) {

        GenerateContentConfig config = 
            GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .candidateCount(1)
                .responseSchema(ServiceConstants.ANALYSIS_SCHEMA)
                .build();

        String response = sendGeminiReq(config, String.format(ServiceConstants.RESUME_ANALYSIS_PROMPT, resumeContent, jobDescription), "Resume Analysis");

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ResumeAnalysis analysisData = objectMapper.readValue(response, ResumeAnalysis.class);
            return analysisData;
        } catch (Exception e) {
            log.warn("Object mapper was unable to interpret resume analysis to object");
            e.printStackTrace();
        }
        return null;

    }

    public JobMatchResponse analyzeJobMatch(String resumeContent, String jobContent) {
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .candidateCount(1)
                .responseSchema(ServiceConstants.JOB_MATCH_SCHEMA)
                .tools(
                        Tool.builder()
                            .urlContext(
                                UrlContext.builder().build()
                            )
                            .build()
                    )
                .build();
                
        String response = sendGeminiReq(config, String.format(ServiceConstants.JOB_MATCH_PROMPT, resumeContent, jobContent), "Job Match Analysis");

        try {
            return new ObjectMapper().readValue(response, JobMatchResponse.class);
        } catch (Exception exception) {
            log.warn("Object mapper was unable to interpret job match analysis to response");
            throw new IllegalStateException("Unable to interpret the job match analysis.", exception);
        }
    }

  public String sendGeminiReq(
        GenerateContentConfig config,
        String prompt,
        String service
) {
    GeminiQuotaException lastQuotaException = null;

    for (GeminiModel model : GeminiModel.values()) {

        if (!geminiAvailability.isAvailable(model)) {
            log.debug(
                    "Skipping unavailable Gemini model: {}",
                    model.getModelName()
            );
            continue;
        }

        try {
            return sendRequest(model, config, prompt);

        } catch (ApiException e) {

            if (isQuotaOrRateLimitError(e)) {

                GeminiQuotaException quotaException =
                        createQuotaException(model, e);

                geminiAvailability.markUnavailable(
                        model,
                        quotaException.getRetryAt()
                );

                lastQuotaException = quotaException;

                continue;
            }

            logGeminiFailure(model, e);

            throw new ServiceUnavailableException(service);
        }
    }

    if (lastQuotaException != null) {
        throw lastQuotaException;
    }

    throw new ServiceUnavailableException(service);
}
    private String sendRequest(
            GeminiModel model,
            GenerateContentConfig config,
            String prompt
    ) {
        GenerateContentResponse response =
                geminiClient.models.generateContent(
                        model.getModelName(),
                        prompt,
                        config
                );
        log.info("Generated Ai Response from Gemini model {}", model.getModelName());
        return response.text();
    }

    private boolean isQuotaOrRateLimitError(ApiException e) {
    return e.code() == 429
            && "RESOURCE_EXHAUSTED".equals(e.status());
}
private GeminiQuotaException createQuotaException(
        GeminiModel model,
        ApiException e
) {
    Instant retryAt = determineRetryAt(e);

    log.warn(
            "Gemini model quota/rate limit reached. " +
            "model={}, retryAt={}, status={}, message={}",
            model.getModelName(),
            retryAt,
            e.status(),
            e.message()
    );

    return new GeminiQuotaException(
            e.message(),
            retryAt
    );
}

private void logGeminiFailure(
        GeminiModel model,
        ApiException e
) {
    log.error(
            "Gemini API failed. model={}, code={}, status={}, message={}",
            model.getModelName(),
            e.code(),
            e.status(),
            e.message(),
            e
    );
}
    private Instant determineRetryAt(ApiException e) {
    // Gemini's API error information does not always provide
    // a reliable retry timestamp.
    //
    // For a daily quota exhaustion, use the next documented
    // quota reset time.
    //
    // For short-term rate limits, don't mark Gemini unavailable
    // for the entire day; let the retry/backoff mechanism handle it.

    if ("quota_exceeded".equalsIgnoreCase(e.status())) {
        return nextGeminiQuotaReset();
    }

    return Instant.now().plusSeconds(60);
}

private Instant nextGeminiQuotaReset() {
    ZoneId pacific = ZoneId.of("America/Los_Angeles");

    ZonedDateTime now = ZonedDateTime.now(pacific);

    ZonedDateTime nextMidnight = now
            .plusDays(1)
            .toLocalDate()
            .atStartOfDay(pacific);

    return nextMidnight.toInstant();
}
}
