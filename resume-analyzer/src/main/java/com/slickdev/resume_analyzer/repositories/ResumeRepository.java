package com.slickdev.resume_analyzer.repositories;
import org.springframework.data.jpa.repository.JpaRepository;

import com.slickdev.resume_analyzer.entities.UploadedResume;

public interface ResumeRepository extends JpaRepository<UploadedResume, Long>{
    
    boolean existsByContent(String content);
}
