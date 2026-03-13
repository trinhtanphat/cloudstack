#!/usr/bin/env bash
set -euo pipefail

if [[ "$#" -lt 1 ]]; then
  echo "Usage: $0 <port> [port ...]"
  exit 1
fi

conflict=0
for port in "$@"; do
  if ss -lntup | grep -E "[:.]${port}[[:space:]]" >/dev/null 2>&1; then
    echo "[CONFLICT] port ${port} is already in use"
    ss -lntup | grep -E "[:.]${port}[[:space:]]" | head -n 3
    conflict=1
  else
    echo "[OK] port ${port} is free"
  fi
  echo "---"
done

if [[ "$conflict" -ne 0 ]]; then
  exit 2
fi
