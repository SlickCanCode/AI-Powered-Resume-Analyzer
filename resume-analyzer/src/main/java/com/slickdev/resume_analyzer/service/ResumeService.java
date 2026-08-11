package com.slickdev.resume_analyzer.service;


import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.slickdev.resume_analyzer.entities.UploadedResume;
import com.slickdev.resume_analyzer.reponses.ResumeAnalysisResponse;
import com.slickdev.resume_analyzer.reponses.ResumeIdResponse;
import com.slickdev.resume_analyzer.reponses.ResumeResponse;

public interface ResumeService {
    UploadedResume saveResume (UploadedResume resume);
    UploadedResume findById(String id);
    ResumeIdResponse parseFile(MultipartFile file, String jwt);
    // ResumeAnalysisResponse analyzeResume(String id, String jobDescription);
    // List<ResumeResponse> getUserResumes(String id);
    List<UploadedResume> getAnalyzedResumes(String userId);
}
