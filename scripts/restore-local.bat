@echo off
REM Restore locale del Gestionale Edile per Windows

setlocal enabledelayedexpansion

set BACKUP_FILE=%1

if "!BACKUP_FILE!"=="" (
    echo Uso: restore-local.bat path\to\backup.zip
    echo Esempio: restore-local.bat backups\gestionale-edile-20241028-1425.zip
    exit /b 1
)

if not exist "!BACKUP_FILE!" (
    echo File backup non trovato: !BACKUP_FILE!
    exit /b 1
)

setlocal enabledelayedexpansion
set /p confirm="Attenzione: questo ripristinera il backup e sovrascrivera i dati attuali. Continui? (S/N): "

if /i not "!confirm!"=="S" (
    echo Operazione annullata
    exit /b 0
)

echo Ripristino dal backup...
powershell -Command "Expand-Archive -Path '!BACKUP_FILE!' -DestinationPath '.' -Force"

if %errorlevel% equ 0 (
    echo Ripristino completato da: !BACKUP_FILE!
    echo Riavvia l'applicazione con: mvn spring-boot:run
) else (
    echo Errore durante il ripristino
    exit /b 1
)
