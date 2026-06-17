CREATE TABLE IF NOT EXISTS api_keys (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(255),
    hash              VARCHAR(255),
    created_at        TIMESTAMP                   NOT NULL,
    user_id           BIGINT REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT users_api_keys_unique UNIQUE (name, user_id)
);

CREATE INDEX hash_api_keys_idx
    ON api_keys (hash);