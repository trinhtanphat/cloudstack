# 🚀 CloudStack Deployment - Step-by-Step Complete Guide

**Status: All preparation done. You just need to follow this guide!**

---

## 📍 Current Infrastructure State

✅ **Done:**
- 4 servers provisioned (1 management + 3 KVM)
- Ansible automation configured and tested
- Base OS packages installed (chrony, docker, libvirt, NFS)
- NFS storage exports ready (`/export/primary` and `/export/secondary`)
- CloudStack management running in Docker (port 28080)
- libvirtd activated on all 3 KVM hosts
- Firewall rules configured

⏳ **Pending (what you'll do now):**
- Add Zone/Pod/Cluster via CloudStack web UI
- Add 3 KVM hosts to CloudStack
- Configure storage
- Deploy first test VM

---

## 📋 Complete Step-by-Step Walkthrough

### **Part 1: CloudStack Web Interface Setup (15-20 minutes)**

**Read:** [`CLOUDSTACK-UI-SETUP-GUIDE.md`](./CLOUDSTACK-UI-SETUP-GUIDE.md)

This file contains **exact step-by-step instructions** for every CloudStack UI screen:
- Creating Zone (Advanced)
- Creating Pod (Physical Rack)
- Creating Cluster (KVM)
- Adding 3 KVM Hosts
- Adding Primary Storage (NFS)
- Adding Secondary Storage (NFS)

**Key Info to Use:**

| Setting | Value |
|---------|-------|
| **CloudStack URL** | `http://103.9.157.6:28080/client/` |
| **Default Username** | `admin` |
| **Default Password** | `password` (or as configured during initial setup) |
| **Zone Name** | `zone-1` |
| **Pod Name** | `pod-1` |
| **Cluster Name** | `cluster-kvm-1` |
| **Cluster Type** | `KVM` |
| **Host 1 IP** | `103.9.159.151` (user: `root`, pass: `Admin@@3224@@`) |
| **Host 2 IP** | `103.9.159.165` (user: `root`, pass: `Admin@@3224@@`) |
| **Host 3 IP** | `103.9.159.188` (user: `root`, pass: `Admin@@3224@@`) |
| **Primary Storage** | `NFS 103.9.157.6:/export/primary` |
| **Secondary Storage** | `NFS 103.9.157.6:/export/secondary` |

---

### **Part 2: Network Bridge Configuration (Optional but Recommended)**

**Read:** [`CLOUDSTACK-DEPLOYMENT-COMPLETE-GUIDE.md`](./CLOUDSTACK-DEPLOYMENT-COMPLETE-GUIDE.md)

This file includes:
- Bridge architecture explanation
- Deployment commands (copy-paste ready)
- Verification steps
- Troubleshooting guide

**Network Bridge Files Ready:**
- Pre-configured for all 3 KVM hosts
- Located in: `/root/cloudstack/deploy/ansible/host_vars/`
  - `103.9.159.151-bridges.yml`
  - `103.9.159.165-bridges.yml`
  - `103.9.159.188-bridges.yml`

**Quick Deploy Bridges (if you want VLAN separation):**

```bash
# 1. Go to Ansible directory
cd /root/cloudstack/deploy/ansible

# 2. Run bridge playbook
source .venv/bin/activate
ansible-playbook -i inventory.ini playbooks/network-bridges.yml

# 3. Verify bridges created
sshpass -p 'Admin@@3224@@' ssh root@103.9.159.151 'ip link show | grep br-'
```

---

## 🎯 The Actual Workflow (For You To Execute)

### **Step 1: Open CloudStack Web Interface**

1. Open browser, navigate to: `http://103.9.157.6:28080/client/`
2. Login with: `admin` / `password`
3. Wait for dashboard to load (may take 10-15 seconds)

### **Step 2: Create Zone (Advanced)**

1. Left menu → **Infrastructure** → **Zones**
2. Click **"Add Zone"** button
3. Fill in:
   - **Name:** `zone-1`
   - **DNS 1:** `8.8.8.8`
   - **DNS 2:** `8.8.4.4`
   - **Hypervisor:** `KVM`
   - **Network Type:** `Advanced`
4. Click **OK**

### **Step 3: Create Pod**

1. Click zone name **`zone-1`** to expand
2. Click **"Add Pod"**
3. Fill in:
   - **Name:** `pod-1`
   - **Gateway:** `10.10.10.1`
   - **Netmask:** `255.255.255.0`
   - **Start IP:** `10.10.10.10`
   - **End IP:** `10.10.10.50`
4. Click **OK**

### **Step 4: Create Cluster**

1. Click pod name **`pod-1`** to expand
2. Click **"Add Cluster"**
3. Fill in:
   - **Hypervisor Type:** `KVM`
   - **Cluster Name:** `cluster-kvm-1`
4. Click **OK**

### **Step 5: Add Host 1 (103.9.159.151)**

1. Click cluster **`cluster-kvm-1`** to expand
2. Click **"Add Host"**
3. Fill in:
   - **Host:** `103.9.159.151`
   - **Username:** `root`
   - **Password:** `Admin@@3224@@`
4. Click **Next**
5. Should detect: `Hypervisor: KVM`, `Memory: ~64GB`, `CPUs: N cores`
6. Click **OK** (status will show **Up** after 30-60 seconds)

### **Step 6: Add Host 2 & 3**

Repeat Step 5 for:
- `103.9.159.165` (root / Admin@@3224@@)
- `103.9.159.188` (root / Admin@@3224@@)

**Wait for all 3 hosts to show "Up" status** (check by refreshing page)

### **Step 7: Add Primary Storage**

1. Left menu → **Infrastructure** → **Primary Storage**
2. Click **"Add Primary Storage"**
3. Fill in:
   - **Scope:** `Cluster`
   - **Hypervisor:** `KVM`
   - **Zone:** `zone-1`
   - **Pod:** `pod-1`
   - **Cluster:** `cluster-kvm-1`
   - **Name:** `primary-storage-nfs-1`
   - **Protocol:** `NFS`
   - **Server:** `103.9.157.6`
   - **Path:** `/export/primary`
4. Click **OK** (status will show **Up** after 30-60 seconds)

### **Step 8: Add Secondary Storage**

1. Left menu → **Infrastructure** → **Secondary Storage**
2. Click **"Add Secondary Storage"**
3. Fill in:
   - **Zone:** `zone-1`
   - **NFS Server:** `103.9.157.6`
   - **Path:** `/export/secondary`
4. Click **OK**

### **Step 9: Verify Everything**

Go to **Dashboard** and verify:
- ✅ 1 Zone
- ✅ 1 Pod
- ✅ 1 Cluster
- ✅ 3 Hosts (all **Up**)
- ✅ 1 Primary Storage (status **Up**)
- ✅ 1 Secondary Storage (status **Up**)

---

## 🎓 What These Components Do

**Zone:** Logical grouping of resources (like a datacenter or region)
- You created: `zone-1` (your entire 4-host cluster is one zone)

**Pod:** Physical grouping within a zone (like a rack in a datacenter)
- You created: `pod-1` (all 3 KVM hosts in same physical location)

**Cluster:** Group of hypervisors (KVM, ESXi, etc.)
- You created: `cluster-kvm-1` (all 3 KVM hosts together)

**Hosts:** Actual compute servers where VMs run
- Added 3: 103.9.159.151, 103.9.159.165, 103.9.159.188

**Storage:** Where VM images and data live
- Primary: Fast storage for VM disk I/O (NFS from /export/primary)
- Secondary: Template/ISO repository (NFS from /export/secondary)

---

## ⚠️ Troubleshooting During Setup

### **Host stays "Connecting" or "Disconnected"**

**Cause:** CloudStack can't reach libvirtd on that host

**Fix:**
```bash
# Check libvirtd is running on the problematic host:
sshpass -p 'Admin@@3224@@' ssh root@<HOST_IP> 'systemctl is-active libvirtd'

# If inactive, start it:
sshpass -p 'Admin@@3224@@' ssh root@<HOST_IP> 'systemctl start libvirtd'
```

### **Storage shows "Connecting" or "Down"**

**Cause:** NFS mount issues

**Fix (on each KVM host):**
```bash
# Test NFS mount:
sshpass -p 'Admin@@3224@@' ssh root@<HOST_IP> 'mount -t nfs 103.9.157.6:/export/primary /mnt && ls /mnt && umount /mnt'

# If fails, check firewall:
sshpass -p 'Admin@@3224@@' ssh root@103.9.157.6 'ufw status | grep 111'
```

### **Cluster shows "Disabled"**

**Cause:** Cluster is disabled by default

**Fix:**
1. Click cluster name to open it
2. Look for **"Edit Cluster"** button
3. Enable the checkbox for cluster
4. Click **OK**

---

## ✅ Success Criteria

After completing all steps above, you should have:

- [ ] CloudStack web interface accessible and responding
- [ ] 1 Zone created (`zone-1`)
- [ ] 1 Pod created (`pod-1`)
- [ ] 1 Cluster created (`cluster-kvm-1`)
- [ ] 3 Hosts showing "Up" status
- [ ] 1 Primary Storage showing "Up" status
- [ ] 1 Secondary Storage showing "Up" status

**Time Estimate:** 20-30 minutes of UI interactions

---

## 🚀 What's Next? (After Infrastructure Setup)

Once all above is verified, you can:

### **1. Create Guest Network**
1. Go to **Infrastructure** → **Networks**
2. Click **"Add Network"**
3. Create guest network at 10.20.0.0/16 for VM traffic

### **2. Create Service Offerings**
1. Go to **Service Offerings** → **Compute**
2. Create offerings like:
   - **tiny:** 1 vCPU, 512MB RAM
   - **small:** 2 vCPU, 2GB RAM
   - **medium:** 4 vCPU, 4GB RAM

### **3. Register a VM Template**
1. Go to **Images** → **Templates**
2. Register Linux cloud image (Ubuntu, CentOS, etc.)
3. Or use CloudStack's built-in Tiny Linux template

### **4. Deploy First VM**
1. Go to **Instances** → **Add Instance**
2. Select template, service offering, network
3. Click **Launch**
4. VM should boot and get DHCP IP from guest network

---

## 📞 If Something Goes Wrong

**Before contacting support, provide:**

1. Screenshot of the error from CloudStack UI
2. The exact step you were on when error occurred
3. Host IP that's having issues (if applicable)
4. Output of verification script:
   ```bash
   /root/cloudstack/verify-deployment.sh
   ```

---

## 📁 File Reference

All setup files are in `/root/cloudstack/`:

```
/root/cloudstack/
├── CLOUDSTACK-UI-SETUP-GUIDE.md              ← Detailed UI guide (READ THIS)
├── CLOUDSTACK-DEPLOYMENT-COMPLETE-GUIDE.md   ← Bridge config guide
├── DEPLOYMENT-RUNBOOK-4-SERVERS.md           ← Architecture overview
├── verify-deployment.sh                       ← Quick status check
│
└── deploy/ansible/
    ├── host_vars/
    │   ├── 103.9.159.151-bridges.yml         ← Bridge config for host 1
    │   ├── 103.9.159.165-bridges.yml         ← Bridge config for host 2
    │   ├── 103.9.159.188-bridges.yml         ← Bridge config for host 3
    │   └── *.yml                              ← Other host configs
    │
    ├── playbooks/
    │   ├── site.yml                          ← Already executed
    │   └── network-bridges.yml                ← Ready to deploy bridges
    │
    ├── inventory.ini                          ← Host inventory
    ├── group_vars/                            ← Global variables
    └── .venv/                                 ← Ansible environment
```

---

## 🎉 Summary

**Everything is prepared.** You just need to:

1. **Open CloudStack UI** → `http://103.9.157.6:28080/client/`
2. **Follow the UI guide** → `CLOUDSTACK-UI-SETUP-GUIDE.md` (step-by-step)
3. **Create Zone → Pod → Cluster → Add 3 Hosts → Add Storage**
4. **Verify** all 3 hosts show **Up** status
5. **Done!** Infrastructure ready for VMs

**Estimated time:** 20-30 minutes

---

**Questions?** Check the detailed guides or run:
```bash
/root/cloudstack/verify-deployment.sh
```
