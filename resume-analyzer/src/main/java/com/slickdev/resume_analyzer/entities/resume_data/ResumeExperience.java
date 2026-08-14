package com.slickdev.resume_analyzer.entities.resume_data;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResumeExperience {
    
    String title;
    String company;
    String period;
    List<String> highlights;
}
