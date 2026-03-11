#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PORT_UI="25050"
PORT_MGMT="28080"

"${ROOT_DIR}/scripts/vnso/check-ports.sh" "${PORT_UI}"

cd "${ROOT_DIR}/ui"

export NODE_OPTIONS="--openssl-legacy-provider"
# Point UI API calls to local management port. Update this if you front with domain-only routing.
export CS_URL="http://127.0.0.1:${PORT_MGMT}"
export HOST="0.0.0.0"
export PUBLIC_HOST="cloudstack.vnso.vn"
export ALLOWED_HOSTS='["cloudstack.vnso.vn","localhost","127.0.0.1"]'

echo "Starting CloudStack UI on http://0.0.0.0:${PORT_UI}"
exec npx vue-cli-service serve --port "${PORT_UI}"
