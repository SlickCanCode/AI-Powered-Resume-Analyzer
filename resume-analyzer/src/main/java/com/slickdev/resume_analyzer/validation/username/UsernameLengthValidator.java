package com.slickdev.resume_analyzer.validation.username;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UsernameLengthValidator implements ConstraintValidator<UsernameLength, String>{
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) return true;
        return value.length()>=4;
    }
}
