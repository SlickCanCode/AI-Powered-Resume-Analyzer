package com.slickdev.resume_analyzer.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UpdateuserRequest {

    @NotBlank
    private String userName;

    @Email
    private String email;
}
