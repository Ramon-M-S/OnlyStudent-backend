#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"

export POSTGRES_DB="${POSTGRES_DB:-onlystudent}"
export POSTGRES_USER="${POSTGRES_USER:-onlystudent}"
export POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-onlystudent-local-dev}"
export DB_URL="jdbc:postgresql://localhost:55432/${POSTGRES_DB}"
export DB_USERNAME="${POSTGRES_USER}"
export DB_PASSWORD="${POSTGRES_PASSWORD}"
export PORT="${PORT:-8082}"
export CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS:-http://localhost:5173}"
export SPRING_PROFILES_ACTIVE=dev

if [ -z "${JWT_SECRET:-}" ]; then
    export JWT_SECRET="$(openssl rand -base64 32)"
    echo "→ JWT_SECRET gerado"
fi

docker compose -f compose.yaml -f - up -d db <<'OVERRIDE'
services:
  db:
    ports: !override
      - "127.0.0.1:55432:5432"
OVERRIDE


echo "→ Aguardando Postgres..."
for i in 1 2 3 4 5 6 7 8 9 10; do
    docker compose exec -T db pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB" >/dev/null 2>&1 && break
    sleep 1
done
echo "→ Postgres OK"

# 4. Roda a aplicação
echo "→ Iniciando backend na porta 8082..."
./mvnw spring-boot:run