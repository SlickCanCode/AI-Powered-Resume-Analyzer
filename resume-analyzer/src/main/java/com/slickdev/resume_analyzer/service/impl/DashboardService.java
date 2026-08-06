package com.slickdev.resume_analyzer.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.reponses.StatsResponse;
import com.slickdev.resume_analyzer.service.ResumeService;

@Service
public class DashboardService {
    
    ResumeService resumeService;
    @Autowired
    public void setResumeService (ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    public StatsResponse getStats(User user) {
        //get resume analyzed overall & this week
        //calc avg resume score + improvement this week
        //calc avg ats score + improvement this week
        //calc analyses available for the month based on subscription  
    }
}
