package com.slickdev.resume_analyzer.reponses;

import java.util.List;


import com.slickdev.resume_analyzer.entities.resume_data.ResumeEducation;
import com.slickdev.resume_analyzer.entities.resume_data.ResumeExperience;
import com.slickdev.resume_analyzer.entities.resume_data.ResumeOnlineProfile;

import lombok.Builder;

@Builder
public record ResumeDataResponse(String resumeId, String fullName, String email, String phone, String location, String summary, 
    List<ResumeOnlineProfile> onlineProfiles, List<String> skills, List<ResumeExperience> experience, List<ResumeEducation> education
) {
    
}
