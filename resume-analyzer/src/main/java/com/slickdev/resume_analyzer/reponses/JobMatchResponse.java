package com.slickdev.resume_analyzer.reponses;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobMatchResponse {
    
    private Integer matchScore;
    private List<String> foundSkills;
    private List<String> missingSkills;
    private List<String> aiSuggestions;
}
