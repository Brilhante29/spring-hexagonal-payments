CREATE TABLE payments (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    amount_minor BIGINT NOT NULL CHECK (amount_minor > 0),
    currency CHAR(3) NOT NULL,
    merchant_reference VARCHAR(64) NOT NULL,
    status VARCHAR(24) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    captured_at TIMESTAMPTZ NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX payments_merchant_reference_idx ON payments (merchant_reference);
