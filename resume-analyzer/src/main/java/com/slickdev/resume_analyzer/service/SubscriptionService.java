package com.slickdev.resume_analyzer.service;

import com.slickdev.resume_analyzer.entities.User;

public interface SubscriptionService {
    
    int getAnalysesAllowed(String userId);
}
