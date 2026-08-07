package com.slickdev.resume_analyzer.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.drew.lang.annotations.NotNull;

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

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "resume_analysis")
public class ResumeAnalysis {
    
    @Id
    @GeneratedValue()
    private UUID id;

    @NotNull
    @Column(name = "score", nullable = false)
    private int score;

    @NotNull
    @Column(name = "ats_score", nullable = false)
    private int atsScore;

    @NotNull
    @Column(name = "strengths", nullable = false)
    private List<String> strengths;

    @NotNull
    @Column(name = "weaknesses", nullable = false)
    private List<String> weaknesses;

    @NotNull
    @Column(name = "improvement_suggestions", nullable = false)
    private List<String> improvementSuggestions;

    @ManyToOne
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    private UploadedResume resume;

    @NotNull
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
