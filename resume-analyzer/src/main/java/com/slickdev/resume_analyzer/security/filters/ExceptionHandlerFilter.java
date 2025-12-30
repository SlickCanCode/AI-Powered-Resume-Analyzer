package com.slickdev.resume_analyzer.security.filters;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slickdev.resume_analyzer.exception.ApiError;
import com.slickdev.resume_analyzer.exception.EntityNotFoundException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ExceptionHandlerFilter extends OncePerRequestFilter{
    

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try{
            filterChain.doFilter(request, response);
        } catch (EntityNotFoundException e) {
            ApiError error = new ApiError(
                HttpServletResponse.SC_NOT_FOUND,
                "Username or Email does not exist"
            );

            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getWriter(), error);

        }catch (JWTVerificationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }catch (RuntimeException e) {
            ApiError error = new ApiError(
                HttpServletResponse.SC_BAD_REQUEST,
                e.getMessage()
            );
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getWriter(), error);
        }
    }
}
