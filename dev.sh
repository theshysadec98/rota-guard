#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

wait_for_postgres() {
  echo "Waiting for postgres..."
  until docker compose exec -T postgres pg_isready -U rotaguard -d rotaguard >/dev/null 2>&1; do
    sleep 1
  done
}

case "${1:-run}" in
  up|postgres)
    docker compose up -d postgres
    wait_for_postgres
    echo "PostgreSQL: localhost:5432 (rotaguard/rotaguard)"
    ;;
  schema)
    docker compose up -d postgres
    wait_for_postgres
    docker compose exec -T postgres psql -U rotaguard -d rotaguard < "$ROOT/be/sql/schema.sql"
    echo "Applied schema: be/sql/schema.sql"
    ;;
  run|app)
    docker compose up -d postgres
    wait_for_postgres
    cd be
    ./gradlew run
    ;;
  build)
    cd be
    ./gradlew spotlessApply build
    ;;
  down)
    docker compose down
    echo "Stopped postgres."
    ;;
  db-reset)
    docker compose down -v
    echo "DB volume removed."
    ;;
  *)
    echo "Usage: $0 {run|app|up|postgres|schema|build|down|db-reset}"
    exit 1
    ;;
esac
