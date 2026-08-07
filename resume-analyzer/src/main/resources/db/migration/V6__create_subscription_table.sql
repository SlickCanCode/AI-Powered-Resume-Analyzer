CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL UNIQUE,

    plan subscription_plan NOT NULL DEFAULT 'FREE',
    status subscription_status NOT NULL DEFAULT 'ACTIVE',

    payment_provider payment_provider NOT NULL DEFAULT 'NONE',
    provider_subscription_id VARCHAR(255),

    current_period_start TIMESTAMP,
    current_period_end TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_subscription_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_subscription_user
    ON subscriptions(user_id);

CREATE INDEX idx_subscription_status
    ON subscriptions(status);
