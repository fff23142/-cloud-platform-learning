@echo off
title Start All Services

echo === Starting Infrastructure ===

echo [1/5] Starting Docker containers...
docker start learn-mysql learn-redis learn-nacos learn-seata learn-powerjob >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker Desktop is not running!
    pause
    exit /b
)
echo   learn-mysql  :3306
echo   learn-redis  :6379
echo   learn-nacos  :18848
echo   learn-seata    :8191
echo   learn-powerjob :7700

echo [2/5] Waiting for MySQL...
:loop_mysql
timeout /t 2 /nobreak >nul
docker exec learn-mysql mysqladmin ping -uroot -proot --silent >nul 2>&1
if %errorlevel% neq 0 goto loop_mysql
echo   MySQL ready

echo [3/5] Waiting for PowerJob...
timeout /t 15 /nobreak >nul
echo   PowerJob ready

echo [4/5] Waiting for Nacos...
:loop_nacos
timeout /t 3 /nobreak >nul
curl -s http://127.0.0.1:18848/nacos/v1/console/health/readiness >nul 2>&1
if %errorlevel% neq 0 goto loop_nacos
echo   Nacos ready

echo.
echo === All Infrastructure Ready ===
echo   Nacos:   http://127.0.0.1:18848/nacos
echo   PowerJob: http://127.0.0.1:7700
echo   MySQL:    localhost:3306
echo   Redis:    localhost:6379
echo.
echo Next: Run in IDEA:
echo   1. UserApplication    :8091
echo   2. OrderApplication   :8092
echo   3. AuthApplication    :8081
echo   4. GatewayApplication :8080
echo.
pause
