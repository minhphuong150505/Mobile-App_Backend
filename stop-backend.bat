@echo off
setlocal

cd /d "%~dp0"

echo [Camera Shop] Stopping backend and database...
docker compose stop database backend
if errorlevel 1 goto :error

echo.
echo [Camera Shop] Current service status:
docker compose ps
if errorlevel 1 goto :error

exit /b 0

:error
echo.
echo [Camera Shop] Failed to stop backend stack.
exit /b 1
