package com.slickdev.resume_analyzer.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.slickdev.resume_analyzer.entities.ResumeData;

public interface ResumeDataRepository extends JpaRepository<ResumeData, UUID>   {
    
}
