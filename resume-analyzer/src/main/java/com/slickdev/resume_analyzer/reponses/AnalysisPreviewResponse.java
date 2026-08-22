package com.slickdev.resume_analyzer.reponses;

public record AnalysisPreviewResponse(String id, String resumeId, String fileName, int resumeScore, int atsScore, String date) {
    
}
