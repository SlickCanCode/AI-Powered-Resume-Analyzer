package com.slickdev.resume_analyzer.service.constants;



public class ServiceConstants {
    public static final String API_KEY = System.getenv("GEMINI_API_KEY");
    public static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
    
    public static final String RESUME_ANALYSIS_PROMPT = """ 
              You are a resume analyzer.

Compare the resume with the job description and respond ONLY with a valid JSON object in the exact format below.

Focus on job match, skills alignment, missing keywords, and ATS relevance. Be concise and objective.

{
  "score": 0–100,
  "strengths": [],
  "weaknesses": [],
  "improvementSuggestions": [],
  "jobRecommendations": [
    { "platform": "LinkedIn", "link": "https://..." },
    { "platform": "Indeed", "link": "https://..." }
  ]
}


Job links must be relevant to the candidate’s skills and experience.

Resume:
%s

Job Description:
%s
        """;

}
