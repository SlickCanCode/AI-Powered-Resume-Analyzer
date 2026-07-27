package com.slickdev.resume_analyzer.exception;

public class RateLimitException extends RuntimeException{
    
    public RateLimitException(String message) {
        super(message);
    }
}
