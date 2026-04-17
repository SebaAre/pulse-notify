CREATE SCHEMA IF NOT EXISTS user_svc;

CREATE TABLE IF NOT EXISTS user_svc.users (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    phone VARCHAR(30) UNIQUE,
    push_token VARCHAR(255),
    display_name VARCHAR(255) NOT NULL,
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS user_svc.user_preferences (

    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_svc.users(id) ON DELETE CASCADE,
    channel VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (user_id, channel)
);

CREATE INDEX idx_user_preferences_user_id ON user_svc.user_preferences(user_id);