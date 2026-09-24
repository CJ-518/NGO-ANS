# Limpiar pantalla para una vista limpia
Clear-Host

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "   SISTEMA DE GESTION - NGO SAECA" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# Limpiar jobs huerfanos de ejecuciones anteriores (p. ej. si el arranque
# fallo la ultima vez y el job que espera el puerto 8080 quedo colgado).
# Si no se limpian, cada intento fallido deja un job mas corriendo en segundo
# plano, y cuando el servidor finalmente arranca, todos abren una pestana.
Get-Job | Stop-Job -ErrorAction SilentlyContinue
Get-Job | Remove-Job -Force -ErrorAction SilentlyContinue

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
$browserJob = Start-Job -ScriptBlock {
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
}

Write-Host "`n[+] Servidor iniciado. Abriendo navegador..." -ForegroundColor Green
Write-Host "[!] Para cerrar el sistema, presione Ctrl + C en esta ventana.`n" -ForegroundColor Yellow

try {
    # Ejecutar Spring Boot en modo silencioso (-q oculta los bloques de texto de Maven)
    .\mvnw.cmd spring-boot:run -q
}
finally {
    # Pase lo que pase (arranque exitoso, fallo de Maven, o Ctrl+C), se
    # detiene y elimina el job para que no quede esperando el puerto 8080
    # en la proxima ejecucion.
    if ($browserJob) {
        Stop-Job $browserJob -ErrorAction SilentlyContinue
        Remove-Job $browserJob -Force -ErrorAction SilentlyContinue
    }
}
