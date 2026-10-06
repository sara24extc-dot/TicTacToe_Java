@echo off
title Tic-Tac-Toe Game
cd /d "%~dp0"

echo ===================================================
echo           Starting Tic-Tac-Toe Game...
echo ===================================================

if "%MAVEN_OPTS%"=="" (
    set "MAVEN_OPTS=-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true -Djavax.net.ssl.trustStoreType=WINDOWS-ROOT"
)

call "%~dp0mvnw.cmd" javafx:run

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Application exited with an error.
    pause
)
