# Complete CloudStack Deployment Checklist & Bridge Configuration Guide

This guide combines UI infrastructure setup with advanced network bridge configuration.

---

## 📋 Phase 1: Infrastructure UI Setup

**GUIDEd steps:** See `CLOUDSTACK-UI-SETUP-GUIDE.md` for detailed screen-by-screen instructions.

**Quick Checklist:**
1. ✅ Access http://103.9.157.6:28080/client/
2. ✅ Create Zone: `zone-1` (Advanced, KVM, DNS 8.8.8.8/8.8.4.4)
3. ✅ Create Pod: `pod-1` (Reserved IPs: 10.10.10.10-10.10.10.50)
4. ✅ Create Cluster: `cluster-kvm-1` (KVM, CPU overcommit 2x, Memory 1x)
5. ✅ Add Host 1: `103.9.159.151` (root / Admin@@3224@@)
6. ✅ Add Host 2: `103.9.159.165` (root / Admin@@3224@@)
7. ✅ Add Host 3: `103.9.159.188` (root / Admin@@3224@@)

**Validation:**
```bash
# After adding all 3 hosts, check them:
curl -s "http://103.9.157.6:28080/client/api/?command=listHosts&response=json" | jq '.host[] | {id, name, state, hypervisor}'

# Should see 3 hosts with state: "Up", hypervisor: "KVM"
```

8. ✅ Add Primary Storage: `primary-storage-nfs-1` (NFS from 103.9.157.6:/export/primary)
9. ✅ Add Secondary Storage: `secondary-storage-nfs-1` (NFS from 103.9.157.6:/export/secondary)

**Validation:**
```bash
# Check storage is added:
curl -s "http://103.9.157.6:28080/client/api/?command=listStoragePools&response=json" | jq '.storagepool[] | {id, name, state, type}'

# Should see 2 storage entries with state: "Up"
```

---

## 🌉 Phase 2: Advanced Network Bridge Configuration (Optional but Recommended)

The bridge configuration separates network traffic into 3 logical networks using VLAN tagging:
- **VLAN 10 (br-mgmt)**: CloudStack management & internal system VMs
- **VLAN 20 (br-storage)**: NFS storage I/O (primary/secondary storage)
- **VLAN 30 (br-guest)**: Guest VM networks (10.20.0.0/16)

### Prerequisites:
- All 3 hosts must be in "Up" state in CloudStack (from Phase 1)
- Each host currently has only ONE active NIC (eno1 or eno2), so bridges will coexist logically on same interface

### Host-to-Interface Mapping:
| Host | Management NIC | Guest/Storage NICs |
|------|----------------|-------------------|
| 103.9.159.151 | eno1 (UP) | eno1 (shared via VLAN 20, 30) |
| 103.9.159.165 | eno1 (UP) | eno1 (shared via VLAN 20, 30) |
| 103.9.159.188 | eno2 (UP) | eno2 (shared via VLAN 20, 30) |

> **Note:** If you have additional physical NICs for guest/storage traffic, please tell me NIC names and I'll update the bridge configs.

---

## 🚀 How to Deploy Bridges

### Step 1: Verify Bridge Files are Ready

```bash
# Check pre-prepared bridge configs:
ls -la /root/cloudstack/deploy/ansible/host_vars/*-bridges.yml

# Output should show:
# -rw-r--r-- 103.9.159.151-bridges.yml
# -rw-r--r-- 103.9.159.165-bridges.yml
# -rw-r--r-- 103.9.159.188-bridges.yml
```

### Step 2: Review Bridge Configuration

```bash
# Review what bridges will be created on each host:
cat /root/cloudstack/deploy/ansible/host_vars/103.9.159.151-bridges.yml | grep -A2 "^  br-"
# Shows: br-mgmt (10.10.10.21/24), br-guest (VLAN 30), br-storage (VLAN 20)

cat /root/cloudstack/deploy/ansible/host_vars/103.9.159.165-bridges.yml | grep -A2 "^  br-"
# Shows: br-mgmt (10.10.10.22/24), br-guest (VLAN 30), br-storage (VLAN 20)

cat /root/cloudstack/deploy/ansible/host_vars/103.9.159.188-bridges.yml | grep -A2 "^  br-"
# Shows: br-mgmt (10.10.10.23/24), br-guest (VLAN 30), br-storage (VLAN 20)
```

### Step 3: Deploy Bridges Using Ansible

