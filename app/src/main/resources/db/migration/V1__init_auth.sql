CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id  VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    full_name  VARCHAR(255),
    phone      VARCHAR(50),
    role       VARCHAR(50)  NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_tenant  ON users(tenant_id);
CREATE INDEX idx_users_email   ON users(email);

-- Row-Level Security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON users
    USING (current_setting('app.tenant_id', true) IS NULL OR tenant_id = current_setting('app.tenant_id', true));

-- Mot de passe en clair pour dev ({noop} accepté par Spring Security DelegatingPasswordEncoder)
INSERT INTO users (tenant_id, email, password, full_name, phone, role, active)
VALUES ('platform', 'superadmin@kermanager.local',
        '{noop}admin123',
        'Super Admin', '+221 77 000 00 00', 'SUPER_ADMIN', true)
ON CONFLICT (email) DO NOTHING;
