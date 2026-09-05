package com.slickdev.resume_analyzer.service;

import org.springframework.stereotype.Service;

import com.slickdev.resume_analyzer.entities.ResumeAnalysis;
import com.slickdev.resume_analyzer.entities.ResumeData;
import com.slickdev.resume_analyzer.reponses.JobMatchResponse;

@Service 
public interface AiService {
    
    public ResumeData parseResume(String resumeContent);
    public ResumeAnalysis analyzeResume(String resumeContent, String jobDescription);
    public JobMatchResponse analyzeJobMatch(String resumeContent, String jobContent);
}
