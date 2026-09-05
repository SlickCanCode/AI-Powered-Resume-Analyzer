package com.slickdev.resume_analyzer.service.impl;

import java.time.Duration;
import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.security.SecurityConstants;
import com.slickdev.resume_analyzer.service.JwtService;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class JwtServiceImpl implements JwtService{
    

    private final Algorithm algorithm;
    public JwtServiceImpl() {
        this.algorithm = Algorithm.HMAC512(SecurityConstants.JWT_SECRET);
    }

    /** Generate a JWT for a user */
    public String generateAccessToken(User user) {
        return JWT.create()
                .withSubject(user.getId().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.ACCESS_TOKEN_EXPIRATION))
                .sign(algorithm);
    }

    public String generateRefreshToken(User user) {
        return JWT.create()
                .withSubject(user.getId().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.REFRESH_TOKEN_EXPIRATION))
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

    public void validateToken(String token) {
        JWT.require(algorithm)
                    .build()
                    .verify(token);
    }

    public String extractUserId(String token) {
        return JWT.require(algorithm)
                .build()
                .verify(token)
                .getSubject();
    }

    @Override
    public void sendAccessToken(HttpServletResponse response, String refreshToken, User user) {

            String accessToken = generateAccessToken(user);
            ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
            .httpOnly(true)
            .secure(true)      // false only for local HTTP development
            .path("/")
            .sameSite("None")   // or "Lax" if frontend is on the same domain
            .maxAge(Duration.ofMinutes(15))
            .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    public void sendRefreshAndAccessTokens(HttpServletResponse response, User user) {

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
            .httpOnly(true)
            .secure(true)      // false only for local HTTP development
            .path("/")
            .sameSite("None")   // or "Lax" if frontend is on the same domain
            .maxAge(Duration.ofMinutes(15))
            .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .secure(true)      // false only for local HTTP development
            .path("/")
            .sameSite("None")   // or "Lax" if frontend is on the same domain
            .maxAge(Duration.ofDays(7))
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    response.setStatus(HttpServletResponse.SC_OK);
    }
}
