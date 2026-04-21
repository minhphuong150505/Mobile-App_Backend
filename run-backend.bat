@echo off
setlocal

cd /d "%~dp0"

echo [Camera Shop] Starting database and backend...

docker compose up -d --build database backend
if errorlevel 1 goto :error

echo.
echo [Camera Shop] Current backend status:
docker compose ps backend
if errorlevel 1 goto :error

echo.
echo [Camera Shop] Backend should be available at: http://localhost:8080
echo [Camera Shop] Use this command to view logs:
echo docker compose logs -f backend

exit /b 0

:error
echo.
echo [Camera Shop] Failed to start backend stack.
exit /b 1
