-- ******************* COMMON UTILS *******************
CREATE DOMAIN money_amount AS DECIMAL(19, 3);

CREATE DOMAIN positive_money_amount AS DECIMAL(19, 3) CHECK (value > 0);

CREATE DOMAIN non_negative_money_amount AS DECIMAL(19, 3) CHECK (value >= 0);

-- ******************* EXPENSES *******************
-- Stores an expense recorded for a group, including its total amount, currency, and completion time.
CREATE TABLE expenses (
  id UUID PRIMARY KEY,
  group_id UUID NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
  description description NOT NULL,
  currency_code currency_code NOT NULL,
  done_at TIMESTAMPTZ NOT NULL,
  created_by UUID NOT NULL REFERENCES users (id), -- the user who created the expense. for auditing purposes.
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- the time when the expense was created. for auditing purposes.
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW() -- the time when the expense was last updated. for auditing purposes.
);

CREATE INDEX idx_expenses_group_id_done_at ON expenses (group_id, done_at DESC);

CREATE INDEX idx_expenses_created_by ON expenses (created_by);

SELECT
  trigger_updated_at ('expenses');

-- Records how much each user paid toward an expense for settlement calculations.
CREATE TABLE expense_payments (
  expense_id UUID NOT NULL REFERENCES expenses (id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users (id),
  amount positive_money_amount NOT NULL, -- Currency is defined in expenses table, so this amount is in the same currency as the expense.
  PRIMARY KEY (expense_id, user_id)
);

CREATE INDEX idx_expense_payments_expense_id ON expense_payments (expense_id);

CREATE INDEX idx_expense_payments_user_id ON expense_payments (user_id);

-- Records each user's share of an expense so the amount owed can be calculated.
CREATE TABLE expense_shares (
  expense_id UUID NOT NULL REFERENCES expenses (id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users (id),
  amount positive_money_amount NOT NULL, -- Currency is defined in expenses table, so this amount is in the same currency as the expense.
  PRIMARY KEY (expense_id, user_id)
);

CREATE INDEX idx_expense_shares_expense_id ON expense_shares (expense_id);

CREATE INDEX idx_expense_shares_user_id ON expense_shares (user_id);

-- ******************* PAYMENTS *******************
-- Stores a completed payment between group members, separate from the expenses being settled.
CREATE TABLE payments (
  id UUID PRIMARY KEY,
  group_id UUID NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
  description description NOT NULL,
  amount positive_money_amount NOT NULL,
  currency_code currency_code NOT NULL,
  creditor UUID NOT NULL REFERENCES users (id),
  debitor UUID NOT NULL REFERENCES users (id),
  done_at TIMESTAMPTZ NOT NULL,
  created_by UUID NOT NULL REFERENCES users (id), -- the user who created the payment. for auditing purposes.
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- the time when the payment was created. for auditing purposes.
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- the time when the payment was last updated. for auditing purposes.
  CHECK (creditor <> debitor)
);

CREATE INDEX idx_payments_group_id_done_at ON payments (group_id, done_at DESC);

CREATE INDEX idx_payments_creditor ON payments (creditor);

CREATE INDEX idx_payments_debitor ON payments (debitor);

CREATE INDEX idx_payments_created_by ON payments (created_by);

SELECT
  trigger_updated_at ('payments');
