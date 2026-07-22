CREATE TABLE verification_token (
    id UUID PRIMARY KEY,
    otp_hash VARCHAR(255) NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    user_id UUID NOT NULL,
    expires_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_verification_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);