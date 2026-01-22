CREATE TABLE uploaded_resumes (
    id UUID PRIMARY KEY,
    file_name VARCHAR(255),
    file_type VARCHAR(255),
    source_url TEXT,
    file_content TEXT NOT NULL,
    analysis TEXT,
    user_id UUID,

    CONSTRAINT fk_uploaded_resumes_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

