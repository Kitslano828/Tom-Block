@echo off
setlocal
cd /d "%~dp0.."
call gradlew.bat prepareLocalServer %*
if errorlevel 1 (
    echo Local server preparation failed. Paper was not started.
    exit /b 1
)
call "C:\Users\tomda\Desktop\26.2\launch-windows.bat"
