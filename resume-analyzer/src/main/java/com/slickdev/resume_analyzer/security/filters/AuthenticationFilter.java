package com.slickdev.resume_analyzer.security.filters;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.exception.ApiError;
import com.slickdev.resume_analyzer.reponses.AuthResponse;
import com.slickdev.resume_analyzer.reponses.UserResponseDto;
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
             Authentication authentication = new UsernamePasswordAuthenticationToken(loginRequest.getUserNameOrEmail(), loginRequest.getPassword());
            return authenticationManager.authenticate(authentication);

        }catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {

                User user = userService.getUserByUsernameOrEmail(authResult.getName());

        String token = jwtService.generateToken(user);
                AuthResponse response2 = new AuthResponse(token, new UserResponseDto(user.getId(), user.getUserName(), user.getEmail()));

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getWriter(), response2);
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
