CREATE TABLE idempotency_keys (
    idempotency_key     VARCHAR(36) PRIMARY KEY,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    endpoint            VARCHAR(255) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_idempotency_keys_expires_at ON idempotency_keys(expires_at);
