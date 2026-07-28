package com.slickdev.resume_analyzer.service;


import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.reponses.RegisterResponse;
import com.slickdev.resume_analyzer.reponses.UserResponseDto;
import com.slickdev.resume_analyzer.requests.RegisterRequest;
import com.slickdev.resume_analyzer.requests.UpdateuserRequest;

public interface UserService {

    User saveUser(User user);
    User getUser(String id);
    UserResponseDto getUserinfo(String id);
    UserResponseDto updateUser(String id, UpdateuserRequest  request);
    RegisterResponse registerUser(RegisterRequest user);
    User getUserByEmail(String email);
    void deleteUser(String id);
    
}
