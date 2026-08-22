package com.slickdev.resume_analyzer.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.slickdev.resume_analyzer.entities.ResumeAnalysis;
import com.slickdev.resume_analyzer.entities.UploadedResume;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.reponses.AnalysisSummaryResponse;
import com.slickdev.resume_analyzer.repositories.ResumeAnalysisRepository;
import com.slickdev.resume_analyzer.repositories.ResumeRepository;
import com.slickdev.resume_analyzer.repositories.UserRepository;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class ResumeAnalysisRepositoryIntegrationTest {
    @Autowired private UserRepository userRepository;
    @Autowired private ResumeRepository resumeRepository;
    @Autowired private ResumeAnalysisRepository resumeAnalysisRepository;

    @Test
    void summaryQueryReturnsOnlyTheUsersAnalysesNewestFirst() {
        User owner = userRepository.save(new User("Ada", "Lovelace", "ada@example.com", "password"));
        User otherUser = userRepository.save(new User("Grace", "Hopper", "grace@example.com", "password"));
        UploadedResume olderResume = saveResume(owner, "older.pdf");
        UploadedResume newerResume = saveResume(owner, "newer.pdf");
        UploadedResume otherResume = saveResume(otherUser, "other.pdf");
        saveAnalysis(olderResume, 71, 69, LocalDateTime.of(2026, 1, 1, 10, 0));
        saveAnalysis(newerResume, 88, 86, LocalDateTime.of(2026, 1, 2, 10, 0));
        saveAnalysis(otherResume, 99, 99, LocalDateTime.of(2026, 1, 3, 10, 0));

        List<AnalysisSummaryResponse> summaries = resumeAnalysisRepository.findAllSummariesByUserId(owner.getId());

        assertEquals(2, summaries.size());
        assertEquals("newer.pdf", summaries.get(0).resumeName());
        assertEquals(88, summaries.get(0).score());
        assertEquals("older.pdf", summaries.get(1).resumeName());
    }

    private UploadedResume saveResume(User user, String filename) {
        return resumeRepository.save(UploadedResume.builder()
                .filename(filename).fileType("application/pdf").parsedContent("resume content").user(user).build());
    }

    private void saveAnalysis(UploadedResume resume, int overallScore, int atsScore, LocalDateTime createdAt) {
        resumeAnalysisRepository.save(ResumeAnalysis.builder()
                .resume(resume).overallScore(overallScore).atsScore(atsScore).keywordScore(atsScore)
                .strengths(List.of()).weaknesses(List.of()).existingSkills(List.of()).skillsToDevelop(List.of())
                .grammarIssues(List.of()).recommendations(List.of()).createdAt(createdAt).build());
    }
}
