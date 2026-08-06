package com.slickdev.resume_analyzer.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.slickdev.resume_analyzer.entities.ResumeAnalysis;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, UUID> { 

    
    
} 
