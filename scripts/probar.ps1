$ErrorActionPreference = 'Stop'
$proyecto = Split-Path $PSScriptRoot -Parent
Push-Location $proyecto
try {
    $swipl = $env:AULA_SWIPL
    if (-not $swipl) {
        $local = Join-Path $proyecto '.tools/swipl-full/bin/swipl.exe'
        $swipl = if (Test-Path -LiteralPath $local) { $local } else { 'swipl.exe' }
    }
    & $swipl -q -f none -s conocimientos/aula_accesible_tests.pl -g run_tests -t halt
    if ($LASTEXITCODE -ne 0) { throw 'Fallaron las pruebas de Prolog.' }
    & .\mvnw.cmd -B -ntp test
    if ($LASTEXITCODE -ne 0) { throw 'Fallaron las pruebas de Java.' }
} finally {
    Pop-Location
}
