package com.slickdev.resume_analyzer.service.ai;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.slickdev.resume_analyzer.entities.ResumeAnalysis;
import com.slickdev.resume_analyzer.entities.ResumeData;
import com.slickdev.resume_analyzer.exception.ServiceUnavailableException;
import com.slickdev.resume_analyzer.reponses.JobMatchResponse;
import com.slickdev.resume_analyzer.service.AiService;
import com.slickdev.resume_analyzer.service.constants.ServiceConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Service 
@RequiredArgsConstructor 
public class OpenAiService implements AiService {

    private final ChatClient chatClient;

    public ResumeData parseResume(String resumeContent) {
        try {
            return chatClient.prompt()
                    .user(String.format(
                            ServiceConstants.RESUME_PARSING_PROMPT,
                            resumeContent
                    ))
                    .call()
                    .entity(ResumeData.class);
        } catch (Exception e) {
            log.error("OpenAI failed while parsing resume", e);
            throw new ServiceUnavailableException("Resume Parsing");
        }
    }

    public ResumeAnalysis analyzeResume(
            String resumeContent,
            String jobDescription) {

        try {
            return chatClient.prompt()
                    .user(String.format(
                            ServiceConstants.RESUME_ANALYSIS_PROMPT,
                            resumeContent,
                            jobDescription
                    ))
                    .call()
                    .entity(ResumeAnalysis.class);
        } catch (Exception e) {
            log.error("OpenAI failed while analyzing resume", e);
            throw new ServiceUnavailableException("Resume Analysis");
        }
    }

    public JobMatchResponse analyzeJobMatch(
            String resumeContent,
            String jobContent) {

        try {
            return chatClient.prompt()
                    .user(String.format(
                            ServiceConstants.JOB_MATCH_PROMPT,
                            resumeContent,
                            jobContent
                    ))
                    .call()
                    .entity(JobMatchResponse.class);
        } catch (Exception e) {
            log.error("OpenAI failed while analyzing job match", e);
            throw new ServiceUnavailableException("Job Match Analysis");
        }
    }
}
