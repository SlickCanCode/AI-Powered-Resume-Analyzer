package com.slickdev.resume_analyzer.service.ai.Gemini;


import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GeminiAvailability {

    private final Map<GeminiModel, Instant> unavailableUntil =
            new ConcurrentHashMap<>();

    /**
     * Returns true when the specified Gemini model can be attempted.
     */
    public boolean isAvailable(GeminiModel model) {
        validateModel(model);

        Instant retryAt = unavailableUntil.get(model);

        if (retryAt == null) {
            return true;
        }

        if (Instant.now().isAfter(retryAt)) {
            unavailableUntil.remove(model, retryAt);
            return true;
        }

        return false;
    }

    /**
     * Marks the specified Gemini model as unavailable
     * until the specified time.
     */
    public void markUnavailable(
            GeminiModel model,
            Instant retryAt
    ) {
        validateModel(model);

        if (retryAt == null) {
            throw new IllegalArgumentException(
                    "retryAt cannot be null"
            );
        }

        unavailableUntil.put(model, retryAt);
    }

    /**
     * Marks the specified Gemini model as unavailable
     * for the specified duration.
     */
    public void markUnavailable(
            GeminiModel model,
            Duration duration
    ) {
        validateModel(model);

        if (duration == null || duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException(
                    "Duration must be greater than zero"
            );
        }

        unavailableUntil.put(
                model,
                Instant.now().plus(duration)
        );
    }

    /**
     * Marks the specified Gemini model as available again.
     */
    public void markAvailable(GeminiModel model) {
        validateModel(model);

        unavailableUntil.remove(model);
    }

    /**
     * Returns the time at which the specified model
     * can next be attempted.
     *
     * Returns null if the model is currently available.
     */
    public Instant getUnavailableUntil(GeminiModel model) {
        validateModel(model);

        Instant retryAt = unavailableUntil.get(model);

        if (retryAt == null) {
            return null;
        }

        if (Instant.now().isAfter(retryAt)) {
            unavailableUntil.remove(model, retryAt);
            return null;
        }

        return retryAt;
    }

        public boolean areAllModelsUnavailable() {
        for (GeminiModel model : GeminiModel.values()) {
            if (isAvailable(model)) {
                return false;
            }
        }

        return true;
    }


    private void validateModel(GeminiModel model) {
        if (model == null) {
            throw new IllegalArgumentException(
                    "Gemini model cannot be null"
            );
        }
    }
}