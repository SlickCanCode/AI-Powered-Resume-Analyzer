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
                "careerSummary", Schema.builder().type("STRING").build(),
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
                "careerSummary",
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
                Schema.builder().type("STRING").description("Max 1 sentence").build()
            )
            .build(),

        "weaknesses",
        Schema.builder()
            .type("ARRAY")
            .items(
                Schema.builder().type("STRING").description("Max 1 sentence").build()
            )
            .build(),

        "existingSkills",
        Schema.builder()
            .type("ARRAY")
            .items(
                Schema.builder().type("STRING").build()
            ).description("Short keyword or phrase from the job requirements; maximum 4 words; never a sentence")
            .build(),

        "skillsToDevelop",
        Schema.builder()
            .type("ARRAY")
            .items(
                Schema.builder().type("STRING").build()
            ).description("Short keyword or phrase from the job requirements; maximum 4 words; never a sentence")
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
                        Schema.builder().type("STRING").description("Max 2 Sentences").build(),

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
    "existingSkills",
    "skillsToDevelop",
    "grammarIssues",
    "recommendations"
))
    .build();

        public static final Schema JOB_MATCH_SCHEMA = Schema.builder()
            .type("OBJECT")
            .properties(Map.of(
                "matchScore", Schema.builder().type("INTEGER").build(),
                "foundSkills", Schema.builder().type("ARRAY").items(Schema.builder().type("STRING").description("Short keyword or phrase from the job requirements; maximum 4 words; never a sentence").build()).build(),
                "missingSkills", Schema.builder().type("ARRAY").items(Schema.builder().type("STRING").description("Short keyword or phrase from the job requirements; maximum 4 words; never a sentence").build()).build(),
                "aiSuggestions", Schema.builder().type("ARRAY").items(Schema.builder().type("STRING").description("Max 2 sentences").build()).build()
            ))
            .required(List.of("matchScore", "foundSkills", "missingSkills", "aiSuggestions"))
            .build();

        public static final String JOB_MATCH_PROMPT = """

            Compare the candidate's resume against the provided job posting and produce a strict, evidence-based match assessment.

            Evaluate the match **from the perspective of the job requirements**, not the resume alone.

            ### Skill Matching

            Identify the important skills, technologies, qualifications, and requirements explicitly stated in the job posting.

            **foundSkills** must contain only important skills or requirements that:

            * Are stated or clearly required by the job posting, AND
            * Are explicitly listed or convincingly demonstrated in the resume.
            * Use the terminology from the job posting where possible.
            * Do not include skills that exist only in the resume but are irrelevant to the job.

            **missingSkills** must contain important skills or requirements that:

            * Are stated or clearly required by the job posting, AND
            * Are absent from the resume or not sufficiently supported by evidence in the resume.
            * Do not mark a skill as missing when the resume clearly demonstrates a legitimate equivalent or synonym.
            * Prioritize required skills over preferred skills.

            Do not treat vaguely related technologies as equivalent. For example, do not assume JavaScript means Java, SQL means PostgreSQL, React means React Native, or AWS means every AWS service.

            ### Match Score

            Score the overall match from **0–100** based primarily on how well the resume satisfies the job's important requirements.

            Consider:

            * Required skills and technologies
            * Preferred skills
            * Relevant experience
            * Qualifications
            * Responsibilities
            * Education and certifications when relevant
            * Strength and relevance of the evidence in the resume

            Required requirements should have substantially more influence on the score than optional requirements.

            Use conservative scoring. A high score requires strong evidence that the candidate satisfies most of the important requirements. Do not give a high score simply because the resume contains many skills or is professionally written.

            ### AI Suggestions

            Provide concise, high-impact suggestions for improving the candidate's match for this specific job.

            Suggestions should:

            * Be directly related to gaps between the job and resume
            * Focus on the highest-impact improvements
            * Be actionable
            * Be truthful
            * Never recommend fabricating experience, skills, qualifications, certifications, or achievements

            If a missing skill is genuinely possessed by the candidate but simply absent from the resume, suggest making that experience visible. Otherwise, do not suggest adding it.

            ### Output Rules

            Keep `foundSkills`, `missingSkills`, and `aiSuggestions` concise:

            * Use keywords or short phrases only.
            * Maximum 4 words per item.
            * Never write sentences in these fields.
            * Avoid duplicates.
            * Prioritize the most important items rather than listing every minor requirement.

            If the job posting is provided only as a URL, retrieve and analyze the actual job requirements from that URL before evaluating the match.

            Return only the structured response required by the provided schema. Do not output additional explanations.

            Resume:
            %s

            Job posting:
            %s
            """;


        public static final String RESUME_ANALYSIS_PROMPT = """ 
           You are a strict, evidence-based ATS evaluator and senior recruiter. Evaluate the candidate's resume against the provided job description as a modern ATS followed by a skeptical recruiter.

            Your goal is to determine:

            * How well the candidate's skills match this specific job.
            * Which relevant skills the candidate already demonstrates.
            * Which important skills they genuinely need to develop.
            * How competitive the candidate is if they continue applying with this resume.
            * Whether the primary problem is the resume, the candidate's skill gaps, or both.

            Evaluate using realistic current hiring standards. Do not be overly generous or judge whether the candidate is generally employable. Judge whether this resume provides credible evidence that the candidate can compete for THIS job.

            ### REQUIREMENT ANALYSIS

            First identify the job's important requirements and internally classify them as:

            * Required: essential to performing the role.
            * Preferred: useful but non-essential.
            * Contextual: terminology or knowledge that strengthens the match.

            For each important requirement, determine whether the resume provides:

            * Strong evidence
            * Partial/weak evidence
            * No evidence

            Only count what the resume actually demonstrates.

            Never assume a skill because of a related skill, degree, job title, or adjacent technology. For example, JavaScript ≠ Java, SQL ≠ PostgreSQL expertise, React ≠ React Native, and AWS ≠ every AWS service.

            When evidence is ambiguous, score conservatively.

            ### ATS + RECRUITER EVALUATION

            Evaluate:

            * Required and preferred skills/keywords
            * Legitimate synonyms and terminology
            * Relevant experience and job titles
            * Responsibilities
            * Qualifications and certifications
            * Technical skills supported by evidence
            * Achievement and measurable impact
            * Relevance, clarity, and information hierarchy

            Do not reward keyword stuffing, repeated keywords, irrelevant skills, or unexplained technologies.

            A professionally formatted resume must not receive a high score when important job requirements are missing.

            ### EXISTING SKILLS

            `existingSkills` = important skills from the job description that the candidate already demonstrates in the resume.

            Only include skills supported by credible evidence and relevant to this job.

            Do not list every resume skill. Maximum 4 words per item. No sentences.

            ### SKILLS TO DEVELOP

            `skillsToDevelop` = important skills required or strongly expected for this job that the candidate does not sufficiently demonstrate.

            Only include genuine skill gaps supported by the job description.

            Do not include skills the candidate already demonstrates, minor preferences, generic career advice, or technologies that are merely related.

            Maximum 4 words per item. No sentences.

            ### SCORING

            Use integers from 0–100.

            `keywordScore` = meaningful job-specific skill/keyword alignment. Weight critical requirements much more heavily than minor keywords. A keyword supported by actual experience is stronger than one appearing only in a skills list.

            `atsScore` = likelihood of passing job-specific ATS matching based on requirements, skills, terminology, experience, qualifications, and evidence.

            `overallScore` = round((atsScore * 0.50) + (keywordScore * 0.50)).

            If the candidate lacks critical requirements, do not allow strong formatting or other strengths to produce an unrealistically high overall score.

            Score calibration:

            * 90–100: Exceptional match; nearly all critical requirements strongly supported.
            * 80–89: Strong match; most important requirements supported.
            * 70–79: Competitive but noticeable gaps exist.
            * 60–69: Moderate/weak match; several important gaps or weak evidence.
            * 40–59: Poor match; major requirements missing or weak.
            * 0–39: Very poor match.

            90+ must be rare.

            ### RESUME PROBLEM VS SKILL PROBLEM

            Distinguish these carefully.

            If the candidate appears to possess a required skill but the resume fails to communicate it, this is primarily a **resume problem**.

            If the resume provides credible evidence that the candidate lacks an important requirement, this is a **skill problem**.

            Do not tell the candidate to improve their resume when the real issue is missing skills. Do not tell them to learn skills when the resume already demonstrates them.

            ### STRENGTHS + WEAKNESSES

            Strengths must explain why the candidate is competitive for this specific job and be supported by resume evidence.

            Weaknesses must identify factors that materially reduce ATS or recruiter performance for this position.

            Avoid generic statements such as "good technical skills" or "needs improvement."

            ### GRAMMAR

            Report only genuine grammar, spelling, wording, or clarity problems found in the resume.

            For each issue, provide the relevant short `text` and a corrected `suggestion`. Do not invent issues. Return an empty array when appropriate.

            ### RECOMMENDATIONS

            Return only the highest-impact, job-specific improvements.

            Prioritize:

            1. Fixing resume problems that hide existing relevant skills.
            2. Adding truthful evidence for important skills already possessed.
            3. Strengthening weak evidence.
            4. Developing genuine critical skill gaps.
            5. Improving achievement/impact evidence.
            6. Removing irrelevant content.
            7. Adding legitimate job-specific terminology when supported by actual experience.

            Never recommend fabricating or falsely claiming skills or experience.

            ### CONSISTENCY RULES

            Keep all fields consistent.

            Do not:

            * Put an existing skill in `skillsToDevelop`.
            * Give a high `keywordScore` when critical skills are unsupported.
            * Give a high `overallScore` when major requirements are missing.
            * Recommend learning a skill already convincingly demonstrated.
            * Recommend adding a skill to the resume without evidence the candidate possesses it.
            * Inflate scores to encourage the candidate.
            * Invent experience, achievements, metrics, qualifications, or certifications.

            Use this internal sequence:

            Job requirements → importance → resume evidence → skill match → ATS alignment → recruiter competitiveness → scores → resume vs skill problem → recommendations.

            Use only the provided resume and job description.

            Return only the structured response required by the response schema. Do not output explanations, Markdown, or additional text outside the schema.

            resume:
            %s

            jobDescription:
            %s
            """;

}
