param(
    [string]$SwiPrologDir,
    [string]$JdkDir = $env:JAVA_HOME
)

$ErrorActionPreference = 'Stop'
$proyecto = Split-Path $PSScriptRoot -Parent
$version = ([xml](Get-Content -LiteralPath (Join-Path $proyecto 'pom.xml') -Raw)).project.version
if (-not $SwiPrologDir) { $SwiPrologDir = Join-Path $proyecto '.tools/swipl-full' }
if (-not $JdkDir) { throw 'Configura JAVA_HOME o pasa -JdkDir con el directorio del OpenJDK 17 x64.' }
$SwiPrologDir = (Resolve-Path -LiteralPath $SwiPrologDir).Path
$JdkDir = (Resolve-Path -LiteralPath $JdkDir).Path
$jpackage = Join-Path $JdkDir 'bin/jpackage.exe'
$swipl = Join-Path $SwiPrologDir 'bin/swipl.exe'
foreach ($archivo in @($jpackage, $swipl, (Join-Path $SwiPrologDir 'boot.prc'), (Join-Path $SwiPrologDir 'LICENSE'))) {
    if (-not (Test-Path -LiteralPath $archivo -PathType Leaf)) { throw "No se encuentra $archivo" }
}
$jdkRelease = Get-Content -LiteralPath (Join-Path $JdkDir 'release')
if (-not ($jdkRelease -match '^JAVA_VERSION="17\.') -or
        $jdkRelease -notcontains 'OS_ARCH="x86_64"') {
    throw 'El paquete requiere un JDK 17 x64 para Windows.'
}
$swiVersion = & $swipl --version
if ($LASTEXITCODE -ne 0 -or $swiVersion -notmatch 'x64-win64') { throw 'El paquete requiere SWI-Prolog x64.' }

$jdkAnterior = $env:JAVA_HOME
$env:JAVA_HOME = $JdkDir
try {
    & (Join-Path $PSScriptRoot 'compilar.ps1')
    $id = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
    $destino = Join-Path $proyecto "dist/$id"
    $entrada = Join-Path $proyecto "target/portable-$id"
    $fx = Join-Path $entrada 'fx'
    New-Item -ItemType Directory -Path $fx, $destino -Force | Out-Null
    Copy-Item -LiteralPath (Join-Path $proyecto "target/aula-accesible-$version.jar") -Destination $entrada
    $libs = Join-Path $proyecto 'target/dependency'
    foreach ($modulo in @('base', 'graphics', 'controls', 'fxml', 'media')) {
        $plataforma = @(Get-ChildItem -LiteralPath $libs -Filter "javafx-$modulo-*-win.jar")
        if ($plataforma.Count -ne 1) { throw "Falta el modulo JavaFX para Windows x64: $modulo" }
        Copy-Item -LiteralPath $plataforma[0].FullName -Destination $fx
    }
    # Estas librerías van en el classpath (junto al jar), no en el module-path de JavaFX.
    foreach ($patron in @('jackson-*.jar', 'atlantafx-*.jar', 'ikonli-*.jar')) {
        $encontrados = @(Get-ChildItem -LiteralPath $libs -Filter $patron)
        if ($encontrados.Count -eq 0) { throw "Falta la dependencia de runtime '$patron' en target/dependency." }
        $encontrados | ForEach-Object { Copy-Item -LiteralPath $_.FullName -Destination $entrada }
    }

    $opciones = @('--type', 'app-image', '--name', 'Aula Accesible', '--app-version', $version,
        '--dest', $destino, '--input', $entrada, '--main-jar', "aula-accesible-$version.jar",
        '--main-class', 'mx.aulaaccesible.Lanzador', '--add-modules',
        'java.desktop,java.xml,java.logging,java.scripting,jdk.unsupported,jdk.crypto.ec',
        '--java-options', '--module-path=$APPDIR/fx', '--java-options', '--add-modules=javafx.controls,javafx.fxml,javafx.media')
    & $jpackage @opciones
    if ($LASTEXITCODE -ne 0) { throw 'jpackage no pudo generar el lanzador.' }

    $imagen = Join-Path $destino 'Aula Accesible'
    $app = Join-Path $imagen 'app'
    $swiPortable = Join-Path $app 'swipl'
    $base = Join-Path $app 'conocimientos'
    New-Item -ItemType Directory -Path $swiPortable, $base -Force | Out-Null
    Get-ChildItem -LiteralPath $SwiPrologDir -Force |
        Where-Object { $_.Name -notin @('$PLUGINSDIR', 'Uninstall.exe') } |
        ForEach-Object { Copy-Item -LiteralPath $_.FullName -Destination $swiPortable -Recurse }
    foreach ($archivo in @('aula_accesible.pl', 'puente.pl')) {
        Copy-Item -LiteralPath (Join-Path $proyecto "conocimientos/$archivo") -Destination $base
    }
    $catalogo = '{"operacion":"catalogo"}' | & (Join-Path $swiPortable 'bin/swipl.exe') "--home=$swiPortable" -q -f none -s (Join-Path $base 'puente.pl') -g main -t halt
    if ($LASTEXITCODE -ne 0 -or ($catalogo | Out-String | ConvertFrom-Json).criterios.Count -ne 24) {
        throw 'No se pudo abrir el catalogo con el Prolog incluido.'
    }
    Copy-Item -LiteralPath (Join-Path $proyecto 'README.md') -Destination $imagen
    @($swiVersion, ($jdkRelease -match '^(IMPLEMENTOR|JAVA_VERSION|FULL_VERSION)='), 'Maven 3.9.11',
        'JavaFX 21.0.9', 'Jackson 2.21.2') | Set-Content -LiteralPath (Join-Path $imagen 'VERSIONES.txt') -Encoding utf8

    $zip = Join-Path $destino "Aula-Accesible-$version-win-x64.zip"
    & tar.exe -a -cf $zip -C $destino 'Aula Accesible'
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo crear el ZIP.' }
    Write-Output "Ejecutable: $(Join-Path $imagen 'Aula Accesible.exe')"
    Write-Output "Entrega: $zip"
} finally {
    $env:JAVA_HOME = $jdkAnterior
}
