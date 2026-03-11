#!/usr/bin/env bash
set -euo pipefail

DB_CONTAINER="${DB_CONTAINER:-xiaozhi-esp32-server-db}"
DB_NAME="${DB_NAME:-cloud}"
DB_USER="${DB_USER:-cloud}"
DB_PASS="${DB_PASS:-cloud}"
INTERVAL_SECONDS="${INTERVAL_SECONDS:-900}"
RUN_ONCE="${RUN_ONCE:-0}"

cleanup_once() {
  docker exec "${DB_CONTAINER}" sh -lc "mysql -u${DB_USER} -p${DB_PASS} -D ${DB_NAME} -e \"
DELETE s
FROM mshost_status s
JOIN mshost m ON s.ms_id = m.uuid
WHERE m.state = 'Down';

DELETE p
FROM mshost_peer p
LEFT JOIN mshost mo ON p.owner_mshost = mo.id
LEFT JOIN mshost mp ON p.peer_mshost = mp.id
WHERE mo.id IS NULL OR mp.id IS NULL OR mo.state = 'Down' OR mp.state = 'Down';

DELETE FROM mshost
WHERE state = 'Down';

SELECT id, msid, name, uuid, state, last_update
FROM mshost
ORDER BY id;
\""
}

if [[ "${RUN_ONCE}" == "1" ]]; then
  cleanup_once
  exit 0
fi

while true; do
  cleanup_once
  sleep "${INTERVAL_SECONDS}"
done
