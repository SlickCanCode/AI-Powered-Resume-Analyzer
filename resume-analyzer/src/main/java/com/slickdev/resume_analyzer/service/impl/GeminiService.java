package com.slickdev.resume_analyzer.service.impl;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.ThinkingConfig;
import com.slickdev.resume_analyzer.entities.ResumeAnalysis;
import com.slickdev.resume_analyzer.entities.ResumeData;
import com.slickdev.resume_analyzer.reponses.JobMatchResponse;
import com.slickdev.resume_analyzer.service.constants.ServiceConstants;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeminiService {
    
    private final Client geminiClient;

    
    public ResumeData parseResume(String resumeContent) {

        GenerateContentConfig config = 
            GenerateContentConfig.builder()
                // Sets the thinking budget to 0 to disable thinking mode
                .thinkingConfig(ThinkingConfig.builder().thinkingBudget(0).build())
                .responseMimeType("application/json")
                .candidateCount(1)
                .responseSchema(ServiceConstants.PARSING_SCHEMA)
                .build();

        GenerateContentResponse response =
        geminiClient.models.generateContent("gemini-2.5-flash", String.format(ServiceConstants.RESUME_PARSING_PROMPT, resumeContent), config);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ResumeData resumeData = objectMapper.readValue(response.text(), ResumeData.class);
            return resumeData;
        } catch (Exception e) {
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

        GenerateContentResponse response = geminiClient.models.
            generateContent("gemini-2.5-flash", String.format(ServiceConstants.RESUME_ANALYSIS_PROMPT, resumeContent, jobDescription), config);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ResumeAnalysis analysisData = objectMapper.readValue(response.text(), ResumeAnalysis.class);
            return analysisData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public JobMatchResponse analyzeJobMatch(String resumeContent, String jobContent) {
        GenerateContentConfig config = GenerateContentConfig.builder()
                .thinkingConfig(ThinkingConfig.builder().thinkingBudget(0).build())
                .responseMimeType("application/json")
                .candidateCount(1)
                .responseSchema(ServiceConstants.JOB_MATCH_SCHEMA)
                .build();

        GenerateContentResponse response = geminiClient.models.generateContent(
                "gemini-2.5-flash",
                String.format(ServiceConstants.JOB_MATCH_PROMPT, resumeContent, jobContent),
                config);

        try {
            return new ObjectMapper().readValue(response.text(), JobMatchResponse.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to interpret the job match analysis.", exception);
        }
    }
}
