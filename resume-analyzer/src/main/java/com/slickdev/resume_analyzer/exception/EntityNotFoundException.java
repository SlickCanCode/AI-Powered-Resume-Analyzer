package com.slickdev.resume_analyzer.exception;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException{
    public EntityNotFoundException(UUID id, Class<?> entity) {
        super("This "+ entity.getSimpleName() +" does not exist in our records");
}
    public EntityNotFoundException(String Email) {
                super("This email does not exist in our records");
    }
}
