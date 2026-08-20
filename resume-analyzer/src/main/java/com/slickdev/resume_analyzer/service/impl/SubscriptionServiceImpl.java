package com.slickdev.resume_analyzer.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.slickdev.resume_analyzer.config.SubscriptionPlanConfig;
import com.slickdev.resume_analyzer.entities.Subscription;
import com.slickdev.resume_analyzer.entities.SubscriptionUsage;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.entities.enums.SubscriptionPlan;
import com.slickdev.resume_analyzer.entities.enums.SubscriptionStatus;
import com.slickdev.resume_analyzer.exception.EntityNotFoundException;
import com.slickdev.resume_analyzer.exception.RateLimitException;
import com.slickdev.resume_analyzer.repositories.SubscriptionRepository;
import com.slickdev.resume_analyzer.repositories.SubscriptionUsageRepository;
import com.slickdev.resume_analyzer.service.SubscriptionService;

import java.util.Optional;
import java.util.UUID;


@Service
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private SubscriptionRepository subscriptionRepository;

    @Autowired
    public void setSubscriptionRepository(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    private SubscriptionUsageRepository usageRepository;

    @Autowired
    public void setUsageRepository(SubscriptionUsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    /**
     * Creates a default FREE subscription for a new user.
     * Also creates a SubscriptionUsage record with initial limits.
     */
    @Override
    public void createDefaultSubscription(User user) {
        // Create default subscription
        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(SubscriptionPlan.FREE)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        subscriptionRepository.save(subscription);

        // Create subscription usage record with limits from plan config
        int monthlyLimit = SubscriptionPlanConfig.getConfig(SubscriptionPlan.FREE).getMonthlyAnalysisLimit();
        SubscriptionUsage usage = SubscriptionUsage.builder()
                .user(user)
                .resumeAnalysesUsed(0)
                .resumesAnalysesAllowed(monthlyLimit)
                .build();

        usageRepository.save(usage);
    }

    /**
     * Gets the subscription for a user.
     * Throws EntityNotFoundException if not found.
     */
    @Override
    public Subscription getSubscription(String userId) {
        UUID refinedId = formatUUID(userId);
        return subscriptionRepository.findByUserId(refinedId)
                .orElseThrow(() -> new EntityNotFoundException(refinedId, Subscription.class));
    }

    /**
     * Gets the subscription usage for a user.
     * Throws EntityNotFoundException if not found.
     */
    @Override
    public SubscriptionUsage getSubscriptionUsage(String userId) {
        UUID refinedId = formatUUID(userId);
        return usageRepository.findByUserId(refinedId)
                .orElseThrow(() -> new EntityNotFoundException(refinedId, SubscriptionUsage.class));
    }

    /**
     * Check if user has remaining analyses quota.
     */
    @Override
    public boolean hasAnalysisQuota(String userId) {
        SubscriptionUsage usage = getSubscriptionUsage(userId);
        return usage.getResumeAnalysesUsed() < usage.getResumesAnalysesAllowed();
    }

    /**
     * Increments the analysis usage count for a user.
     * Checks quota before incrementing.
     * 
     * @throws RateLimitException if quota exceeded
     */
    @Override
    public void incrementAnalysisUsage(String userId) {
        SubscriptionUsage usage = getSubscriptionUsage(userId);

        if (!hasAnalysisQuota(userId)) {
            throw new RateLimitException(
                    String.format("Analysis quota exceeded. You have used %d out of %d allowed analyses.",
                            usage.getResumeAnalysesUsed(), usage.getResumesAnalysesAllowed())
            );
        }

        usage.setResumeAnalysesUsed(usage.getResumeAnalysesUsed() + 1);
        usageRepository.save(usage);
    }

    /**
     * Get the number of analyses allowed for a user based on their plan.
     */
    @Override
    public int getAnalysesAllowed(String userId) {
        SubscriptionUsage usage = getSubscriptionUsage(userId);
        return usage.getResumesAnalysesAllowed();
    }

    /**
     * Get the number of analyses already used by a user in current period.
     */
    @Override
    public int getAnalysesUsed(String userId) {
        SubscriptionUsage usage = getSubscriptionUsage(userId);
        return usage.getResumeAnalysesUsed();
    }

    /**
     * Resets usage for a user (typically called at start of new billing period).
     */
    @Override
    public void resetUsageForNewPeriod(String userId) {
        SubscriptionUsage usage = getSubscriptionUsage(userId);
        Subscription subscription = getSubscription(userId);

        // Reset usage counter
        usage.setResumeAnalysesUsed(0);

        // Update allowed analyses based on current plan
        int monthlyLimit = SubscriptionPlanConfig
                .getConfig(subscription.getPlan())
                .getMonthlyAnalysisLimit();
        usage.setResumesAnalysesAllowed(monthlyLimit);

        usageRepository.save(usage);
    }

    /**
     * Upgrade or downgrade user subscription plan.
     * Updates both subscription and usage records.
     */
    @Override
    public void updateSubscriptionPlan(String userId, SubscriptionPlan newPlan) {
        Subscription subscription = getSubscription(userId);
        SubscriptionUsage usage = getSubscriptionUsage(userId);

        // Update subscription plan
        subscription.setPlan(newPlan);
        subscriptionRepository.save(subscription);

        // Update usage limits based on new plan
        int newMonthlyLimit = SubscriptionPlanConfig.getConfig(newPlan).getMonthlyAnalysisLimit();
        usage.setResumesAnalysesAllowed(newMonthlyLimit);
        usageRepository.save(usage);
    }

    /**
     * Helper method to format UUID string from user input.
     * Ensures proper UUID format for database queries.
     */
    private UUID formatUUID(String raw) {
        return UUID.fromString(raw.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5"
        ));
    }
}

