package com.slickdev.resume_analyzer.service.impl;

import com.slickdev.resume_analyzer.service.constants.ServiceConstants;

public class PromptBuilder {
   
    public String buildPrompt(String resumeContent, String jobDescription) {
        return String.format(ServiceConstants.RESUME_ANALYSIS_PROMPT, resumeContent, jobDescription);
    }
}
