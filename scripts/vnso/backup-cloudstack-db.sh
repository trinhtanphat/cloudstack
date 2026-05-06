#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
if [[ -f "${ROOT_DIR}/.env" ]]; then
  set -a
  # shellcheck source=/dev/null
  source "${ROOT_DIR}/.env"
  set +a
fi

DB_CONTAINER="${DB_CONTAINER:-xiaozhi-esp32-server-db}"
DB_NAME="${DB_NAME:-cloud}"
DB_USER="${DB_USER:-cloud}"
DB_PASSWORD_EFFECTIVE="${DB_PASSWORD:-${DB_PASS:-cloud}}"
DB_BACKUP_DIR="${DB_BACKUP_DIR:-/var/backups/cloudstack}"
RETENTION_DAYS="${DB_BACKUP_RETENTION_DAYS:-14}"
TIMESTAMP="$(date -u +%Y%m%dT%H%M%SZ)"
BACKUP_FILE="${DB_BACKUP_DIR}/${DB_NAME}-${TIMESTAMP}.sql.gz"

mkdir -p "${DB_BACKUP_DIR}"

docker ps --format '{{.Names}}' | grep -qx "${DB_CONTAINER}" || {
  echo "[backup][ERROR] DB container ${DB_CONTAINER} is not running" >&2
  exit 1
}

echo "[backup] writing ${BACKUP_FILE}"
docker exec "${DB_CONTAINER}" mysqldump \
  "--user=${DB_USER}" \
  "--password=${DB_PASSWORD_EFFECTIVE}" \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  "${DB_NAME}" | gzip -c > "${BACKUP_FILE}"

chmod 600 "${BACKUP_FILE}"
find "${DB_BACKUP_DIR}" -type f -name "${DB_NAME}-*.sql.gz" -mtime "+${RETENTION_DAYS}" -delete

echo "[backup] OK: ${BACKUP_FILE}"
