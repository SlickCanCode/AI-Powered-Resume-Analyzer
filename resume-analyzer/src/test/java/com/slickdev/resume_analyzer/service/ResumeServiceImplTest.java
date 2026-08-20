package com.slickdev.resume_analyzer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.slickdev.resume_analyzer.entities.ResumeAnalysis;
import com.slickdev.resume_analyzer.entities.ResumeData;
import com.slickdev.resume_analyzer.entities.UploadedResume;
import com.slickdev.resume_analyzer.exception.EntityNotFoundException;
import com.slickdev.resume_analyzer.reponses.AnalysisSummaryResponse;
import com.slickdev.resume_analyzer.reponses.ResumeDataResponse;
import com.slickdev.resume_analyzer.repositories.ResumeAnalysisRepository;
import com.slickdev.resume_analyzer.repositories.ResumeDataRepository;
import com.slickdev.resume_analyzer.repositories.ResumeRepository;
import com.slickdev.resume_analyzer.service.impl.GeminiService;
import com.slickdev.resume_analyzer.service.impl.JobPostingExtractor;
import com.slickdev.resume_analyzer.service.impl.JwtServiceImpl;
import com.slickdev.resume_analyzer.service.impl.ResumeServiceImpl;
import com.slickdev.resume_analyzer.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {
    private static final UUID USER_ID = UUID.fromString("12345678-1234-1234-1234-1234567890ab");
    private static final UUID RESUME_ID = UUID.fromString("87654321-1234-1234-1234-1234567890ab");
    private static final String JWT = "jwt";

    @Mock private ResumeRepository resumeRepository;
    @Mock private GeminiService geminiService;
    @Mock private UserServiceImpl userService;
    @Mock private JwtServiceImpl jwtService;
    @Mock private ResumeDataRepository resumeDataRepository;
    @Mock private ResumeAnalysisRepository resumeAnalysisRepository;
    @Mock private JobPostingExtractor jobPostingExtractor;
    @InjectMocks private ResumeServiceImpl resumeService;

    @BeforeEach
    void setUp() {
        when(jwtService.extractUserId(JWT)).thenReturn(USER_ID.toString());
    }

    @Test
    void getResumeDataReturnsEveryParsedFieldForItsOwner() {
        UploadedResume resume = UploadedResume.builder().id(RESUME_ID).build();
        ResumeData resumeData = ResumeData.builder()
                .resume(resume).fullName("Ada Lovelace").email("ada@example.com").phone("123")
                .location("Lagos").careerSummary("Backend engineer").onlineProfiles(List.of())
                .skills(List.of("Java")).experience(List.of()).education(List.of()).build();
        when(resumeDataRepository.findByResumeIdAndResumeUserId(RESUME_ID, USER_ID)).thenReturn(Optional.of(resumeData));

        ResumeDataResponse response = resumeService.getResumeData(RESUME_ID.toString(), JWT);

        assertEquals("Lagos", response.location());
        assertEquals("Backend engineer", response.summary());
        assertEquals(List.of("Java"), response.skills());
    }

    @Test
    void getResumeDataDoesNotExposeAnotherUsersResume() {
        when(resumeDataRepository.findByResumeIdAndResumeUserId(RESUME_ID, USER_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> resumeService.getResumeData(RESUME_ID.toString(), JWT));
    }

    @Test
    void getResumeAnalysesRejectsAnUnownedResumeBeforeReadingAnalyses() {
        when(resumeRepository.existsByIdAndUserId(RESUME_ID, USER_ID)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> resumeService.getResumeAnalyses(RESUME_ID.toString(), JWT));
        verify(resumeAnalysisRepository, org.mockito.Mockito.never()).findByResumeIdAndResumeUserIdOrderByCreatedAtDesc(RESUME_ID, USER_ID);
    }

    @Test
    void getAllAnalysesUsesSummaryProjectionForTheAuthenticatedUser() {
        AnalysisSummaryResponse summary = new AnalysisSummaryResponse("resume.pdf", LocalDateTime.now(), 82, 79);
        when(resumeAnalysisRepository.findAllSummariesByUserId(USER_ID)).thenReturn(List.of(summary));

        assertEquals(List.of(summary), resumeService.getAllAnalyses(JWT));
        verify(resumeAnalysisRepository).findAllSummariesByUserId(USER_ID);
    }

    @Test
    void getResumeAnalysesReturnsNewestFirstRepositoryResults() {
        ResumeAnalysis analysis = ResumeAnalysis.builder().id(UUID.randomUUID()).overallScore(90).atsScore(88)
                .keywordScore(87).strengths(List.of("Java")).weaknesses(List.of()).neededSkills(List.of())
                .valuableSkills(List.of("Spring")).grammarIssues(List.of()).recommendations(List.of()).build();
        when(resumeRepository.existsByIdAndUserId(RESUME_ID, USER_ID)).thenReturn(true);
        when(resumeAnalysisRepository.findByResumeIdAndResumeUserIdOrderByCreatedAtDesc(RESUME_ID, USER_ID)).thenReturn(List.of(analysis));

        assertEquals(90, resumeService.getResumeAnalyses(RESUME_ID.toString(), JWT).get(0).getOverallScore());
    }
}
