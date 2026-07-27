CREATE TABLE verification_token (
    id UUID PRIMARY KEY,
    otp_hash VARCHAR(255) NOT NULL,
    valid BOOLEAN NOT NULL DEFAULT TRUE,
    user_id UUID NOT NULL,
    verification_count INT NOT NULL DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_verification_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);