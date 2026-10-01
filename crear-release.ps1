# Crea el release portable de NGO SAECA (carpeta + .zip) que corre SIN Java ni PostgreSQL instalados.
# Ejecutar en la maquina de desarrollo (con JDK 21 instalado), desde la carpeta del proyecto:
#     .\crear-release.ps1
# IMPORTANTE: el release solo corre en el mismo sistema operativo donde se genera (Windows -> Windows).

$ErrorActionPreference = "Stop"
$nombre  = "NGO-SAECA"
$destino = Join-Path $PSScriptRoot "release\$nombre"

Write-Host "[1/4] Compilando el proyecto (jar ejecutable)..." -ForegroundColor Cyan
& "$PSScriptRoot\mvnw.cmd" clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "Fallo la compilacion con Maven." }

if (Test-Path "$PSScriptRoot\release") { Remove-Item "$PSScriptRoot\release" -Recurse -Force }
New-Item -ItemType Directory -Path "$destino\app" -Force | Out-Null

Write-Host "[2/4] Creando el Java reducido (jlink)..." -ForegroundColor Cyan
$modulos = "java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.transaction.xa,java.xml,jdk.crypto.ec,jdk.unsupported,jdk.zipfs"
& jlink --add-modules $modulos --strip-debug --no-header-files --no-man-pages --compress=zip-6 --output "$destino\runtime"
if ($LASTEXITCODE -ne 0) { throw "Fallo jlink. Verifica que JAVA_HOME apunte a un JDK 21 (no a un JRE)." }

Write-Host "[3/4] Copiando archivos..." -ForegroundColor Cyan
Copy-Item "$PSScriptRoot\target\ngo-saeca.jar" "$destino\app\ngo-saeca.jar"
Copy-Item "$PSScriptRoot\iniciar.bat" "$destino\iniciar.bat"

Write-Host "[4/4] Comprimiendo..." -ForegroundColor Cyan
$zip = Join-Path $PSScriptRoot "release\$nombre.zip"
Compress-Archive -Path $destino -DestinationPath $zip -Force

Write-Host "`nListo: $zip" -ForegroundColor Green
