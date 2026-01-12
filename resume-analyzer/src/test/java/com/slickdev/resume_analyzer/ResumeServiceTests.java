package com.slickdev.resume_analyzer;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slickdev.resume_analyzer.Constants.TestConstants;
import com.slickdev.resume_analyzer.entities.UploadedResume;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.exception.EntityNotFoundException;
import com.slickdev.resume_analyzer.exception.FileProcessingException;
import com.slickdev.resume_analyzer.reponses.ResumeAnalysisResponse;
import com.slickdev.resume_analyzer.reponses.ResumeIdResponse;
import com.slickdev.resume_analyzer.reponses.ResumeResponse;
import com.slickdev.resume_analyzer.repositories.ResumeRepository;
import com.slickdev.resume_analyzer.service.constants.ServiceConstants;
import com.slickdev.resume_analyzer.service.impl.CloudinaryService;
import com.slickdev.resume_analyzer.service.impl.PromptBuilder;
import com.slickdev.resume_analyzer.service.impl.ResumeServiceImpl;
import com.slickdev.resume_analyzer.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ResumeServiceTests {
    
    @Mock
    private ResumeRepository repository;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PromptBuilder promptBuilder;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private ApplicationContext context;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    


private UploadedResume resume;
private User user;
private UUID fakeID;
private List<UploadedResume> resumeHistory;

@BeforeEach
void setup() {
    fakeID = UUID.fromString("12345678-1234-1234-1234-1234567890ab");

    user = new User(
        TestConstants.FAKEUSER_USERNAME_STRING,
        TestConstants.FAKEUSER_EMAIL_STRING,
        TestConstants.FAKEUSER_PASSWORD_STRING
    );
    user.setId(fakeID);

    resume = new UploadedResume(
        TestConstants.RESUME_FILENAME,
        TestConstants.RESUME_CONTENT_TYPE,
        TestConstants.RESUME_CONTENT,
        TestConstants.SOURCE_URL,
        user
    );
    resume.setId(fakeID);
    resume.setAnalysis(TestConstants.AI_RESPONSE_CLEANED);

    resumeHistory = new ArrayList<>();
    resumeHistory.add(resume);
}



    @Test
    public void saveResume_ShouldReturnSavedResume() {
        when(repository.save(any(UploadedResume.class))).thenReturn(resume);

        UploadedResume savedResume = resumeService.saveResume(resume);

        assertNotNull(savedResume);
        assertEquals(savedResume.getContent(), resume.getContent());
    }

    @Test
    public void findByConent_ShouldFindResumeByContentAndReturnIt() {
        when(repository.findByContent(anyString())).thenReturn(Optional.of(resume));

        UploadedResume FoundResume = resumeService.findByContent(TestConstants.RESUME_CONTENT);
        
        assertNotNull(FoundResume);
        assertEquals(FoundResume.getFilename(), resume.getFilename());
    }

    @Test
    public void findByContent_ShouldThrowException_WhenNotFound() {
        when(repository.findByContent(anyString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            resumeService.findByContent(TestConstants.RESUME_CONTENT)
        );
    }

    @Test
    public void findById_ShouldFindResumeByIdAndReturnIt() {
        when(repository.findById(fakeID)).thenReturn(Optional.of(resume));

        UploadedResume foundResume = resumeService.findById(TestConstants.FAKE_UUID_STRING);

        assertNotNull(foundResume);
        assertEquals(foundResume.getContent(), resume.getContent());
    }

    @Test
    public void findById_ShouldThrowExceptionWhenNotFound() {
        when(repository.findById(fakeID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            resumeService.findById(TestConstants.FAKE_UUID_STRING)
        );
    }

    @Test
    public void findByContentAndUser_ShouldReturnResumeFromUserAndContent() {
        when(repository.findByUserAndContent(any(User.class), anyString())).thenReturn(Optional.of(resume));

        UploadedResume foundResume = resumeService.findResumeByContentAndUser(user, TestConstants.RESUME_CONTENT);

        assertNotNull(foundResume);
        assertEquals(foundResume.getFilename(), resume.getFilename());
        assertEquals(foundResume.getUser(), user);
    }

    @Test
    public void findByContentAndUser_ShouldThrowExceptionWhenNotFound() {
        when(repository.findByUserAndContent(any(User.class), anyString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
        resumeService.findResumeByContentAndUser(user, TestConstants.RESUME_CONTENT));
    }

    @Test
    public void parseFile_ShouldSaveResumeAndReturnIdAsResponse_ForAuthenticatedUsers_AndUnexistingResume() {
        when(repository.existsByUserAndContent(any(User.class), anyString())).thenReturn(false);
        when(repository.save(any(UploadedResume.class))).thenReturn(resume);
        when(repository.findByUserAndContent(any(User.class), anyString())).thenReturn(Optional.of(resume));
        when(userService.getUser(anyString())).thenReturn(user);
        when(cloudinaryService.uploadResume(any(MultipartFile.class),anyString(),anyString())).thenReturn("source_url");

        ResumeIdResponse responseId = resumeService.parseFile(TestConstants.pdfFile, TestConstants.FAKE_UUID_STRING);

        assertNotNull(responseId);
        verify(userService).getUser(TestConstants.FAKE_UUID_STRING);
        assertEquals(responseId.getResumeId(), resume.getId().toString());
    }

    @Test
    public void parseFile_ShouldSaveResumeAndReturnIdAsResponse_ForAuthenticatedUsers_AndExistingResume() {
        when(repository.existsByUserAndContent(any(User.class), anyString())).thenReturn(true);
        when(repository.findByUserAndContent(any(User.class), anyString())).thenReturn(Optional.of(resume));
        when(userService.getUser(anyString())).thenReturn(user);
                when(cloudinaryService.uploadResume(any(MultipartFile.class),anyString(),anyString())).thenReturn("source_url");

        ResumeIdResponse responseId = resumeService.parseFile(TestConstants.pdfFile, TestConstants.FAKE_UUID_STRING);

        assertNotNull(responseId);
        verify(userService).getUser(TestConstants.FAKE_UUID_STRING);
        assertEquals(responseId.getResumeId(), resume.getId().toString());
    }

    @Test
    public void parseFile_ShouldThrowIOException() throws IOException{
        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
        when(multipartFile.getInputStream()).thenThrow(new IOException("Simulated IO error"));

        assertThrows(FileProcessingException.class, () -> 
        resumeService.parseFile(multipartFile, null));
    }
    
    
    @Test
    @SuppressWarnings("unchecked")
    public void analyzeResume_ShouldReturnResumeAnalysis() {
        when(repository.findById(fakeID)).thenReturn(Optional.of(resume));
        when(promptBuilder.buildPrompt(anyString(), anyString())).thenReturn(TestConstants.PROMPT_STRING);

        //AI response
        Map<String, String> textPart = Map.of("text", TestConstants.AI_RESPONSE);
        Map<String, Object> content = Map.of("parts", List.of(textPart));
        Map<String, Object> candidate = Map.of("content", content);
        Map<String, Object> responseBody = Map.of("candidates", List.of(candidate));
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(ServiceConstants.API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        ResumeAnalysisResponse response = resumeService.analyzeResume(TestConstants.FAKE_UUID_STRING, TestConstants.JOB_DESCRIPTION);
        assertEquals(TestConstants.score, response.getScore());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyzeResume_ShouldThrowRuntimeException_whenGeminiApiReturnsError() {
        when(repository.findById(fakeID)).thenReturn(Optional.of(resume));
        when(promptBuilder.buildPrompt(anyString(), anyString())).thenReturn(TestConstants.PROMPT_STRING);

        when(restTemplate.exchange(
                eq(ServiceConstants.API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad request"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
        resumeService.analyzeResume(TestConstants.FAKE_UUID_STRING, TestConstants.JOB_DESCRIPTION)
            );
        assertTrue(ex.getMessage().contains("Gemini API Error"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyzeResume_ShouldThrowRuntimeException_WhenApiConnectionsFail() {
        when(repository.findById(fakeID)).thenReturn(Optional.of(resume));
        when(promptBuilder.buildPrompt(anyString(), anyString())).thenReturn(TestConstants.PROMPT_STRING);

        when(restTemplate.exchange(
            eq(ServiceConstants.API_URL),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
    )).thenThrow(new ResourceAccessException("Connection timeout"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
        resumeService.analyzeResume(TestConstants.FAKE_UUID_STRING, TestConstants.JOB_DESCRIPTION)
            );
        assertTrue(ex.getMessage().contains("Connection Error"));
    }

    @Test
    public void isStrictPdf_shouldThrowIOException_whenPDFisMalformed() {
        byte[] malformedPdf = (
                "%PDF-1.4\n" +
                "1 0 obj\n" +
                "<< /Type /Catalog >>\n" +
                "endobj\n"
        ).getBytes();
        //InputStream
        InputStream inputStream = new ByteArrayInputStream(malformedPdf);
        boolean result = resumeService.isStrictPdf(inputStream);
        assertFalse(result);
    }

    @Test
    public void getUserResumes_shouldReturnUsersResumeList() {
        when(userService.getUser(anyString())).thenReturn(user);
        when(repository.findAllByUser(user)).thenReturn(resumeHistory);

        List<ResumeResponse> history = resumeService.getUserResumes(TestConstants.FAKE_UUID_STRING);
        assertEquals(resume.getFilename(), history.get(0).getName());

    }
    
}

