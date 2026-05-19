CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE agencies (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id  VARCHAR(100) NOT NULL UNIQUE,
    nom        VARCHAR(255) NOT NULL,
    adresse    TEXT,
    telephone  VARCHAR(20),
    email      VARCHAR(255),
    plan       VARCHAR(50) NOT NULL DEFAULT 'STARTER',
    active     BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT agencies_plan_ck CHECK (plan IN ('STARTER','PRO','ENTERPRISE','PROFESSIONNEL'))
);
CREATE INDEX idx_agencies_tenant ON agencies(tenant_id);

CREATE TABLE subscriptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL UNIQUE
        REFERENCES agencies(tenant_id) ON DELETE CASCADE ON UPDATE CASCADE,
    plan            VARCHAR(50) NOT NULL DEFAULT 'STARTER',
    status          VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    monthly_price   NUMERIC(12,2) NOT NULL DEFAULT 0,
    started_at      DATE NOT NULL DEFAULT CURRENT_DATE,
    next_billing_at DATE NOT NULL DEFAULT CURRENT_DATE + INTERVAL '1 month',
    CONSTRAINT subscriptions_plan_ck   CHECK (plan   IN ('STARTER','PRO','ENTERPRISE','PROFESSIONNEL')),
    CONSTRAINT subscriptions_status_ck CHECK (status IN ('ACTIVE','SUSPENDED','TRIAL','CANCELLED')),
    CONSTRAINT subscriptions_price_ck  CHECK (monthly_price >= 0)
);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_subscriptions_tenant ON subscriptions(tenant_id);

CREATE TABLE platform_notifications (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title      VARCHAR(255) NOT NULL,
    message    TEXT,
    type       VARCHAR(50),
    priority   VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    read       BOOLEAN NOT NULL DEFAULT false,
    target     VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT notif_type_ck     CHECK (type     IN ('AGENCY','PAYMENT','DISPUTE','SYSTEM','MAINTENANCE')),
    CONSTRAINT notif_priority_ck CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL'))
);
