package com.slickdev.resume_analyzer.security.filters;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.slickdev.resume_analyzer.exception.EntityNotFoundException;
import com.slickdev.resume_analyzer.security.SecurityConstants;
import com.slickdev.resume_analyzer.service.JwtService;
import com.slickdev.resume_analyzer.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter{
    
    JwtService jwtService;
    @Autowired 
    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    UserService userService;
    @Autowired
    public void setUser(UserService userService) {
        this.userService = userService;
    }

    //Authorization: Bearer JWT
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();
        String token = null;

        String path = request.getServletPath();
        if (path.equals(SecurityConstants.REGISTER_PATH) || path.equals("/") || path.startsWith("/h2")) {
            filterChain.doFilter(request, response);
            return;
        }
                if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
                if (token == null || !jwtService.isValid(token)) {
                    filterChain.doFilter(request, response);
                    return;
                }
        //prevent user that does'nt exist from making requests
        String user = jwtService.extractUserId(token);
                try{
                    userService.getUser(user);
                }catch(EntityNotFoundException e) {
                    filterChain.doFilter(request, response);
                    return;
                }
                
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

}
