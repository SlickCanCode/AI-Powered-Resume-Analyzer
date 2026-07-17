package com.slickdev.resume_analyzer.validation.username;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsernameLengthValidator.class)
public @interface UsernameLength {
    String message() default "Your name cannot be less than 2 characters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
