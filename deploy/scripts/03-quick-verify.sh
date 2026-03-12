#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   bash 03-quick-verify.sh <management_ip>

MGMT_IP="${1:-}"
if [[ -z "${MGMT_IP}" ]]; then
  echo "Usage: bash 03-quick-verify.sh <management_ip>"
  exit 1
fi

echo "== Local checks =="
command -v curl >/dev/null && echo "curl: ok" || echo "curl: missing"
command -v docker >/dev/null && echo "docker: ok" || echo "docker: missing"

echo "== Management endpoint checks =="
curl -fsS "http://${MGMT_IP}:28080/client/" >/dev/null && echo "UI endpoint: ok" || echo "UI endpoint: failed"
curl -fsS "http://${MGMT_IP}:28080/client/api/?command=listCapabilities&response=json" >/dev/null && echo "API endpoint: ok" || echo "API endpoint: failed"

echo "Done."
