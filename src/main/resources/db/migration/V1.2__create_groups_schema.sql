-- ******************* COMMON UTILS *******************
CREATE DOMAIN currency_code AS VARCHAR(3) CHECK (value ~ '^[A-Z]{3}$');

-- ******************* GROUPS *******************
-- Represents a shares household or financial group whose members track expenses in one currency.
CREATE TABLE groups (
  id UUID PRIMARY KEY,
  name VARCHAR(64) NOT NULL CHECK (CHAR_LENGTH(BTRIM(name)) > 0),
  description description NOT NULL,
  currency_code currency_code NOT NULL,
  created_by UUID NOT NULL REFERENCES users (id), -- the user who created it. for auditing purposes.
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- the time when it was created. for auditing purposes.
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- the time when it was last updated. for auditing purposes.
  deleted_at TIMESTAMPTZ NULL -- the time when it was soft-deleted. rows are purged in batches afterwards.
);

CREATE INDEX idx_groups_deleted_at ON groups (deleted_at)
WHERE
  deleted_at IS NOT NULL;

SELECT
  trigger_updated_at ('groups');

-- Records membership of users in groups and the time each user joined.
CREATE TABLE user_groups (
  user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  group_id UUID NOT NULL REFERENCES groups (id),
  joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  PRIMARY KEY (user_id, group_id)
);

CREATE INDEX idx_user_groups_group_id ON user_groups (group_id);

-- ******************* GROUP INVITATIONS *******************
-- Stores pending, expiring invitations that allow a user to join a group.
CREATE TABLE group_invitations (
  id UUID PRIMARY KEY,
  group_id UUID NOT NULL REFERENCES groups (id),
  user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  token_hash VARCHAR(255) UNIQUE NOT NULL CHECK (CHAR_LENGTH(BTRIM(token_hash)) > 0),
  created_by UUID NOT NULL REFERENCES users (id), -- the user who created it. for auditing purposes.
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- the time when it was created. for auditing purposes.
  expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_group_invitations_group_id ON group_invitations (group_id);

CREATE INDEX idx_group_invitations_user_id ON group_invitations (user_id);

CREATE INDEX idx_group_invitations_token_hash ON group_invitations (token_hash);
