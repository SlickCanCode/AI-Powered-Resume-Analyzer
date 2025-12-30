package com.slickdev.resume_analyzer.service.impl;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.security.SecurityConstants;
import com.slickdev.resume_analyzer.service.JwtService;

@Service
public class JwtServiceImpl implements JwtService{
    

    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtServiceImpl() {
        this.algorithm = Algorithm.HMAC512(SecurityConstants.SECRET_KEY);
        this.verifier = JWT.require(algorithm).build();
    }

    /** Generate a JWT for a user */
    public String generateToken(User user) {
        return JWT.create()
                .withSubject(user.getId().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.TOKEN_EXPIRATION))
                .sign(algorithm);
    }

    // /** Validate token and return true/false */
    // public boolean isTokenValid(String token) {
    //     try {
    //         verifier.verify(token);
    //         return true;
    //     } catch (JWTVerificationException ex) {
    //         return false;
    //     }
    // }

    // /** Extract the user id (subject) from token */
    // public String extractUserId(String token) {
    //     DecodedJWT decoded = verifier.verify(token);
    //     return decoded.getSubject();
    // }

    // /** Extract roles from token */
    // public List<String> extractRoles(String token) {
    //     DecodedJWT decoded = verifier.verify(token);
    //     return decoded.getClaim(ROLES_CLAIM).asList(String.class);
    // }

    // /** Extract expiration date */
    // public Date extractExpiration(String token) {
    //     DecodedJWT decoded = verifier.verify(token);
    //     return decoded.getExpiresAt();
    // }
}
