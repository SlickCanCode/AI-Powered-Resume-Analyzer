package com.slickdev.resume_analyzer.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "subscription_usage",
        indexes = {
                @Index(name = "idx_usage_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionUsage {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_usage_user")
    )
    private User user;

    @Column(name = "resume_analyses_used", nullable = false)
    @Builder.Default
    private Integer resumeAnalysesUsed = 0;

    @Column(name = "resumes_analyses_allowed", nullable = false)
    @Builder.Default
    private Integer resumesAnalysesAllowed = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}