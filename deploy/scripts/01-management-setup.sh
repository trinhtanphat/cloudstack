#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   sudo bash 01-management-setup.sh
# Optional env:
#   CLOUDSTACK_DOMAIN=cloudstack.vnso.vn
#   NFS_EXPORT_CIDR=10.10.0.0/16
#   SECONDARY_STORAGE_PATH=/export/secondary

CLOUDSTACK_DOMAIN="${CLOUDSTACK_DOMAIN:-cloudstack.vnso.vn}"
NFS_EXPORT_CIDR="${NFS_EXPORT_CIDR:-10.10.0.0/16}"
SECONDARY_STORAGE_PATH="${SECONDARY_STORAGE_PATH:-/export/secondary}"

if [[ "${EUID}" -ne 0 ]]; then
  echo "Please run as root."
  exit 1
fi

echo "[1/7] Install base packages"
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y \
  ca-certificates curl gnupg lsb-release jq \
  nfs-kernel-server nginx ufw

echo "[2/7] Install Docker engine + compose plugin"
if ! command -v docker >/dev/null 2>&1; then
  apt-get install -y docker.io docker-compose-plugin
fi
systemctl enable --now docker

echo "[3/7] Prepare secondary storage path: ${SECONDARY_STORAGE_PATH}"
mkdir -p "${SECONDARY_STORAGE_PATH}"
chown -R nobody:nogroup "${SECONDARY_STORAGE_PATH}"
chmod 0775 "${SECONDARY_STORAGE_PATH}"

echo "[4/7] Configure NFS export"
EXPORT_LINE="${SECONDARY_STORAGE_PATH} ${NFS_EXPORT_CIDR}(rw,sync,no_subtree_check,no_root_squash)"
if ! grep -Fq "${SECONDARY_STORAGE_PATH} ${NFS_EXPORT_CIDR}" /etc/exports 2>/dev/null; then
  echo "${EXPORT_LINE}" >> /etc/exports
fi
exportfs -rav
systemctl enable --now nfs-kernel-server

echo "[5/7] Configure basic firewall"
ufw --force enable || true
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
# Keep 28080 open only if needed for internal troubleshooting.
# Prefer exposing only through Nginx 443.
ufw allow from 10.10.0.0/16 to any port 28080 proto tcp || true

echo "[6/7] Enable Nginx"
systemctl enable --now nginx

echo "[7/7] Summary"
echo "Domain: ${CLOUDSTACK_DOMAIN}"
echo "Secondary storage path: ${SECONDARY_STORAGE_PATH}"
echo "NFS CIDR allowed: ${NFS_EXPORT_CIDR}"
echo
echo "Next:"
echo "  1) Put CloudStack reverse proxy config in /etc/nginx/sites-available/cloudstack.conf"
echo "  2) Run: nginx -t && systemctl reload nginx"
echo "  3) Point DNS ${CLOUDSTACK_DOMAIN} to this server public IP"
