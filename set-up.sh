#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"

cd "$SCRIPT_DIR"

cat <<'MESSAGE'
This script prepares Frankenslides for local use.

It can create a .env file, build the Spring Boot JAR, and start the Docker Compose services.
MESSAGE

read -r -p "Do you wish to proceed? (y/n) " confirm
case "${confirm,,}" in
    y|yes) ;;
    n|no) echo "Setup aborted"; exit 0 ;;
    *) echo "Please answer y or n." >&2; exit 1 ;;
esac

if ! command -v java >/dev/null 2>&1; then
    echo "Java is required to build the backend JAR. Install Java 21 and run this script again." >&2
    exit 1
fi
if ! docker compose version >/dev/null 2>&1; then
    echo "Docker Compose is required. Install Docker with the Compose plugin and run this script again." >&2
    exit 1
fi
if [[ ! -f "$COMPOSE_FILE" ]]; then
    echo "Could not find $COMPOSE_FILE" >&2
    exit 1
fi

write_env_file() {
    local env_user env_password

    read -r -p "PostgreSQL username [slides_user]: " env_user
    env_user=${env_user:-slides_user}
    if [[ ! "$env_user" =~ ^[A-Za-z0-9_]+$ ]]; then
        echo "Use only letters, numbers, and underscores in the PostgreSQL username." >&2
        return 1
    fi

    read -r -s -p "PostgreSQL password (letters/numbers and . _ @ % + = : ! -): " env_password
    printf '\n'
    if [[ ! "$env_password" =~ ^[A-Za-z0-9._@%+=:!-]+$ ]]; then
        echo "Password is empty or contains characters this .env setup does not accept." >&2
        return 1
    fi

    umask 077
    cat > "$ENV_FILE" <<EOF_ENV
POSTGRES_DB=franken_slides
POSTGRES_USER=$env_user
POSTGRES_PASSWORD=$env_password
EOF_ENV
    chmod 600 "$ENV_FILE"
    echo "Created $ENV_FILE with owner-only permissions."
}

if [[ -e "$ENV_FILE" ]]; then
    echo "An existing .env file was found; it will not be overwritten automatically."
    read -r -p "Replace it? This can desynchronize credentials from an existing PostgreSQL volume. (y/N) " replace_env
    case "${replace_env,,}" in
        y|yes) write_env_file ;;
        *) echo "Keeping the existing .env file." ;;
    esac
else
    echo "Step 1: creating .env"
    write_env_file
fi

if [[ ! -f "$ENV_FILE" ]]; then
    echo "No .env file is available; cannot start the application." >&2
    exit 1
fi

echo "Step 2: building the Spring Boot JAR"
(
    cd "$SCRIPT_DIR/backend"
    bash ./mvnw clean package -DskipTests
)

echo "Step 3: starting Docker Compose"
docker compose --env-file "$ENV_FILE" --project-directory "$SCRIPT_DIR" -f "$COMPOSE_FILE" up --build -d

echo "Frankenslides is starting. Open the frontend on port 5173."
echo "To stop it, run: docker compose --env-file $ENV_FILE --project-directory $SCRIPT_DIR -f $COMPOSE_FILE down"
