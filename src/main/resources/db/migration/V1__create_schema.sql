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
    id              UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
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

-- ******************* CARD TYPES *******************

CREATE TABLE card_types
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL
);

INSERT INTO card_types (name)
VALUES ('ticketrestaurant'),
       ('pluxee');

CREATE TABLE user_card_types
(
    user_id      UUID    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    card_type_id INTEGER NOT NULL REFERENCES card_types (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, card_type_id)
);

-- ******************* PERMISSIONS AND ROLES TABLES *******************

CREATE TABLE permissions
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(64) UNIQUE NOT NULL,
    description VARCHAR(255)       NOT NULL
);

CREATE TABLE users_permissions
(
    user_id       UUID    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    permission_id INTEGER NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, permission_id)
);

INSERT INTO permissions (name, description)
VALUES ('manage_users', 'Ability to manage user accounts'),
       ('edit_shop', 'Ability to add or modify shop details'),
       ('manage_votes', 'Ability to manage votes'),
       ('view_shop', 'Ability to view shop information'),
       ('vote_shop', 'Ability to vote on shops');

CREATE TABLE roles
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(64) UNIQUE NOT NULL,
    description VARCHAR(255)       NOT NULL
);

CREATE TABLE roles_permissions
(
    role_id       INTEGER NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id INTEGER NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

INSERT INTO roles(name, description)
VALUES ('admin', 'Administrator with full access'),
       ('user', 'Regular user with standard access');

INSERT INTO roles_permissions (role_id, permission_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (1, 5),
       (2, 4),
       (2, 5);

-- ******************* EMAIL VERIFICATION *******************

CREATE TABLE email_verification_tokens
(
    id         UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
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
    id         UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
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
    id         UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
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

-- ******************* SHOPS *******************

CREATE TABLE shops
(
    id              UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    name            VARCHAR(255)             NOT NULL,
    google_place_id VARCHAR(255) UNIQUE      NOT NULL,
    location        POINT                    NOT NULL,
    address         VARCHAR(255)             NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

SELECT trigger_updated_at('shops');

CREATE TABLE shop_types
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE shop_shop_types
(
    shop_id      UUID    NOT NULL REFERENCES shops (id) ON DELETE CASCADE,
    shop_type_id INTEGER NOT NULL REFERENCES shop_types (id) ON DELETE CASCADE,
    PRIMARY KEY (shop_id, shop_type_id)
);

-- ******************* VOTES *******************

CREATE TABLE votes
(
    id           UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    user_id      UUID                     NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    shop_id      UUID                     NOT NULL REFERENCES shops (id) ON DELETE CASCADE,
    card_type_id INTEGER                  NOT NULL REFERENCES card_types (id) ON DELETE CASCADE,
    can_use      BOOL                     NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, shop_id, card_type_id)
);

CREATE INDEX idx_user_id ON votes (user_id);
CREATE INDEX idx_shop_id ON votes (shop_id);
CREATE INDEX idx_card_type ON votes (card_type_id);

SELECT trigger_updated_at('votes');
