package com.slickdev.resume_analyzer.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.drew.lang.annotations.NotNull;
import com.slickdev.resume_analyzer.entities.resume_data.ResumeEducation;
import com.slickdev.resume_analyzer.entities.resume_data.ResumeExperience;
import com.slickdev.resume_analyzer.entities.resume_data.ResumeOnlineProfile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
    name = "resume_data",
    indexes = {
        @Index(name = "idx_resume_data_resume_id", columnList = "resume_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "resume_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_resume_data_resume")
    )
    private UploadedResume resume;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(name = "career_summary", columnDefinition = "TEXT")
    private String careerSummary;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "online_profiles", columnDefinition = "jsonb")
    private List<ResumeOnlineProfile> onlineProfiles;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> skills;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<ResumeExperience> experience;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<ResumeEducation> education;
    
}
