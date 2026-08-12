package com.slickdev.resume_analyzer.service.constants;


import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.genai.types.Schema;

public class ServiceConstants {
    public static final String API_KEY = System.getenv("GEMINI_API_KEY");
    public static final String RESEND_API_KEY = "" + System.getenv("RESEND_API_KEY");
    // public static final String RESUMATCH_EMAIL = System.getenv("RESUMATCH_EMAIL");
    public static final String EMAIL_OTP_SUBJECT = "Verify your email address";
    public static final String EMAIL_OTP_BODY = "<p>Your OTP verification code is</p><br><strong>%s</strong><p>It will expire in 5 minutes.</p>";
    public static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
        public static final Schema PARSING_SCHEMA = Schema.builder()
            .type("OBJECT")
            .properties(Map.of(
                "fullName", Schema.builder().type("STRING").build(),
                "email", Schema.builder().type("STRING").build(),
                "phone", Schema.builder().type("STRING").build(),
                "location", Schema.builder().type("STRING").build(),
                "summary", Schema.builder().type("STRING").build(),
                "onlineProfiles", Schema.builder()
                    .type("ARRAY")
                    .items(
                        Schema.builder()
                            .type("OBJECT")
                            .properties(Map.of(
                                "platform", Schema.builder().type("STRING").build(),
                                "url", Schema.builder().type("STRING").build()
                            ))
                            .required(List.of("platform", "url"))
                            .build()
                    )
                    .build(),

                "skills", Schema.builder()
                    .type("ARRAY")
                    .items(
                        Schema.builder().type("STRING").build()
                    )
                    .build(),

                "experience", Schema.builder()
                    .type("ARRAY")
                    .items(
                        Schema.builder()
                            .type("OBJECT")
                            .properties(Map.of(
                                "title", Schema.builder().type("STRING").build(),
                                "company", Schema.builder().type("STRING").build(),
                                "period", Schema.builder().type("STRING").build(),
                                "highlights", Schema.builder()
                                    .type("ARRAY")
                                    .items(
                                        Schema.builder().type("STRING").build()
                                    )
                                    .build()
                            ))
                            .required(List.of(
                                "title",
                                "company",
                                "period",
                                "highlights"
                            ))
                            .build()
                    )
                    .build(),

                "education", Schema.builder()
                    .type("ARRAY")
                    .items(
                        Schema.builder()
                            .type("OBJECT")
                            .properties(Map.of(
                                "degree", Schema.builder().type("STRING").build(),
                                "school", Schema.builder().type("STRING").build(),
                                "period", Schema.builder().type("STRING").build()
                            ))
                            .required(List.of(
                                "degree",
                                "school",
                                "period"
                            ))
                            .build()
                    )
                    .build()
            ))
            .required(List.of(
                "fullName",
                "email",
                "phone",
                "location",
                "summary",
                "onlineProfiles",
                "skills",
                "experience",
                "education"
            ))
            .build();

        public static final String RESUME_PARSING_PROMPT = """
                Extract structured data from the provided parsed resume content according to the JSON schema configured for this model.

                Rules:

                * Extract only information explicitly present in the resume. Never invent or infer unsupported facts.
                * Preserve the candidate's original information and relationships between fields (e.g. job → company → dates → responsibilities).
                * Normalize formatting, whitespace, dates, and OCR artifacts where the intended value is clear.
                * Handle varied section names and resume layouts intelligently.
                * Do not duplicate information unnecessarily.
                * If a value cannot be reliably determined, use null or an empty value appropriate to the schema.
                * Do not evaluate, improve, rewrite, or summarize the resume.
                * Return only valid JSON matching the configured schema. No Markdown, explanations, comments, or additional fields.

                Prioritize accuracy and faithful extraction over completeness. When uncertain, leave the value empty rather than guessing.

                Resume: 
                %s
                """;




    public static final String RESUME_ANALYSIS_PROMPT = """ 
              You are a resume analyzer.

Compare the resume with the job description and respond ONLY with a valid JSON object in the exact format below.

Focus on job match, skills alignment, missing keywords, and ATS relevance. Be concise and objective.

{
  "score": 0–100,
  "strengths": [Max 10–12 words per point],
  "weaknesses": [Max 12–15 words per point],
  "improvementSuggestions": [Max 15–18 words per point],
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
