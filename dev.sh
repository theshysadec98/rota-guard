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
  health)
    curl -sf http://localhost:8080/api/v1/health | python3 -m json.tool
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
    echo "Usage: $0 {up|postgres|be|core|docker|health|build|fe}"
    exit 1
    ;;
esac
