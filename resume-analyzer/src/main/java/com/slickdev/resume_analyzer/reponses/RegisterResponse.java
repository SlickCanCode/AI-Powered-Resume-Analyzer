package com.slickdev.resume_analyzer.reponses;

import com.drew.lang.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RegisterResponse {
    
    @NotNull
    private String email;

}
