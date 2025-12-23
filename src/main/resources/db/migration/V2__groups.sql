-- ******************* GROUPS *******************

CREATE TABLE groups
(
    id            UUID PRIMARY KEY                  DEFAULT uuidv7(),
    name          VARCHAR(64)              NOT NULL,
    description   VARCHAR(255)             NOT NULL,
    currency_code VARCHAR(3)               NOT NULL CHECK (currency_code ~ '^[A-Z]{3}$'),
    created_by    UUID                     REFERENCES users (id) ON DELETE SET NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

SELECT trigger_updated_at('groups');

CREATE TABLE user_groups
(
    user_id   UUID                     NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    group_id  UUID                     NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, group_id)
);

-- ******************* BALANCE *******************

CREATE TABLE balances
(
    id            UUID PRIMARY KEY        DEFAULT uuidv7(),
    group_id      UUID           NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    amount        DECIMAL(19, 4) NOT NULL DEFAULT 0,
    currency_code VARCHAR(3)     NOT NULL CHECK (currency_code ~ '^[A-Z]{3}$'),
    user_id_1     UUID           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    user_id_2     UUID           NOT NULL REFERENCES users (id) ON DELETE CASCADE,

    CONSTRAINT chk_balances_lender_borrower_not_equal CHECK (user_id_1 <> user_id_2)
);

CREATE UNIQUE INDEX ux_balances_group_unordered_pair
    ON balances (
                 group_id,
                 LEAST(user_id_1, user_id_2),
                 GREATEST(user_id_1, user_id_2)
        );

-- ******************* TRANSACTIONS *******************

CREATE TABLE transactions
(
    id            UUID PRIMARY KEY                  DEFAULT uuidv7(),
    description   VARCHAR(255)             NOT NULL,
    group_id      UUID                     NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    amount        DECIMAL(19, 4)           NOT NULL CHECK (amount > 0),
    currency_code VARCHAR(3)               NOT NULL CHECK (currency_code ~ '^[A-Z]{3}$'),
    lender        UUID                     NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    borrower      UUID                     NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

SELECT trigger_updated_at('transactions');

CREATE INDEX idx_transactions_group_id ON transactions (group_id);
CREATE INDEX idx_transactions_emitter_user_id ON transactions (lender);
CREATE INDEX idx_transactions_receiver_user_id ON transactions (borrower);
