#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
if [[ -f "${ROOT_DIR}/.env" ]]; then
  set -a
  # shellcheck source=/dev/null
  source "${ROOT_DIR}/.env"
  set +a
fi

API_URL="${CLOUDSTACK_API_URL:-http://${CLOUDSTACK_MGMT_IP:-127.0.0.1}:28080/client/api/}"
ADMIN_USER="${CLOUDSTACK_ADMIN_USER:-}"
ADMIN_PASS="${CLOUDSTACK_ADMIN_PASS:-}"
ADMIN_DOMAIN="${CLOUDSTACK_ADMIN_DOMAIN:-}"
DB_CONTAINER="${DB_CONTAINER:-xiaozhi-esp32-server-db}"
DB_NAME="${DB_NAME:-cloud}"
DB_USER="${DB_USER:-cloud}"
DB_PASSWORD_EFFECTIVE="${DB_PASSWORD:-${DB_PASS:-cloud}}"

fail() {
  echo "[smoke][ERROR] $*" >&2
  exit 1
}

require_bin() {
  command -v "$1" >/dev/null 2>&1 || fail "missing required command: $1"
}

api_get() {
  local command_name="$1"
  curl -fsS --get "${API_URL}" \
    --data-urlencode "command=${command_name}" \
    --data-urlencode "sessionkey=${SESSION}" \
    --data-urlencode "response=json"
}

require_bin curl
require_bin jq
require_bin docker

[[ -n "${ADMIN_USER}" ]] || fail "CLOUDSTACK_ADMIN_USER is not set; copy .env.example to .env and fill it"
[[ -n "${ADMIN_PASS}" ]] || fail "CLOUDSTACK_ADMIN_PASS is not set; copy .env.example to .env and fill it"

echo "[smoke] api=${API_URL}"

code=$(curl -sS -o /dev/null -w '%{http_code}' --max-time 8 "${API_URL%/}/?command=listCapabilities&response=json" 2>/dev/null || echo 000)
[[ "${code}" =~ ^(200|401|405)$ ]] || fail "management API health returned HTTP ${code}"
echo "[smoke] management API reachable (${code})"

if docker ps --format '{{.Names}}' | grep -qx "${DB_CONTAINER}"; then
  docker exec "${DB_CONTAINER}" mysql "--user=${DB_USER}" "--password=${DB_PASSWORD_EFFECTIVE}" -D "${DB_NAME}" -e "SELECT 1" >/dev/null
  echo "[smoke] database connectivity OK"
else
  echo "[smoke][WARN] DB container ${DB_CONTAINER} not running locally; skipping DB check"
fi

login_json=$(curl -fsS -X POST "${API_URL}" \
  --data-urlencode "command=login" \
  --data-urlencode "username=${ADMIN_USER}" \
  --data-urlencode "password=${ADMIN_PASS}" \
  --data-urlencode "domain=${ADMIN_DOMAIN}" \
  --data-urlencode "response=json")
SESSION=$(jq -r '.loginresponse.sessionkey // empty' <<<"${login_json}")
[[ -n "${SESSION}" ]] || fail "admin login failed"
echo "[smoke] admin login OK"

zones_json=$(api_get listZones)
zone_count=$(jq -r '.listzonesresponse.count // (.listzonesresponse.zone // [] | length)' <<<"${zones_json}")
echo "[smoke] zones=${zone_count}"

hosts_json=$(api_get listHosts)
host_count=$(jq -r '.listhostsresponse.count // (.listhostsresponse.host // [] | length)' <<<"${hosts_json}")
hosts_not_up=$(jq -r '[.listhostsresponse.host[]? | select(.state != "Up")] | length' <<<"${hosts_json}")
echo "[smoke] hosts=${host_count}, hosts_not_up=${hosts_not_up}"

storage_json=$(api_get listStoragePools)
storage_count=$(jq -r '.liststoragepoolsresponse.count // (.liststoragepoolsresponse.storagepool // [] | length)' <<<"${storage_json}")
storage_not_up=$(jq -r '[.liststoragepoolsresponse.storagepool[]? | select(.state != "Up")] | length' <<<"${storage_json}")
echo "[smoke] storage_pools=${storage_count}, storage_not_up=${storage_not_up}"

[[ "${hosts_not_up}" == "0" ]] || fail "one or more hosts are not Up"
[[ "${storage_not_up}" == "0" ]] || fail "one or more storage pools are not Up"

echo "[smoke] PASS"
