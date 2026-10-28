@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "PROJECT_DIR=%~dp0"
set "APP_URL=http://localhost:8080/api/dashboard.html"
set "JAVA_HOME_DETECTED="
set "MAVEN_CMD="
set "RUN_SCRIPT=%TEMP%\gestionale-edile-run-%RANDOM%.cmd"

echo [1/5] Verifica server gia avviato...
call :check_up
if "%ERRORLEVEL%"=="0" (
  echo Server gia attivo su %APP_URL%
  goto :success
)

echo [2/5] Rilevazione JDK...
call :resolve_jdk
if not "%ERRORLEVEL%"=="0" (
  echo ERRORE: JDK non trovato. Installa un JDK e riprova.
  goto :fail
)
echo JAVA_HOME usato: %JAVA_HOME_DETECTED%

echo [3/5] Rilevazione Maven...
call :resolve_maven
if not "%ERRORLEVEL%"=="0" (
  echo ERRORE: Maven non trovato. Installa Maven oppure aggiungi mvn al PATH.
  goto :fail
)
echo Maven usato: %MAVEN_CMD%

echo [4/5] Avvio applicazione Spring Boot...
cd /d "%PROJECT_DIR%"
>"%RUN_SCRIPT%" echo @echo off
>>"%RUN_SCRIPT%" echo set "JAVA_HOME=%JAVA_HOME_DETECTED%"
>>"%RUN_SCRIPT%" echo set "Path=%JAVA_HOME_DETECTED%\bin;%%Path%%"
>>"%RUN_SCRIPT%" echo cd /d "%PROJECT_DIR%"
>>"%RUN_SCRIPT%" echo "%MAVEN_CMD%" -f "%PROJECT_DIR%pom.xml" spring-boot:run
start "Gestionale Edile" cmd /k ""%RUN_SCRIPT%""

echo [5/5] Attendo disponibilita servizio...
call :wait_up 120
if not "%ERRORLEVEL%"=="0" (
  echo ERRORE: server non raggiungibile entro 120 secondi.
  echo Controlla la finestra "Gestionale Edile" per il dettaglio errore.
  goto :fail
)

:success
if exist "%RUN_SCRIPT%" del "%RUN_SCRIPT%" >nul 2>&1
echo.
echo URL applicazione: %APP_URL%
exit /b 0

:fail
if exist "%RUN_SCRIPT%" del "%RUN_SCRIPT%" >nul 2>&1
echo.
echo URL applicazione (attesa): %APP_URL%
exit /b 1

:check_up
powershell -NoProfile -ExecutionPolicy Bypass -Command "try { $r = Invoke-WebRequest -Uri '%APP_URL%' -UseBasicParsing -TimeoutSec 3; if($r.StatusCode -eq 200){ exit 0 } else { exit 1 } } catch { exit 1 }"
exit /b %ERRORLEVEL%

:wait_up
set "WAIT_SECONDS=%~1"
powershell -NoProfile -ExecutionPolicy Bypass -Command "$deadline = (Get-Date).AddSeconds(%WAIT_SECONDS%); $ok = $false; while((Get-Date) -lt $deadline){ try { $r = Invoke-WebRequest -Uri '%APP_URL%' -UseBasicParsing -TimeoutSec 1; if($r.StatusCode -eq 200){ $ok = $true; break } } catch {}; Start-Sleep -Milliseconds 750 }; if($ok){ exit 0 } else { exit 1 }"
exit /b %ERRORLEVEL%

:resolve_jdk
if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\javac.exe" (
    set "JAVA_HOME_DETECTED=%JAVA_HOME%"
    exit /b 0
  )
)

for /f "usebackq delims=" %%I in (`powershell -NoProfile -ExecutionPolicy Bypass -Command "$javac = (Get-Command javac -ErrorAction SilentlyContinue).Source; if($javac){ Split-Path -Parent (Split-Path -Parent $javac) }"`) do (
  if exist "%%I\bin\javac.exe" (
    set "JAVA_HOME_DETECTED=%%I"
    exit /b 0
  )
)

if exist "C:\Program Files\Eclipse Adoptium\jdk-19.0.2.7-hotspot\bin\javac.exe" (
  set "JAVA_HOME_DETECTED=C:\Program Files\Eclipse Adoptium\jdk-19.0.2.7-hotspot"
  exit /b 0
)
if exist "C:\Program Files\Java\jdk1.8.0_202\bin\javac.exe" (
  set "JAVA_HOME_DETECTED=C:\Program Files\Java\jdk1.8.0_202"
  exit /b 0
)

exit /b 1

:resolve_maven
if exist "%PROJECT_DIR%mvnw.cmd" (
  set "MAVEN_CMD=%PROJECT_DIR%mvnw.cmd"
  exit /b 0
)

for /f "delims=" %%I in ('dir "%USERPROFILE%\.m2\wrapper\dists\apache-maven-*\*\bin\mvn.cmd" /b /s /o-n 2^>nul') do (
  set "MAVEN_CMD=%%I"
  exit /b 0
)

for /f "delims=" %%I in ('where mvn.cmd 2^>nul') do (
  set "MAVEN_CMD=%%I"
  exit /b 0
)

if exist "%USERPROFILE%\Downloads\apache-maven-3.9.16-bin\apache-maven-3.9.16\bin\mvn.cmd" (
  set "MAVEN_CMD=%USERPROFILE%\Downloads\apache-maven-3.9.16-bin\apache-maven-3.9.16\bin\mvn.cmd"
  exit /b 0
)

exit /b 1
