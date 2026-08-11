package com.slickdev.resume_analyzer.service.impl;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.ThinkingConfig;
import com.slickdev.resume_analyzer.entities.ResumeData;
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
        System.out.println("Gemini Response: " + response.text());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Convert the response text to a JSON object
            ResumeData resumeData = objectMapper.readValue(response.text(), ResumeData.class);
            // Convert the JSON object back to a pretty-printed JSON string
            return resumeData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
