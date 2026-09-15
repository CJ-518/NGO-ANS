# Limpiar pantalla para una vista limpia
Clear-Host

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "   SISTEMA DE GESTION - NGO SAECA" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

$dbUser = Read-Host "Usuario de PostgreSQL (por defecto 'postgres')"
if ([string]::IsNullOrWhiteSpace($dbUser)) { $dbUser = "postgres" }

$dbPass = Read-Host -AsSecureString "Contrasenha de PostgreSQL"
$BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbPass)
Clear-Variable dbPass
$plainPass = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)

# Asignar variables de entorno
$env:DB_USER = $dbUser
$env:DB_PASSWORD = $plainPass

# Tarea en segundo plano para abrir el navegador limpiamente cuando el puerto 8080 responda
Start-Job -ScriptBlock {
    $url = "http://localhost:8080"
    do {
        Start-Sleep -Seconds 1
        try {
            $request = [System.Net.WebRequest]::Create($url)
            $request.Timeout = 1000
            if (($request.GetResponse()).StatusCode -eq 200) {
                Start-Process $url
                break
            }
        } catch {}
    } while ($true)
} | Out-Null

Write-Host "`n[+] Servidor iniciado. Abriendo navegador..." -ForegroundColor Green
Write-Host "[!] Para cerrar el sistema, presione Ctrl + C en esta ventana.`n" -ForegroundColor Yellow

# Ejecutar Spring Boot en modo silencioso (-q oculta los bloques de texto de Maven)
.\mvnw.cmd spring-boot:run -q