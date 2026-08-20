package com.slickdev.resume_analyzer.config;

import com.slickdev.resume_analyzer.entities.enums.SubscriptionPlan;

/**
 * Configuration for subscription plans.
 * Centralized location for subscription limits to follow DRY principle.
 * Easy to change: Simply modify values here for new plan configurations.
 */
public enum SubscriptionPlanConfig {
    FREE(SubscriptionPlan.FREE, 5, "Free tier with 5 monthly analyses"),
    PRO(SubscriptionPlan.PRO, 25, "Professional tier with 25 monthly analyses");

    private final SubscriptionPlan plan;
    private final int monthlyAnalysisLimit;
    private final String description;

    SubscriptionPlanConfig(SubscriptionPlan plan, int monthlyAnalysisLimit, String description) {
        this.plan = plan;
        this.monthlyAnalysisLimit = monthlyAnalysisLimit;
        this.description = description;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public int getMonthlyAnalysisLimit() {
        return monthlyAnalysisLimit;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get configuration for a specific subscription plan.
     * Returns configuration or throws IllegalArgumentException if plan not found.
     */
    public static SubscriptionPlanConfig getConfig(SubscriptionPlan plan) {
        for (SubscriptionPlanConfig config : values()) {
            if (config.plan == plan) {
                return config;
            }
        }
        throw new IllegalArgumentException("Unknown subscription plan: " + plan);
    }
}
