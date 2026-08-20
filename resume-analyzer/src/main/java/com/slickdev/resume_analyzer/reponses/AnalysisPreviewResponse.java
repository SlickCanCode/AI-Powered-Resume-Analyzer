package com.slickdev.resume_analyzer.reponses;

public record AnalysisPreviewResponse(String id, String fileName, int resumeScore, int atsScore, String date) {
    
}
