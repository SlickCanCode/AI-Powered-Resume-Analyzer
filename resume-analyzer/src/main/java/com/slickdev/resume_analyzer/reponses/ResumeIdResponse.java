package com.slickdev.resume_analyzer.reponses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResumeIdResponse {
    private String resumeId;

    public ResumeIdResponse (String resumeId) {
        this.resumeId = resumeId;
    }
    
}
