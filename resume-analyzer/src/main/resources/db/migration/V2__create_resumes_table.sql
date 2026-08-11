CREATE TABLE resumes (
    id UUID PRIMARY KEY,
    file_name VARCHAR(255),
    file_type VARCHAR(255),
    analysis_count INT NOT NULL DEFAULT 0,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_uploaded_resumes_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

