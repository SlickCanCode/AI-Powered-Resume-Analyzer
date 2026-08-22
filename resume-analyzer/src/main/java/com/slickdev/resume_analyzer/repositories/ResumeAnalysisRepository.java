package com.slickdev.resume_analyzer.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.slickdev.resume_analyzer.entities.ResumeAnalysis;
import com.slickdev.resume_analyzer.reponses.AnalysisSummaryResponse;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, UUID> { 
    Optional<ResumeAnalysis> findFirstByResumeId(UUID resumeId);

    @Query("""
            select new com.slickdev.resume_analyzer.reponses.AnalysisSummaryResponse(
                resume.id, resume.filename, analysis.createdAt, analysis.overallScore, analysis.atsScore)
            from ResumeAnalysis analysis
            join analysis.resume resume
            where resume.user.id = :userId
            order by analysis.createdAt desc
            """)
    List<AnalysisSummaryResponse> findAllSummariesByUserId(@Param("userId") UUID userId);
} 
