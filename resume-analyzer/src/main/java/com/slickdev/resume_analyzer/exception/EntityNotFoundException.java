package com.slickdev.resume_analyzer.exception;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException{
    public EntityNotFoundException(UUID id, Class<?> entity) {
        super("The " + entity.getSimpleName().toLowerCase() + " with id '" + id + "' does not exist in our records");
}
    public EntityNotFoundException(String usernameOrEmail, Class<?> entity) {
                super("The " + entity.getSimpleName().toLowerCase() + " with '" + usernameOrEmail + "' does not exist in our records");
    }
}
