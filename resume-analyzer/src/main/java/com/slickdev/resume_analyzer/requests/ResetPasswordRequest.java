package com.slickdev.resume_analyzer.requests;

public record ResetPasswordRequest(String newPassword, String resetToken) {}
