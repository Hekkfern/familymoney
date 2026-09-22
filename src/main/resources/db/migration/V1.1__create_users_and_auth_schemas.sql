-- ******************* COMMON UTILS *******************
CREATE DOMAIN description AS VARCHAR(255);

CREATE OR REPLACE FUNCTION set_updated_at () returns trigger AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;

$$ language plpgsql;

CREATE OR REPLACE FUNCTION trigger_updated_at (table_name regclass) returns void AS $$
BEGIN
    EXECUTE format(
        'CREATE TRIGGER set_updated_at
        BEFORE UPDATE
        ON %s
        FOR EACH ROW
        WHEN (OLD is distinct from NEW)
        EXECUTE FUNCTION set_updated_at();',
        table_name
            );
END;

$$ language plpgsql;

-- ******************* USERS *******************
-- Stores each application account and the authentication state needed to access the service.
CREATE TABLE users (
  id UUID PRIMARY KEY,
  username VARCHAR(128) UNIQUE NOT NULL,
  email VARCHAR(254) UNIQUE NOT NULL CHECK (
    CHAR_LENGTH(BTRIM(email)) > 0
    AND email = LOWER(email)
  ),
  hashed_password VARCHAR(255) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_email_verified BOOLEAN NOT NULL,
  is_enabled BOOLEAN NOT NULL
);

SELECT
  trigger_updated_at ('users');

-- ******************* PERMISSIONS AND ROLES TABLES *******************
-- Defines the roles that control the level of access granted to application users.
CREATE TABLE roles (
  id serial PRIMARY KEY,
  name VARCHAR(64) UNIQUE NOT NULL,
  description description NOT NULL
);

-- One-to-One relationship between users and roles
-- Assigns one role to each user, linking their account to its authorization permissions.
CREATE TABLE users_roles (
  user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  role_id INTEGER NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
  PRIMARY KEY (user_id)
);

INSERT INTO
  roles (name, description)
VALUES
  ('ADMIN', 'Administrator with full access'),
  ('USER', 'Regular user with standard access');

-- ******************* EMAIL VERIFICATION *******************
-- Holds the current hashed token for confirming a user's email address before it expires.
CREATE TABLE email_verification_tokens (
  user_id UUID PRIMARY KEY NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  token_hash VARCHAR(255) UNIQUE NOT NULL CHECK (CHAR_LENGTH(BTRIM(token_hash)) > 0),
  expires_at TIMESTAMPTZ NOT NULL,
  last_sent_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_email_verification_tokens_expires_at ON email_verification_tokens (expires_at);

SELECT
  trigger_updated_at ('email_verification_tokens');

-- ******************* PASSWORD RESET *******************
-- Holds the current hashed token used to authorize a password-reset request for a user.
CREATE TABLE password_reset_tokens (
  user_id UUID PRIMARY KEY NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  token_hash VARCHAR(255) UNIQUE NOT NULL CHECK (CHAR_LENGTH(BTRIM(token_hash)) > 0),
  expires_at TIMESTAMPTZ NOT NULL,
  last_sent_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens (expires_at);

SELECT
  trigger_updated_at ('password_reset_tokens');

-- ******************* REFRESH TOKEN *******************
-- Stores active hashed refresh tokens so authenticated sessions can obtain new access tokens.
CREATE TABLE refresh_tokens (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  token_hash VARCHAR(255) UNIQUE NOT NULL CHECK (CHAR_LENGTH(BTRIM(token_hash)) > 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMPTZ NOT NULL,
  family UUID NOT NULL,
  UNIQUE (user_id, family)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);

CREATE INDEX idx_refresh_tokens_family ON refresh_tokens (family);

CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens (token_hash);

SELECT
  trigger_updated_at ('refresh_tokens');

-- ******************* USED REFRESH TOKEN *******************
-- Records consumed refresh tokens to detect token reuse and protect against replay attacks.
CREATE UNLOGGED TABLE used_refresh_tokens (
  token_hash VARCHAR(255) PRIMARY KEY NOT NULL CHECK (CHAR_LENGTH(BTRIM(token_hash)) > 0),
  family UUID NOT NULL,
  used_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_used_refresh_tokens_family ON used_refresh_tokens (family);

CREATE INDEX idx_used_refresh_tokens_used_at ON used_refresh_tokens (used_at);

-- ******************* TOKEN FAMILY BLACKLIST *******************
-- Lists revoked refresh-token families so every token in a compromised or logged-out session is rejected.
CREATE UNLOGGED TABLE tokenfamily_blacklist (
  family UUID PRIMARY KEY NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
