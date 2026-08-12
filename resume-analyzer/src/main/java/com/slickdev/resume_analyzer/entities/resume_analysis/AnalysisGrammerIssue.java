package com.slickdev.resume_analyzer.entities.resume_analysis;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnalysisGrammerIssue {
    
    String text;
    String suggestion;
}
