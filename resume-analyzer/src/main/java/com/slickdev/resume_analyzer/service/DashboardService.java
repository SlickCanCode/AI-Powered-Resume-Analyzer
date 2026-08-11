package com.slickdev.resume_analyzer.service;

import java.util.List;

import com.slickdev.resume_analyzer.reponses.AnalysisPreviewResponse;
import com.slickdev.resume_analyzer.reponses.StatsResponse;

public interface DashboardService {
    
    public List<AnalysisPreviewResponse> getRecentAnalyses(String jwt);
    public StatsResponse getStats(String jwt);
}
