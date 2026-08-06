CREATE TABLE resume_analysis (
    id UUID PRIMARY KEY,
    resume_id UUID NOT NULL,
    score INT NOT NULL,
    ats_score INT NOT NULL,
    strengths JSONB NOT NULL,
    weaknesses JSONB NOT NULL,
    improvement_suggestions JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_resume_analysis_resume
        FOREIGN KEY (resume_id)
        REFERENCES resumes(id)
        ON DELETE CASCADE
);