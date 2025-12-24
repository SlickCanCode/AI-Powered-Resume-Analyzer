package com.slickdev.resume_analyzer.Constants;

import java.util.List;


public class TestConstants {
    public static final String FAKE_UUID_STRING = "123456781234123412341234567890ab";
    public static final String FAKE_WRONG_UUID_STRING = "543216781234123412341234567890ab";
    public static final String FAKEUSER_EMAIL_STRING = "johndoe@gmail.com";
    public static final String FAKEUSER_FULLNAME_STRING = "John Doe";
    public static final String FAKEUSER_USERNAME_STRING = "joe";
    public static final String FAKEUSER_PASSWORD_STRING = "somePassword123";
    public static final String FAKE_ENCODED_PASSWORD = "encodedPassword";

    public static final String RESUME_FILENAME = "Test_resume";
    public static final String RESUME_CONTENT_TYPE = ".PDF";
    public static final String RESUME_CONTENT = "Some_Content";
    public static final byte[] RESUME_DATA = "Test1234".getBytes();
    public static final String JOB_DESCRIPTION = "Backend Developer";
    public static final String PROMPT_STRING = "Prompt built with resume and job description";
    public static final String AI_RESPONSE = "```{\n" +
                    "  \"score\": 30,\n" +
                    "  \"strengths\": [\"Good Skill\"],\n" +
                    "  \"weaknesses\": [\"Bad Grammar\"],\n" +
                    "  \"improvementSuggestions\": [\"Improve Grammar\"]\n" +
                    "}";
    public static final int score = 30;
    public static final List<String> strengths = List.of("Good Skill");
    public static final List<String> weaknesses = List.of("Bad Grammar");
    public static final List<String> improvementSuggestions = List.of("Improve Grammar");
}