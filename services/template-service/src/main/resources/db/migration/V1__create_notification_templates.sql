CREATE SCHEMA IF NOT EXISTS template;

CREATE TABLE IF NOT EXISTS template.notification_templates (
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    channel    VARCHAR(50) NOT NULL,
    subject    VARCHAR(255),
    body       TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);