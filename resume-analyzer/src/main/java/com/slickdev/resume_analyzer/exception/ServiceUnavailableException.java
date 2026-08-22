package com.slickdev.resume_analyzer.exception;

public class ServiceUnavailableException extends RuntimeException{
    
    public ServiceUnavailableException(String service) {
        super(service + " is unavailable at the moment, pls try again later.");
    }
}
