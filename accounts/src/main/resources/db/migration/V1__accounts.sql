-- CREATE SCHEMA
CREATE SCHEMA IF NOT EXISTS accounts;

-- ACCOUNTS
CREATE TABLE IF NOT EXISTS accounts.user_account (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    email VARCHAR(256) NOT NULL UNIQUE,
    password VARCHAR(256) NOT NULL,
    level VARCHAR(256) NOT NULL DEFAULT 'user',
    active BOOLEAN NOT NULL DEFAULT FALSE,
    banned BOOLEAN NOT NULL DEFAULT FALSE
);

-- PROFILE
CREATE TABLE IF NOT EXISTS accounts.user_profile (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    name VARCHAR(256),
    phone VARCHAR(25),
    identity_document VARCHAR(256),
    gender VARCHAR(256),
    birthdate VARCHAR(50),
    biography VARCHAR(256),
    profile_image VARCHAR(555),
    language VARCHAR(50),
    theme VARCHAR(100)
);

-- USER LOGS
CREATE TABLE IF NOT EXISTS accounts.user_log (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    ip_address VARCHAR(256) NOT NULL,
    id_user UUID NOT NULL,
    agent VARCHAR(512) NOT NULL,
    update_type VARCHAR(256) NOT NULL,
    old_value TEXT NOT NULL,
    new_value TEXT NOT NULL
);