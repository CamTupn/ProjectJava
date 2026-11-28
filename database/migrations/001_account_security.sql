ALTER TABLE users
ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE AFTER password_hash,
ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0 AFTER must_change_password;