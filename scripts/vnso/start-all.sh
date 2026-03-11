#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

"${ROOT_DIR}/scripts/vnso/check-ports.sh" 25050 28080

echo "Use separate terminals for stable logs:"
echo "  1) ${ROOT_DIR}/scripts/vnso/start-mgmt-28080.sh"
echo "  2) ${ROOT_DIR}/scripts/vnso/start-ui-25050.sh"
