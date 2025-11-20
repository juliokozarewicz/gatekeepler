-- SCHEMA
CREATE SCHEMA IF NOT EXISTS modules;

-- TABLE: modules
CREATE TABLE IF NOT EXISTS modules.modules (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- TABLE: modules_mutually_exclusive
CREATE TABLE IF NOT EXISTS modules.modules_mutually_exclusive (
    id UUID PRIMARY KEY,
    module_a_name VARCHAR(255) NOT NULL,
    module_b_name VARCHAR(255) NOT NULL,
    CONSTRAINT uq_exclusive UNIQUE (module_a_name, module_b_name)
);

-- TABLE: modules_allowed_departments
CREATE TABLE IF NOT EXISTS modules.modules_allowed_departments (
    id UUID PRIMARY KEY,
    module_name VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    CONSTRAINT uq_allowed UNIQUE (module_name, department)
);

-- TABLE: module_requests
CREATE TABLE IF NOT EXISTS modules.module_requests (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    protocol_number VARCHAR(255) NOT NULL UNIQUE,
    justification VARCHAR(500) NOT NULL,
    urgent BOOLEAN NOT NULL,
    status VARCHAR(50) NOT NULL,
    denial_reason VARCHAR(500),
    id_user VARCHAR(255) NOT NULL
);

-- AUX TABLE: module_names
CREATE TABLE IF NOT EXISTS modules.module_names_requested (
    module_request_id UUID REFERENCES modules.module_requests(id) ON DELETE CASCADE,
    module_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (module_request_id, module_name)
);

-- ============================================================
INSERT INTO modules.modules (id, created_at, updated_at, name, description, active) VALUES
    (gen_random_uuid(), NOW(), NOW(), 'Portal do Colaborador', 'Acesso básico ao sistema', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'Relatórios Gerenciais', 'Dashboards e relatórios', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'Gestão Financeira', 'Funções gerais financeiras', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'Aprovador Financeiro', 'Aprova solicitações financeiras', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'Solicitante Financeiro', 'Cria solicitações financeiras', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'Administrador RH', 'Acesso administrativo do RH', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'Colaborador RH', 'Acesso operacional do RH', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'Gestão de Estoque', 'Controle de estoque', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'Compras', 'Processos de compras', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'Auditoria', 'Acesso auditoria interna', TRUE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO modules.modules_mutually_exclusive (id, module_a_name, module_b_name) VALUES
    (gen_random_uuid(), 'Aprovador Financeiro', 'Solicitante Financeiro'),
    (gen_random_uuid(), 'Solicitante Financeiro', 'Aprovador Financeiro'),
    (gen_random_uuid(), 'Administrador RH', 'Colaborador RH'),
    (gen_random_uuid(), 'Colaborador RH', 'Administrador RH')
ON CONFLICT (module_a_name, module_b_name) DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'Portal do Colaborador', 'TI'),
    (gen_random_uuid(), 'Portal do Colaborador', 'Financeiro'),
    (gen_random_uuid(), 'Portal do Colaborador', 'RH'),
    (gen_random_uuid(), 'Portal do Colaborador', 'Operações'),
    (gen_random_uuid(), 'Portal do Colaborador', 'Outros')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'Relatórios Gerenciais', 'TI'),
    (gen_random_uuid(), 'Relatórios Gerenciais', 'Financeiro'),
    (gen_random_uuid(), 'Relatórios Gerenciais', 'RH'),
    (gen_random_uuid(), 'Relatórios Gerenciais', 'Operações'),
    (gen_random_uuid(), 'Relatórios Gerenciais', 'Outros')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'Gestão Financeira', 'TI'),
    (gen_random_uuid(), 'Gestão Financeira', 'Financeiro')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'Aprovador Financeiro', 'TI'),
    (gen_random_uuid(), 'Aprovador Financeiro', 'Financeiro')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'Solicitante Financeiro', 'TI'),
    (gen_random_uuid(), 'Solicitante Financeiro', 'Financeiro')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'Administrador RH', 'TI'),
    (gen_random_uuid(), 'Administrador RH', 'RH')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'Colaborador RH', 'TI'),
    (gen_random_uuid(), 'Colaborador RH', 'RH')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'Gestão de Estoque', 'TI'),
    (gen_random_uuid(), 'Gestão de Estoque', 'Operações')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'Compras', 'TI'),
    (gen_random_uuid(), 'Compras', 'Operações')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'Auditoria', 'TI')
ON CONFLICT DO NOTHING;
-- ============================================================