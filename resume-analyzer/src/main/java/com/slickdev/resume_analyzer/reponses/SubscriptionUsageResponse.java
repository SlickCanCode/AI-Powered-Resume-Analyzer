package com.slickdev.resume_analyzer.reponses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for subscription usage information returned to clients.
 * Provides quota and usage metrics for frontend UI.
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SubscriptionUsageResponse {
    
    /** Current subscription plan (FREE, PRO, etc.) */
    private String plan;
    
    /** Total analyses allowed in current billing period */
    private Integer analysesAllowed;
    
    /** Analyses already used in current billing period */
    private Integer analysesUsed;
    
    /** Remaining analyses quota */
    private Integer analysesRemaining;
    
    /** Percentage of quota used (0-100) */
    private Integer usagePercentage;
    
    /** Whether user has analyses remaining */
    private Boolean hasQuotaRemaining;

    /**
     * Factory method to build response from usage metrics.
     * Automatically calculates remaining and percentage.
     */
    public static SubscriptionUsageResponse fromMetrics(
            String plan,
            Integer analysesAllowed,
            Integer analysesUsed) {
        
        Integer remaining = analysesAllowed - analysesUsed;
        Integer percentage = (int) ((double) analysesUsed / analysesAllowed * 100);
        Boolean hasQuota = remaining > 0;
        
        return SubscriptionUsageResponse.builder()
                .plan(plan)
                .analysesAllowed(analysesAllowed)
                .analysesUsed(analysesUsed)
                .analysesRemaining(remaining)
                .usagePercentage(percentage)
                .hasQuotaRemaining(hasQuota)
                .build();
    }
}
