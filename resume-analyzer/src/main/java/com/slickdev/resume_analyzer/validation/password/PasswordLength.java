package com.slickdev.resume_analyzer.validation.password;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordLengthValidator.class)
public @interface PasswordLength {

    String message() default "Password cannot be less than 6 characters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
