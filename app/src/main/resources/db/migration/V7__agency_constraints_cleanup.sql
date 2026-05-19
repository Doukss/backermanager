-- ============================================================
--  V7 — Post-cleanup & constraint hardening for agency tables
--  Runs once; idempotent guards against re-application.
--  1. Remove corrupt seed rows injected before CHECK constraints existed
--  2. Add CHECK constraints to agencies (idempotent — already in V2, kept for safety)
--  3. Add CHECK constraints to subscriptions (idempotent — already in V2, kept for safety)
--  4. Add FK subscriptions.tenant_id → agencies.tenant_id (already in V2, kept for safety)
-- ============================================================

-- ── 1. Remove rows with obviously corrupt values ──────────────────
--      FIXED: parentheses added to enforce correct AND/OR precedence
DELETE FROM agencies
 WHERE (plan IS NOT NULL AND plan NOT IN ('STARTER', 'PRO', 'ENTERPRISE', 'PROFESSIONNEL'))
    OR tenant_id IS NULL
    OR nom IS NULL
    OR nom = ''
    OR (email IS NOT NULL AND email !~* '^[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$')
    OR (telephone IS NOT NULL AND telephone !~* '^[\+]?[0-9 .\-\(\)]{5,20}$');

DELETE FROM subscriptions
 WHERE (plan IS NOT NULL AND plan NOT IN ('STARTER', 'PRO', 'ENTERPRISE', 'PROFESSIONNEL'))
    OR (status IS NOT NULL AND status NOT IN ('ACTIVE', 'SUSPENDED', 'TRIAL', 'CANCELLED'))
    OR (monthly_price IS NOT NULL AND monthly_price < 0);

-- ── 2. CHECK constraints on agencies table (idempotent) ───────────
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname  = 'ck_agencies_plan'
          AND conrelid = 'agencies'::regclass
    ) THEN
        ALTER TABLE agencies
            ADD CONSTRAINT ck_agencies_plan
            CHECK (plan IN ('STARTER','PRO','ENTERPRISE','PROFESSIONNEL'));
    END IF;
END $$;

-- ── 3. CHECK constraints on subscriptions table (idempotent) ──────
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname  = 'ck_subscriptions_plan'
          AND conrelid = 'subscriptions'::regclass
    ) THEN
        ALTER TABLE subscriptions
            ADD CONSTRAINT ck_subscriptions_plan
            CHECK (plan IN ('STARTER','PRO','ENTERPRISE','PROFESSIONNEL'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname  = 'ck_subscriptions_status'
          AND conrelid = 'subscriptions'::regclass
    ) THEN
        ALTER TABLE subscriptions
            ADD CONSTRAINT ck_subscriptions_status
            CHECK (status IN ('ACTIVE','SUSPENDED','TRIAL','CANCELLED'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname  = 'ck_subscriptions_price_nonneg'
          AND conrelid = 'subscriptions'::regclass
    ) THEN
        ALTER TABLE subscriptions
            ADD CONSTRAINT ck_subscriptions_price_nonneg
            CHECK (monthly_price >= 0);
    END IF;
END $$;

-- ── 4. FK: subscriptions.tenant_id → agencies.tenant_id (idempotent)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname  = 'fk_subscriptions_agency'
          AND conrelid = 'subscriptions'::regclass
    ) THEN
        ALTER TABLE subscriptions
            ADD CONSTRAINT fk_subscriptions_agency
            FOREIGN KEY (tenant_id)
            REFERENCES agencies (tenant_id)
            ON DELETE CASCADE ON UPDATE CASCADE;
    END IF;
END $$;

-- ── 5. Sync subscription status with agency active flag ────────────
UPDATE subscriptions s
SET    status = 'SUSPENDED'
FROM   agencies a
WHERE  a.tenant_id   = s.tenant_id
  AND  a.active      = false
  AND  s.status     != 'SUSPENDED';

UPDATE subscriptions s
SET    status = 'ACTIVE'
FROM   agencies a
WHERE  a.tenant_id   = s.tenant_id
  AND  a.active      = true
  AND  s.status      = 'SUSPENDED';

-- ── 2. CHECK constraints on agencies table ────────────────────────
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname  = 'ck_agencies_plan'
          AND conrelid = 'agencies'::regclass
    ) THEN
        ALTER TABLE agencies
            ADD CONSTRAINT ck_agencies_plan
            CHECK (plan IN ('STARTER','PRO','ENTERPRISE','PROFESSIONNEL'));
    END IF;
END $$;

-- ── 3. CHECK constraints on subscriptions table ───────────────────
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname  = 'ck_subscriptions_plan'
          AND conrelid = 'subscriptions'::regclass
    ) THEN
        ALTER TABLE subscriptions
            ADD CONSTRAINT ck_subscriptions_plan
            CHECK (plan IN ('STARTER','PRO','ENTERPRISE','PROFESSIONNEL'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname  = 'ck_subscriptions_status'
          AND conrelid = 'subscriptions'::regclass
    ) THEN
        ALTER TABLE subscriptions
            ADD CONSTRAINT ck_subscriptions_status
            CHECK (status IN ('ACTIVE','SUSPENDED','TRIAL','CANCELLED'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname  = 'ck_subscriptions_price_nonneg'
          AND conrelid = 'subscriptions'::regclass
    ) THEN
        ALTER TABLE subscriptions
            ADD CONSTRAINT ck_subscriptions_price_nonneg
            CHECK (monthly_price >= 0);
    END IF;
END $$;

-- ── 4. FK: subscriptions.tenant_id → agencies.tenant_id ───────────
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname  = 'fk_subscriptions_agency'
          AND conrelid = 'subscriptions'::regclass
    ) THEN
        ALTER TABLE subscriptions
            ADD CONSTRAINT fk_subscriptions_agency
            FOREIGN KEY (tenant_id)
            REFERENCES agencies (tenant_id)
            ON DELETE CASCADE ON UPDATE CASCADE;
    END IF;
END $$;

-- ── 5. Sync subscription status with agency active flag ────────────
UPDATE subscriptions s
SET    status = 'SUSPENDED'
FROM   agencies a
WHERE  a.tenant_id   = s.tenant_id
  AND  a.active      = false
  AND  s.status     != 'SUSPENDED';

UPDATE subscriptions s
SET    status = 'ACTIVE'
FROM   agencies a
WHERE  a.tenant_id   = s.tenant_id
  AND  a.active      = true
  AND  s.status      = 'SUSPENDED';
