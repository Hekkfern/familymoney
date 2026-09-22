-- ******************* COMMON UTILS *******************
CREATE DOMAIN currency_code AS VARCHAR(3) CHECK (value ~ '^[A-Z]{3}$');

-- ******************* GROUPS *******************
-- Represents a shares household or financial group whose members track expenses in one currency.
CREATE TABLE groups (
  id UUID PRIMARY KEY,
  name VARCHAR(64) NOT NULL CHECK (CHAR_LENGTH(BTRIM(name)) > 0),
  description description NOT NULL,
  currency_code currency_code NOT NULL,
  created_by UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

SELECT
  trigger_updated_at ('groups');

-- Records membership of users in groups and the time each user joined.
CREATE TABLE user_groups (
  user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  group_id UUID NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
  joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  PRIMARY KEY (user_id, group_id)
);

-- ******************* GROUP INVITATIONS *******************
-- Stores pending, expiring invitations that allow a user to join a group.
CREATE TABLE group_invitations (
  id UUID PRIMARY KEY,
  group_id UUID NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  token_hash VARCHAR(255) UNIQUE NOT NULL CHECK (CHAR_LENGTH(BTRIM(token_hash)) > 0),
  created_by UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_group_invitations_group_id ON group_invitations (group_id);

CREATE INDEX idx_group_invitations_user_id ON group_invitations (user_id);

CREATE INDEX idx_group_invitations_token_hash ON group_invitations (token_hash);
