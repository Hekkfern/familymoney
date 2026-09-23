CREATE UNLOGGED TABLE idempotency_keys (
  user_id UUID NOT NULL REFERENCES users (id),
  key UUID NOT NULL,
  request_hash bytea NOT NULL,
  response_status INTEGER NULL,
  response_body JSONB NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMPTZ NOT NULL,
  CHECK (
    (
      response_status IS NULL
      AND response_body IS NULL
    )
    OR (
      response_status IS NOT NULL
      AND response_body IS NOT NULL
    )
  ),
  PRIMARY KEY (user_id, key)
);

CREATE INDEX idx_idempotency_keys_expires_at ON idempotency_keys (expires_at);
