package com.slickdev.resume_analyzer.reponses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResumeResponse {
    
    String name;
    String uploadDate;
    int latestScore;
    int analysisCount;
}
