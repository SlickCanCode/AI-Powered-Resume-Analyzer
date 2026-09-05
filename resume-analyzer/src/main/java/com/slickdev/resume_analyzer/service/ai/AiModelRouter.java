package com.slickdev.resume_analyzer.service.ai;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.slickdev.resume_analyzer.entities.ResumeAnalysis;
import com.slickdev.resume_analyzer.entities.ResumeData;
import com.slickdev.resume_analyzer.exception.GeminiQuotaException;
import com.slickdev.resume_analyzer.reponses.JobMatchResponse;
import com.slickdev.resume_analyzer.service.AiService;

@Component
public class AiModelRouter {

    private final GeminiService geminiService;
    private final OpenAiService openAiService;
    private final GeminiAvailability geminiAvailability;

    public AiModelRouter(
            GeminiService geminiService,
            OpenAiService openAiService,
            GeminiAvailability geminiAvailability
    ) {
        this.geminiService = geminiService;
        this.openAiService = openAiService;
        this.geminiAvailability = geminiAvailability;
    }

    public ResumeData parseResume(String resumeContent) {
        return execute(
                service -> service.parseResume(resumeContent)
        );
    }

    public ResumeAnalysis analyzeResume(
            String resumeContent,
            String jobDescription
    ) {
        return execute(
                service -> service.analyzeResume(
                        resumeContent,
                        jobDescription
                )
        );
    }

    public JobMatchResponse analyzeJobMatch(
            String resumeContent,
            String jobContent
    ) {
        return execute(
                service -> service.analyzeJobMatch(
                        resumeContent,
                        jobContent
                )
        );
    }

    private <T> T execute(Function<AiService, T> operation) {

        if (!geminiAvailability.isAvailable()) {
            return operation.apply(openAiService);
        }

        try {
            return operation.apply(geminiService);

        } catch (GeminiQuotaException e) {

            geminiAvailability.markUnavailable(
                    e.getRetryAt()
            );

            return operation.apply(openAiService);
        }
    }
}