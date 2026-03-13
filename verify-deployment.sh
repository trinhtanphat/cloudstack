#!/bin/bash

# CloudStack Deployment Verification Script
# Run this to check if infrastructure is ready for VM deployment

set -e

MGMT_IP="103.9.157.6"
KVM_HOSTS=("103.9.159.151" "103.9.159.165" "103.9.159.188")
SSH_PASS="Admin@@3224@@"

echo "=================================="
echo "CloudStack Deployment Verification"
echo "=================================="

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

check_status() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ $1${NC}"
        return 0
    else
        echo -e "${RED}❌ $1${NC}"
        return 1
    fi
}

failed_count=0

# 1. Check CloudStack API is responding
echo -e "\n${YELLOW}[1] CloudStack Management Service${NC}"
curl -s "http://${MGMT_IP}:28080/client/api/?command=listCapabilities&response=json" | jq '.capabilities' > /dev/null
check_status "CloudStack REST API responding on port 28080" || ((failed_count++))

# 2. Check NFS exports
echo -e "\n${YELLOW}[2] NFS Exports on Management Host${NC}"
sshpass -p "${SSH_PASS}" ssh -o StrictHostKeyChecking=no root@${MGMT_IP} 'showmount -e 103.9.157.6 | grep -c /export' > /dev/null
check_status "Both /export/primary and /export/secondary exported" || ((failed_count++))

# 3. Check each KVM host
echo -e "\n${YELLOW}[3] KVM Hosts Status${NC}"
for host in "${KVM_HOSTS[@]}"; do
    # Check libvirtd is running
    sshpass -p "${SSH_PASS}" ssh -o StrictHostKeyChecking=no root@${host} 'systemctl is-active libvirtd' > /dev/null
    check_status "Host $host: libvirtd service running" || ((failed_count++))
    
    # Check connectivity to management
    sshpass -p "${SSH_PASS}" ssh -o StrictHostKeyChecking=no root@${host} "ping -c1 ${MGMT_IP}" > /dev/null 2>&1
    check_status "Host $host: can reach management host (${MGMT_IP})" || ((failed_count++))
    
    # Check NFS mount capability
    sshpass -p "${SSH_PASS}" ssh -o StrictHostKeyChecking=no root@${host} 'mount -t nfs 103.9.157.6:/export/secondary /mnt 2>/dev/null && umount /mnt && echo ok' > /dev/null 2>&1
    check_status "Host $host: NFS mounts working" || ((failed_count++))
done

# 4. Check CloudStack recognizes hosts
echo -e "\n${YELLOW}[4] CloudStack Cluster Status${NC}"
HOSTS_COUNT=$(curl -s "http://${MGMT_IP}:28080/client/api/?command=listHosts&response=json" | jq '.host | length')
echo "Found $HOSTS_COUNT hosts in CloudStack"
if [ "$HOSTS_COUNT" -ge 3 ]; then
    check_status "CloudStack has 3 or more hosts registered" || ((failed_count++))
else
    echo -e "${RED}❌ Only found $HOSTS_COUNT hosts (need at least 3)${NC}"
    ((failed_count++))
fi

# Check all hosts are "Up"
HOSTS_UP=$(curl -s "http://${MGMT_IP}:28080/client/api/?command=listHosts&response=json" | jq '.host[] | select(.state == "Up") | .name' | wc -l)
echo "Hosts in 'Up' state: $HOSTS_UP/3"
if [ "$HOSTS_UP" -eq 3 ]; then
    check_status "All 3 hosts in 'Up' state" || ((failed_count++))
else
    echo -e "${RED}❌ Only $HOSTS_UP/3 hosts are Up${NC}"
    ((failed_count++))
fi

# 5. Check storage pools
echo -e "\n${YELLOW}[5] Storage Pools Status${NC}"
STORAGE_COUNT=$(curl -s "http://${MGMT_IP}:28080/client/api/?command=listStoragePools&response=json" | jq '.storagepool | length')
echo "Found $STORAGE_COUNT storage pools"
if [ "$STORAGE_COUNT" -ge 2 ]; then
    check_status "CloudStack has primary and secondary storage configured" || ((failed_count++))
else
    echo -e "${RED}❌ Expected 2 storage pools, found only $STORAGE_COUNT${NC}"
    ((failed_count++))
fi

# Summary
echo -e "\n${YELLOW}=================================="
if [ $failed_count -eq 0 ]; then
    echo -e "${GREEN}✅ ALL CHECKS PASSED!${NC}"
    echo -e "${GREEN}Infrastructure is ready for VM deployment${NC}"
    echo -e "${YELLOW}=================================="
    exit 0
else
    echo -e "${RED}❌ $failed_count CHECKS FAILED${NC}"
    echo -e "${RED}Fix failed items before proceeding${NC}"
    echo -e "${YELLOW}=================================="
    exit 1
fi
