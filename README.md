# Franken Slides
 
> Download, double-click, done.
 
## One-click install
 
**Prerequisite:** [Docker Desktop](https://docs.docker.com/get-docker/) installed and running.
 
1. **[Download the ZIP](https://github.com/thegesse/franken-slides/archive/refs/heads/master.zip)** and unzip it
2. **Double-click the installer for your system:**

| System  | Double-click          |
| ------- | --------------------- |
| Windows | `install.bat`         |
| macOS   | `install.command`     |
 
3. Your browser opens on **http://localhost:5173** when it's ready. First run takes a few minutes while Docker builds.
The installer creates the `.env` with random secrets for you and starts everything. Running it again is safe.
 
> **macOS:** if it says the file can't be opened, right-click `install.command` → **Open** → **Open**. If macOS refuses to run it because the ZIP stripped its permissions, use the terminal install below instead.
 
## Terminal install (macOS / Linux / WSL)
 
```bash
git clone https://github.com/thegesse/franken-slides.git
cd franken-slides
./install.command
```
 
| Service  | URL                     |
| -------- | ----------------------- |
| App      | http://localhost:5173   |
| API      | http://localhost:8080   |
 
---
 
## What the installer does
 
1. Checks that Docker is installed and running
2. Creates a `.env` file with a generated database password and JWT signing key if you don't already have one
3. Builds the Spring Boot backend JAR inside a Maven container (**no Java install needed**)
4. Builds and starts the database, backend and frontend with Docker Compose
Re-running it is safe: an existing `.env` is never overwritten.
 
---
 
## Manual install
 
If you prefer to do it by hand:
 
```bash
git clone https://github.com/thegesse/franken-slides.git
cd franken-slides
```
 
Create a `.env` file at the root of the project:
 
```env
# Database
POSTGRES_DB=franken_slides
POSTGRES_USER=slides_user
POSTGRES_PASSWORD=change-me
 
# Auth (generate with: openssl rand -base64 48)
JWT_SECRET=change-me-to-a-long-random-string
 
# Optional (defaults shown)
# JWT_ISSUER=franken-slides
# JWT_ACCESS_TOKEN_LIFETIME=PT5M
# JWT_REFRESH_TOKEN_LIFETIME=P30D
# JWT_REFRESH_COOKIE_SECURE=false
# VITE_API_BASE_URL=http://localhost:8080
```
 
Build the backend JAR (needs Java 21), then start everything:
 
```bash
cd backend && ./mvnw clean package -DskipTests && cd ..
docker compose up --build
```
 
`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` and `JWT_SECRET` are required; Compose will stop with an error message if one is missing.
 
---
 
## Everyday commands
 
```bash
docker compose up -d        # start in the background
docker compose logs -f      # follow the logs
docker compose down         # stop everything
docker compose down -v      # stop and DELETE the database
```
 
Uploaded files are stored in the `./uploads` folder.
 
## Architecture
 
```
frontend  (port 5173)  ──►  backend  (port 8080)  ──►  PostgreSQL 17
```
 
## Troubleshooting
 
- **Port already in use**: something else is using 5173, 8080 or the database. Stop it, or change the left side of the `ports:` mapping in `docker-compose.yml`.
- **`Set JWT_SECRET in .env` error**: your `.env` is missing or incomplete. Re-run the installer or see *Manual install*.
- **`permission denied`**: run `chmod +x install.command` first.
- **Starting from scratch**: `docker compose down -v` then run the installer again.