```bash
# Navigate to Ansible directory
cd /root/cloudstack/deploy/ansible

# Activate virtualenv
source .venv/bin/activate

# Run network-bridges playbook (applies netplan configs with bridges)
ansible-playbook -i inventory.ini playbooks/network-bridges.yml

# Output should show:
# PLAY [Setup network bridges for CloudStack] ****
# 103.9.159.151 : ok=4, changed=1
# 103.9.159.165 : ok=4, changed=1
# 103.9.159.188 : ok=4, changed=1
```

### Step 4: Verify Bridges Created

```bash
# Check br-mgmt bridge exists and has correct IP on each host:
sshpass -p 'Admin@@3224@@' ssh -o StrictHostKeyChecking=no root@103.9.159.151 'ip addr show br-mgmt | grep "inet "'
# Output: inet 10.10.10.21/24 brd ...

sshpass -p 'Admin@@3224@@' ssh -o StrictHostKeyChecking=no root@103.9.159.165 'ip addr show br-mgmt | grep "inet "'
# Output: inet 10.10.10.22/24 brd ...

sshpass -p 'Admin@@3224@@' ssh -o StrictHostKeyChecking=no root@103.9.159.188 'ip addr show br-mgmt | grep "inet "'
# Output: inet 10.10.10.23/24 brd ...

# Check br-guest VLAN bridge:
sshpass -p 'Admin@@3224@@' ssh -o StrictHostKeyChecking=no root@103.9.159.151 'ip link show | grep br-'
# Output should show: br-mgmt, br-guest, br-storage

# Check VLAN tag numbers:
sshpass -p 'Admin@@3224@@' ssh -o StrictHostKeyChecking=no root@103.9.159.151 'ip -d link show br-guest'
# Output should show: vlan id 30
```

### Step 5: Verify Connectivity Between Hosts via Bridges

```bash
# Test ping between hosts on management bridge (should work):
sshpass -p 'Admin@@3224@@' ssh -o StrictHostKeyChecking=no root@103.9.159.151 'ping -c1 10.10.10.22'
# Output: 1 packets transmitted, 1 received, 0% packet loss

sshpass -p 'Admin@@3224@@' ssh -o StrictHostKeyChecking=no root@103.9.159.165 'ping -c1 10.10.10.23'
# Output: 1 packets transmitted, 1 received, 0% packet loss

# Test guest bridge connectivity (VLAN 30):
sshpass -p 'Admin@@3224@@' ssh -o StrictHostKeyChecking=no root@103.9.159.151 'ping -c1 -I br-guest 10.20.0.2'
# Output: 1 packets transmitted, 1 received, 0% packet loss
```

### Step 6: Verify CloudStack Still Recognizes Hosts

```bash
# CloudStack should still see all 3 hosts as "Up" (no network disruption):
curl -s "http://103.9.157.6:28080/client/api/?command=listHosts&response=json" | jq '.host[] | {name, state}'

# Output should show all 3 hosts with state: "Up"
# If any show "Disconnected" or "Down", network config broke connectivity - see troubleshooting below
```

---

## ⚠️ Troubleshooting Bridge Deployment

### Issue: Host goes "Disconnected" after bridge deployment

**Symptom:** CloudStack shows host state as "Disconnected" instead of "Up"

**Cause:** Bridge configuration changed the network path to the host

**Solution:**
```bash
# On the affected host, check if management bridge has correct IP:
sshpass -p 'Admin@@3224@@' ssh -o StrictHostKeyChecking=no root@103.9.159.151 'ip addr show br-mgmt'

# If IP is missing, apply netplan manually:
sshpass -p 'Admin@@3224@@' ssh -o StrictHostKeyChecking=no root@103.9.159.151 'netplan apply && sleep 2 && ip addr show br-mgmt'

# If still broken, revert to previous netplan:
sshpass -p 'Admin@@3224@@' ssh -o StrictHostKeyChecking=no root@103.9.159.151 'ls /etc/netplan/backup/'
# Check backup files, restore if needed
```

### Issue: VLAN bridges show but not tagging properly

**Check VLAN ID:**
```bash
# Verify VLAN ID in bridge config:
sshpass -p 'Admin@@3224@@' ssh -o StrictHostKeyChecking=no root@103.9.159.151 'ip -d link show | grep -A1 "br-guest"'

# Should show: vlan id 30
# If shows "vlan id 0", VLAN not applying - check netplan template
```

### Issue: NFS storage mounts fail after bridge deployment

