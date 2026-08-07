CREATE TABLE resume (
    id UUID PRIMARY KEY,
    file_name VARCHAR(255),
    file_type VARCHAR(255),
    source_url TEXT,
    file_content TEXT NOT NULL,
    analysis_count INT NOT NULL DEFAULT 0,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_uploaded_resumes_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

