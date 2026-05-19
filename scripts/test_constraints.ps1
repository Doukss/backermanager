$env:PGPASSWORD = "passer0412"
$pq = '& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d kermanager -v ON_ERROR_STOP=1'

# -- 1. Try invalid agency plan
$env:PGPASSWORD = "passer0412"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d kermanager -v ON_ERROR_STOP=1 -c "ALTER TABLE agencies DISABLE TRIGGER ALL;" 2>&1 | Out-Null
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d kermanager -v ON_ERROR_STOP=1 -c "ALTER TABLE subscriptions DISABLE TRIGGER ALL;" 2>&1 | Out-Null

& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d kermanager -c "
DO \$\$
DECLARE plan_violation BOOLEAN := false;
    status_violation BOOLEAN := false;
    price_violation  BOOLEAN := false;
    fk_violation     BOOLEAN := false;
BEGIN
    -- test agency plan CHECK
    BEGIN
        UPDATE agencies SET plan = 'INVALID_PLAN' WHERE tenant_id = 'wwwwww';
        plan_violation := true;
    EXCEPTION WHEN check_violation THEN
        plan_violation := false;
        UPDATE agencies SET nom = 'wwwwww' WHERE tenant_id = 'wwwwww';
    END;

    -- test subscription status CHECK
    BEGIN
        UPDATE subscriptions SET status = 'INVALID_STATUS' WHERE tenant_id = 'wwwwww';
        status_violation := true;
    EXCEPTION WHEN check_violation THEN
        status_violation := false;
    END;

    -- test subscription price CHECK
    BEGIN
        UPDATE subscriptions SET monthly_price = -1 WHERE tenant_id = 'dakar-prestige';
        price_violation := true;
    EXCEPTION WHEN check_violation THEN
        price_violation := false;
    END;

    -- test FK
    BEGIN
        INSERT INTO subscriptions (tenant_id, plan, status, monthly_price, started_at, next_billing_at)
        VALUES ('no-such-agency', 'PRO', 'TRIAL', 0, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 month');
        fk_violation := true;
        DELETE FROM subscriptions WHERE tenant_id = 'no-such-agency';
    EXCEPTION WHEN foreign_key_violation THEN
        fk_violation := false;
    END;

    RAISE INFO 'CHECK agencies plan:  %', CASE WHEN plan_violation   THEN 'FAIL (no constraint)' ELSE 'PASS (enforced)' END;
    RAISE INFO 'CHECK subs status:  %', CASE WHEN status_violation THEN 'FAIL (no constraint)' ELSE 'PASS (enforced)' END;
    RAISE INFO 'CHECK subs price:   %', CASE WHEN price_violation  THEN 'FAIL (no constraint)' ELSE 'PASS (enforced)' END;
    RAISE INFO 'FK subscriptions:   %', CASE WHEN fk_violation     THEN 'FAIL (no constraint)' ELSE 'PASS (enforced)' END;
END \$\$;
" 2>&1

# Re-enable triggers
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d kermanager -c "ALTER TABLE agencies ENABLE TRIGGER ALL; ALTER TABLE subscriptions ENABLE TRIGGER ALL;" 2>&1 | Out-Null
