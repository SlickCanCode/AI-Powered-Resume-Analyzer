package com.slickdev.resume_analyzer.service.impl;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.slickdev.resume_analyzer.entities.Subscription;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.exception.DuplicateResourceException;
import com.slickdev.resume_analyzer.exception.EntityNotFoundException;
import com.slickdev.resume_analyzer.exception.UserNotFound;
import com.slickdev.resume_analyzer.reponses.RegisterResponse;
import com.slickdev.resume_analyzer.reponses.SubscriptionUsageResponse;
import com.slickdev.resume_analyzer.reponses.UserResponseDto;
import com.slickdev.resume_analyzer.repositories.UserRepository;
import com.slickdev.resume_analyzer.requests.RegisterRequest;
import com.slickdev.resume_analyzer.requests.UpdateuserRequest;
import com.slickdev.resume_analyzer.service.JwtService;
import com.slickdev.resume_analyzer.service.OtpService;
import com.slickdev.resume_analyzer.service.SubscriptionService;
import com.slickdev.resume_analyzer.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;


@Slf4j
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

    SubscriptionService subscriptionService;
    @Autowired
    public void setSubscriptionService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public RegisterResponse registerUser(RegisterRequest user, HttpServletResponse response) {
        User savedUser = saveUser(new User(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword()));
        jwtService.sendRefreshAndAccessTokens(response, savedUser);
        log.info("User created. Name: {} ",savedUser.getFirstName());
        return new RegisterResponse(savedUser.getEmail());
    }

    @Override
    public User saveUser(User user) {
        if (user.getPassword()!= null) {
         user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (isEmailUnique(user.getEmail())) {
            User savedUser = userRepository.save(user);
            // Create default subscription for new user
            subscriptionService.createDefaultSubscription(savedUser);
            return savedUser;
        }else {
            throw new DuplicateResourceException("Email");
        }
    } 

    @Override
    public User getUser(String id) {
        UUID refinedId = UUID.fromString(formatUUID(id));
        Optional<User> user = userRepository.findById(refinedId);
        return unwrapUser(user, refinedId);
    }

    @Override
    public UserResponseDto getUserinfo(String jwt) {
        String userId = jwtService.extractUserId(jwt);
        UUID refinedId = UUID.fromString(formatUUID(userId));
        User user = unwrapUser(userRepository.findById(refinedId), refinedId);
        return new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.isEmailVerified());
    }

    @Override
    public User getUserByEmail(String email) {
        Optional<User> entity = userRepository.findByEmail(email);
        if (entity.isPresent()) return entity.get();
        else throw new EntityNotFoundException(email);
    }

    @Override
    public void deleteUser(String jwt) {
        String userId = jwtService.extractUserId(jwt);
        userRepository.delete(getUser(userId));
    }

    @Override
    public UserResponseDto updateUser(String jwt, UpdateuserRequest request) {
        String userId = jwtService.extractUserId(jwt);
        UUID refinedId = UUID.fromString(formatUUID(userId));
        User user = unwrapUser(userRepository.findById(refinedId), refinedId);
        
    if (!user.getEmail().equals(request.getEmail())) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email");
        }
    }
    
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        return new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.isEmailVerified());
    }

    @Override
    public void resetPassword(User user, String newPassword) {
        if (!passwordEncoder.matches(newPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
        } else {
            throw new IllegalArgumentException("New password cannot be the same as the old password.");
        }
    }

    @Override
    public void changePassword(String jwt, String currentPassword, String newPassword) {
        User user = getUser(jwtService.extractUserId(jwt));
        //check if the currentPassword is actually correct before changing it
        if (passwordEncoder.matches(currentPassword, user.getPassword())) {
            resetPassword(user, newPassword);
        } else {
            throw new IllegalArgumentException("Incorrect Password");
        }
    }

    static User unwrapUser(Optional<User> entity, UUID id) {
        if (entity.isPresent()) return entity.get();
        else throw new UserNotFound(id.toString());
    } 

    @Override
    public boolean isEmailUnique(String email) {
        return !userRepository.existsByEmail(email);
    }

    private String formatUUID(String raw) {
    return raw.replaceFirst(
        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
        "$1-$2-$3-$4-$5"
    );
}

    @Override
    public SubscriptionUsageResponse getSubscriptionUsage(String jwt) {
        String userId = jwtService.extractUserId(jwt);
        
        // Get subscription info
        Subscription subscription = subscriptionService.checkSubscription(userId);
        LocalDateTime startPeriod = subscription.getCurrentPeriodStart();
        LocalDateTime endPeriod = subscription.getCurrentPeriodEnd();
        int analysesAllowed = subscriptionService.getAnalysesAllowed(userId);
        int analysesUsed = subscriptionService.getAnalysesUsed(userId);
        
        return SubscriptionUsageResponse.fromMetrics(
                subscription.getPlan().toString(),
                startPeriod,
                endPeriod,
                analysesAllowed,
                analysesUsed
        );
    }

   

    
}
