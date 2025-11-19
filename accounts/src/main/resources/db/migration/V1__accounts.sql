-- CREATE SCHEMA
CREATE SCHEMA IF NOT EXISTS accounts;

-- ACCOUNTS
CREATE TABLE IF NOT EXISTS accounts.user_account (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    email VARCHAR(256) NOT NULL UNIQUE,
    password VARCHAR(256) NOT NULL,
    department VARCHAR(256) NOT NULL,
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

-- ========================================
INSERT INTO accounts.user_account (id, created_at, updated_at, email, password, department, level, active, banned)
VALUES
('019a9dab-0a5a-7973-ba74-18c3b82c0ad6', NOW(), NOW(), 'ti@email.com', '$argon2id$v=19$m=131072,t=6,p=2$H14VuZZS6DUe6jnWyu4gaA$la5tUz1SvsQHdpmDl9GqBMl/abZ8qZfMiDNIjDRO6Mc', 'ti', 'user', TRUE, FALSE),
('019a9dab-3cff-7023-8a27-82bfc0c3212a', NOW(), NOW(), 'financeiro@email.com', '$argon2id$v=19$m=131072,t=6,p=2$H14VuZZS6DUe6jnWyu4gaA$la5tUz1SvsQHdpmDl9GqBMl/abZ8qZfMiDNIjDRO6Mc', 'financeiro', 'user', TRUE, FALSE),
('019a9dab-6763-7bf8-9c74-920cc2b8c34e', NOW(), NOW(), 'rh@email.com', '$argon2id$v=19$m=131072,t=6,p=2$H14VuZZS6DUe6jnWyu4gaA$la5tUz1SvsQHdpmDl9GqBMl/abZ8qZfMiDNIjDRO6Mc', 'rh', 'user', TRUE, FALSE),
('019a9dab-d804-7046-a6be-8a2e73780a9c', NOW(), NOW(), 'operacoes@email.com', '$argon2id$v=19$m=131072,t=6,p=2$H14VuZZS6DUe6jnWyu4gaA$la5tUz1SvsQHdpmDl9GqBMl/abZ8qZfMiDNIjDRO6Mc', 'operações', 'user', TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO accounts.user_profile (id, created_at, updated_at, name, phone, identity_document, gender, birthdate, biography, profile_image, language, theme)
VALUES
('019a9dab-0a5a-7973-ba74-18c3b82c0ad6', NOW(), NOW(), 'TI User', '+5511999999991', '12345678901', 'Masculino', '1990-01-01', 'Administrador de TI', NULL, 'pt-BR', 'light'),
('019a9dab-3cff-7023-8a27-82bfc0c3212a', NOW(), NOW(), 'Financeiro User', '+5511999999992', '23456789012', 'Feminino', '1991-02-02', 'Responsável Financeiro', NULL, 'pt-BR', 'light'),
('019a9dab-6763-7bf8-9c74-920cc2b8c34e', NOW(), NOW(), 'RH User', '+5511999999993', '34567890123', 'Masculino', '1992-03-03', 'Recursos Humanos', NULL, 'pt-BR', 'light'),
('019a9dab-d804-7046-a6be-8a2e73780a9c', NOW(), NOW(), 'Operações User', '+5511999999994', '45678901234', 'Feminino', '1993-04-04', 'Operações', NULL, 'pt-BR', 'light')
ON CONFLICT (id) DO NOTHING;
-- ========================================