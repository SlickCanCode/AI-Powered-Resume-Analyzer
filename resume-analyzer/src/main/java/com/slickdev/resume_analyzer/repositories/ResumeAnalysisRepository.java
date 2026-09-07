package com.slickdev.resume_analyzer.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.slickdev.resume_analyzer.entities.ResumeAnalysis;
import com.slickdev.resume_analyzer.reponses.AnalysisPreviewResponse;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, UUID> { 
    Optional<ResumeAnalysis> findFirstByResumeIdAndResumeUserId(UUID resumeId, UUID userId);
    void deleteFirstByResumeId(UUID resumeId);

    @Query("""
            select new com.slickdev.resume_analyzer.reponses.AnalysisPreviewResponse(analysis.id, 
                resume.id, resume.filename, analysis.overallScore, analysis.atsScore, analysis.createdAt)
            from ResumeAnalysis analysis
            join analysis.resume resume
            where resume.user.id = :userId
            order by analysis.createdAt desc
            """)
    List<AnalysisPreviewResponse> findAllSummariesByUserId(@Param("userId") UUID userId);
} 
