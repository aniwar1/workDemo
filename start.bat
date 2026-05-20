@echo off
title KG Platform - Starting

echo ========================================
echo   KG Platform 一键启动
echo ========================================
echo.

:: 启动后端
echo [1/2] Starting Backend (Spring Boot on port 8080)...
start "Backend" cmd /k "cd /d %~dp0 && mvnw.cmd spring-boot:run"

:: 等待后端启动
echo Waiting for backend to start...
timeout /t 15 /nobreak >nul

:: 启动前端
echo [2/2] Starting Frontend (Vite on port 5173)...
cd /d %~dp0frontend
if exist "node_modules" (
    start "Frontend" cmd /k "npm run dev"
) else (
    echo Installing frontend dependencies...
    call npm install
    start "Frontend" cmd /k "npm run dev"
)

echo.
echo ========================================
echo   All services started!
echo   Backend: http://localhost:8080
echo   Frontend: http://localhost:5173
echo   API Docs: http://localhost:8080/doc.html
echo ========================================
echo.
echo Default login: admin / admin123
echo.
pause
