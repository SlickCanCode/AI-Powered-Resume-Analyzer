package com.slickdev.resume_analyzer.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.service.AuthService;
import com.slickdev.resume_analyzer.service.OtpService;
import com.slickdev.resume_analyzer.service.UserService;


@Service
public class AuthServiceImpl implements AuthService {
    
    UserService userService;
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    OtpService otpService;
    @Autowired
    public void setOtpService(OtpService otpService) {
        this.otpService = otpService;
    }

    @Override
    public void sendVerification(String email) {
        User user = userService.getUserByEmail(email);
        otpService.sendOtp(otpService.generateOtp(user), email);
    }
}
