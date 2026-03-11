#!/bin/bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
DB_CONTAINER="${DB_CONTAINER:-xiaozhi-esp32-server-db}"
DB_NAME="${DB_NAME:-cloud}"
DB_USER="${DB_USER:-cloud}"
DB_PASSWORD="${DB_PASSWORD:-cloud}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_ACCOUNT_ID="${ADMIN_ACCOUNT_ID:-2}"
ADMIN_DOMAIN_ID="${ADMIN_DOMAIN_ID:-1}"
SECRET_DIR="${SECRET_DIR:-$ROOT_DIR/docker/secrets}"
SECRET_FILE="${SECRET_FILE:-$SECRET_DIR/admin-api-key.env}"
KEY_NAME_PREFIX="${KEY_NAME_PREFIX:-admin-production-key}"

mkdir -p "$SECRET_DIR"
umask 077

readarray -t GENERATED < <(python3 - <<'PY'
import secrets, uuid
print(secrets.token_urlsafe(24))
print(secrets.token_urlsafe(48))
print(str(uuid.uuid4()))
print('admin-production-key-' + uuid.uuid4().hex[:12])
PY
)

API_KEY="${GENERATED[0]}"
SECRET_KEY="${GENERATED[1]}"
RECORD_UUID="${GENERATED[2]}"
KEY_NAME="${GENERATED[3]}"

SQL=$(cat <<SQL
UPDATE api_keypair
SET removed = NOW()
WHERE user_id = (SELECT id FROM user WHERE username='${ADMIN_USERNAME}' AND account_id=${ADMIN_ACCOUNT_ID} LIMIT 1)
  AND removed IS NULL;
INSERT INTO api_keypair (uuid, name, domain_id, account_id, user_id, api_key, secret_key, created)
SELECT '${RECORD_UUID}', '${KEY_NAME}', ${ADMIN_DOMAIN_ID}, ${ADMIN_ACCOUNT_ID}, id, '${API_KEY}', '${SECRET_KEY}', NOW()
FROM user
WHERE username='${ADMIN_USERNAME}' AND account_id=${ADMIN_ACCOUNT_ID} AND removed IS NULL;
SELECT COUNT(*) AS active_keys
FROM api_keypair
WHERE user_id = (SELECT id FROM user WHERE username='${ADMIN_USERNAME}' AND account_id=${ADMIN_ACCOUNT_ID} LIMIT 1)
  AND removed IS NULL;
SQL
)

docker exec "$DB_CONTAINER" sh -lc "mysql -u${DB_USER} -p${DB_PASSWORD} -D ${DB_NAME} -e \"${SQL}\"" >/dev/null

cat > "$SECRET_FILE" <<EOF
CLOUDSTACK_ADMIN_API_KEY=${API_KEY}
CLOUDSTACK_ADMIN_SECRET_KEY=${SECRET_KEY}
CLOUDSTACK_ADMIN_KEY_NAME=${KEY_NAME}
CLOUDSTACK_ADMIN_ROTATED_AT=$(date -u +%Y-%m-%dT%H:%M:%SZ)
EOF

chmod 600 "$SECRET_FILE"

echo "Admin API key rotated successfully."
echo "Secret file: $SECRET_FILE"
