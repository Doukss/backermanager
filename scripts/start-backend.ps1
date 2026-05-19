param(
    [switch]$SkipCompile
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

Push-Location $root
try {
    if (-not $SkipCompile) {
        Write-Host "Compiling Kermanager backend..."
        .\mvnw.cmd -q -DskipTests compile
        if ($LASTEXITCODE -ne 0) {
            throw "Backend compilation failed"
        }
    }

    Write-Host "Starting Kermanager monolith on port 8080..."
    .\mvnw.cmd -pl app spring-boot:run
}
finally {
    Pop-Location
}
