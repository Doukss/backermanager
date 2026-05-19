CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE properties (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      VARCHAR(100) NOT NULL,
    titre          VARCHAR(255) NOT NULL,
    adresse        TEXT,
    ville          VARCHAR(100),
    type           VARCHAR(50),
    loyer_mensuel  NUMERIC(12,2),
    surface        NUMERIC(8,2),
    nombre_pieces  INT,
    disponible     BOOLEAN NOT NULL DEFAULT true,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE contracts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    property_id     UUID REFERENCES properties(id),
    locataire_nom   VARCHAR(255),
    locataire_email VARCHAR(255),
    date_debut      DATE,
    date_fin        DATE,
    loyer_mensuel   NUMERIC(12,2),
    depot           NUMERIC(12,2),
    statut          VARCHAR(50) NOT NULL DEFAULT 'BROUILLON',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_properties_tenant ON properties(tenant_id);
CREATE INDEX idx_contracts_tenant  ON contracts(tenant_id);
CREATE INDEX idx_contracts_property ON contracts(property_id);

ALTER TABLE properties ENABLE ROW LEVEL SECURITY;
ALTER TABLE contracts  ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_prop ON properties USING (current_setting('app.tenant_id', true) IS NULL OR tenant_id = current_setting('app.tenant_id', true));
CREATE POLICY tenant_isolation_cont ON contracts  USING (current_setting('app.tenant_id', true) IS NULL OR tenant_id = current_setting('app.tenant_id', true));
