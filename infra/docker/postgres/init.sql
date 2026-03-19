-- Create separate schemas per service (logical separation in a shared DB for local dev)
-- In production each service uses its own RDS instance or schema with separate credentials.

CREATE SCHEMA IF NOT EXISTS notification;
CREATE SCHEMA IF NOT EXISTS template;
CREATE SCHEMA IF NOT EXISTS "user";

-- Flyway migrations for each service will handle table creation.
