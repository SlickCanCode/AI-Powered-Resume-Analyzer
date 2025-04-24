package com.slickdev.resume_analyzer.requests;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String userNameOrEmail;
    private String password;


}
