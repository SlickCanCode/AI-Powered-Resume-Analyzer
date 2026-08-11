package com.slickdev.resume_analyzer.service;


import java.util.List;

import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.reponses.AnalysisPreviewResponse;
import com.slickdev.resume_analyzer.reponses.RegisterResponse;
import com.slickdev.resume_analyzer.reponses.StatsResponse;
import com.slickdev.resume_analyzer.reponses.UserResponseDto;
import com.slickdev.resume_analyzer.requests.RegisterRequest;
import com.slickdev.resume_analyzer.requests.UpdateuserRequest;

public interface UserService {

    User saveUser(User user);
    User getUser(String id);
    UserResponseDto getUserinfo(String jwt);
    UserResponseDto updateUser(String jwt, UpdateuserRequest  request);
    RegisterResponse registerUser(RegisterRequest user);
    void resetPassword(String jwt, String newPassword);
    User getUserByEmail(String email);
    void deleteUser(String jwt);
    boolean isEmailUnique(String email);

}
