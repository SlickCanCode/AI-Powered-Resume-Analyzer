package com.slickdev.resume_analyzer.security.manager;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.slickdev.resume_analyzer.entities.User;

import com.slickdev.resume_analyzer.service.impl.UserServiceImpl;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserServiceImpl userService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        User user = userService.getUserByEmail(authentication.getName());

        if (!passwordEncoder.matches(
                authentication.getCredentials().toString(),
                user.getPassword())) {

            throw new BadCredentialsException("Invalid email or password");
        }

        return new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                null
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class
                .isAssignableFrom(authentication);
    }
}
