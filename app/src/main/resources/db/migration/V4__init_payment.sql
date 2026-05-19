CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE payments (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      VARCHAR(100) NOT NULL,
    contract_id    UUID NOT NULL,
    montant        NUMERIC(12,2) NOT NULL,
    date_echeance  DATE NOT NULL,
    date_paiement  DATE,
    statut         VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE',
    reference      VARCHAR(100),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_payments_tenant   ON payments(tenant_id);
CREATE INDEX idx_payments_contract ON payments(contract_id);
CREATE INDEX idx_payments_statut   ON payments(statut);
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON payments USING (current_setting('app.tenant_id', true) IS NULL OR tenant_id = current_setting('app.tenant_id', true));
