package com.slickdev.resume_analyzer.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slickdev.resume_analyzer.reponses.AuthResponse;
import com.slickdev.resume_analyzer.requests.VerifyOtpRequest;
import com.slickdev.resume_analyzer.service.AuthService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return new ResponseEntity<AuthResponse>(authService.verifyOtp(request.getOtp(), request.getEmail()), HttpStatus.OK);
    }
    
}
