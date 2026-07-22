package com.slickdev.resume_analyzer.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.entities.VerificationToken;
import com.slickdev.resume_analyzer.repositories.VerificationTokenRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class OtpService {
    
    private final SecureRandom random = new SecureRandom();

    BCryptPasswordEncoder passwordEncoder;
    @Autowired
    public void setEncoder(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    VerificationTokenRepository tokenRepository;
    @Autowired
    public void setRepository(VerificationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public String generateOTP(User user) {
        String otp = String.format("%04d", random.nextInt(1_000_000));
        String otpHashed = passwordEncoder.encode(otp);
        VerificationToken token = VerificationToken.builder().otpHash(otpHashed).user(user).expiresAt(LocalDateTime.now().plusMinutes(5)).build();
        tokenRepository.save(token);
        return otp;
    }

    public void sendOtp(String otp, String email) {
        
    }
}
