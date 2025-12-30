package com.slickdev.resume_analyzer.reponses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AuthResponse {
    String jwt;
    UserResponseDto user;
}
