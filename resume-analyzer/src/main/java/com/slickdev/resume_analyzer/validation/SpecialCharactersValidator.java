package com.slickdev.resume_analyzer.validation;

import java.util.Arrays;
import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SpecialCharactersValidator implements ConstraintValidator<SpecialCharactersValidation, String> {

    private List<String> allowedCharacters = Arrays.asList(
        "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", 
                      "+", "=", "[", "]", "{", "}", "|", "\\", ";", ":", 
                      "\"", ",", "<", ">", "/", "?", "`", "~"
    ); 

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        for (String ch : allowedCharacters) {
            if (value.contains(ch)) return false;
        }
        return true;
    }
    
    
}
