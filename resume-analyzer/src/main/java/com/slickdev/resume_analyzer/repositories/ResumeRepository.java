package com.slickdev.resume_analyzer.repositories;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.slickdev.resume_analyzer.entities.UploadedResume;
import com.slickdev.resume_analyzer.entities.User;

public interface ResumeRepository extends JpaRepository<UploadedResume, UUID>{
    
    List<UploadedResume> findAllByUser(User user);
    List<UploadedResume> findByUserIdAndAnalysisCountGreaterThan(UUID userId, int count); 
}
