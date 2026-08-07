package com.slickdev.resume_analyzer.repositories;


import org.springframework.data.jpa.repository.JpaRepository;

import com.slickdev.resume_analyzer.entities.SubscriptionUsage;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionUsageRepository extends JpaRepository<SubscriptionUsage, UUID> {

    Optional<SubscriptionUsage> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
