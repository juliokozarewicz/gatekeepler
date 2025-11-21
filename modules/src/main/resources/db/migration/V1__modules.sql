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
    (gen_random_uuid(), NOW(), NOW(), 'portal do colaborador', 'acesso básico ao sistema', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'relatórios gerenciais', 'dashboards e relatórios', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'gestão financeira', 'funções gerais financeiras', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'aprovador financeiro', 'aprova solicitações financeiras', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'solicitante financeiro', 'cria solicitações financeiras', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'administrador rh', 'acesso administrativo do rh', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'colaborador rh', 'acesso operacional do rh', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'gestão de estoque', 'controle de estoque', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'compras', 'processos de compras', TRUE),
    (gen_random_uuid(), NOW(), NOW(), 'auditoria', 'acesso auditoria interna', TRUE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO modules.modules_mutually_exclusive (id, module_a_name, module_b_name) VALUES
    (gen_random_uuid(), 'aprovador financeiro', 'solicitante financeiro'),
    (gen_random_uuid(), 'solicitante financeiro', 'aprovador financeiro'),
    (gen_random_uuid(), 'administrador rh', 'colaborador rh'),
    (gen_random_uuid(), 'colaborador rh', 'administrador rh')
ON CONFLICT (module_a_name, module_b_name) DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'portal do colaborador', 'ti'),
    (gen_random_uuid(), 'portal do colaborador', 'financeiro'),
    (gen_random_uuid(), 'portal do colaborador', 'rh'),
    (gen_random_uuid(), 'portal do colaborador', 'operações'),
    (gen_random_uuid(), 'portal do colaborador', 'outros')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'relatórios gerenciais', 'ti'),
    (gen_random_uuid(), 'relatórios gerenciais', 'financeiro'),
    (gen_random_uuid(), 'relatórios gerenciais', 'rh'),
    (gen_random_uuid(), 'relatórios gerenciais', 'operações'),
    (gen_random_uuid(), 'relatórios gerenciais', 'outros')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'gestão financeira', 'ti'),
    (gen_random_uuid(), 'gestão financeira', 'financeiro')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'aprovador financeiro', 'ti'),
    (gen_random_uuid(), 'aprovador financeiro', 'financeiro')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'solicitante financeiro', 'ti'),
    (gen_random_uuid(), 'solicitante financeiro', 'financeiro')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'administrador rh', 'ti'),
    (gen_random_uuid(), 'administrador rh', 'rh')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'colaborador rh', 'ti'),
    (gen_random_uuid(), 'colaborador rh', 'rh')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'gestão de estoque', 'ti'),
    (gen_random_uuid(), 'gestão de estoque', 'operações')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'compras', 'ti'),
    (gen_random_uuid(), 'compras', 'operações')
ON CONFLICT DO NOTHING;

INSERT INTO modules.modules_allowed_departments (id, module_name, department) VALUES
    (gen_random_uuid(), 'auditoria', 'ti')
ON CONFLICT DO NOTHING;
-- ============================================================