**Symptom:** CloudStack storage shows "Down" after bridges up

**Solution:**
```bash
# NFS mounts should hit br-storage VLAN 20:
sshpass -p 'Admin@@3224@@' ssh -o StrictHostKeyChecking=no root@103.9.159.151 'mount | grep nfs'

# If no NFS mounts, manually mount to test:
sshpass -p 'Admin@@3224@@' ssh -o StrictHostKeyChecking=no root@103.9.159.151 'mount -t nfs 103.9.157.6:/export/secondary /mnt && ls /mnt && umount /mnt'

# If mount fails, check br-storage IP exists:
sshpass -p 'Admin@@3224@@' ssh -o StrictHostKeyChecking=no root@103.9.159.151 'ip addr show br-storage'

# Should show: inet 10.10.20.21/24 ...
```

---

## 🎯 Quick Summary - What Each Bridge Does

| Bridge | VLAN | IP Range | Purpose | Example Traffic |
|--------|------|----------|---------|-----------------|
| **br-mgmt** | Untagged (native) | 10.10.10.x/24 | CloudStack control & agent communication | CloudStack agent ↔ management server |
| **br-storage** | 20 | 10.10.20.x/24 | NFS storage (primary & secondary) I/O | VM disk reads/writes |
| **br-guest** | 30 | 10.20.0.x/16 | Guest VM networks | VM-to-VM, VM-to-internet |

---

## ⏭️ Next Steps After Bridge Verification

1. **Create Guest Network in CloudStack UI:**
   - Go to **Infrastructure** → **Networks** → **Add Network**
   - Network Name: `guest-network-1`
   - CIDR: `10.20.0.0/16`
   - Gateway: `10.20.0.1`
   - DHCP Range: `10.20.1.0-10.20.1.254`
   - Select zone: `zone-1`

2. **Deploy First Test VM:**
   - Go to **Instances** → **Add Instance**
   - Select service offering (e.g., 2 vCPU / 2GB RAM)
   - Select Linux template (CloudStack has built-in Tiny Linux)
   - Select network: `guest-network-1`
   - Click **Launch**

3. **Validate VM Networking:**
   - VM should boot and get DHCP IP in `10.20.1.0-10.20.1.254` range
   - Check VM has route to storage (for OS image)
   - Check VM can ping gateway (10.20.0.1)

---

## 📞 Support Notes

**If bridges fail:**
- Contact DevOps with: host IP, bridge name (br-mgmt/br-guest/br-storage), VLAN ID, netplan error message
- Provide: `/etc/netplan/*` file contents for debugging

**If you have additional physical NICs:**
- Share: NIC names (eth1, eth2, etc.) and what traffic should go on each
- I'll update bridge configs to use dedicated physical NICs instead of VLANs

**Manual rollback (if bridges cause issues):**
```bash
# On each affected host, restore original netplan:
sshpass -p 'Admin@@3224@@' ssh root@103.9.159.151 'cd /etc/netplan && ls -la'
# If backup exists, restore: cp 01-netcfg.yaml.bak 01-netcfg.yaml
# Then apply: netplan apply
```

---

## 🔒 Security Note

Bridges are logical network separations (VLAN-based). For true isolation, implement:
- Firewall rules between VLANs (UFW rules with -i br-storage, -i br-guest)
- Network ACLs on physical switches (802.1Q VLAN enforcement)
- Storage VLAN encryption (if storing sensitive data)

---

**File Locations for Reference:**

```
/root/cloudstack/
├── CLOUDSTACK-UI-SETUP-GUIDE.md          ← Step-by-step UI screenshots/instructions
├── DEPLOYMENT-RUNBOOK-4-SERVERS.md        ← Architecture overview
├── deploy/
│   └── ansible/
│       ├── host_vars/
│       │   ├── 103.9.159.151-bridges.yml  ← Bridge config for host 1
│       │   ├── 103.9.159.165-bridges.yml  ← Bridge config for host 2
│       │   ├── 103.9.159.188-bridges.yml  ← Bridge config for host 3
│       │   └── 103.9.159.151.yml          ← Original management config
│       ├── playbooks/
│       │   ├── site.yml                   ← Base infrastructure setup (already run)
│       │   └── network-bridges.yml        ← Bridge deployment playbook
│       └── inventory.ini                  ← Host inventory
```

---

**Status: Ready for Phase 1 (UI) → Phase 2 (Bridges) → Phase 3 (VM Deployment)**
