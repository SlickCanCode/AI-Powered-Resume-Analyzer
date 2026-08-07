CREATE TYPE subscription_plan AS ENUM (
    'FREE',
    'PRO'
);

CREATE TYPE subscription_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'CANCELED',
    'EXPIRED'
);

CREATE TYPE payment_provider AS ENUM (
    'NONE',
    'PAYSTACK',
    'STRIPE'
);