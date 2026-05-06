#!/usr/bin/env bash

# CloudStack Deployment Verification Script
# Reads credentials from .env or CI/CD environment variables.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ -f "${ROOT_DIR}/.env" ]]; then
  set -a
  # shellcheck source=/dev/null
  source "${ROOT_DIR}/.env"
  set +a
fi

MGMT_IP="${CLOUDSTACK_MGMT_IP:-103.9.157.6}"
API_URL="${CLOUDSTACK_API_URL:-http://${MGMT_IP}:28080/client/api/}"
ADMIN_USER="${CLOUDSTACK_ADMIN_USER:-}"
ADMIN_PASS="${CLOUDSTACK_ADMIN_PASS:-}"
ADMIN_DOMAIN="${CLOUDSTACK_ADMIN_DOMAIN:-}"
KVM_ROOT_USER="${KVM_ROOT_USER:-root}"
KVM_ROOT_PASSWORD="${KVM_ROOT_PASSWORD:-}"
read -r -a KVM_HOSTS <<< "${CLOUDSTACK_KVM_HOSTS:-103.9.159.151 103.9.159.165 103.9.159.188}"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'
failed_count=0
SESSION=""

echo "=================================="
echo "CloudStack Deployment Verification"
echo "=================================="

fail_check() {
  echo -e "${RED}[FAIL] $1${NC}"
  ((failed_count += 1))
}

pass_check() {
  echo -e "${GREEN}[ OK ] $1${NC}"
}

require_bin() {
  if ! command -v "$1" >/dev/null 2>&1; then
    fail_check "Missing required command: $1"
    return 1
  fi
}

run_ssh() {
  local host="$1"
  shift
  if [[ -n "${KVM_ROOT_PASSWORD}" ]]; then
    sshpass -p "${KVM_ROOT_PASSWORD}" ssh -o StrictHostKeyChecking=no -o ConnectTimeout=10 "${KVM_ROOT_USER}@${host}" "$@"
  else
    ssh -o StrictHostKeyChecking=no -o ConnectTimeout=10 "${KVM_ROOT_USER}@${host}" "$@"
  fi
}

api_get() {
  local command_name="$1"
  curl -fsS --get "${API_URL}" \
    --data-urlencode "command=${command_name}" \
    --data-urlencode "sessionkey=${SESSION}" \
    --data-urlencode "response=json"
}

require_bin curl || true
require_bin jq || true
if [[ -n "${KVM_ROOT_PASSWORD}" ]]; then
  require_bin sshpass || true
fi

if [[ -z "${ADMIN_USER}" || -z "${ADMIN_PASS}" ]]; then
  fail_check "CLOUDSTACK_ADMIN_USER/CLOUDSTACK_ADMIN_PASS must be set in .env for authenticated checks"
else
  echo -e "\n${YELLOW}[0] Admin Login${NC}"
  login_json=$(curl -fsS -X POST "${API_URL}" \
    --data-urlencode "command=login" \
    --data-urlencode "username=${ADMIN_USER}" \
    --data-urlencode "password=${ADMIN_PASS}" \
    --data-urlencode "domain=${ADMIN_DOMAIN}" \
    --data-urlencode "response=json" || true)
  SESSION=$(jq -r '.loginresponse.sessionkey // empty' <<<"${login_json:-{}}")
  if [[ -n "${SESSION}" ]]; then
    pass_check "Admin login succeeded"
  else
    fail_check "Admin login failed"
  fi
fi

if [[ -z "${SESSION}" ]]; then
  echo -e "\n${RED}Cannot continue authenticated infrastructure checks without a session.${NC}"
  exit 1
fi

echo -e "\n${YELLOW}[1] CloudStack Management Service${NC}"
if api_get listCapabilities | jq -e '.listcapabilitiesresponse // .capabilities // .' >/dev/null; then
  pass_check "CloudStack REST API responding on ${API_URL}"
else
  fail_check "CloudStack REST API check failed"
fi

echo -e "\n${YELLOW}[2] NFS Exports on Management Host${NC}"
if run_ssh "${MGMT_IP}" 'showmount -e 127.0.0.1 | grep -c /export' >/dev/null; then
  pass_check "Management host exports /export paths"
else
  fail_check "NFS exports check failed on management host"
fi

echo -e "\n${YELLOW}[3] KVM Hosts Status${NC}"
for host in "${KVM_HOSTS[@]}"; do
  if run_ssh "${host}" 'systemctl is-active libvirtd' >/dev/null; then
    pass_check "Host ${host}: libvirtd service running"
  else
    fail_check "Host ${host}: libvirtd service not active"
  fi

  if run_ssh "${host}" "ping -c1 ${MGMT_IP}" >/dev/null 2>&1; then
    pass_check "Host ${host}: can reach management host (${MGMT_IP})"
  else
    fail_check "Host ${host}: cannot reach management host (${MGMT_IP})"
  fi

  if run_ssh "${host}" "mount -t nfs ${MGMT_IP}:/export/secondary /mnt 2>/dev/null && umount /mnt && echo ok" >/dev/null 2>&1; then
    pass_check "Host ${host}: NFS mounts working"
  else
    fail_check "Host ${host}: NFS mount check failed"
  fi
done

echo -e "\n${YELLOW}[4] CloudStack Cluster Status${NC}"
hosts_json=$(api_get listHosts || true)
hosts_count=$(jq -r '.listhostsresponse.count // (.listhostsresponse.host // [] | length)' <<<"${hosts_json:-{}}")
hosts_up=$(jq -r '[.listhostsresponse.host[]? | select(.state == "Up")] | length' <<<"${hosts_json:-{}}")
echo "Found ${hosts_count} hosts in CloudStack; Up=${hosts_up}/${#KVM_HOSTS[@]}"
if [[ "${hosts_count}" -ge "${#KVM_HOSTS[@]}" ]]; then
  pass_check "CloudStack has expected hosts registered"
else
  fail_check "CloudStack host count is lower than expected"
fi
if [[ "${hosts_up}" -eq "${#KVM_HOSTS[@]}" ]]; then
  pass_check "All expected hosts are Up"
else
  fail_check "One or more expected hosts are not Up"
fi

echo -e "\n${YELLOW}[5] Storage Pools Status${NC}"
storage_json=$(api_get listStoragePools || true)
storage_count=$(jq -r '.liststoragepoolsresponse.count // (.liststoragepoolsresponse.storagepool // [] | length)' <<<"${storage_json:-{}}")
storage_up=$(jq -r '[.liststoragepoolsresponse.storagepool[]? | select(.state == "Up")] | length' <<<"${storage_json:-{}}")
echo "Found ${storage_count} storage pools; Up=${storage_up}"
if [[ "${storage_count}" -ge 1 && "${storage_up}" -ge 1 ]]; then
  pass_check "CloudStack has active storage pools"
else
  fail_check "No active storage pool found"
fi

echo -e "\n${YELLOW}==================================${NC}"
if [[ ${failed_count} -eq 0 ]]; then
  echo -e "${GREEN}ALL CHECKS PASSED${NC}"
  echo -e "${GREEN}Infrastructure is ready for VM deployment${NC}"
  echo -e "${YELLOW}==================================${NC}"
  exit 0
else
  echo -e "${RED}${failed_count} CHECKS FAILED${NC}"
  echo -e "${RED}Fix failed items before proceeding${NC}"
  echo -e "${YELLOW}==================================${NC}"
  exit 1
fi
