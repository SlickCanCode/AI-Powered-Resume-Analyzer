package com.slickdev.resume_analyzer.reponses;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResumeAnalysisResponse {
    
    private int score;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> improvementSuggestions;
    private List<Map<String,String>> jobRecommendations;

}
