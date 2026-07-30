package com.slickdev.resume_analyzer.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slickdev.resume_analyzer.reponses.jwtResponse;
import com.slickdev.resume_analyzer.requests.VerifyOtpRequest;
import com.slickdev.resume_analyzer.service.AuthService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final AuthService authService;

    @PostMapping("/send-otp")
    public ResponseEntity<HttpStatus> sendOtp(@RequestBody String email) {
        authService.sendOtp(email);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<jwtResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return new ResponseEntity<jwtResponse>(authService.verifyOtp(request.getOtp(), request.getEmail()), HttpStatus.OK);
    }
    
}
