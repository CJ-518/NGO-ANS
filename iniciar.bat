@echo off
rem Inicia NGO SAECA con el Java incluido en la carpeta "runtime" (no hace falta instalar Java ni PostgreSQL).
cd /d "%~dp0"
title NGO SAECA
echo =========================================
echo    SISTEMA DE GESTION - NGO SAECA
echo =========================================
echo Para cerrar el sistema, cierre esta ventana o presione Ctrl + C.
echo.
rem Abre el navegador unos segundos despues de arrancar el servidor
start "" /b cmd /c "timeout /t 10 /nobreak >nul & start http://localhost:8080"
"%~dp0runtime\bin\java.exe" -jar "%~dp0app\ngo-saeca.jar"
pause
