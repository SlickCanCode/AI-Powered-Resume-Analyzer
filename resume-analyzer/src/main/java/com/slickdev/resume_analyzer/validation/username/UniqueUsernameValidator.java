package com.slickdev.resume_analyzer.validation.username;


import org.springframework.beans.factory.annotation.Autowired;

import com.slickdev.resume_analyzer.repositories.UserRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;



public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String>{

     @Autowired
  private UserRepository userRepository;

   @Override
   public boolean isValid(String value, ConstraintValidatorContext context) {
          if (value == null) return true;
          if (userRepository == null) return true;
          return !userRepository.existsByUserName(value);
   }
}
