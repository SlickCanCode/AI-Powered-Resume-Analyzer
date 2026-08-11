package com.slickdev.resume_analyzer.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slickdev.resume_analyzer.entities.ResumeAnalysis;
import com.slickdev.resume_analyzer.entities.UploadedResume;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.reponses.AnalysisPreviewResponse;
import com.slickdev.resume_analyzer.reponses.StatsResponse;
import com.slickdev.resume_analyzer.service.DashboardService;
import com.slickdev.resume_analyzer.service.JwtService;
import com.slickdev.resume_analyzer.service.ResumeService;
import com.slickdev.resume_analyzer.service.SubscriptionService;

@Service
public class DashboardServiceimpl implements DashboardService {
    
    ResumeService resumeService;
    @Autowired
    public void setResumeService (ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    SubscriptionService subscriptionService;
    @Autowired
        public void setSubscriptionService (SubscriptionService subscriptionService) {
                this.subscriptionService = subscriptionService;
        }

     JwtService jwtService;
    @Autowired
        public void setJwtService (JwtService jwtService) {
                this.jwtService = jwtService;
        }

        
   public StatsResponse getStats(String jwt) {
        String userId = jwtService.extractUserId(jwt);
        List<UploadedResume> analyzedResumes = resumeService.getAnalyzedResumes(userId);

        LocalDateTime startOfThisWeek = LocalDate.now()
        .with(DayOfWeek.MONDAY)
        .atStartOfDay();
        
        LocalDateTime startOfLastWeek = startOfThisWeek.minusWeeks(1);
        int totalAnalyzedResumes = analyzedResumes.size();

        int totalAnalyzedResumesThisWeek = (int) analyzedResumes.stream()
                .filter(resume -> resume.getAnalysis().stream()
                        .anyMatch(a -> !a.getCreatedAt().isBefore(startOfThisWeek)))
                .count();

        List<ResumeAnalysis> analyses = analyzedResumes.stream()
                .flatMap(r -> r.getAnalysis().stream())
                .toList();

        double avgResumeScore = analyses.stream()
                .mapToInt(ResumeAnalysis::getScore)
                .average()
                .orElse(0);

        double lastWeekResumeAvg = analyses.stream()
                        .filter(a -> !a.getCreatedAt().isBefore(startOfLastWeek)
                                && a.getCreatedAt().isBefore(startOfThisWeek))
                        .mapToInt(ResumeAnalysis::getScore)
                        .average()
                        .orElse(0);

        double thisWeekResumeAvg = analyses.stream()
                        .filter(a -> !a.getCreatedAt().isBefore(startOfThisWeek))
                        .mapToInt(ResumeAnalysis::getScore)
                        .average()
                        .orElse(0);

        double resumeScoreImprovement = thisWeekResumeAvg - lastWeekResumeAvg;

        double avgAtsScore = analyses.stream()
                .mapToInt(ResumeAnalysis::getAtsScore)
                .average()
                .orElse(0);

        double lastWeekAtsAvg = analyses.stream()
                .filter(a -> !a.getCreatedAt().isBefore(startOfLastWeek) &&
                         a.getCreatedAt().isBefore(startOfThisWeek))
                .mapToInt(ResumeAnalysis::getAtsScore)
                .average()
                .orElse(avgAtsScore);

        double thisWeekAtsAvg = analyses.stream()
                .filter(a -> !a.getCreatedAt().isBefore(startOfThisWeek))
                .mapToInt(ResumeAnalysis::getAtsScore)
                .average()
                .orElse(lastWeekAtsAvg);

        double atsScoreImprovement = thisWeekAtsAvg - lastWeekAtsAvg;

        int analysesAvailable = subscriptionService.getAnalysesAllowed(userId);
        return new StatsResponse(
            totalAnalyzedResumes,
            totalAnalyzedResumesThisWeek,
            (int) avgResumeScore,
            (int) resumeScoreImprovement,
            (int) avgAtsScore,
            (int) atsScoreImprovement,
            analysesAvailable
        );
}

    public List<AnalysisPreviewResponse> getRecentAnalyses(String jwt) {
        String userId = jwtService.extractUserId(jwt);
        List<UploadedResume> analyzedResumes = resumeService.getAnalyzedResumes(userId);
        
                return analyzedResumes.stream()
                        .filter(resume -> resume.getAnalysis() != null && !resume.getAnalysis().isEmpty())
                        .sorted((r1, r2) ->
                                r2.getAnalysis().get(r2.getAnalysis().size() - 1).getCreatedAt()
                                        .compareTo(r1.getAnalysis().get(r1.getAnalysis().size() - 1).getCreatedAt()))
                        .limit(3)
                        .map(resume -> {
                        ResumeAnalysis latestAnalysis = resume.getAnalysis().get(resume.getAnalysis().size() - 1);
                        return new AnalysisPreviewResponse(
                                resume.getFilename(),
                                latestAnalysis.getScore(),
                                latestAnalysis.getAtsScore(),
                                latestAnalysis.getCreatedAt().toString()
                        );
                        })
                        .toList();
                }

}
