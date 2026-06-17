CREATE TABLE IF NOT EXISTS analytics_data
(
    id              BIGSERIAL primary key,
    type            TEXT                    NOT NULL,
    created_at      TIMESTAMP DEFAULT now() NOT NULL,
    metadata        JSONB
);
