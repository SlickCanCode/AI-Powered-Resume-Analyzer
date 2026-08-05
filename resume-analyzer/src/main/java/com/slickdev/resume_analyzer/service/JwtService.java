package com.slickdev.resume_analyzer.service;



import com.slickdev.resume_analyzer.entities.User;

public interface JwtService {

    String generateToken(User user);
    String extractUserId(String token);
    String generateResetToken(User user);
    boolean isValid(String token);
    // List<String> extractRoles(String token);
    // Date extractExpiration(String token);

}
