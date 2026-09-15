# Solicitar credenciales en la terminal de forma interactiva
Write-Host "=== CONFIGURACION DE BASE DE DATOS NGO SAECA ===" -ForegroundColor Cyan
$dbUser = Read-Host "Ingrese su usuario de PostgreSQL (por defecto 'postgres')"
if ([string]::IsNullOrWhiteSpace($dbUser)) { $dbUser = "postgres" }

$dbPass = Read-Host -AsSecureString "Ingrese su contraseña de PostgreSQL"
$BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbPass)
Clear-Variable dbPass
$plainPass = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)

# Asignar a variables de entorno para esta sesion
$env:DB_USER = $dbUser
$env:DB_PASSWORD = $plainPass

Write-Host "Iniciando la aplicacion Spring Boot..." -ForegroundColor Green

# Ejecutar Maven en segundo plano o dejarlo corriendo y abrir el navegador en paralelo
Start-Job -ScriptBlock {
    param($user, $pass)
    $env:DB_USER = $user
    $env:DB_PASSWORD = $pass
    .\mvnw.cmd spring-boot:run
} -ArgumentList $dbUser, $plainPass | Out-Null

# Esperar unos segundos a que Spring Boot arranque e iniciar el navegador
Write-Host "Esperando a que el servidor este listo..." -ForegroundColor Yellow
Start-Sleep -Seconds 8

Write-Host "Abriendo el navegador..." -ForegroundColor Green
Start-Process "http://localhost:8080"

Write-Host "¡Sistema en ejecucion! Para cerrar la aplicacion, cierre esta terminal." -ForegroundColor Cyan