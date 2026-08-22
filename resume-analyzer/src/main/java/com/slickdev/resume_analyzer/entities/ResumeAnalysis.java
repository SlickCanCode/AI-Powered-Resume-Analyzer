package com.slickdev.resume_analyzer.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.drew.lang.annotations.NotNull;
import com.slickdev.resume_analyzer.entities.resume_analysis.AnalysisGrammerIssue;
import com.slickdev.resume_analyzer.entities.resume_analysis.AnalysisRecommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "resume_analysis",
    indexes = {
        @Index(name = "idx_resume_analysis_resume_id", columnList = "resume_id"),
        @Index(name = "idx_resume_analysis_created_at", columnList = "created_at"),
        @Index(
            name = "idx_resume_analysis_resume_created_at",
            columnList = "resume_id, created_at DESC"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "resume_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_resume_analysis_resume")
    )
    private UploadedResume resume;

    @Column(name = "overall_score", nullable = false)
    private Integer overallScore;

    @Column(name = "ats_score", nullable = false)
    private Integer atsScore;

    @Column(name = "keyword_score", nullable = false)
    private Integer keywordScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "strengths", nullable = false, columnDefinition = "jsonb")
    private List<String> strengths;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "weaknesses", nullable = false, columnDefinition = "jsonb")
    private List<String> weaknesses;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "existing_skills", nullable = false, columnDefinition = "jsonb")
    private List<String> existingSkills;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skills_to_develop", nullable = false, columnDefinition = "jsonb")
    private List<String> skillsToDevelop;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "grammar_issues", nullable = false, columnDefinition = "jsonb")
    private List<AnalysisGrammerIssue> grammarIssues;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recommendations", nullable = false, columnDefinition = "jsonb")
    private List<AnalysisRecommendation> recommendations;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}