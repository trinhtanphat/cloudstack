#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
"${ROOT_DIR}/.venv/bin/ansible" -i "${ROOT_DIR}/inventory.ini" kvm -m shell -a "ip -br link show | awk '{print \$1, \$2}'"
