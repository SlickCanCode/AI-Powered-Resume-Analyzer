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
import com.slickdev.resume_analyzer.requests.RegisterRequest;
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

    OtpService otpService;
    @Autowired
    public void setOtpService(OtpService otpService) {
        this.otpService = otpService;
    }

    @Override
    public AuthResponse registerUser(RegisterRequest user) {
        User savedUser = saveUser(new User(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword()));
        String jwt = jwtService.generateToken(savedUser);
        otpService.generateOTP(savedUser);
        // otpService.sendOtp(otpService.generateOTP(savedUser), savedUser.getEmail());
        UserResponseDto userResponse= new UserResponseDto(savedUser.getId(), savedUser.getFirstName(), savedUser.getLastName(), savedUser.getEmail());
        return new AuthResponse(jwt, userResponse);
    }

    @Override
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (isEmailUnique(user.getEmail())) {
            return userRepository.save(user);
        } else {
            throw new DuplicateResourceException("Email");
        }
    }

    public boolean isEmailUnique(String email) {
        return !userRepository.existsByEmail(email);
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
        return new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
    }

    @Override
    public User getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return unwrapUser(user, null);
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
    
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        return new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
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
