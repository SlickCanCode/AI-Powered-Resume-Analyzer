package com.slickdev.resume_analyzer.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.drew.lang.annotations.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "resumes")
public class UploadedResume {
    
    @Id
    @GeneratedValue()
    private UUID id;

    @Column(name = "file_name")
    private String filename;

    @Column(name = "file_type")
    private String contentType;  

    @NotNull
    @Column(name = "analysis_count")
    @Builder.Default
    private int analysisCount = 0;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ResumeAnalysis> analysis;

    @OneToOne(mappedBy = "resume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ResumeData resumeData;

    @NotNull
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    
    public UploadedResume (String fileName, String contentType, String content, User user, ResumeData resumeData) {
        this.filename = fileName;
        this.contentType = contentType;
        this.user = user;
        this.resumeData = resumeData;
    }

}
