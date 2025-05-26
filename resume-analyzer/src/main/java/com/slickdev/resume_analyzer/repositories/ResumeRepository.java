package com.slickdev.resume_analyzer.repositories;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.slickdev.resume_analyzer.entities.UploadedResume;
import com.slickdev.resume_analyzer.entities.User;

public interface ResumeRepository extends JpaRepository<UploadedResume, UUID>{
    
    boolean existsByUserAndContent(User user, String content);
    boolean existsByContent(String content);
    Optional<UploadedResume> findByUserAndContent(User user, String content);
    Optional<UploadedResume> findByContent(String content);
    
}
