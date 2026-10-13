#!/usr/bin/env bash
set -euo pipefail

BACKUP_FILE="${1:-}"
CONTAINER_NAME="${2:-gatenest-prod-db}"
DB_NAME="${3:-gatenest_db}"
DB_USER="${4:-gatenest_user}"
DB_PASSWORD="${5:-gatenest_secure_pass_2026}"

if [[ -z "$BACKUP_FILE" || ! -f "$BACKUP_FILE" ]]; then
  echo "[ERROR] Please specify a valid backup file path: ./scripts/restore-database.sh <file.sql>"
  exit 1
fi

echo "WARNING: Restoring database will overwrite records in $DB_NAME."
echo "Restoring ${BACKUP_FILE} into ${CONTAINER_NAME}..."

docker exec -i "$CONTAINER_NAME" mysql -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$BACKUP_FILE"
echo "[SUCCESS] Database restoration complete!"
