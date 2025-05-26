package com.slickdev.resume_analyzer.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SpecialCharactersValidator.class)
public @interface SpecialCharactersValidation {

        String message() default "Special characters arent allowed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
