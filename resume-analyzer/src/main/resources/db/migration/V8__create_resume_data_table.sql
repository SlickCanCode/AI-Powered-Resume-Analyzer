CREATE TABLE resume_data (
    id UUID PRIMARY KEY,
    resume_id UUID NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    location VARCHAR(255) NOT NULL,
    summary TEXT,
    online_profiles JSONB,
    skills JSONB,
    experience JSONB,
    education JSONB,

    CONSTRAINT fk_resume_data_resume
        FOREIGN KEY (resume_id)
        REFERENCES resumes(id)
        ON DELETE CASCADE
);