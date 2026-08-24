package com.slickdev.resume_analyzer.security;

import java.time.Duration;
import java.util.List;


public class SecurityConstants {
    public static final String SECRET_KEY = System.getenv("SECRET_KEY"); 
    public static final long ACCESS_TOKEN_EXPIRATION = Duration.ofMinutes(15).toMillis();
    public static final long REFRESH_TOKEN_EXPIRATION =  Duration.ofDays(7).toMillis();
    public static final int RESET_TOKEN_EXPIRATION = 300000; // 300000 milliseconds = 300 seconds = 5 minutes.
    public static final String BEARER = "Bearer "; 
    public static final String AUTHORIZATION = "Authorization"; 
    public static final String REGISTER_PATH = "/api/v1/users"; // Public path that clients can use to register.
    public static final String RESUME_UPLOAD_PATH = "/resume/upload";
    public static final String RESUME_ANALYZE_PATH = "/resume/analyze/**";
    public static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:3000",
            "https://ai-powered-resume-analyzer-frontend.vercel.app/"
        );
}
