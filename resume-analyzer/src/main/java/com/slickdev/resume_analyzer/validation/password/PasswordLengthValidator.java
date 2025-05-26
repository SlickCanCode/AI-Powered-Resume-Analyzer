package com.slickdev.resume_analyzer.validation.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordLengthValidator implements ConstraintValidator<PasswordLength, String>{
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null|| value.trim().isEmpty()) return true;
        return value.length() >=6;
    }
}
