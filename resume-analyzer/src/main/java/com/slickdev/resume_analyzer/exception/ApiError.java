package com.slickdev.resume_analyzer.exception;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiError {
    
    private int status;
    private String message;
    private String timestamp;

    public ApiError(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = Instant.now().toString();
    }
}
