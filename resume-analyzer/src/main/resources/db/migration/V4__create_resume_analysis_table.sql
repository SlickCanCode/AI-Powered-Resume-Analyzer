CREATE TABLE resume_analysis (
    id UUID PRIMARY KEY,
    resume_id UUID NOT NULL,
    overall_score INT NOT NULL,
    ats_score INT NOT NULL,
    keyword_score INT NOT NULL,
    strengths JSONB NOT NULL,
    weaknesses JSONB NOT NULL,
    existing_skills JSONB NOT NULL,
    skills_to_develop JSONB NOT NULL,
    grammar_issues JSONB NOT NULL,
    recommendations JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_resume_analysis_resume
        FOREIGN KEY (resume_id)
        REFERENCES resumes(id)
        ON DELETE CASCADE
);
-- Lookup analyses for a resume
CREATE INDEX idx_resume_analysis_resume_id
    ON resume_analysis (resume_id);

-- Get recent analyses
CREATE INDEX idx_resume_analysis_created_at
    ON resume_analysis (created_at);

-- Get analyses for a resume ordered by newest first
CREATE INDEX idx_resume_analysis_resume_created_at
    ON resume_analysis (resume_id, created_at DESC);