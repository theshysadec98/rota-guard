#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

show_help() {
  cat <<'EOF'

Chạy ứng dụng:
  ./dev.sh

Lệnh phụ:
  ./dev.sh build
  ./dev.sh down
EOF
}

wait_for_mysql() {
  echo "Đang chờ MySQL khởi động..."
  until docker compose exec -T mysql mysqladmin ping -h localhost -uroot -pstaffmanager_root >/dev/null 2>&1; do
    sleep 1
  done
  echo "MySQL đã sẵn sàng."
}

start_project() {
  echo "Đang mở MySQL bằng Docker..."
  docker compose up -d mysql
  wait_for_mysql

  echo "Đang nạp dữ liệu mẫu..."
  docker compose exec -T mysql mysql -ustaffmanager -pstaffmanager staffmanager < "$ROOT/be/sql/schema.sql"

  echo "Đang mở ứng dụng desktop..."
  cd "$ROOT/be"
  export STAFF_MANAGER_DB_URL="jdbc:mysql://127.0.0.1:3307/staffmanager?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh"
  export STAFF_MANAGER_DB_USERNAME="staffmanager"
  export STAFF_MANAGER_DB_PASSWORD="staffmanager"
  ./gradlew run
}

case "${1:-start}" in
  start)
    start_project
    ;;
  build)
    echo "Đang format và build project..."
    cd "$ROOT/be"
    ./gradlew spotlessApply build
    echo "Build hoàn tất."
    ;;
  down)
    docker compose down
    echo "Đã tắt MySQL."
    ;;
  help|-h|--help)
    show_help
    ;;
  *)
    echo "Lệnh không hợp lệ: $1"
    echo ""
    show_help
    exit 1
    ;;
esac
