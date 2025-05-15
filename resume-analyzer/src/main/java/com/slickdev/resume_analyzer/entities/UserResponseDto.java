package com.slickdev.resume_analyzer.entities;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserResponseDto{
    private UUID id;
    private String fullName;
    private String userName;
    private String email;
}