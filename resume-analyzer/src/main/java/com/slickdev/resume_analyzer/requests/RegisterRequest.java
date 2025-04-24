package com.slickdev.resume_analyzer.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterRequest {
    private String userName;
    private String fullName;
    private String email;
    private String password;
    
}
