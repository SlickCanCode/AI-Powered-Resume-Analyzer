CREATE TABLE subscription_usage (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    resume_analyses_used INTEGER NOT NULL DEFAULT 0,
    resumes_analyses_allowed INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_usage_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_usage_user
        UNIQUE (user_id)
);

CREATE INDEX idx_usage_user
    ON subscription_usage(user_id);
