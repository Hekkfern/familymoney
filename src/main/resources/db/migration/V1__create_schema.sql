-- ******************* COMMON UTILS *******************

create
    or replace function set_updated_at()
    returns trigger as
$$
begin
    NEW.updated_at
        = now();
    return NEW;
end;
$$ language plpgsql;

create
    or replace function trigger_updated_at(table_name regclass)
    returns void as
$$
begin
    execute format('CREATE TRIGGER set_updated_at
        BEFORE UPDATE
        ON %s
        FOR EACH ROW
        WHEN (OLD is distinct from NEW)
    EXECUTE FUNCTION set_updated_at();', table_name);
end;
$$ language plpgsql;

-- ******************* USERS *******************

CREATE TABLE users
(
    id              UUID PRIMARY KEY                  DEFAULT uuidv7(),
    username        VARCHAR(128) UNIQUE      NOT NULL,
    email           VARCHAR(255) UNIQUE      NOT NULL,
    hashed_password VARCHAR(255)             NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    email_verified  BOOLEAN                  NOT NULL DEFAULT FALSE,
    is_enabled      BOOLEAN                  NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_users_created_at ON users (created_at);

SELECT trigger_updated_at('users');

-- ******************* PERMISSIONS AND ROLES TABLES *******************

CREATE TABLE roles
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(64) UNIQUE NOT NULL,
    description VARCHAR(255)       NOT NULL
);

-- One-to-One relationship between users and roles
CREATE TABLE users_roles
(
    user_id UUID    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id)
);

INSERT INTO roles(name, description)
VALUES ('admin', 'Administrator with full access'),
       ('user', 'Regular user with standard access');

-- ******************* EMAIL VERIFICATION *******************

CREATE TABLE email_verification_tokens
(
    id         UUID PRIMARY KEY                  DEFAULT uuidv7(),
    user_id    UUID                     NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(255) UNIQUE      NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_email_verification_tokens_user_id ON email_verification_tokens (user_id);
CREATE INDEX idx_email_verification_tokens_expires_at ON email_verification_tokens (expires_at);

-- ******************* PASSWORD RESET *******************

CREATE TABLE password_reset_tokens
(
    id         UUID PRIMARY KEY                  DEFAULT uuidv7(),
    user_id    UUID                     NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(255) UNIQUE      NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens (user_id);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens (expires_at);

-- ******************* REFRESH TOKEN *******************

CREATE TABLE refresh_tokens
(
    id         UUID PRIMARY KEY                  DEFAULT uuidv7(),
    user_id    UUID                     NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(255) UNIQUE      NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() + INTERVAL '7 days'),
    is_used    BOOLEAN                  NOT NULL DEFAULT FALSE,
    used_at    TIMESTAMP WITH TIME ZONE,
    family     UUID                     NOT NULL
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
CREATE INDEX idx_refresh_tokens_is_used ON refresh_tokens (is_used);
CREATE INDEX idx_refresh_tokens_family ON refresh_tokens (family);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
