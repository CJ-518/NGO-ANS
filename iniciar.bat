@echo off
rem Inicia NGO SAECA con el Java incluido en la carpeta "runtime" (no hace falta instalar Java ni PostgreSQL).
cd /d "%~dp0"
title NGO SAECA

rem Quita la marca de "descargado de Internet" (Zona 3 / Mark of the Web) de todos los
rem archivos de esta carpeta. Windows agrega esa marca a los .bat, .exe, .jar, etc. que
rem vienen dentro de un .zip descargado, y por eso SmartScreen y Windows Defender pueden
rem volver a mostrar el aviso "Windows protegio su PC" cada vez que se ejecuta algo nuevo
rem (el java.exe incluido, el .jar, etc.), aunque el usuario ya haya aceptado ejecutar
rem este .bat una vez. Esto NO evita el primer aviso al hacer doble clic en iniciar.bat
rem (ese aviso aparece antes de que el script pueda correr), pero evita que vuelva a
rem salir en las ejecuciones siguientes ni en los archivos internos del programa.
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-ChildItem -Path '%~dp0' -Recurse | Unblock-File -ErrorAction SilentlyContinue" >nul 2>&1

echo =========================================
echo    SISTEMA DE GESTION - NGO SAECA
echo =========================================
echo Para cerrar el sistema, cierre esta ventana o presione Ctrl + C.
echo.
rem Abre el navegador unos segundos despues de arrancar el servidor
start "" /b cmd /c "timeout /t 10 /nobreak >nul & start http://localhost:8080"
"%~dp0runtime\bin\java.exe" -jar "%~dp0app\ngo-saeca.jar"
pause
