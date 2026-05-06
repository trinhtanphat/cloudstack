#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
if [[ -f "${ROOT_DIR}/.env" ]]; then
  set -a
  # shellcheck source=/dev/null
  source "${ROOT_DIR}/.env"
  set +a
fi

RESTORE_FILE="${1:-${RESTORE_FILE:-}}"
DB_CONTAINER="${DB_CONTAINER:-xiaozhi-esp32-server-db}"
DB_NAME="${DB_NAME:-cloud}"
DB_USER="${DB_USER:-cloud}"
DB_PASSWORD_EFFECTIVE="${DB_PASSWORD:-${DB_PASS:-cloud}}"
COMPOSE_FILE="${COMPOSE_FILE:-${ROOT_DIR}/docker-compose.prod.yml}"
MGMT_SERVICE="${MGMT_SERVICE:-cloudstack-mgmt}"

[[ -n "${RESTORE_FILE}" ]] || {
  echo "Usage: $0 /path/to/cloudstack-backup.sql[.gz]" >&2
  exit 2
}
[[ -f "${RESTORE_FILE}" ]] || {
  echo "[restore][ERROR] file not found: ${RESTORE_FILE}" >&2
  exit 1
}

docker ps --format '{{.Names}}' | grep -qx "${DB_CONTAINER}" || {
  echo "[restore][ERROR] DB container ${DB_CONTAINER} is not running" >&2
  exit 1
}

if [[ "${FORCE_RESTORE:-0}" != "1" ]]; then
  echo "[restore] This will replace database '${DB_NAME}' in container '${DB_CONTAINER}'."
  read -r -p "Type RESTORE to continue: " confirm
  [[ "${confirm}" == "RESTORE" ]] || {
    echo "[restore] aborted"
    exit 1
  }
fi

if [[ -f "${COMPOSE_FILE}" ]]; then
  docker compose -f "${COMPOSE_FILE}" stop "${MGMT_SERVICE}" >/dev/null || true
fi

echo "[restore] recreating database ${DB_NAME}"
docker exec -i "${DB_CONTAINER}" mysql "--user=${DB_USER}" "--password=${DB_PASSWORD_EFFECTIVE}" \
  -e "DROP DATABASE IF EXISTS \`${DB_NAME}\`; CREATE DATABASE \`${DB_NAME}\`;"

echo "[restore] importing ${RESTORE_FILE}"
if [[ "${RESTORE_FILE}" == *.gz ]]; then
  gzip -dc "${RESTORE_FILE}" | docker exec -i "${DB_CONTAINER}" mysql "--user=${DB_USER}" "--password=${DB_PASSWORD_EFFECTIVE}" "${DB_NAME}"
else
  docker exec -i "${DB_CONTAINER}" mysql "--user=${DB_USER}" "--password=${DB_PASSWORD_EFFECTIVE}" "${DB_NAME}" < "${RESTORE_FILE}"
fi

if [[ -f "${COMPOSE_FILE}" ]]; then
  docker compose -f "${COMPOSE_FILE}" up -d "${MGMT_SERVICE}" >/dev/null
fi

echo "[restore] OK"
