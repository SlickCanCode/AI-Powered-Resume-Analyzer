package com.slickdev.resume_analyzer.service;

import com.slickdev.resume_analyzer.reponses.jwtResponse;

import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    
    public void sendOtp(String email);
    public void verifyOtp(String otp, String email, HttpServletResponse response);
}
