$env:PGPASSWORD = "passer0412"
$pq = '& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d kermanager -v ON_ERROR_STOP=1'

# Fix corrupt rows manually
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d kermanager -v ON_ERROR_STOP=1 -c "DELETE FROM subscriptions WHERE tenant_id NOT IN (SELECT tenant_id FROM agencies);" 2>&1 | Out-Null
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d kermanager -v ON_ERROR_STOP=1 -c "DELETE FROM agencies WHERE tenant_id NOT IN (SELECT tenant_id FROM agencies WHERE tenant_id = tenant_id);" 2>&1 | Out-Null

# Insert V7 record into agency flyway history
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d kermanager -v ON_ERROR_STOP=1 -c "INSERT INTO flyway_schema_history_agency (installed_rank, version, description, type, script, installed_on, execution_time, success) VALUES (4, '7', 'agency constraints cleanup', 'SQL', 'V7__agency_constraints_cleanup.sql', now(), 0, true) ON CONFLICT DO NOTHING;" 2>&1

# Verify
Write-Host "== Agency constraints =="
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d kermanager -v ON_ERROR_STOP=1 -c "SELECT conname::text, conrelid::regclass::text, contype, pg_get_constraintdef(oid)::text FROM pg_constraint WHERE conrelid IN ('agencies'::regclass,'subscriptions'::regclass) ORDER BY conrelid, conname;" 2>&1
Write-Host "== Agency FK =="
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d kermanager -v ON_ERROR_STOP=1 -c "SELECT conname::text FROM pg_constraint WHERE contype = 'f' AND conrelid = 'subscriptions'::regclass;" 2>&1
Write-Host "== Flyway agency history =="
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d kermanager -v ON_ERROR_STOP=1 -c "SELECT installed_rank, version, description, script, success FROM flyway_schema_history_agency ORDER BY installed_rank;" 2>&1
