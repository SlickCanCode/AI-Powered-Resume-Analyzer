package com.slickdev.resume_analyzer.service.ai;


import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class GeminiAvailability {

    private volatile Instant unavailableUntil;

    /**
     * Returns true when Gemini can be attempted.
     */
    public boolean isAvailable() {
        Instant retryAt = unavailableUntil;

        return retryAt == null || Instant.now().isAfter(retryAt);
    }

    /**
     * Marks Gemini as unavailable until the specified time.
     */
    public void markUnavailable(Instant retryAt) {
        if (retryAt == null) {
            throw new IllegalArgumentException("retryAt cannot be null");
        }

        this.unavailableUntil = retryAt;
    }

    /**
     * Marks Gemini as unavailable for the specified duration.
     */
    public void markUnavailable(Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException(
                    "Duration must be greater than zero"
            );
        }

        this.unavailableUntil = Instant.now().plus(duration);
    }

    /**
     * Clears the unavailable state and allows Gemini to be attempted again.
     */
    public void markAvailable() {
        this.unavailableUntil = null;
    }

    /**
     * Returns the time at which Gemini can next be attempted.
     * Returns null if Gemini is currently available.
     */
    public Instant getUnavailableUntil() {
        Instant retryAt = unavailableUntil;

        if (retryAt == null || Instant.now().isAfter(retryAt)) {
            return null;
        }

        return retryAt;
    }
}
