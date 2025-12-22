-- Create user_balances table
CREATE TABLE user_balances (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    doner_coins NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create coin_transactions table
CREATE TABLE coin_transactions (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    order_id BIGINT,
    amount NUMERIC(10, 2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_balances(user_id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_user_balances_user_id ON user_balances(user_id);
CREATE INDEX idx_coin_transactions_user_id ON coin_transactions(user_id);
CREATE INDEX idx_coin_transactions_order_id ON coin_transactions(order_id);
CREATE INDEX idx_coin_transactions_created_at ON coin_transactions(created_at DESC);

