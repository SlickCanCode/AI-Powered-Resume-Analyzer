package com.slickdev.resume_analyzer.service;

import java.io.IOException;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.exception.DuplicateResourceException;
import com.slickdev.resume_analyzer.security.SecurityConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    JwtService jwtService;
    @Autowired
    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    UserService userService;
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException {

        OAuth2User googleuser = (OAuth2User) authentication.getPrincipal();

        User user = null;
        try{
            user = userService.saveUser(new User(googleuser.getAttribute("given_name"), googleuser.getAttribute("family_name"), googleuser.getAttribute("email"),true));
        } catch (DuplicateResourceException e) {
            user = userService.getUserByEmail(googleuser.getAttribute("email"));
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while processing the request.");
            return;
        }
        jwtService.sendRefreshAndAccessTokens(response, user);
        response.sendRedirect(SecurityConstants.ALLOWED_ORIGIN + "/dashboard");
}
}