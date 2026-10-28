@echo off
setlocal EnableExtensions

set "APP_PORT=8080"

for /f "usebackq delims=" %%I in (`powershell -NoProfile -ExecutionPolicy Bypass -Command "$conn = Get-NetTCPConnection -LocalPort %APP_PORT% -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1; if($conn){ $conn.OwningProcess }"`) do set "APP_PID=%%I"

if not defined APP_PID (
  echo Nessun processo in ascolto sulla porta %APP_PORT%.
  exit /b 0
)

echo Arresto processo PID %APP_PID% sulla porta %APP_PORT%...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Stop-Process -Id %APP_PID% -ErrorAction Stop"
if not "%ERRORLEVEL%"=="0" (
  echo ERRORE: arresto processo non riuscito.
  exit /b 1
)

echo Processo arrestato correttamente.
exit /b 0
