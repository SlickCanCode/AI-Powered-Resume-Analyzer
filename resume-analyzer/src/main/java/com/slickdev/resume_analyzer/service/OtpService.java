package com.slickdev.resume_analyzer.service;

import com.slickdev.resume_analyzer.entities.User;

public interface OtpService {
    
    public String generateOtp(User user);
    public void sendOtp(String otp, String email);
}
