$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    & .\mvnw.cmd -B -ntp -DskipTests package dependency:copy-dependencies '-DincludeScope=runtime'
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo compilar Aula Accesible.' }
} finally {
    Pop-Location
}
