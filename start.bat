@echo off
chcp 65001 >nul
setlocal
title 黑马点评 - 启动器

set REDIS_HOME=D:\DevTools\redis
set APP_PORT=8081

echo ==================================================
echo    黑马点评 hmdp  ·  Spring Boot 3 + Redis 实战
echo ==================================================
echo.

echo [1/4] 检查 Redis 127.0.0.1:6379 ...
"%REDIS_HOME%\redis-cli.exe" ping >nul 2>&1
if errorlevel 1 (
    if exist "%REDIS_HOME%\redis-server.exe" (
        echo       未检测到 Redis，正在启动 ...
        start "Redis" /min "%REDIS_HOME%\redis-server.exe" "%REDIS_HOME%\redis.windows.conf"
        timeout /t 3 >nul
        echo       Redis 已启动
    ) else (
        echo       [错误] 未找到 %REDIS_HOME%\redis-server.exe
        echo       请修改本脚本中的 REDIS_HOME 变量
        pause
        exit /b 1
    )
) else (
    echo       Redis 运行中
)

echo [2/4] 检查 MySQL 127.0.0.1:3306 ...
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul
if errorlevel 1 (
    echo       [错误] MySQL 未启动，请先启动 MySQL 服务
    pause
    exit /b 1
)
echo       MySQL 运行中

echo [3/4] 检查 jar 包 ...
cd /d "%~dp0"
if not exist "target\hmdp-1.0.0.jar" (
    echo       未找到 jar，正在打包（首次较慢，请耐心等待）...
    call mvn -B -DskipTests package
    if errorlevel 1 (
        echo       [错误] 打包失败
        pause
        exit /b 1
    )
)
echo       jar 就绪

echo [4/4] 启动应用 ...
echo.
echo       访问地址: http://localhost:%APP_PORT%
echo       演示账号: 13800000001  验证码会自动填入
echo.
start "" "http://localhost:%APP_PORT%"
java -jar target\hmdp-1.0.0.jar --server.port=%APP_PORT%

echo.
echo 应用已退出
pause
