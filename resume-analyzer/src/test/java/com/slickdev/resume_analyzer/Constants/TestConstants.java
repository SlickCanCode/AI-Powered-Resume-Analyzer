package com.slickdev.resume_analyzer.Constants;

import java.util.List;
import java.util.UUID;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;


public class TestConstants {
    public static final String FAKE_UUID_STRING = "123456781234123412341234567890ab";
    public static final String FAKE_WRONG_UUID_STRING = "543216781234123412341234567890ab";
    public static final String FAKEUSER_EMAIL_STRING = "johndoe@gmail.com";
    public static final String FAKEUSER_FULLNAME_STRING = "John Doe";
    public static final String FAKEUSER_USERNAME_STRING = "joey123";
    public static final String FAKEUSER_PASSWORD_STRING = "somePassword123";
    public static final String FAKE_ENCODED_PASSWORD = "encodedPassword";

    public static final String SOURCE_URL = "https://some_url";
    public static final String RESUME_FILENAME = "Test_resume";
    public static final String RESUME_CONTENT_TYPE = ".PDF";
    public static final String RESUME_CONTENT = "Some Content";
    public static final String JOB_DESCRIPTION = "Backend Developer";
    public static final String PROMPT_STRING = "Prompt built with resume and job description";
    public static final String AI_RESPONSE = "```{\n" +
                    "  \"score\": 30,\n" +
                    "  \"strengths\": [\"Good Skill\"],\n" +
                    "  \"weaknesses\": [\"Bad Grammar\"],\n" +
                    "  \"improvementSuggestions\": [\"Improve Grammar\"]\n" +
                    "}";

    public static String AI_RESPONSE_CLEANED = "{\n" +
                    "  \"score\": 30,\n" +
                    "  \"strengths\": [\"Good Skill\"],\n" +
                    "  \"weaknesses\": [\"Bad Grammar\"],\n" +
                    "  \"improvementSuggestions\": [\"Improve Grammar\"]\n" +
                    "}";
    public static final int score = 30;
    public static final List<String> strengths = List.of("Good Skill");
    public static final List<String> weaknesses = List.of("Bad Grammar");
    public static final List<String> improvementSuggestions = List.of("Improve Grammar");
    public static final byte[] pdfBytes = (
                "%PDF-1.4\n" +
                "1 0 obj\n" +
                "<< /Type /Catalog /Pages 2 0 R >>\n" +
                "endobj\n" +
                "2 0 obj\n" +
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>\n" +
                "endobj\n" +
                "3 0 obj\n" +
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 300 144] >>\n" +
                "endobj\n" +
                "xref\n" +
                "0 4\n" +
                "0000000000 65535 f \n" +
                "trailer\n" +
                "<< /Root 1 0 R >>\n" +
                "%%EOF"
        ).getBytes();
    public static final MultipartFile pdfFile = new MockMultipartFile(
                "file",
                "test-file.pdf",
                "application/pdf",
                pdfBytes
        );

    public static final String signinRequest = "{\"userName\":\"slickfeet\", \"email\":\"johndoe123@gmail.com\", \"password\":\"johndoe123\"}";
    public static final String badSigninRequest = "{\"fullName\":\"joh\", \"userName\":\"sli\", \"email\":\"johndoe123\", \"password\":\"jo\"}";
    public static final UUID FAKE_UUID = UUID.fromString("12345678-1234-1234-1234-1234567890ab");
    public static final String BAD_EDITUSER_REQ = "{\"userName\":\"slicky\", \"email\":\"johndoe12@gmail.com\"}";
    public static final String GOOD_EDITUSER_REQ = "{\"userName\":\"Userame\", \"email\":\"johndoe1@gmail.com\"}";    
    public static final String JOB_DESCRIPTION_REQUEST = "{\"jobDescription\":\"juiorDev\"}";
}