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
import com.slickdev.resume_analyzer.exception.BadRequestException;
import com.slickdev.resume_analyzer.exception.RateLimitException;
import com.slickdev.resume_analyzer.repositories.VerificationTokenRepository;
import com.slickdev.resume_analyzer.service.OtpService;
import com.slickdev.resume_analyzer.service.constants.ServiceConstants;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
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

    public void saveToken(String hashedOtp, User user, VerificationToken lastToken) {
        if (lastToken != null) lastToken.setValid(false);
        VerificationToken token = tokenRepository.save(VerificationToken.builder().otpHash(hashedOtp).user(user).expiresAt(LocalDateTime.now().plusMinutes(5)).build());
        user.addToken(token);
    }

    @Override
    public String generateOtp(User user) {
        List<VerificationToken> userTokens = user.getTokens();
        VerificationToken lastToken = null;
        if (!userTokens.isEmpty() && userTokens != null) {
            lastToken = userTokens.get(userTokens.size() - 1);
            sendIsValid(userTokens, lastToken);
        }
        String otp = String.format("%06d", random.nextInt(1_000_000));
        String otpHashed = passwordEncoder.encode(otp);
        saveToken(otpHashed, user, lastToken);
        return otp;
    }

    @Override
    public void sendOtp(String otp, String email) {
        Resend resend = new Resend(ServiceConstants.RESEND_API_KEY);
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

    @Override
    public void verifyOtp(String otp, User user) {
        List<VerificationToken> userTokens = user.getTokens();
        if (userTokens.isEmpty()) {
            throw new BadRequestException("No OTP found for this user, please request a new OTP");
        }
        VerificationToken lastToken = userTokens.get(userTokens.size() - 1);
        verificationIsValid(lastToken);
        incrementVerificationCount(lastToken);
        if (!passwordEncoder.matches(otp, lastToken.getOtpHash())) {
            throw new BadRequestException("Invalid OTP, please try again");
        }
        lastToken.setValid(false);
        user.setEmailVerified(true);
        tokenRepository.save(lastToken);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementVerificationCount(VerificationToken token) {
        token.setVerificationCount(token.getVerificationCount() + 1);
        tokenRepository.save(token);
    }

    public void sendIsValid(List<VerificationToken> userTokens, VerificationToken lastToken) {
        if (lastToken.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1))) {
            throw new RateLimitException("wait for 1 minute cooldown before resend");
        } else if (userTokens.size() == 5 && 
            userTokens.get(0).getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(60))) {
            throw new RateLimitException("Too much otp sent, please try again in 1 hour");
        }
    }
    
    public void verificationIsValid(VerificationToken token) {
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            token.setValid(false);
            tokenRepository.save(token);
        }
        if (token.getVerificationCount() == 5) {
            token.setValid(false);
            tokenRepository.save(token);
            throw new RateLimitException("Too many verification attempts, please request a new OTP");
        }
        if (token.isValid() == false) {
            throw new BadRequestException("invalid OTP, please request a new OTP");
        }

    }

    
}
