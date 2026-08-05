package com.slickdev.resume_analyzer.requests;

import com.drew.lang.annotations.NotNull;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {
    @NotNull
    @NotBlank(message = "OTP cannot be blank")
    private String otp;

    @NotNull
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    private String purpose;
}
