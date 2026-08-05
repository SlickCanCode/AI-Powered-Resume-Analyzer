package com.slickdev.resume_analyzer.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slickdev.resume_analyzer.reponses.VerifyOtpResponse;
import com.slickdev.resume_analyzer.requests.ResetPasswordRequest;
import com.slickdev.resume_analyzer.requests.SendOtpRequest;
import com.slickdev.resume_analyzer.requests.VerifyOtpRequest;
import com.slickdev.resume_analyzer.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final AuthService authService;

    @PostMapping("/send-otp")
    public ResponseEntity<HttpStatus> sendOtp(@RequestBody SendOtpRequest request) {
        authService.sendOtp(request.email());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@RequestBody VerifyOtpRequest request, HttpServletResponse response) {
        
        return new ResponseEntity<>(authService.verifyOtp(request.getOtp(), request.getEmail(), response, request.getPurpose()), HttpStatus.OK);
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@CookieValue(name = "access_token") String jwt, @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(jwt, request.newPassword(), request.resetToken());
        return ResponseEntity.ok().build();
    }
    
} 
