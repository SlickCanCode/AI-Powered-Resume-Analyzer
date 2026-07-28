CREATE TABLE verification_token (
    id UUID PRIMARY KEY,
    otp_hash VARCHAR(255) NOT NULL,
<<<<<<< HEAD
    valid BOOLEAN NOT NULL DEFAULT TRUE,
    user_id UUID NOT NULL,
    verification_count INT NOT NULL DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

=======
    used BOOLEAN NOT NULL DEFAULT FALSE,
    user_id UUID NOT NULL,
    expires_at TIMESTAMP NOT NULL,
>>>>>>> 28b8af1e563f3762273402429ddfa65840e5de1a

    CONSTRAINT fk_verification_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);