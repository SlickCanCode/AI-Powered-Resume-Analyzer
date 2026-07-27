package com.slickdev.resume_analyzer.service;

import com.slickdev.resume_analyzer.reponses.AuthResponse;

public interface AuthService {
    
    public void sendOtp(String email);
    public AuthResponse verifyOtp(String otp, String email);
}
