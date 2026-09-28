CREATE TYPE idempotency_state AS ENUM('IN_PROGRESS', 'COMPLETED');

CREATE UNLOGGED TABLE idempotency_keys (
  user_id UUID NOT NULL REFERENCES users (id),
  key UUID NOT NULL,
  request_hash bytea NOT NULL,
  state idempotency_state NOT NULL DEFAULT 'IN_PROGRESS',
  response_body JSONB NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMPTZ NOT NULL,
  CHECK (
    state = 'COMPLETED'
    OR response_body IS NULL
  ),
  PRIMARY KEY (user_id, key)
);

CREATE INDEX idx_idempotency_keys_expires_at ON idempotency_keys (expires_at);
