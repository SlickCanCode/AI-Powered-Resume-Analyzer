package com.slickdev.resume_analyzer.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;

import com.slickdev.resume_analyzer.reponses.AnalysisSummaryResponse;
import com.slickdev.resume_analyzer.reponses.ResumeDataResponse;
import com.slickdev.resume_analyzer.service.ResumeService;

@WebMvcTest(ResumeController.class)
@AutoConfigureMockMvc(addFilters = false)
class ResumeControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private ResumeService resumeService;

    @Test
    void getAllAnalysesReturnsOnlySummaryFields() throws Exception {
        when(resumeService.getAllAnalyses("jwt")).thenReturn(List.of(
                new AnalysisSummaryResponse("resume.pdf", LocalDateTime.of(2026, 1, 1, 9, 0), 82, 79)));

        mockMvc.perform(get("/api/v1/resumes/analyses").cookie(new jakarta.servlet.http.Cookie("access_token", "jwt")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resumeName").value("resume.pdf"))
                .andExpect(jsonPath("$[0].score").value(82))
                .andExpect(jsonPath("$[0].atsScore").value(79));
    }

    @Test
    void getResumeReturnsStructuredParsedData() throws Exception {
        ResumeDataResponse response = ResumeDataResponse.builder().resumeId("resume-id").fullName("Ada Lovelace")
                .email("ada@example.com").phone("123").location("Lagos").summary("Engineer")
                .onlineProfiles(List.of()).skills(List.of("Java")).experience(List.of()).education(List.of()).build();
        when(resumeService.getResumeData("resume-id", "jwt")).thenReturn(response);

        mockMvc.perform(get("/api/v1/resumes/resume-id").cookie(new jakarta.servlet.http.Cookie("access_token", "jwt")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Lagos"))
                .andExpect(jsonPath("$.summary").value("Engineer"));
    }
}
