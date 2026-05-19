DO $$
DECLARE
  r text;
BEGIN
  -- Test 1: Agency plan CHECK
  BEGIN
    UPDATE agencies SET plan = 'BAD_PLAN' WHERE tenant_id = 'dakar-prestige';
    RAISE INFO 'CHECK_AGENCY_PLAN: FAIL (no constraint blocking)';
  EXCEPTION WHEN check_violation THEN
    RAISE INFO 'CHECK_AGENCY_PLAN: PASS (enforced)';
  END;

  -- Restore just in case the false branch was taken
  UPDATE agencies SET nom = agencies.nom WHERE tenant_id = 'dakar-prestige';

  -- Test 2: Subscription status CHECK
  BEGIN
    UPDATE subscriptions SET status = 'BAD_STATUS' WHERE tenant_id = 'dakar-prestige';
    RAISE INFO 'CHECK_SUBS_STATUS: FAIL (no constraint blocking)';
  EXCEPTION WHEN check_violation THEN
    RAISE INFO 'CHECK_SUBS_STATUS: PASS (enforced)';
  END;

  -- Test 3: Subscription price CHECK
  BEGIN
    UPDATE subscriptions SET monthly_price = -5 WHERE tenant_id = 'dakar-prestige';
    RAISE INFO 'CHECK_SUBS_PRICE: FAIL (no constraint blocking)';
  EXCEPTION WHEN check_violation THEN
    RAISE INFO 'CHECK_SUBS_PRICE: PASS (enforced)';
  END;

  -- Test 4: FK (subscriptions.tenant_id -> agencies.tenant_id)
  BEGIN
    INSERT INTO subscriptions (tenant_id, plan, status, monthly_price, started_at, next_billing_at)
    VALUES ('no-such-agency-xyz', 'PRO', 'TRIAL', 0, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 month');
    DELETE FROM subscriptions WHERE tenant_id = 'no-such-agency-xyz';
    RAISE INFO 'FK_SUBSCRIPTIONS: FAIL (no constraint)';
  EXCEPTION WHEN foreign_key_violation THEN
    RAISE INFO 'FK_SUBSCRIPTIONS: PASS (enforced)';
  END;
END $$;
