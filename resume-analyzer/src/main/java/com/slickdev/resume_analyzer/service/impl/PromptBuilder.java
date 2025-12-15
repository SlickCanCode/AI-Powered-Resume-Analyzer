package com.slickdev.resume_analyzer.service.impl;

import org.springframework.stereotype.Service;

import com.slickdev.resume_analyzer.service.constants.ServiceConstants;

@Service
public class PromptBuilder {
    public String buildPrompt(String resumeContent, String jobDescription) {
        return String.format(ServiceConstants.RESUME_ANALYSIS_PROMPT, resumeContent, jobDescription);
    }
}
