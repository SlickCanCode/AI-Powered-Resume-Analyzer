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


        public static final Schema ANALYSIS_SCHEMA = Schema.builder()
    .type("OBJECT")
    .properties(Map.of(

        "overallScore",
        Schema.builder().type("INTEGER").build(),

        "atsScore",
        Schema.builder().type("INTEGER").build(),

        "keywordScore",
        Schema.builder().type("INTEGER").build(),

        "strengths",
        Schema.builder()
            .type("ARRAY")
            .items(
                Schema.builder().type("STRING").build()
            )
            .build(),

        "weaknesses",
        Schema.builder()
            .type("ARRAY")
            .items(
                Schema.builder().type("STRING").build()
            )
            .build(),

        "missingKeywords",
        Schema.builder()
            .type("ARRAY")
            .items(
                Schema.builder().type("STRING").build()
            )
            .build(),

        "foundKeywords",
        Schema.builder()
            .type("ARRAY")
            .items(
                Schema.builder().type("STRING").build()
            )
            .build(),

        "grammarIssues",
        Schema.builder()
            .type("ARRAY")
            .items(
                Schema.builder()
                    .type("OBJECT")
                    .properties(Map.of(
                        "text",
                        Schema.builder().type("STRING").build(),

                        "suggestion",
                        Schema.builder().type("STRING").build()
                    ))
                    .required(List.of("text", "suggestion"))
                    .build()
            )
            .build(),

        "recommendations",
        Schema.builder()
            .type("ARRAY")
            .items(
                Schema.builder()
                    .type("OBJECT")
                    .properties(Map.of(
                        "title",
                        Schema.builder().type("STRING").build(),

                        "description",
                        Schema.builder().type("STRING").build(),

                        "impact",
                        Schema.builder()
                            .type("STRING")
                            .description("Must be one of: high, medium, low")
                            .build()
                    ))
                    .required(List.of(
                        "title",
                        "description",
                        "impact"
                    ))
                    .build()
            )
            .build()
    ))
    .required(List.of(
        "overallScore",
        "atsScore",
        "keywordScore",
        "strengths",
        "weaknesses",
        "missingKeywords",
        "foundKeywords",
        "grammarIssues",
        "recommendations"
    ))
    .build();


        public static final String RESUME_ANALYSIS_PROMPT = """ 
            You are an expert ATS evaluator and senior recruiter with 30+ years of hiring experience. Evaluate the candidate's resume against the provided job description as a modern ATS would, followed by an experienced recruiter reviewing the candidate.

            Your goal is to determine how competitive this specific resume is for this specific job, not whether the resume is generally good.

            Evaluation Method

            First, identify the job's most important requirements and internally classify them as:

            Required: essential skills, qualifications, experience, technologies, responsibilities, or credentials.
            Preferred: valuable but non-essential requirements.
            Contextual: terminology, domain knowledge, or expectations that strengthen the match but are not explicit requirements.

            Then compare each important requirement against the resume and determine whether it is:

            Strongly supported: clear, relevant evidence exists.
            Partially supported: related evidence exists but is incomplete or weak.
            Unsupported: the requirement is absent or cannot reasonably be established from the resume.

            Use this internal requirement-to-evidence comparison as the foundation for all scores, keywords, weaknesses, and recommendations.

            ATS Evaluation

            Evaluate whether a typical modern ATS could identify the candidate as relevant based on:

            Required and preferred keywords
            Technical skills and technologies
            Relevant job titles and terminology
            Work experience
            Responsibilities
            Education
            Certifications
            Qualifications
            Relevant domain terminology
            Legitimate keyword variations and synonyms

            Distinguish between genuinely equivalent terminology and different technologies.

            For example, do not treat JavaScript as Java, SQL as PostgreSQL expertise, React as React Native, or AWS as evidence of every AWS service.

            A keyword is stronger when it is supported by relevant experience or achievements than when it appears only in a skills list.

            Do not reward keyword stuffing, irrelevant skills, unexplained technologies, or repetitive keyword usage.

            Recruiter Evaluation

            Evaluate the resume as an experienced recruiter would after the ATS stage.

            Determine:

            Whether the candidate appears qualified for this particular role
            Whether the most relevant experience is immediately apparent
            Whether experience demonstrates the required responsibilities
            Whether achievements demonstrate meaningful impact
            Whether technical skills are supported by evidence
            Whether the career history is relevant and credible
            Whether important requirements are missing
            Whether the resume communicates value quickly
            Whether the recruiter would likely continue to the next screening stage

            A strong general resume should still receive a modest score if it is poorly aligned with the target job.

            Resume Quality

            Evaluate only factors that affect this job's screening outcome, including:

            Clarity
            Relevance
            Conciseness
            Grammar and wording
            Achievement orientation
            Quantifiable impact
            Action verbs
            Consistency
            Vague or redundant statements
            Professional presentation and information hierarchy

            Do not invent errors or assume information that is not present.

            Keywords
            Found Keywords

            Include important job-specific keywords and concepts that are genuinely supported by the resume.

            Prioritize meaningful requirements over generic words. Do not count incidental mentions as meaningful matches.

            Missing Keywords

            Include important requirements or concepts that are absent, weakly supported, or insufficiently demonstrated.

            Prioritize:

            Required skills and technologies
            Required qualifications
            Core responsibilities
            Required experience
            Important domain terminology
            High-value preferred requirements

            Do not mark a requirement as missing when the resume clearly demonstrates it through a legitimate equivalent term.

            Never recommend adding a keyword unless the candidate genuinely possesses the underlying skill or experience.

            Scoring

            Use integers from 0–100.

            Overall Score

            The overall score measures the candidate's overall competitiveness for this specific job, combining ATS alignment and recruiter-level relevance.

            Calibrate scores strictly:

            90–100: Exceptional match. Strong evidence for nearly all critical requirements with very few meaningful gaps.
            80–89: Strong match. Most important requirements are supported, with some gaps or opportunities.
            70–79: Competitive but imperfect. Good alignment but noticeable missing or weak requirements.
            60–69: Weak/moderate match. Several important requirements are missing or insufficiently supported.
            40–59: Poor match. Major requirements are absent or weakly demonstrated.
            0–39: Very poor match for the target role.

            A 90+ score must be rare and represent genuinely exceptional alignment. Do not inflate scores because the resume is well formatted, professionally written, or impressive in a general sense.

            ATS Score

            Measure the likelihood that the resume would satisfy the job-specific matching criteria of a typical modern ATS.

            Base this primarily on:

            Critical requirement coverage
            Keyword and terminology alignment
            Skills alignment
            Relevant experience
            Job-title alignment
            Qualifications
            Certifications
            Evidence supporting required capabilities

            Excellent writing or formatting must not compensate for missing job requirements.

            Keyword Score

            Measure the strength of meaningful keyword and terminology alignment.

            Consider:

            Required keyword coverage
            Preferred keyword coverage
            Exact matches
            Legitimate semantic equivalents
            Context surrounding keywords
            Relevance of keyword usage
            Evidence supporting the keywords

            Do not count every occurrence equally and do not reward keyword stuffing.

            Evidence and Scoring Rules

            All conclusions must be grounded in the provided resume and job description.

            Never:

            Invent experience, achievements, metrics, technologies, qualifications, certifications, or education
            Assume proficiency merely because a related technology is listed
            Treat vague similarity as a requirement match
            Reward keyword stuffing
            Give a high score simply because the resume is professionally written
            Penalize the candidate for information that is irrelevant to the target job
            Recommend fabricated information
            Inflate scores to encourage the candidate

            When evidence is ambiguous, score conservatively.

            When a requirement is critical, missing evidence for that requirement should have a materially larger effect on the score than missing a minor preferred keyword.

            Strengths and Weaknesses

            Strengths must explain why the resume is strong for this particular job and should reference meaningful evidence.

            Weaknesses must identify factors that could materially reduce ATS or recruiter performance for this position.

            Avoid generic statements such as "good technical skills" or "needs improvement."

            Grammar Issues

            Report only genuine grammar, spelling, wording, or clarity problems found in the resume.

            For each issue:

            Include only the relevant short text in text.
            Provide the corrected wording or actionable correction in suggestion.

            Do not invent issues. If there are no meaningful issues, return an empty array.

            Recommendations

            Return the highest-impact improvements first.

            Recommendations must be specific to the target job and based on evidence from the resume.

            Prioritize changes such as:

            Adding truthful evidence for a critical requirement
            Strengthening weak evidence for an important skill
            Rewriting vague responsibilities into achievement-oriented statements
            Adding relevant metrics when the candidate actually has them
            Improving visibility of relevant experience
            Removing irrelevant content
            Clarifying ambiguous experience
            Adding legitimate job-specific terminology when supported by the candidate's actual experience

            If the resume does not contain information necessary to support a recommendation, make clear that the candidate should add it only if truthful.

            Final Instruction

            Think in this sequence:

            Job requirements → requirement/evidence matching → ATS compatibility → recruiter relevance → score → weaknesses → highest-impact improvements.

            The analysis must be job-specific, evidence-based, conservative, and internally consistent.

            Do not judge whether the candidate is a good professional in general. Judge whether the provided resume convincingly demonstrates that the candidate is a strong match for the provided job.

            Use only the information contained in the resume and job description.

            Return only the structured response required by the provided response schema. Do not output explanations, Markdown, or additional text outside the schema.

            resume:
            %s

            jobDescription:
            %s
            """;

}
