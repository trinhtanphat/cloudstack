#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PORT_MGMT="28080"

"${ROOT_DIR}/scripts/vnso/check-ports.sh" "${PORT_MGMT}"

cd "${ROOT_DIR}"

export MAVEN_OPTS="-Xmx2048m"

echo "Starting CloudStack management on http://127.0.0.1:${PORT_MGMT}"
exec mvn -f client/pom.xml -DskipTests -Djetty.http.port="${PORT_MGMT}" jetty:run
