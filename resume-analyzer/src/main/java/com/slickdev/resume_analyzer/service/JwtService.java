package com.slickdev.resume_analyzer.service;



import com.slickdev.resume_analyzer.entities.User;

import jakarta.servlet.http.HttpServletResponse;

public interface JwtService {

    String generateRefreshToken(User user);
    String generateAccessToken(User user);
    String extractUserId(String token);
    String generateResetToken(User user);
    boolean isValid(String token);
    void validateToken(String token);
    public void sendRefreshAndAccessTokens(HttpServletResponse response, User user);
    public void sendAccessToken(HttpServletResponse response, String refreshToken, User user);
    // List<String> extractRoles(String token);
    // Date extractExpiration(String token);

}
