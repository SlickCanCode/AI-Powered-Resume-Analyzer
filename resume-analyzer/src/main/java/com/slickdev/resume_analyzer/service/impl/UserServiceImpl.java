package com.slickdev.resume_analyzer.service.impl;


import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.exception.DuplicateResourceException;
import com.slickdev.resume_analyzer.exception.EntityNotFoundException;
import com.slickdev.resume_analyzer.reponses.AuthResponse;
import com.slickdev.resume_analyzer.reponses.UserResponseDto;
import com.slickdev.resume_analyzer.repositories.UserRepository;
import com.slickdev.resume_analyzer.requests.UpdateuserRequest;
import com.slickdev.resume_analyzer.service.JwtService;
import com.slickdev.resume_analyzer.service.UserService;

import jakarta.transaction.Transactional;


@Service
@Transactional
public class UserServiceImpl implements UserService{
    
    BCryptPasswordEncoder passwordEncoder;
    @Autowired
    public void setEncoder(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    UserRepository userRepository;
    @Autowired
    public void setRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    JwtService jwtService;
    @Autowired
    public void setJwtservice(JwtService jService) {
        this.jwtService = jService;
    }


    @Override
    public AuthResponse registerUser(User user) {
        User savedUser = saveUser(user);
        String jwt = jwtService.generateToken(savedUser);
        UserResponseDto userResponse= new UserResponseDto(user.getId(), user.getUserName(), user.getEmail());
        return new AuthResponse(jwt, userResponse);
    }

    @Override
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User getUser(String id) {
        UUID refinedId = UUID.fromString(formatUUID(id));
        Optional<User> user = userRepository.findById(refinedId);
        return unwrapUser(user, refinedId);
    }

    @Override
    public UserResponseDto getUserinfo(String id) {
        UUID refinedId = UUID.fromString(formatUUID(id));
        User user = unwrapUser(userRepository.findById(refinedId), refinedId);
        return new UserResponseDto(user.getId(), user.getUserName(), user.getEmail());
    }

    @Override
    public User getUserByUsernameOrEmail(String usernameOrEmail) {
        Optional<User> user = userRepository.findByUserNameOrEmail(usernameOrEmail, usernameOrEmail);
        if (user.isPresent()) return user.get();
        else throw new EntityNotFoundException(usernameOrEmail, User.class);
    }

    @Override
    public void deleteUser(String id) {
         getUser(id);
        userRepository.deleteById(UUID.fromString(formatUUID(id)));
    }

    @Override
    public UserResponseDto updateUser(String id, UpdateuserRequest request) {
        UUID refinedId = UUID.fromString(formatUUID(id));
        User user = unwrapUser(userRepository.findById(refinedId), refinedId);

    if (!user.getEmail().equals(request.getEmail())) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email");
        }
    }

    // Username uniqueness check
    if (!user.getUserName().equals(request.getUserName())) {
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new DuplicateResourceException("Username");
        }
    }

        user.setEmail(request.getEmail());
        user.setUserName(request.getUserName());
        return new UserResponseDto(user.getId(), user.getUserName(), user.getEmail());
    }

    static User unwrapUser(Optional<User> entity, UUID id) {
        if (entity.isPresent()) return entity.get();
        else throw new EntityNotFoundException(id, User.class);
    }

    private String formatUUID(String raw) {
    return raw.replaceFirst(
        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
        "$1-$2-$3-$4-$5"
    );
}

   

    
}
