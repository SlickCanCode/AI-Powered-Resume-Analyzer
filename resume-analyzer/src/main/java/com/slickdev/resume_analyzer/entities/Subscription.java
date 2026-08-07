package com.slickdev.resume_analyzer.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.slickdev.resume_analyzer.entities.enums.PaymentProvider;
import com.slickdev.resume_analyzer.entities.enums.SubscriptionPlan;
import com.slickdev.resume_analyzer.entities.enums.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "subscriptions",
        indexes = {
                @Index(name = "idx_subscription_user", columnList = "user_id"),
                @Index(name = "idx_subscription_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_subscription_user")
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriptionPlan plan = SubscriptionPlan.FREE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_provider", nullable = false)
    @Builder.Default
    private PaymentProvider paymentProvider = PaymentProvider.NONE;

    @Column(name = "provider_subscription_id")
    private String providerSubscriptionId;

    @Column(name = "current_period_start")
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}