package com.slickdev.resume_analyzer.exception;

import java.time.Instant;

public class GeminiQuotaException extends RuntimeException {

    private final Instant retryAt;

    public GeminiQuotaException(String message, Instant retryAt) {
        super(message);
        this.retryAt = retryAt;
    }

    public Instant getRetryAt() {
        return retryAt;
    }
}