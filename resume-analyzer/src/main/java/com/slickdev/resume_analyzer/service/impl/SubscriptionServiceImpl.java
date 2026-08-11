package com.slickdev.resume_analyzer.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.repositories.SubscriptionRepository;
import com.slickdev.resume_analyzer.repositories.SubscriptionUsageRepository;
import com.slickdev.resume_analyzer.service.SubscriptionService;
import com.slickdev.resume_analyzer.service.UserService;



@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    SubscriptionRepository repository;
    @Autowired
    public void setRespository(SubscriptionRepository repository) {
        this.repository = repository;
    }

    SubscriptionUsageRepository usageRepository;
    @Autowired
    public void setUsageRespository(SubscriptionUsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    UserService userService;
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public int getAnalysesAllowed(String userId) {
        User user = userService.getUser(userId);
        return user.getSubscriptionUsage().getResumesAnalysesAllowed();
    }


}
