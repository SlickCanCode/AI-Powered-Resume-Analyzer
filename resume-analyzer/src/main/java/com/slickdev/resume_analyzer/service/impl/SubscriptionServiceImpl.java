package com.slickdev.resume_analyzer.service.impl;


import org.springframework.beans.factory.annotation.Autowired;

import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.repositories.SubscriptionRepository;
import com.slickdev.resume_analyzer.repositories.SubscriptionUsageRepository;
import com.slickdev.resume_analyzer.service.SubscriptionService;

import jakarta.persistence.Entity;

@Entity
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


    @Override
    public int getAnalysesAllowed(User user) {
        return user.getSubscriptionUsage().getResumesAnalysesAllowed();
    }


}
