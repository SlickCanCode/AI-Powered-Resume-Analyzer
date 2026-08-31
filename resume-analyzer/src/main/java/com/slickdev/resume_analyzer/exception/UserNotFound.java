package com.slickdev.resume_analyzer.exception;

public class UserNotFound extends RuntimeException{
    
    public UserNotFound(String id) {
        super("user with the id " + id + " does not exist in our records");
    }
}
