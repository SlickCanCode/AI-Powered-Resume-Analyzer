package com.slickdev.resume_analyzer.service;

import com.slickdev.resume_analyzer.entities.User;

public interface UserService {

    User saveUser(User user);
    User getUser(long id);
    User getUser(String username);
    void deleteUser(Long id);
}
