package com.slickdev.resume_analyzer.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.slickdev.resume_analyzer.security.filters.AuthenticationFilter;
import com.slickdev.resume_analyzer.security.filters.ExceptionHandlerFilter;
import com.slickdev.resume_analyzer.security.manager.CustomAuthenticationManager;
import com.slickdev.resume_analyzer.service.impl.UserServiceImpl;
import com.slickdev.resume_analyzer.security.filters.JWTAuthorizationFilter;


import lombok.AllArgsConstructor;


    
@Configuration
@AllArgsConstructor
public class SecurityConfig {

    CustomAuthenticationManager authentication;
    UserServiceImpl userService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(authentication, userService);
        authenticationFilter.setFilterProcessesUrl("/authenticate");

        http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/h2/**").permitAll()
            .requestMatchers(HttpMethod.POST, SecurityConstants.REGISTER_PATH).permitAll()
            .requestMatchers(HttpMethod.POST, SecurityConstants.RESUME_UPLOAD_PATH).permitAll()
            .requestMatchers(HttpMethod.GET, SecurityConstants.RESUME_ANALYZE_PATH).permitAll()
            .anyRequest().authenticated()
        ).addFilterBefore(new ExceptionHandlerFilter(), AuthenticationFilter.class)
        .addFilter(authenticationFilter)
        .addFilterAfter(new JWTAuthorizationFilter(), AuthenticationFilter.class)
        .headers(headers -> headers.frameOptions(frameOption -> frameOption.sameOrigin()))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
       
    }
    
}

