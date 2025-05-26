package com.slickdev.resume_analyzer.service.constants;



public class ServiceConstants {
    public static final String API_KEY = System.getenv("GEMINI_API_KEY");
    public static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;
    
    public static final String RESUME_ANALYSIS_PROMPT = """
        You are an expert career coach. Compare the resume below to the job description and reply ONLY with a JSON object like this:
        {
            "score": (number between 0 and 100),
            "strengths": [list of strengths],
            "weaknesses": [list of weaknesses],
            "improvementSuggestions": [list of improvements]
        }

        Resume:
        %s

        Job Description:
        %s
        """;
}
