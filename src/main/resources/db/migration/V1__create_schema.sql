-- ******************* COMMON UTILS *******************
CREATE
    OR replace FUNCTION set_updated_at() RETURNS TRIGGER AS $$ BEGIN NEW.updated_at = now();

RETURN NEW;
END;

$$ LANGUAGE plpgsql;

CREATE
    OR replace FUNCTION trigger_updated_at(
        table_name regclass
    ) RETURNS void AS $$ BEGIN EXECUTE format(
        'CREATE TRIGGER set_updated_at
        BEFORE UPDATE
        ON %s
        FOR EACH ROW
        WHEN (OLD is distinct from NEW)
    EXECUTE FUNCTION set_updated_at();',
        table_name
    );
END;

$$ LANGUAGE plpgsql;

-- ******************* USERS *******************
CREATE
    TABLE
        users(
            id UUID PRIMARY KEY,
            username VARCHAR(128) UNIQUE NOT NULL,
            email VARCHAR(255) UNIQUE NOT NULL,
            hashed_password VARCHAR(255) NOT NULL,
            created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
            updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
            is_email_verified BOOLEAN NOT NULL,
            is_enabled BOOLEAN NOT NULL
        );

SELECT
    trigger_updated_at('users');

-- ******************* PERMISSIONS AND ROLES TABLES *******************
CREATE
    TABLE
        roles(
            id SERIAL PRIMARY KEY,
            name VARCHAR(64) UNIQUE NOT NULL,
            description VARCHAR(255) NOT NULL
        );

-- One-to-One relationship between users and roles
CREATE
    TABLE
        users_roles(
            user_id UUID NOT NULL REFERENCES users(id) ON
            DELETE
                CASCADE,
                role_id INTEGER NOT NULL REFERENCES roles(id) ON
                DELETE
                    CASCADE,
                    PRIMARY KEY(user_id)
        );

INSERT
    INTO
        roles(
            name,
            description
        )
    VALUES(
        'ADMIN',
        'Administrator with full access'
    ),
    (
        'USER',
        'Regular user with standard access'
    );

-- ******************* EMAIL VERIFICATION *******************
CREATE
    TABLE
        email_verification_tokens(
            user_id UUID PRIMARY KEY NOT NULL REFERENCES users(id) ON
            DELETE
                CASCADE,
                token VARCHAR(255) UNIQUE NOT NULL CHECK(
                    CHAR_LENGTH( BTRIM( token ))> 0
                ),
                expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
        );

CREATE
    INDEX idx_email_verification_tokens_expires_at ON
    email_verification_tokens(expires_at);

SELECT
    trigger_updated_at('email_verification_tokens');

-- ******************* PASSWORD RESET *******************
CREATE
    TABLE
        password_reset_tokens(
            user_id UUID PRIMARY KEY NOT NULL REFERENCES users(id) ON
            DELETE
                CASCADE,
                token VARCHAR(255) UNIQUE NOT NULL CHECK(
                    CHAR_LENGTH( BTRIM( token ))> 0
                ),
                expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
        );

CREATE
    INDEX idx_password_reset_tokens_expires_at ON
    password_reset_tokens(expires_at);

SELECT
    trigger_updated_at('password_reset_tokens');

-- ******************* REFRESH TOKEN *******************
CREATE
    TABLE
        refresh_tokens(
            id UUID PRIMARY KEY,
            user_id UUID NOT NULL REFERENCES users(id) ON
            DELETE
                CASCADE,
                token VARCHAR(255) UNIQUE NOT NULL CHECK(
                    CHAR_LENGTH( BTRIM( token ))> 0
                ),
                created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                family UUID NOT NULL,
                UNIQUE(
                    user_id,
                    family
                )
        );

CREATE
    INDEX idx_refresh_tokens_user_id ON
    refresh_tokens(user_id);

CREATE
    INDEX idx_refresh_tokens_expires_at ON
    refresh_tokens(expires_at);

CREATE
    INDEX idx_refresh_tokens_family ON
    refresh_tokens(family);

CREATE
    INDEX idx_refresh_tokens_token ON
    refresh_tokens(token);

SELECT
    trigger_updated_at('refresh_tokens');

