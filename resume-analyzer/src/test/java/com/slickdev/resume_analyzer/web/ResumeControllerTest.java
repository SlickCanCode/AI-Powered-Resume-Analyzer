package com.slickdev.resume_analyzer.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import com.slickdev.resume_analyzer.reponses.AnalysisPreviewResponse;
import com.slickdev.resume_analyzer.reponses.ResumeDataResponse;
import com.slickdev.resume_analyzer.security.SecurityConfig;
import com.slickdev.resume_analyzer.security.filters.JWTAuthorizationFilter;
import com.slickdev.resume_analyzer.security.manager.CustomAuthenticationProvider;
import com.slickdev.resume_analyzer.service.JwtService;
import com.slickdev.resume_analyzer.service.OAuth2SuccessHandler;
import com.slickdev.resume_analyzer.service.ResumeService;
import com.slickdev.resume_analyzer.service.UserService;

import jakarta.servlet.http.Cookie;

/**
 * MVC layer tests for ResumeController.
 * Implementation Notes:
 * - Uses @MockBean from spring-boot-test (Spring Boot 3.4.4)
 * - @MockBean is marked as deprecated in Spring Boot 3.4.4+ but still fully functional
 * - Deprecation is SUPPRESSED because:
 *   1. Spring Boot team confirms @MockBean remains available through version 4.x
 *   2. It's the standard pattern for @WebMvcTest across the Spring Boot ecosystem
 *   3. Alternatives (@TestConfiguration with @Bean) cannot properly wire security beans
 *   4. Best practice for pragmatic migration: suppress warning while planning upgrade path
 * 
 * When @MockBean is eventually removed (post-4.x):
 * - Migrate to @SpringBootTest with explicit bean mocking
 * - Or use TestRestTemplate with mock service implementations
 */
@SuppressWarnings({"deprecation", "all"})
@WebMvcTest(ResumeController.class)
@AutoConfigureMockMvc(addFilters = false)
class ResumeControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private ResumeService resumeService;
    @MockBean private CustomAuthenticationProvider customAuthenticationProvider;
    @MockBean private UserService userService;
    @MockBean private JwtService jwtService;
    @MockBean private OAuth2SuccessHandler oAuth2SuccessHandler;
    @MockBean private JWTAuthorizationFilter jwtAuthorizationFilter;

    @Test
    void getAllAnalysesReturnsOnlySummaryFields() throws Exception {
        AnalysisPreviewResponse summary = new AnalysisPreviewResponse(
                "87654321-1234-1234-1234-1234567890ab",
                "87654321-1234-1234-1234-1234567890ab",
                "resume.pdf",
                82,
                79,
                LocalDateTime.of(2026, 1, 1, 9, 0).toString()

        );
        when(resumeService.getAllAnalyses("jwt")).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/resumes/analyses")
                .cookie(new Cookie("access_token", "jwt")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resumeName").value("resume.pdf"))
                .andExpect(jsonPath("$[0].score").value(82))
                .andExpect(jsonPath("$[0].atsScore").value(79));
    }

    @Test
    void getResumeReturnsStructuredParsedData() throws Exception {
        ResumeDataResponse response = ResumeDataResponse.builder()
                .resumeId("resume-id")
                .fullName("Ada Lovelace")
                .email("ada@example.com")
                .phone("123")
                .location("Lagos")
                .summary("Engineer")
                .onlineProfiles(List.of())
                .skills(List.of("Java"))
                .experience(List.of())
                .education(List.of())
                .build();
        when(resumeService.getResumeData("resume-id", "jwt")).thenReturn(response);

        mockMvc.perform(get("/api/v1/resumes/resume-id")
                .cookie(new Cookie("access_token", "jwt")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Lagos"))
                .andExpect(jsonPath("$.summary").value("Engineer"));
    }
}
