package com.slickdev.resume_analyzer.service;



import com.slickdev.resume_analyzer.reponses.VerifyOtpResponse;

import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    
    public void sendOtp(String email);
    public VerifyOtpResponse verifyOtp(String otp, String email, HttpServletResponse response, String purpose);
    public void resetPassword(String jwt, String newPassword, String resetToken);
}
