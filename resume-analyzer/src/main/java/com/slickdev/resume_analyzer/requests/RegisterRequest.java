package com.slickdev.resume_analyzer.requests;

import com.drew.lang.annotations.NotNull;
import com.slickdev.resume_analyzer.validation.SpecialCharactersValidation;
import com.slickdev.resume_analyzer.validation.password.PasswordLength;
import com.slickdev.resume_analyzer.validation.username.UsernameLength;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class RegisterRequest {
    @NotNull
    @NotBlank(message = "First name cannot be blank")
    @SpecialCharactersValidation
    @UsernameLength
    private String firstName;

    @NotNull
    @NotBlank(message = "Last name cannot be blank")
    @SpecialCharactersValidation
    @UsernameLength
    private String lastName;

    @NotNull
    @NotBlank(message = "Email cannot be blank")
    @Email
    private String email;
    
    @NotNull
    @NotBlank(message = "Password cannot be blank")
    @PasswordLength
    private String password;
}
