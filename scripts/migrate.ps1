param(
    [string]$DbUrl = "jdbc:postgresql://localhost:5432/kermanager",
    [string]$DbUser = "postgres",
    [string]$DbPassword = "passer0412"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

Push-Location $root
try {
    Write-Host "Migrating Kermanager monolith database..."
    .\mvnw.cmd -q -N `
        "-Dflyway.url=$DbUrl" `
        "-Dflyway.user=$DbUser" `
        "-Dflyway.password=$DbPassword" `
        "-Dflyway.locations=filesystem:app/src/main/resources/db/migration" `
        "-Dflyway.table=flyway_schema_history" `
        flyway:migrate

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Creating database baseline..."
        .\mvnw.cmd -q -N `
            "-Dflyway.url=$DbUrl" `
            "-Dflyway.user=$DbUser" `
            "-Dflyway.password=$DbPassword" `
            "-Dflyway.locations=filesystem:app/src/main/resources/db/migration" `
            "-Dflyway.table=flyway_schema_history" `
            "-Dflyway.baselineVersion=5" `
            flyway:baseline

        if ($LASTEXITCODE -ne 0) {
            throw "Flyway baseline failed"
        }

        .\mvnw.cmd -q -N `
            "-Dflyway.url=$DbUrl" `
            "-Dflyway.user=$DbUser" `
            "-Dflyway.password=$DbPassword" `
            "-Dflyway.locations=filesystem:app/src/main/resources/db/migration" `
            "-Dflyway.table=flyway_schema_history" `
            flyway:migrate

        if ($LASTEXITCODE -ne 0) {
            throw "Flyway migrate failed"
        }
    }
}
finally {
    Pop-Location
}
