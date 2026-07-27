package com.slickdev.resume_analyzer.entities;

import java.time.LocalDateTime;
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
import lombok.Setter;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "verification_token")
public class VerificationToken {
    
    @Id
    @GeneratedValue()
    private UUID id;

    @NotNull
    @Column(name="otp_hash", nullable=false)
    private String otpHash;

    @NotNull
    @Column(name = "valid", nullable = false)
    @Builder.Default
    private boolean valid = true;

    @NotNull
    @Column(name = "verification_count", nullable = false)
    @Builder.Default
    private int verificationCount = 0;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @NotNull
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