-- ******************* TOKEN FAMILY BLACKLIST *******************
CREATE
    UNLOGGED TABLE
        tokenfamily_blacklist(
            family UUID PRIMARY KEY NOT NULL,
            created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
        );

-- ******************* GROUPS *******************
CREATE
    TABLE
        groups(
            id UUID PRIMARY KEY,
            name VARCHAR(64) NOT NULL CHECK(
                CHAR_LENGTH( BTRIM( name ))> 0
            ),
            description VARCHAR(255) NOT NULL,
            currency_code VARCHAR(3) NOT NULL CHECK(
                currency_code ~ '^[A-Z]{3}$'
            ),
            created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
            updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
        );

SELECT
    trigger_updated_at('groups');

CREATE
    TABLE
        user_groups(
            user_id UUID NOT NULL REFERENCES users(id) ON
            DELETE
                CASCADE,
                group_id UUID NOT NULL REFERENCES groups(id) ON
                DELETE
                    CASCADE,
                    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                    PRIMARY KEY(
                        user_id,
                        group_id
                    )
        );

-- ******************* BALANCE *******************
CREATE
    TABLE
        balances(
            id UUID PRIMARY KEY,
            group_id UUID NOT NULL REFERENCES groups(id) ON
            DELETE
                CASCADE,
                amount DECIMAL(
                    19,
                    4
                ) NOT NULL,
                currency_code VARCHAR(3) NOT NULL CHECK(
                    currency_code ~ '^[A-Z]{3}$'
                ),
                user_id_1 UUID NOT NULL REFERENCES users(id) ON
                DELETE
                    CASCADE,
                    user_id_2 UUID NOT NULL REFERENCES users(id) ON
                    DELETE
                        CASCADE,
                        CONSTRAINT chk_balances_user1_user2_not_equal CHECK(
                            user_id_1 <> user_id_2
                        )
        );

CREATE
    UNIQUE INDEX ux_balances_group_unordered_pair ON
    balances(
        group_id,
        LEAST(
            user_id_1,
            user_id_2
        ),
        GREATEST(
            user_id_1,
            user_id_2
        )
    );

-- ******************* TRANSACTIONS *******************
CREATE
    TABLE
        transactions(
            id UUID PRIMARY KEY,
            description VARCHAR(255) NOT NULL,
            group_id UUID NOT NULL REFERENCES groups(id) ON
            DELETE
                CASCADE,
                amount DECIMAL(
                    19,
                    4
                ) NOT NULL CHECK(
                    amount > 0
                ),
                currency_code VARCHAR(3) NOT NULL CHECK(
                    currency_code ~ '^[A-Z]{3}$'
                ),
                from_user_id UUID NOT NULL REFERENCES users(id) ON
                DELETE
                    CASCADE,
                    to_user_id UUID NOT NULL REFERENCES users(id) ON
                    DELETE
                        CASCADE,
                        done_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                        updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                        CONSTRAINT chk_transactions_from_to_not_equal CHECK(
                            from_user_id <> to_user_id
                        )
        );

SELECT
    trigger_updated_at('transactions');

CREATE
    INDEX idx_transactions_group_id ON
    transactions(group_id);

CREATE
    INDEX idx_transactions_from_user_id ON
    transactions(from_user_id);

CREATE
    INDEX idx_transactions_to_user_id ON
    transactions(to_user_id);

-- ******************* GROUP INVITATIONS *******************
CREATE
    TABLE
        group_invitations(
            id UUID PRIMARY KEY,
            group_id UUID NOT NULL REFERENCES groups(id) ON
            DELETE
                CASCADE,
                user_id UUID NOT NULL REFERENCES users(id) ON
                DELETE
                    CASCADE,
                    token VARCHAR(255) UNIQUE NOT NULL CHECK(
                        CHAR_LENGTH( BTRIM( token ))> 0
                    ),
                    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
        );

CREATE
    INDEX idx_group_invitations_group_id ON
    group_invitations(group_id);

CREATE
    INDEX idx_group_invitations_user_id ON
    group_invitations(user_id);

CREATE
    INDEX idx_group_invitations_token ON
    group_invitations(token);
