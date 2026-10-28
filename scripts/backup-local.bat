@echo off
REM Backup locale del Gestionale Edile per Windows

setlocal enabledelayedexpansion
for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c%%a%%b)
for /f "tokens=1-2 delims=/:" %%a in ('time /t') do (set mytime=%%a%%b)
set TIMESTAMP=!mydate!-!mytime!

set BACKUP_DIR=backups
set DB_DIR=db
set BACKUP_FILE=!BACKUP_DIR!\gestionale-edile-!TIMESTAMP!.zip

if not exist "!DB_DIR!" (
    echo Database non trovato: !DB_DIR!
    exit /b 1
)

if not exist "!BACKUP_DIR!" (
    mkdir "!BACKUP_DIR!"
)

echo Backup in corso...
powershell -Command "Compress-Archive -Path '!DB_DIR!\gestionale-edile.h2.db', 'src\main\resources\static', 'pom.xml' -DestinationPath '!BACKUP_FILE!' -Force"

if %errorlevel% equ 0 (
    echo Backup completato: !BACKUP_FILE!
) else (
    echo Errore durante il backup
    exit /b 1
)
