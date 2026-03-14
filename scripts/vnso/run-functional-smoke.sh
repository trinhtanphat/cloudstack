#!/usr/bin/env bash
set -euo pipefail

TARGET_URL="${TARGET_URL:-https://cloudstack.vnso.vn}"

echo "[functional] target=${TARGET_URL}"

check_url() {
  local url="$1"
  local code
  code=$(curl -k -sS -o /dev/null -w "%{http_code}" "$url")
  echo "[functional] ${url} -> ${code}"
  if [[ "$code" -lt 200 || "$code" -ge 400 ]]; then
    echo "[functional][ERROR] unexpected status for ${url}"
    return 1
  fi
}

check_url "${TARGET_URL}/"
check_url "${TARGET_URL}/client/"
check_url "${TARGET_URL}/documents/"

latency=$(curl -k -sS -o /dev/null -w "%{time_total}" "${TARGET_URL}/client/")
echo "[functional] /client latency=${latency}s"

echo "[functional] PASS"
