@echo off
chcp 65001 >nul
setlocal
title 黑马点评 - 初始化数据库

set MYSQL_BIN=D:\DevTools\mysql-8.0.45-winx64\bin
set DB_USER=root
set DB_PASS=123456

cd /d "%~dp0"

echo ==================================================
echo    初始化 hmdp 数据库（会删除同名库，请谨慎）
echo ==================================================
echo.
set /p confirm=确认继续？(Y/N):
if /i not "%confirm%"=="Y" exit /b 0

echo [1/2] 建库建表 ...
"%MYSQL_BIN%\mysql.exe" -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 < sql\schema.sql
if errorlevel 1 (
    echo [错误] 建表失败
    pause
    exit /b 1
)

echo [2/2] 导入演示数据 ...
"%MYSQL_BIN%\mysql.exe" -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 < sql\data.sql
if errorlevel 1 (
    echo [错误] 导入数据失败
    pause
    exit /b 1
)

echo.
echo 完成：40 家店铺 / 20 个用户 / 40 张券 / 30 条笔记
pause
