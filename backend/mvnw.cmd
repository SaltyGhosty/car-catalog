@REM ----------------------------------------------------------------------------
@REM Maven Wrapper (only-script) per Windows: scarica Maven se manca e lo esegue.
@REM ----------------------------------------------------------------------------
@echo off
setlocal
set "BASEDIR=%~dp0"
for /f "usebackq tokens=1,* delims==" %%a in ("%BASEDIR%.mvn\wrapper\maven-wrapper.properties") do (
  if "%%a"=="distributionUrl" set "DIST_URL=%%b"
)
for %%f in ("%DIST_URL%") do set "DIST_NAME=%%~nf"
if "%MAVEN_USER_HOME%"=="" set "MAVEN_USER_HOME=%USERPROFILE%\.m2"
set "MVN_HOME=%MAVEN_USER_HOME%\wrapper\dists\%DIST_NAME%"

if not exist "%MVN_HOME%\bin\mvn.cmd" (
  echo [mvnw] Scarico %DIST_URL%
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $t=Join-Path $env:TEMP ([guid]::NewGuid().ToString()); New-Item -ItemType Directory -Path $t | Out-Null; Invoke-WebRequest -UseBasicParsing -Uri '%DIST_URL%' -OutFile ($t+'\maven.zip'); Expand-Archive -Path ($t+'\maven.zip') -DestinationPath $t; New-Item -ItemType Directory -Force -Path (Split-Path '%MVN_HOME%') | Out-Null; Move-Item -Path (Get-ChildItem -Path $t -Directory | Select-Object -First 1).FullName -Destination '%MVN_HOME%'; Remove-Item -Recurse -Force $t"
  if errorlevel 1 exit /b 1
)

set "MAVEN_PROJECTBASEDIR=%BASEDIR:~0,-1%"
"%MVN_HOME%\bin\mvn.cmd" %*
