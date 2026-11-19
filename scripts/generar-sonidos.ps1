# Genera con ElevenLabs los .mp3 de src/main/resources/mx/aulaaccesible/sonidos a partir de catalogo.json.
# Solo crea los que faltan; -Forzar los regenera todos. Los .mp3 se incluyen en git: la app no necesita
# la clave ni conexión para sonar.
#   -Voz       id de la voz (por defecto «Gabriel Narrador Neutral», español de México)
#   -Solo      ids a generar, separados por comas (para repetir un clip)
# La clave se lee de ELEVENLABS_API_KEY (variable de entorno) o del archivo .env en la raíz. Nunca se imprime.
param(
    [switch]$Forzar,
    [string]$Voz = 'Y4A7VD8bsOlWNzF8WIRV',
    [string[]]$Solo = @()
)

$ErrorActionPreference = 'Stop'
$proyecto = Split-Path $PSScriptRoot -Parent
$carpeta = Join-Path $proyecto 'src/main/resources/mx/aulaaccesible/sonidos'
$catalogo = Get-Content -LiteralPath (Join-Path $carpeta 'catalogo.json') -Raw -Encoding utf8 | ConvertFrom-Json

$clave = $env:ELEVENLABS_API_KEY
if (-not $clave) {
    $env_ = Join-Path $proyecto '.env'
    if (Test-Path -LiteralPath $env_) {
        $linea = Get-Content -LiteralPath $env_ | Where-Object { $_ -match '^\s*ELEVENLABS_API_KEY\s*=' } | Select-Object -First 1
        if ($linea) { $clave = ($linea -split '=', 2)[1].Trim().Trim('"').Trim("'") }
    }
}
if (-not $clave) { throw 'Falta ELEVENLABS_API_KEY: defínela en el entorno o en el archivo .env de la raíz.' }

$ids = @($Solo | ForEach-Object { $_ -split ',' } | Where-Object { $_ })
$cabeceras = @{ 'xi-api-key' = $clave }
$generados = 0
foreach ($entrada in $catalogo) {
    if ($ids.Count -gt 0 -and $entrada.id -notin $ids) { continue }
    $destino = Join-Path $carpeta "$($entrada.id).mp3"
    if ((Test-Path -LiteralPath $destino) -and -not $Forzar -and $ids.Count -eq 0) { continue }
    if ($entrada.tipo -eq 'voz') {
        $url = "https://api.elevenlabs.io/v1/text-to-speech/${Voz}?output_format=mp3_44100_128"
        $cuerpo = @{ text = $entrada.texto; model_id = 'eleven_multilingual_v2' }
    } else {
        $url = 'https://api.elevenlabs.io/v1/sound-generation?output_format=mp3_44100_128'
        $cuerpo = @{ text = $entrada.texto; prompt_influence = 0.5 }
        if ($entrada.duracion) { $cuerpo.duration_seconds = [double]$entrada.duracion }
        if ($entrada.bucle) { $cuerpo.loop = $true }
    }
    $json = $cuerpo | ConvertTo-Json -Compress
    try {
        Invoke-WebRequest -Uri $url -Method Post -Headers $cabeceras -ContentType 'application/json; charset=utf-8' `
            -Body ([System.Text.Encoding]::UTF8.GetBytes($json)) -OutFile $destino | Out-Null
    } catch {
        if (Test-Path -LiteralPath $destino) { Remove-Item -LiteralPath $destino }
        throw "No se pudo generar '$($entrada.id)': $($_.Exception.Message)"
    }
    $generados++
    Write-Output "Generado $($entrada.id).mp3"
}
Write-Output "Listo: $generados archivo(s) nuevo(s) en $carpeta"
