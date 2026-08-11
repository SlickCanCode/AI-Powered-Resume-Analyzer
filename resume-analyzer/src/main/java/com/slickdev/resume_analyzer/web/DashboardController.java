package com.slickdev.resume_analyzer.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slickdev.resume_analyzer.reponses.AnalysisPreviewResponse;
import com.slickdev.resume_analyzer.reponses.StatsResponse;
import com.slickdev.resume_analyzer.service.impl.DashboardServiceimpl;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/dashboard")
public class DashboardController {
    
    private final DashboardServiceimpl dashboardService;

    @GetMapping("/me/stats")
    public ResponseEntity<StatsResponse> getStats(@CookieValue(name = "access_token") String jwt) {
        return new ResponseEntity<>(dashboardService.getStats(jwt), HttpStatus.OK);
    }
    
    @GetMapping("/me/recent-analyses")
    public ResponseEntity<List<AnalysisPreviewResponse>> getRecentAnalyses(@CookieValue(name = "access_token") String jwt) {
        return new ResponseEntity<>(dashboardService.getRecentAnalyses(jwt), HttpStatus.OK);
    }
}
