package com.slickdev.resume_analyzer.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.slickdev.resume_analyzer.security.filters.AuthenticationFilter;
import com.slickdev.resume_analyzer.security.filters.ExceptionHandlerFilter;
import com.slickdev.resume_analyzer.security.manager.CustomAuthenticationManager;
import com.slickdev.resume_analyzer.service.JwtService;
import com.slickdev.resume_analyzer.service.OAuth2SuccessHandler;
import com.slickdev.resume_analyzer.service.UserService;

import jakarta.servlet.http.HttpServletResponse;

import com.slickdev.resume_analyzer.security.filters.JWTAuthorizationFilter;


import lombok.AllArgsConstructor;


    
@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationManager authentication;
    private final UserService  userService;
    private final JwtService  jwtService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JWTAuthorizationFilter jwtAuthorizationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(authentication, userService, jwtService);
        authenticationFilter.setFilterProcessesUrl("/api/v1/auth/login");

        http
        .cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/h2-console/**").permitAll()
            .requestMatchers("/api/v1/auth/**").permitAll()
            .requestMatchers(HttpMethod.POST, SecurityConstants.REGISTER_PATH).permitAll()
            .anyRequest().authenticated()
        )
        .exceptionHandling(exception -> exception
            .defaultAuthenticationEntryPointFor(
                 (request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED),
                new AntPathRequestMatcher("/api/**")
            )
        )
        .oauth2Login(oauth -> oauth.successHandler(oAuth2SuccessHandler))
        .addFilterBefore(new ExceptionHandlerFilter(), AuthenticationFilter.class)
        .addFilter(authenticationFilter)
        .addFilterAfter(jwtAuthorizationFilter, AuthenticationFilter.class)
        .headers(headers -> headers.frameOptions(frameOption -> frameOption.sameOrigin()))
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        );

    return http.build();
       
    }

     @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(SecurityConstants.ALLOWED_ORIGINS);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        config.setExposedHeaders(List.of("Location"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    
}

