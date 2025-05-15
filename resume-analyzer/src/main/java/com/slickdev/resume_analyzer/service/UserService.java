package com.slickdev.resume_analyzer.service;


import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.entities.UserResponseDto;

public interface UserService {

    User saveUser(User user);
    User getUser(String id);
    UserResponseDto getUserinfo(String id);
    User getUserByUsername(String username);
    void deleteUser(String id);
}
