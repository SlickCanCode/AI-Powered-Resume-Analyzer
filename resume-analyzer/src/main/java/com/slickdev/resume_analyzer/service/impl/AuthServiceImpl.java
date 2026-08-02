package com.slickdev.resume_analyzer.service.impl;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.reponses.jwtResponse;
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
    public void verifyOtp(String otp, String email, HttpServletResponse response) {
        User user = userService.getUserByEmail(email);
        otpService.verifyOtp(otp, user);
        String jwt = jwtService.generateToken(user);
            ResponseCookie cookie = ResponseCookie.from("access_token", jwt)
            .httpOnly(true)
            .secure(true) // false for local HTTP
            .path("/")
            .sameSite("None") // or "Lax" if frontend is on the same domain
            .maxAge(Duration.ofDays(1))
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
