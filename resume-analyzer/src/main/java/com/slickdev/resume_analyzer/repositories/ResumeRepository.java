package com.slickdev.resume_analyzer.repositories;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import com.slickdev.resume_analyzer.entities.UploadedResume;

public interface ResumeRepository extends JpaRepository<UploadedResume, UUID>{
    

    List<UploadedResume> findAllByUserId(UUID userId);
    @EntityGraph(attributePaths = "analysis")
    List<UploadedResume> findByUserIdAndAnalysisCountGreaterThan(UUID userId, int count); 
    boolean existsByIdAndUserId(UUID resumeId, UUID userId);
    void deleteByIdAndUserId(UUID resumeId, UUID userId);
}
