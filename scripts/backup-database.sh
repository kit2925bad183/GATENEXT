#!/usr/bin/env bash
set -euo pipefail

CONTAINER_NAME="${1:-gatenest-prod-db}"
DB_NAME="${2:-gatenest_db}"
DB_USER="${3:-gatenest_user}"
DB_PASSWORD="${4:-gatenest_secure_pass_2026}"
BACKUP_DIR="${5:-./backups}"

mkdir -p "$BACKUP_DIR"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/gatenest_backup_${TIMESTAMP}.sql"

echo "Backing up GateNest MySQL database to ${BACKUP_FILE}..."
docker exec "$CONTAINER_NAME" mysqldump -u "$DB_USER" -p"$DB_PASSWORD" --single-transaction --routines --triggers "$DB_NAME" > "$BACKUP_FILE"

echo "[SUCCESS] Backup created at ${BACKUP_FILE} ($(stat -c%s "${BACKUP_FILE}" 2>/dev/null || wc -c < "${BACKUP_FILE}") bytes)"
