package com.slickdev.resume_analyzer.security.filters;

import java.io.IOException;
import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.exception.ApiError;
import com.slickdev.resume_analyzer.reponses.jwtResponse;
import com.slickdev.resume_analyzer.requests.LoginRequest;
import com.slickdev.resume_analyzer.security.manager.CustomAuthenticationManager;
import com.slickdev.resume_analyzer.service.JwtService;
import com.slickdev.resume_analyzer.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    
    private CustomAuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
             LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);
             Authentication authentication = new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());
            return authenticationManager.authenticate(authentication);

        }catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {

                User user = userService.getUserByEmail(authResult.getName());

        String token = jwtService.generateToken(user);
        ResponseCookie cookie = ResponseCookie.from("access_token", token)
            .httpOnly(true)
            .secure(true)      // false only for local HTTP development
            .path("/")
            .sameSite("None")   // or "Lax" if frontend is on the same domain
            .maxAge(Duration.ofDays(1))
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    response.setStatus(HttpServletResponse.SC_OK);

    // Cookie uses SameSite=None; 
    // Secure if frontend and backend are on different sites in production. 
    // For local HTTP development, you may need secure(false) with SameSite=Lax depending on your setup.
}


    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
                ApiError error = new ApiError(
                HttpServletResponse.SC_UNAUTHORIZED,
                failed.getMessage()
            );
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getWriter(), error);
    }
   
}
