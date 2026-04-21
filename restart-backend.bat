@echo off
setlocal

cd /d "%~dp0"

echo [Camera Shop] Restarting backend and database...

echo [1/2] Stopping services...
docker compose stop database backend
if errorlevel 1 goto :error

echo [2/2] Starting services...
docker compose up -d --build database backend
if errorlevel 1 goto :error

echo.
echo [Camera Shop] Current service status:
docker compose ps
if errorlevel 1 goto :error

echo.
echo [Camera Shop] Backend should be available at: http://localhost:8080
exit /b 0

:error
echo.
echo [Camera Shop] Failed to restart backend stack.
exit /b 1
