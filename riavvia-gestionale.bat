@echo off
setlocal EnableExtensions

set "PROJECT_DIR=%~dp0"

call "%PROJECT_DIR%stop-gestionale.bat"
call "%PROJECT_DIR%avvia-gestionale.bat"
exit /b %ERRORLEVEL%
