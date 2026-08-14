package com.slickdev.resume_analyzer.service;


import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.slickdev.resume_analyzer.entities.UploadedResume;
import com.slickdev.resume_analyzer.reponses.AnalysisSummaryResponse;
import com.slickdev.resume_analyzer.reponses.JobMatchResponse;
import com.slickdev.resume_analyzer.reponses.ResumeAnalysisResponse;
import com.slickdev.resume_analyzer.reponses.ResumeDataResponse;
import com.slickdev.resume_analyzer.reponses.ResumeIdResponse;
import com.slickdev.resume_analyzer.reponses.ResumeResponse;

public interface ResumeService {
    UploadedResume saveResume (UploadedResume resume);
    UploadedResume findById(String id);
    ResumeDataResponse parseFile(MultipartFile file, String jwt);
    ResumeAnalysisResponse analyzeResume(String id, String jobDescription);
    List<ResumeResponse> getAllResumes(String jwt);
    List<UploadedResume> getAnalyzedResumes(String userId);
    JobMatchResponse analyzeJobMatch(String id, String jobLink);
    ResumeDataResponse getResumeData(String resumeId, String jwt);
    List<ResumeAnalysisResponse> getResumeAnalyses(String resumeId, String jwt);
    List<AnalysisSummaryResponse> getAllAnalyses(String jwt);
}
