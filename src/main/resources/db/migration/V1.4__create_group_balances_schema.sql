-- ******************* GROUP BALANCES *******************
CREATE TABLE group_balances (
  group_id UUID NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
  amount money_amount NOT NULL DEFAULT 0, -- currency is defined by the group
  user_id_1 UUID NOT NULL REFERENCES users (id),
  user_id_2 UUID NOT NULL REFERENCES users (id),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CHECK (user_id_1 <> user_id_2),
  PRIMARY KEY (group_id, user_id_1, user_id_2)
);

SELECT
  trigger_updated_at ('group_balances');

CREATE INDEX idx_group_balances_group_id ON group_balances (group_id);
