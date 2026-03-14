#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "[quality] running functional smoke"
"${SCRIPT_DIR}/run-functional-smoke.sh"

echo "[quality] running pentest baseline"
"${SCRIPT_DIR}/run-pentest-baseline.sh"

echo "[quality] running monitoring checks"
"${SCRIPT_DIR}/run-monitoring-check.sh"

echo "[quality] ALL CHECKS COMPLETED"
