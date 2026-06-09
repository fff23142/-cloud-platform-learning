@echo off
echo 停止所有服务...

echo [1/2] 停止 Docker 容器...
docker stop learn-mysql learn-redis learn-nacos learn-seata 2>nul
echo   已停止

echo [2/2] 停止 PowerJob Server...
taskkill /FI "WINDOWTITLE eq PowerJob-Server*" /F 2>nul
echo   已停止

echo 全部已停止
pause
