package com.slickdev.resume_analyzer.service.impl;


import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.reponses.VerifyOtpResponse;
import com.slickdev.resume_analyzer.service.AuthService;
import com.slickdev.resume_analyzer.service.JwtService;
import com.slickdev.resume_analyzer.service.OtpService;
import com.slickdev.resume_analyzer.service.UserService;

import jakarta.servlet.http.HttpServletResponse;


@Service
public class AuthServiceImpl implements AuthService {
    
    UserService userService;
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    JwtService jwtService;
    @Autowired
    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    OtpService otpService;
    @Autowired
    public void setOtpService(OtpService otpService) {
        this.otpService = otpService;
    }

    @Override
    public void sendOtp(String email) {
        User user = userService.getUserByEmail(email);
        otpService.sendOtp(otpService.generateOtp(user), email);
    }

    @Override
    public VerifyOtpResponse verifyOtp(String otp, String email, HttpServletResponse response, String purpose) {
        User user = userService.getUserByEmail(email);
        otpService.verifyOtp(otp, user);
        jwtService.sendRefreshAndAccessTokens(response, user);
    String resetToken = "No reset Token needed";

        if (purpose != null && purpose.equals("reset-password")) {
            resetToken = jwtService.generateResetToken(user);
        }
        return new VerifyOtpResponse(resetToken);
    }

    @Override
    public void resetPassword(String jwt, String newPassword, String resetToken) {
        String userId = jwtService.extractUserId(jwt);
        if (!jwtService.extractUserId(resetToken).equals(userId)) throw new IllegalArgumentException("Invalid token");
        if (!jwtService.isValid(resetToken)) throw new IllegalArgumentException("Expired reset token");

        userService.resetPassword(userService.getUser(userId), newPassword);
    }
    
    @Override
    public void sendAccessToken(HttpServletResponse response, String refreshToken) {
            jwtService.validateToken(refreshToken);
            jwtService.sendAccessToken(response, refreshToken, userService.getUser(jwtService.extractUserId(refreshToken)));
    }

    @Override
    public void logOutUser(HttpServletResponse response) {
    ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ZERO)
            .sameSite("None")
            .build();
    
    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ZERO)
            .sameSite("None")
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    }

}
