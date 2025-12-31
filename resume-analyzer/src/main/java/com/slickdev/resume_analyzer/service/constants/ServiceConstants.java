package com.slickdev.resume_analyzer.service.constants;



public class ServiceConstants {
    public static final String API_KEY = System.getenv("GEMINI_API_KEY");
    public static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
    
    public static final String RESUME_ANALYSIS_PROMPT = """ 
                You are an expert career coach.

            Compare the resume and job description below and reply ONLY with a valid JSON object in this format:

            {
            "score": number between 0 and 100,
            "strengths": [list of strengths],
            "weaknesses": [list of weaknesses],
            "improvementSuggestions": [list of actionable improvements],
            "jobRecommendations": [
                { "platform": "LinkedIn", "link": "https://..." },
                { "platform": "Indeed", "link": "https://..." }
            ]
            }

            Provide LinkedIn and Indeed job links that match the candidate based on the analysis.

            Resume:
            %s

            Job Description:
            %s
        """;

}
