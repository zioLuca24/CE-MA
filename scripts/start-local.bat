@echo off
REM Script di avvio del Gestionale Edile per Windows

echo ============================================
echo Gestionale Edile - Avvio in locale
echo ============================================
echo.

if not exist pom.xml (
    echo Errore: pom.xml non trovato
    echo Esegui questo script dalla directory root del progetto
    pause
    exit /b 1
)

echo Cleaning e building...
call mvn clean package

if %errorlevel% neq 0 (
    echo Errore durante la build
    pause
    exit /b 1
)

echo.
echo ============================================
echo Build completato! Avvio dell'applicazione...
echo ============================================
echo.
echo URL: http://localhost:8080/api/
echo.

java -jar target\gestionale-edile-1.0.0.jar

echo.
echo Applicazione interrotta
pause
