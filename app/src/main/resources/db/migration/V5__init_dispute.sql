CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE disputes (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(100) NOT NULL,
    contract_id UUID,
    titre       VARCHAR(255) NOT NULL,
    description TEXT,
    statut      VARCHAR(50) NOT NULL DEFAULT 'OUVERT',
    priorite    VARCHAR(20) DEFAULT 'NORMALE',
    resolution  TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_disputes_tenant  ON disputes(tenant_id);
CREATE INDEX idx_disputes_statut  ON disputes(statut);
ALTER TABLE disputes ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON disputes USING (current_setting('app.tenant_id', true) IS NULL OR tenant_id = current_setting('app.tenant_id', true));
