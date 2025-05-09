package com.slickdev.resume_analyzer.security;

public class SecurityConstants {
    public static final String SECRET_KEY = System.getenv("SECRET_KEY"); //Your secret should always be strong (uppercase, lowercase, numbers, symbols) so that nobody can potentially decode the signature.
    public static final int TOKEN_EXPIRATION = 7200000; // 7200000 milliseconds = 7200 seconds = 2 hours.
    public static final String BEARER = "Bearer "; // Authorization : "Bearer " + Token 
    public static final String AUTHORIZATION = "Authorization"; // "Authorization" : Bearer Token
    public static final String REGISTER_PATH = "/user/register"; // Public path that clients can use to register.
    public static final String UPLOAD_PATH = "/upload";
    public static final String RESUME_PATH = "/resume/**";
}
