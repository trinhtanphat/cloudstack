#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   sudo bash 02-kvm-host-setup.sh
# Optional env:
#   LIBVIRT_TCP=0

LIBVIRT_TCP="${LIBVIRT_TCP:-0}"

if [[ "${EUID}" -ne 0 ]]; then
  echo "Please run as root."
  exit 1
fi

echo "[1/6] Install KVM/libvirt packages"
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y \
  qemu-kvm libvirt-daemon-system libvirt-clients \
  bridge-utils virtinst cpu-checker qemu-utils \
  nfs-common chrony

echo "[2/6] Enable services"
systemctl enable --now libvirtd
systemctl enable --now chrony || true

echo "[3/6] Kernel + netfilter bridge settings"
cat >/etc/modules-load.d/kvm-cloudstack.conf <<'EOF'
kvm
kvm_intel
br_netfilter
EOF
modprobe kvm || true
modprobe kvm_intel || true
modprobe br_netfilter || true

cat >/etc/sysctl.d/99-cloudstack-kvm.conf <<'EOF'
net.bridge.bridge-nf-call-iptables = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward = 1
EOF
sysctl --system >/dev/null

echo "[4/6] Optional libvirt TCP listener"
if [[ "${LIBVIRT_TCP}" == "1" ]]; then
  sed -i 's/^#\?listen_tls.*/listen_tls = 0/' /etc/libvirt/libvirtd.conf
  sed -i 's/^#\?listen_tcp.*/listen_tcp = 1/' /etc/libvirt/libvirtd.conf
  sed -i 's/^#\?tcp_port.*/tcp_port = "16509"/' /etc/libvirt/libvirtd.conf
  sed -i 's/^#\?auth_tcp.*/auth_tcp = "none"/' /etc/libvirt/libvirtd.conf
  sed -i 's/^#\?LIBVIRTD_ARGS.*/LIBVIRTD_ARGS="--listen"/' /etc/default/libvirtd || true
  systemctl restart libvirtd
fi

echo "[5/6] Quick checks"
command -v kvm-ok >/dev/null 2>&1 && kvm-ok || true
virsh list --all || true

if [[ -e /dev/kvm ]]; then
  echo "/dev/kvm exists: OK"
else
  echo "WARNING: /dev/kvm not found. Check virtualization setting in BIOS."
fi

echo "[6/6] Done"
echo "Host is prepared for CloudStack KVM add-host step."
