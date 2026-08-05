package com.slickdev.resume_analyzer.service.impl;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.security.SecurityConstants;
import com.slickdev.resume_analyzer.service.JwtService;

@Service
public class JwtServiceImpl implements JwtService{
    

    private final Algorithm algorithm;
    public JwtServiceImpl() {
        this.algorithm = Algorithm.HMAC512(SecurityConstants.SECRET_KEY);
    }

    /** Generate a JWT for a user */
    public String generateToken(User user) {
        return JWT.create()
                .withSubject(user.getId().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.TOKEN_EXPIRATION))
                .sign(algorithm);
    }
    
    public String generateResetToken(User user) {
        return JWT.create()
                .withSubject(user.getId().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.RESET_TOKEN_EXPIRATION))
                .sign(algorithm);
    }

    public boolean isValid(String token) {
        try {
            Date expiresAt = JWT.require(algorithm)
                    .build()
                    .verify(token)
                    .getExpiresAt();

            return expiresAt != null && expiresAt.after(new Date());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public String extractUserId(String token) {
        return JWT.require(algorithm)
                .build()
                .verify(token)
                .getSubject();
    }
}
