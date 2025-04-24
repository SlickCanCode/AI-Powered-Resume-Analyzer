package com.slickdev.resume_analyzer.service;

import org.springframework.web.multipart.MultipartFile;

import com.slickdev.resume_analyzer.entities.UploadedResume;

public interface ResumeService {
    UploadedResume saveResume (UploadedResume resume);
    void parseFile(MultipartFile file);
    UploadedResume findUploadedResume(Long id);
}
