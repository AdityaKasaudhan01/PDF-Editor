@echo off
setlocal

set "MAVEN_DIR=C:\tools\maven\apache-maven-3.9.6"
set "MAVEN_BIN=%MAVEN_DIR%\bin\mvn.cmd"

if not exist "%MAVEN_BIN%" (
  echo Local Maven wrapper not found at %MAVEN_BIN%
  pause
  exit /b 1
)

"%MAVEN_BIN%" %*
