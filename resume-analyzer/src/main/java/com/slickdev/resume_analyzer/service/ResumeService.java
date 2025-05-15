package com.slickdev.resume_analyzer.service;


import org.springframework.web.multipart.MultipartFile;

import com.slickdev.resume_analyzer.entities.UploadedResume;
import com.slickdev.resume_analyzer.entities.User;

public interface ResumeService {
    UploadedResume saveResume (UploadedResume resume);
    String parseFile(MultipartFile file, String userId);
    String analyzeResume(String id, String jobDescription);
    UploadedResume findResumeByContentAndUser(User user, String content);
    UploadedResume findByContent(String content);
}
