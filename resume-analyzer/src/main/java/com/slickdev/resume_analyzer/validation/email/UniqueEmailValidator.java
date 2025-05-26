package com.slickdev.resume_analyzer.validation.email;

import org.springframework.beans.factory.annotation.Autowired;

import com.slickdev.resume_analyzer.repositories.UserRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String>{

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        if (userRepository == null) return true;
        return !userRepository.existsByEmail(value);
    }
}
