package com.slickdev.resume_analyzer.service;

import com.slickdev.resume_analyzer.reponses.jwtResponse;

public interface AuthService {
    
    public void sendOtp(String email);
    public jwtResponse verifyOtp(String otp, String email);
}
