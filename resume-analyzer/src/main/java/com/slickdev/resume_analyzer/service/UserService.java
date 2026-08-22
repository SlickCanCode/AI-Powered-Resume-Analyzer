package com.slickdev.resume_analyzer.service;



import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.reponses.RegisterResponse;
import com.slickdev.resume_analyzer.reponses.SubscriptionUsageResponse;
import com.slickdev.resume_analyzer.reponses.UserResponseDto;
import com.slickdev.resume_analyzer.requests.RegisterRequest;
import com.slickdev.resume_analyzer.requests.UpdateuserRequest;

import jakarta.servlet.http.HttpServletResponse;

public interface UserService {

    User saveUser(User user);
    User getUser(String id);
    UserResponseDto getUserinfo(String jwt);
    UserResponseDto updateUser(String jwt, UpdateuserRequest  request);
    RegisterResponse registerUser(RegisterRequest user, HttpServletResponse response);
    void resetPassword(String jwt, String newPassword);
    User getUserByEmail(String email);
    void deleteUser(String jwt);
    boolean isEmailUnique(String email);
    
    /**
     * Get current subscription usage information for a user.
     * Includes plan, quota, and remaining analyses.
     */
    SubscriptionUsageResponse getSubscriptionUsage(String jwt);

}
