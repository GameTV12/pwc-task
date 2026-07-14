@echo off
setlocal
cd /d "%~dp0"

rem ---- Docker Desktop must already be installed ----
where docker >NUL 2>&1
if errorlevel 1 (
    echo.
    echo Docker Desktop is required but the 'docker' command was not found.
    echo Install Docker Desktop from https://www.docker.com/products/docker-desktop/
    echo then run this script again.
    echo.
    pause
    exit /b 1
)

echo Building and starting the pwc-task container...
echo (the first run downloads base images and dependencies - this can take a few minutes)
docker compose up -d --build
if errorlevel 1 (
    echo.
    echo 'docker compose up' failed. Is Docker Desktop running?
    echo.
    pause
    exit /b 1
)

echo Waiting for the app to answer on http://localhost:8080 ...
set /a tries=0

:wait
set /a tries+=1
curl -s -f -o NUL http://localhost:8080/countries
if not errorlevel 1 goto up
if %tries% geq 60 (
    echo.
    echo The app did not respond within 2 minutes. Check the container logs with:
    echo   docker logs pwc-task
    echo.
    pause
    exit /b 1
)
timeout /t 2 /nobreak >NUL
goto wait

:up
echo App is up - opening http://localhost:8080 in your browser.
start "" http://localhost:8080
echo.
echo Stop or restart the container from Docker Desktop (container "pwc-task").
pause
