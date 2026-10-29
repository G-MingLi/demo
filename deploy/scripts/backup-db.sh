#!/usr/bin/env bash
# 2026/10/29 gongml [需求 待补充] MySQL 每日备份
set -euo pipefail
umask 077

APP_DIR="/opt/demo"
BACKUP_DIR="$APP_DIR/backups"
STAMP="$(date +%F_%H%M)"
DB_NAME="${DB_NAME:-demo}"

if [ ! -f "$APP_DIR/.env" ]; then
  echo "missing $APP_DIR/.env"
  exit 1
fi

set -a
# shellcheck disable=SC1091
. "$APP_DIR/.env"
set +a

mkdir -p "$BACKUP_DIR"
MYSQL_PWD="${SPRING_DATASOURCE_PASSWORD:-}" mysqldump \
  -h 127.0.0.1 \
  -u "${SPRING_DATASOURCE_USERNAME:-demo}" \
  --single-transaction --routines --triggers --events \
  "$DB_NAME" | gzip > "$BACKUP_DIR/db_${STAMP}.sql.gz"

find "$BACKUP_DIR" -name 'db_*.sql.gz' -mtime +30 -delete
echo "backup ok: $BACKUP_DIR/db_${STAMP}.sql.gz"
