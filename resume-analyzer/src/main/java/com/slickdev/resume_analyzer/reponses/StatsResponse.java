package com.slickdev.resume_analyzer.reponses;

public record StatsResponse(int analyzedResumes, int analyzedResumesThisWeek,
    int avgScore, int avgScoreImprovThisWeek, int atsScore, int avgAtsScoreImproveThisWeek, 
    int analysesLeft
) {}
