package com.slickdev.resume_analyzer.validation.fullname;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FullNameLengthValidator implements ConstraintValidator<FullNameLength, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) return true;
        return value.length() >= 3;
    }
}
