package com.slickdev.resume_analyzer.reponses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResumeResponse {
    
    private String name;
    private String source_url;
    private ResumeAnalysisResponse analysis;

}
