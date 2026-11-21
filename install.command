#!/bin/bash
# Double-click me on macOS (or run ./install.command on Linux).
cd "$(dirname "$0")" || exit 1
pause() { read -n 1 -s -r -p "$1"; echo; }
 
if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is not installed. Get it at https://docs.docker.com/get-docker/ then double-click this file again."
  pause "Press any key to close..."; exit 1
fi
if ! docker info >/dev/null 2>&1; then
  echo "Docker is installed but not running. Start Docker Desktop, wait until it is ready, then try again."
  pause "Press any key to close..."; exit 1
fi
 
# .env (same names as set-up.sh)
if [ ! -f .env ]; then
  echo "Creating .env with generated secrets..."
  umask 077
  cat > .env <<ENV
POSTGRES_DB=franken_slides
POSTGRES_USER=slides_user
POSTGRES_PASSWORD=$(openssl rand -hex 16)
ENV
fi
if ! grep -q '^JWT_SECRET=[^[:space:]]' .env; then
  printf '\nJWT_SECRET=%s\n' "$(openssl rand -base64 32)" >> .env
  echo "Generated a JWT signing key."
fi
 
# Build the Spring Boot JAR inside Docker, so Java does not need to be installed
echo "Building the backend (first run downloads dependencies, this can take a few minutes)..."
docker run --rm \
  -v "$PWD/backend":/app -v franken-slides-m2:/root/.m2 -w /app \
  maven:3.9-eclipse-temurin-21 mvn -B -q clean package -DskipTests \
  || { pause "Backend build failed. Press any key to close..."; exit 1; }
 
echo "Starting Franken Slides..."
docker compose --env-file .env up -d --build --remove-orphans \
  || { pause "Start failed. Press any key to close..."; exit 1; }
 
URL="http://localhost:5173"
echo "Ready: $URL"
(open "$URL" || xdg-open "$URL") >/dev/null 2>&1
pause "Done. Press any key to close this window (the app keeps running)..."
 