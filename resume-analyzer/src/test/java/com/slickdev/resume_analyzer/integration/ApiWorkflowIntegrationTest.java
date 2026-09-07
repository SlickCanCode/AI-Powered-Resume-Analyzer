package com.slickdev.resume_analyzer.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.Cookie;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slickdev.resume_analyzer.entities.ResumeAnalysis;
import com.slickdev.resume_analyzer.entities.ResumeData;
import com.slickdev.resume_analyzer.entities.UploadedResume;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.entities.VerificationToken;
import com.slickdev.resume_analyzer.repositories.ResumeAnalysisRepository;
import com.slickdev.resume_analyzer.repositories.ResumeDataRepository;
import com.slickdev.resume_analyzer.repositories.ResumeRepository;
import com.slickdev.resume_analyzer.repositories.UserRepository;
import com.slickdev.resume_analyzer.repositories.VerificationTokenRepository;
import com.slickdev.resume_analyzer.service.ai.Gemini.GeminiService;
import com.slickdev.resume_analyzer.service.impl.JwtServiceImpl;
import com.slickdev.resume_analyzer.service.impl.OtpServiceImpl;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class ApiWorkflowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ResumeRepository resumeRepository;
    @Autowired private ResumeDataRepository resumeDataRepository;
    @Autowired private ResumeAnalysisRepository resumeAnalysisRepository;
    @Autowired private VerificationTokenRepository verificationTokenRepository;
    @Autowired private JwtServiceImpl jwtService;

    @MockBean private GeminiService geminiService;
    @SpyBean private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        doNothing().when(otpService).sendOtp(anyString(), anyString());
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            String otp = "123456";
            String otpHash = new BCryptPasswordEncoder().encode(otp);
            VerificationToken token = VerificationToken.builder()
                    .otpHash(otpHash)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .valid(true)
                    .build();
            user.addToken(token);
            verificationTokenRepository.save(token);
            return otp;
        }).when(otpService).generateOtp(any(User.class));
    }

    @Test
    void registrationAndProfileFlowWorksForAuthenticatedUser() throws Exception {
        String email = "new-user@example.com";

        MvcResult registerResult = mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content("{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\",\"email\":\"" + email + "\",\"password\":\"StrongPass123!\"}"))
                .andExpect(status().isCreated())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        MockHttpServletResponse response = registerResult.getResponse();
        String accessToken = response.getCookie("access_token").getValue();
        assertNotNull(accessToken);

        mockMvc.perform(get("/api/v1/users/me")
                .cookie(response.getCookie("access_token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        mockMvc.perform(get("/api/v1/users/me/subscription")
                .cookie(response.getCookie("access_token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("FREE"));

        mockMvc.perform(get("/api/v1/dashboard/me/stats")
                .cookie(response.getCookie("access_token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysesLeft").exists());
    }

    @Test
    void otpFlowCreatesAndVerifiesTokensForUser() throws Exception {
        String email = "otp-user@example.com";
        User user = userRepository.save(new User("Grace", "Hopper", email, "StrongPass123!"));

        String otpHash = new BCryptPasswordEncoder().encode("123456");
        VerificationToken token = VerificationToken.builder()
                .otpHash(otpHash)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .valid(true)
                .build();
        verificationTokenRepository.save(token);

        mockMvc.perform(post("/api/v1/auth/send-otp")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/verify-otp")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"otp\":\"123456\",\"purpose\":\"reset-password\"}"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"));

        assertThat(userRepository.findByEmail(email).orElseThrow().isEmailVerified()).isTrue();
    }

    @Test
    void authenticatedResumeAndDashboardEndpointsExposeOnlyOwnerData() throws Exception {
        User owner = userRepository.save(new User("Ada", "Lovelace", "owner@example.com", "StrongPass123!"));
        User otherUser = userRepository.save(new User("Linus", "Torvalds", "other@example.com", "StrongPass123!"));

        UploadedResume ownerResume = resumeRepository.save(UploadedResume.builder()
                .filename("owner-resume.pdf")
                .fileType("application/pdf")
                .parsedContent("Ada is a backend engineer with Java and Spring.")
                .user(owner)
                .analysisCount(1)
                .latestScore(92)
                .build());

        resumeRepository.save(UploadedResume.builder()
                .filename("other-resume.pdf")
                .fileType("application/pdf")
                .parsedContent("Other user resume")
                .user(otherUser)
                .analysisCount(0)
                .latestScore(0)
                .build());

        ResumeData resumeData = resumeDataRepository.save(ResumeData.builder()
                .resume(ownerResume)
                .fullName("Ada Lovelace")
                .email("owner@example.com")
                .phone("1234567890")
                .location("London")
                .careerSummary("Backend engineer")
                .onlineProfiles(List.of())
                .skills(List.of("Java", "Spring"))
                .experience(List.of())
                .education(List.of())
                .build());

        ResumeAnalysis analysis = resumeAnalysisRepository.save(ResumeAnalysis.builder()
                .resume(ownerResume)
                .overallScore(92)
                .atsScore(88)
                .keywordScore(90)
                .strengths(List.of("Java", "Spring"))
                .weaknesses(List.of("Testing"))
                .existingSkills(List.of("Java"))
                .skillsToDevelop(List.of("Kubernetes"))
                .grammarIssues(List.of())
                .recommendations(List.of())
                .build());

        ownerResume.setAnalysis(new ArrayList<>(List.of(analysis)));
        ownerResume.setResumeData(resumeData);
        resumeRepository.save(ownerResume);

        String accessToken = jwtService.generateAccessToken(owner);

        mockMvc.perform(get("/api/v1/resumes")
                .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("owner-resume.pdf"));

        mockMvc.perform(get("/api/v1/resumes/" + ownerResume.getId())
                .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").exists());

        mockMvc.perform(get("/api/v1/dashboard/me/recent-analyses")
                .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("owner-resume.pdf"));
    }

    @Test
    void resumeUploadEndpointPersistsParsedDataForAuthenticatedUser() throws Exception {
        String email = "resume-uploader@example.com";
        User user = userRepository.save(new User("Ada", "Lovelace", email, "StrongPass123!"));
        String accessToken = jwtService.generateAccessToken(user);

        when(geminiService.parseResume(anyString())).thenReturn(ResumeData.builder()
                .fullName("Ada Lovelace")
                .email(email)
                .phone("1234567890")
                .location("London")
                .careerSummary("Backend engineer")
                .onlineProfiles(List.of())
                .skills(List.of("Java", "Spring"))
                .experience(List.of())
                .education(List.of())
                .build());

        byte[] pdfBytes;
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 18);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("Ada Lovelace");
                contentStream.endText();
            }
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                document.save(outputStream);
                pdfBytes = outputStream.toByteArray();
            }
        }

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                pdfBytes);

        mockMvc.perform(multipart("/api/v1/resumes/upload")
                .file(file)
                .cookie(new Cookie("access_token", accessToken))
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.fullName").value("Ada Lovelace"));
    }

    @Test
    void protectedRoutesRejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/dashboard/me/stats"))
                .andExpect(status().isUnauthorized());
    }
}
