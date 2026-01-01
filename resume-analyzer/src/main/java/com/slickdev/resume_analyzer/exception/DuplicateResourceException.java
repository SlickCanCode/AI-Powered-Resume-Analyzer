package com.slickdev.resume_analyzer.exception;

public class DuplicateResourceException extends RuntimeException{
    
    public DuplicateResourceException(String resource) {
        super(resource+" already in use");
    }
}
