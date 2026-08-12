package com.slickdev.resume_analyzer.reponses;

import java.util.List;
import java.util.Map;


import com.slickdev.resume_analyzer.entities.resume_analysis.AnalysisGrammerIssue;
import com.slickdev.resume_analyzer.entities.resume_analysis.AnalysisRecommendation;

import lombok.AllArgsConstructor;
import lombok.Builder;



import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResumeAnalysisResponse {

    private String analysisId;
    private Integer overallScore;
    private Integer atsScore;
    private Integer keywordScore;

    private List<String> strengths;
    private List<String> missingKeywords;
    private List<String> foundKeywords;

    private List<AnalysisGrammerIssue> grammarIssues;
    private List<AnalysisRecommendation> recommendations;
}
