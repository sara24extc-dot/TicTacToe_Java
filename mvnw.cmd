@echo off
set "SCRIPT_DIR=%~dp0"
if "%MAVEN_OPTS%"=="" (
    set "MAVEN_OPTS=-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true -Djavax.net.ssl.trustStoreType=WINDOWS-ROOT"
)
"%SCRIPT_DIR%tools\apache-maven-3.9.6\bin\mvn.cmd" %*
