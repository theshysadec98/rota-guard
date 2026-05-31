#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

case "${1:-up}" in
  up)
    docker compose up --build
    ;;
  postgres)
    docker compose up -d postgres
    echo "PostgreSQL: localhost:5432 (rotaguard/rotaguard)"
    ;;
  core|be)
    docker compose up -d postgres
    echo "Waiting for postgres..."
    until docker compose exec postgres pg_isready -U rotaguard -d rotaguard >/dev/null 2>&1; do sleep 1; done
    cd be
    ./gradlew bootRun
    ;;
  docker)
    docker compose up --build -d
    echo "FE:  http://localhost:5173"
    echo "BE:  http://localhost:8080"
    ;;
  down)
    docker compose down
    echo "Stopped all compose services (postgres, be, fe)."
    ;;
  health)
    curl -sf http://localhost:8080/api/v1/health | python3 -m json.tool
    ;;
  db-repair)
    docker compose up -d postgres
    echo "Waiting for postgres..."
    until docker compose exec postgres pg_isready -U rotaguard -d rotaguard >/dev/null 2>&1; do sleep 1; done
    docker run --rm --network host \
      -v "$ROOT/be/src/main/resources/db/migration:/flyway/sql" \
      flyway/flyway:10-alpine \
      -locations=filesystem:/flyway/sql \
      -url=jdbc:postgresql://127.0.0.1:5432/rotaguard \
      -user=rotaguard -password=rotaguard \
      repair
    echo "Flyway repair done. Run: ./dev.sh up"
    ;;
  db-reset)
    docker compose down -v
    echo "DB volume removed. Run: ./dev.sh up"
    ;;
  build)
    cd be
    ./gradlew spotlessApply build
    ;;
  fe)
    docker compose up -d postgres be
    cd fe && npm install && npm run dev
    ;;
  *)
    echo "Usage: $0 {up|down|postgres|be|core|docker|health|build|fe|db-repair|db-reset}"
    exit 1
    ;;
esac
