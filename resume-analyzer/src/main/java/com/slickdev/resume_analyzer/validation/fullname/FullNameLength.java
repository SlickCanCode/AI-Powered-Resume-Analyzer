package com.slickdev.resume_analyzer.validation.fullname;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FullNameLengthValidator.class)
public @interface FullNameLength {
    String message() default "Name cannot be less than 3 characters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
