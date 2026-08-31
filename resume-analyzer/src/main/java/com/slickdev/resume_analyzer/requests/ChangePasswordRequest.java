package com.slickdev.resume_analyzer.requests;

public record ChangePasswordRequest(String currentPassword, String newPassword) {
    
}
