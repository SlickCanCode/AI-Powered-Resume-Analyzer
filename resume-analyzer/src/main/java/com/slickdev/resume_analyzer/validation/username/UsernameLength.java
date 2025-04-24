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
    String message() default "Username cannot be less than 4 characters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
