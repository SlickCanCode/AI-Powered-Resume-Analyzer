package com.slickdev.resume_analyzer.service;

import com.slickdev.resume_analyzer.entities.Subscription;
import com.slickdev.resume_analyzer.entities.SubscriptionUsage;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.entities.enums.SubscriptionPlan;

public interface SubscriptionService {
    
    /**
     * Creates a default FREE subscription for a new user.
     * Called during user registration.
     */
    void createDefaultSubscription(User user);

    Subscription checkSubscription(String userId);

    /**
     * Gets the subscription for a user.
     */
    Subscription getSubscription(String userId);

    /**
     * Gets the subscription usage for a user.
     */
    SubscriptionUsage getSubscriptionUsage(String userId);

    /**
     * Check if user has remaining analyses quota.
     */
    boolean hasAnalysisQuota(String userId);

    /**
     * Increments the analysis usage count for a user.
     * Should be called after a successful analysis.
     * 
     * @throws com.slickdev.resume_analyzer.exception.RateLimitException if quota exceeded
     */
    void incrementAnalysisUsage(String userId);

    /**
     * Get the number of analyses allowed for a user based on their plan.
     */
    int getAnalysesAllowed(String userId);

    /**
     * Get the number of analyses already used by a user in current period.
     */
    int getAnalysesUsed(String userId);

    /**
     * Resets usage for a user (typically called at start of new billing period).
     */
    void resetUsageForNewPeriod(String userId);

    /**
     * Upgrade or downgrade user subscription plan.
     */
    void updateSubscriptionPlan(String userId, SubscriptionPlan newPlan, int months);
}
