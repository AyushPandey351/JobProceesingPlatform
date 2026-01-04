@echo off
SETLOCAL EnableDelayedExpansion

echo ==========================================
echo   Scalable Job Processing Platform
echo ==========================================

:: 1. Check if Docker is running
echo [1/3] Checking Docker...
docker ps >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker is not running. Please start Docker Desktop and try again.
    pause
    exit /b
)

:: 2. Start Infrastructure (PostgreSQL & Redis)
echo [2/3] Starting Infrastructure (PostgreSQL, Redis)...
docker-compose up -d
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to start Docker containers.
    pause
    exit /b
)

:: 3. Start Spring Boot Application
echo [3/3] Starting Application...
echo Dashboard will be available at: http://localhost:8081
echo.
.\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run

pause
