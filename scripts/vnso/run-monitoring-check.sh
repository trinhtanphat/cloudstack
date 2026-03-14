#!/usr/bin/env bash
set -euo pipefail

TARGET_URL="${TARGET_URL:-https://cloudstack.vnso.vn}"
MONITOR_ENDPOINT="${MONITOR_ENDPOINT:-}"

echo "[monitor] target=${TARGET_URL}"

if command -v docker >/dev/null 2>&1; then
  echo "[monitor] docker container health"
  docker ps --format '{{.Names}}\t{{.Status}}' | head -n 120
else
  echo "[monitor][WARN] docker not available, skip container health"
fi

code=$(curl -k -sS -o /dev/null -w "%{http_code}" "${TARGET_URL}/client/")
latency=$(curl -k -sS -o /dev/null -w "%{time_total}" "${TARGET_URL}/client/")
echo "[monitor] client status=${code} latency=${latency}s"

if [[ "$code" -lt 200 || "$code" -ge 400 ]]; then
  echo "[monitor][ERROR] client endpoint unhealthy"
  exit 1
fi

if [[ -n "$MONITOR_ENDPOINT" ]]; then
  mcode=$(curl -k -sS -o /dev/null -w "%{http_code}" "$MONITOR_ENDPOINT")
  echo "[monitor] monitor endpoint ${MONITOR_ENDPOINT} status=${mcode}"
  if [[ "$mcode" -lt 200 || "$mcode" -ge 400 ]]; then
    echo "[monitor][ERROR] monitor endpoint unhealthy"
    exit 1
  fi
fi

echo "[monitor] PASS"
