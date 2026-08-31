package com.slickdev.resume_analyzer.reponses;

import java.util.List;


import com.slickdev.resume_analyzer.entities.resume_analysis.AnalysisGrammerIssue;
import com.slickdev.resume_analyzer.entities.resume_analysis.AnalysisRecommendation;

import lombok.AllArgsConstructor;



import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResumeAnalysisResponse {

    private String analysisId;
    private String resumeId;
    private Integer overallScore;
    private Integer atsScore;
    private Integer keywordScore;

    private List<String> strengths;
    private List<String> weaknesses;
    
    private List<String> existingSkills;
    private List<String> skillsToDevelop;

    private List<AnalysisGrammerIssue> grammarIssues;
    private List<AnalysisRecommendation> recommendations;
}
