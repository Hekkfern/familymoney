-- ******************* GROUP BALANCES *******************
CREATE TABLE group_balances (
  id UUID PRIMARY KEY,
  group_id UUID NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
  amount money_amount NOT NULL,
  currency_code currency_code NOT NULL,
  user_id_1 UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  user_id_2 UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  CHECK (user_id_1 <> user_id_2)
);

CREATE UNIQUE INDEX ux_group_balances_group_unordered_pair ON group_balances (
  group_id,
  LEAST(user_id_1, user_id_2),
  GREATEST(user_id_1, user_id_2)
);

CREATE INDEX idx_group_balances_group_id ON group_balances (group_id);

CREATE INDEX idx_group_balances_user_id_1 ON group_balances (user_id_1);

CREATE INDEX idx_group_balances_user_id_2 ON group_balances (user_id_2);
