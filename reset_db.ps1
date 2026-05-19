# reset_db.ps1 — Trouve psql automatiquement et remet la base a zero
 
# Cherche psql dans les emplacements PostgreSQL habituels
$psqlPath = Get-ChildItem "C:\Program Files\PostgreSQL" -Recurse -Filter "psql.exe" -ErrorAction SilentlyContinue |
            Sort-Object FullName -Descending |
            Select-Object -First 1 -ExpandProperty FullName
 
if (-not $psqlPath) {
    $psqlPath = Get-ChildItem "C:\Program Files (x86)\PostgreSQL" -Recurse -Filter "psql.exe" -ErrorAction SilentlyContinue |
                Sort-Object FullName -Descending |
                Select-Object -First 1 -ExpandProperty FullName
}
 
if (-not $psqlPath) {
    Write-Host "psql.exe introuvable. Verifie que PostgreSQL est installe." -ForegroundColor Red
    exit 1
}
 
Write-Host "psql trouve : $psqlPath" -ForegroundColor Green
 
$env:PGPASSWORD = "passer0412"
 
$sql = @"
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
DROP TABLE IF EXISTS platform_notifications CASCADE;
DROP TABLE IF EXISTS subscriptions CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS disputes CASCADE;
DROP TABLE IF EXISTS contracts CASCADE;
DROP TABLE IF EXISTS properties CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS agencies CASCADE;
DROP EXTENSION IF EXISTS pgcrypto;
"@
 
Write-Host "Reset de la base kermanager en cours..." -ForegroundColor Cyan
 
$sql | & $psqlPath -U postgres -d kermanager
 
if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Base remise a zero avec succes !" -ForegroundColor Green
    Write-Host "Lance maintenant : .\scripts\start-backend.ps1" -ForegroundColor Yellow
} else {
    Write-Host ""
    Write-Host "Erreur psql (code $LASTEXITCODE)." -ForegroundColor Red
    Write-Host "Essaie de changer le mot de passe dans ce script (ligne avec PGPASSWORD) si besoin." -ForegroundColor Yellow
}
