@echo off
cd /d "%~dp0"
 
docker info >nul 2>&1
if errorlevel 1 (
  echo Docker is not installed or not running.
  echo Install Docker Desktop from https://docs.docker.com/get-docker/ , start it, then double-click this file again.
  pause
  exit /b 1
)
 
if exist .env goto jwt
echo Creating .env with generated secrets...
powershell -NoProfile -Command "$p=-join((48..57)+(97..102)|Get-Random -Count 24|%%{[char]$_}); Set-Content -Encoding ascii .env ('POSTGRES_DB=franken_slides','POSTGRES_USER=slides_user',('POSTGRES_PASSWORD='+$p))"
 
:jwt
findstr /b /c:"JWT_SECRET=" .env >nul 2>&1
if not errorlevel 1 goto build
powershell -NoProfile -Command "$r=New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($r); Add-Content -Encoding ascii .env ('JWT_SECRET='+[Convert]::ToBase64String($r))"
echo Generated a JWT signing key.
 
:build
echo Building the backend (first run downloads dependencies, this can take a few minutes)...
docker run --rm -v "%~dp0backend:/app" -v franken-slides-m2:/root/.m2 -w /app maven:3.9-eclipse-temurin-21 mvn -B -q clean package -DskipTests
if errorlevel 1 (
  echo Backend build failed. See the messages above.
  pause
  exit /b 1
)
docker compose --env-file .env down --remove-orphans
if errorlevel 1 (
  echo Could not stop the existing FrankenSlides containers.
  pause
  exit /b 1
)
echo Starting Franken Slides...
docker compose --env-file .env up -d --build --remove-orphans
if errorlevel 1 (
  echo Start failed. See the messages above.
  pause
  exit /b 1
)
 
start "" http://localhost:5173
echo Done. The app keeps running in the background.
pause
 