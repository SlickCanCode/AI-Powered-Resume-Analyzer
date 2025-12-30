package com.slickdev.resume_analyzer.service;



import com.slickdev.resume_analyzer.entities.User;

public interface JwtService {

    String generateToken(User user);
    // String extractUserId(String token);
    // List<String> extractRoles(String token);
    // Date extractExpiration(String token);

}
