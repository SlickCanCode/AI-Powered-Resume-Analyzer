package com.slickdev.resume_analyzer.repositories;


import java.util.Optional;
import java.util.UUID;


import org.springframework.data.jpa.repository.JpaRepository;

import com.slickdev.resume_analyzer.entities.Subscription;
import com.slickdev.resume_analyzer.entities.enums.SubscriptionStatus;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    Optional<Subscription> findByProviderSubscriptionId(String providerSubscriptionId);

    Optional<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);
}