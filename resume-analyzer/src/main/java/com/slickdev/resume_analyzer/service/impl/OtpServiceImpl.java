package com.slickdev.resume_analyzer.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.resend.*;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.entities.VerificationToken;
import com.slickdev.resume_analyzer.repositories.VerificationTokenRepository;
import com.slickdev.resume_analyzer.service.OtpService;
import com.slickdev.resume_analyzer.service.constants.ServiceConstants;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class OtpServiceImpl implements OtpService {
    
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

    public void saveToken(String hashedOtp, User user) {
        VerificationToken token = tokenRepository.save(VerificationToken.builder().otpHash(hashedOtp).user(user).expiresAt(LocalDateTime.now().plusMinutes(5)).build());
        user.getTokens().add(token);
    }

    @Override
    public String generateOtp(User user) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        String otpHashed = passwordEncoder.encode(otp);
        tokenRepository.deleteAllByUser(user);
        System.out.println(tokenRepository.count());
        saveToken(otpHashed, user);
        return otp;
    }

    @Override
    public void sendOtp(String otp, String email) {
        Resend resend = new Resend("re_4MSDBHzk_KuguZKztYKiKf8PdpmQrcMZz");

                CreateEmailOptions params = CreateEmailOptions.builder()
                .from("onboarding@resend.dev")
                .to(email)
                .subject(ServiceConstants.EMAIL_OTP_SUBJECT)
                .html(String.format(ServiceConstants.EMAIL_OTP_BODY, otp))
                .build();

        try {
             resend.emails().send(params);
        } catch (ResendException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

    
}
