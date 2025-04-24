package com.slickdev.resume_analyzer.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.slickdev.resume_analyzer.security.filters.AuthenticationFilter;
import com.slickdev.resume_analyzer.security.filters.ExceptionHandlerFilter;
import com.slickdev.resume_analyzer.security.manager.CustomAuthenticationManager;
import com.slickdev.resume_analyzer.security.filters.JWTAuthorizationFilter;


import lombok.AllArgsConstructor;


    
@Configuration
@AllArgsConstructor
public class SecurityConfig {

    CustomAuthenticationManager authentication;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(authentication);
        authenticationFilter.setFilterProcessesUrl("/authenticate");

        http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/h2/**").permitAll()
            .requestMatchers(HttpMethod.POST, SecurityConstants.REGISTER_PATH).permitAll()
            .requestMatchers(HttpMethod.POST, SecurityConstants.RESUME_PATH).permitAll()
            .anyRequest().authenticated()
        ).addFilterBefore(new ExceptionHandlerFilter(), AuthenticationFilter.class)
        .addFilter(authenticationFilter)
        .addFilterAfter(new JWTAuthorizationFilter(), AuthenticationFilter.class)
        .headers(headers -> headers.frameOptions(frameOption -> frameOption.disable()))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
       
    }
    
}

