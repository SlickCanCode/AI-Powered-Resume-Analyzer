package com.slickdev.resume_analyzer.requests;

import com.drew.lang.annotations.NotNull;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisRequest {
    
    @NotBlank
    @NotNull
    String jobDescription;
}
