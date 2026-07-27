package com.slickdev.resume_analyzer.security;

import java.util.List;


public class SecurityConstants {
    public static final String SECRET_KEY = System.getenv("SECRET_KEY"); 
    public static final int TOKEN_EXPIRATION = 7200000; // 7200000 milliseconds = 7200 seconds = 2 hours.
    public static final String BEARER = "Bearer "; 
    public static final String AUTHORIZATION = "Authorization"; 
    public static final String REGISTER_PATH = "/api/v1/users"; // Public path that clients can use to register.
    public static final String AUTH_PATH = "/api/v1/auth";
    public static final String RESUME_UPLOAD_PATH = "/resume/upload";
    public static final String RESUME_ANALYZE_PATH = "/resume/analyze/**";
    public static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:3000",
            "https://ai-powered-resume-analyzer-frontend.vercel.app/"
        );
}
