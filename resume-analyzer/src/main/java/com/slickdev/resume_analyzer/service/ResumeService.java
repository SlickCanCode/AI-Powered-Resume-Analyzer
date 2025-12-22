package com.slickdev.resume_analyzer.service;


import org.springframework.web.multipart.MultipartFile;

import com.slickdev.resume_analyzer.entities.UploadedResume;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.reponses.ResumeAnalysisResponse;
import com.slickdev.resume_analyzer.reponses.ResumeIdResponse;

public interface ResumeService {
    UploadedResume saveResume (UploadedResume resume);
    UploadedResume findResumeByContentAndUser(User user, String content);
    UploadedResume findByContent(String content);
    UploadedResume findById(String id);
    ResumeIdResponse parseFile(MultipartFile file, String userId);
    ResumeAnalysisResponse analyzeResume(String id, String jobDescription);
}
