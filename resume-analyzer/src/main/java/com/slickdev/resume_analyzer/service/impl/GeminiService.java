package com.slickdev.resume_analyzer.service.impl;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.ThinkingConfig;
import com.slickdev.resume_analyzer.entities.ResumeAnalysis;
import com.slickdev.resume_analyzer.entities.ResumeData;
import com.slickdev.resume_analyzer.exception.ServiceUnavailableException;
import com.slickdev.resume_analyzer.reponses.JobMatchResponse;
import com.slickdev.resume_analyzer.service.constants.ServiceConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {
    
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
        System.out.println(jobContent);
        String response = sendGeminiReq(config, String.format(ServiceConstants.JOB_MATCH_PROMPT, resumeContent, jobContent), "Job Match Analysis");

        try {
            return new ObjectMapper().readValue(response, JobMatchResponse.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to interpret the job match analysis.", exception);
        }
    }

    public String sendGeminiReq(GenerateContentConfig config, String prompt, String service) {
            try {
                    GenerateContentResponse response =
                    geminiClient.models.generateContent
                    ("gemini-2.5-flash", prompt, config);
                    return response.text();
            
            } catch (Exception e) {

                 log.error("Gemini API failed while parsing resume", e);
                throw new ServiceUnavailableException(service);
            }
        
    }
}
