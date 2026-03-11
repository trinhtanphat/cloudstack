#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SERVICE_SRC="${ROOT_DIR}/scripts/vnso/systemd/cloudstack-mshost-cleanup.service"
TIMER_SRC="${ROOT_DIR}/scripts/vnso/systemd/cloudstack-mshost-cleanup.timer"
SERVICE_DST="/etc/systemd/system/cloudstack-mshost-cleanup.service"
TIMER_DST="/etc/systemd/system/cloudstack-mshost-cleanup.timer"

install -m 0644 "${SERVICE_SRC}" "${SERVICE_DST}"
install -m 0644 "${TIMER_SRC}" "${TIMER_DST}"

systemctl daemon-reload
systemctl enable --now cloudstack-mshost-cleanup.timer

systemctl status cloudstack-mshost-cleanup.timer --no-pager || true
systemctl list-timers --all --no-pager | grep cloudstack-mshost-cleanup || true
