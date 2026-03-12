# CloudStack UI Setup Guide - Zone/Pod/Cluster/Hosts

## Overview
This guide walks you through the CloudStack web interface to set up the infrastructure. You'll create:
1. **Zone** (Advanced): Logical grouping of infrastructure
2. **Pod** (Physical Rack): Group of hosts in same network
3. **Cluster** (Compute Hosts): Group of hypervisors 
4. **Hosts** (KVM): Add 3 compute nodes to cluster
5. **Primary Storage** (NFS): Storage for VM disk images
6. **Secondary Storage** (NFS): Template and ISO repository

---

## Access CloudStack Web UI

**URL:** `http://103.9.157.6:28080/client/`

**Default Login Credentials:**
- Username: `admin`
- Password: `password` (default, may need to change)

---

## Step 1: Create Zone (Advanced)

1. **Navigate to Menu:**
   - Left sidebar → **Infrastructure** → **Zones**

2. **Click "Add Zone"** button (top right)

3. **Zone Configuration Screen - Fill these fields:**

   | Field | Value |
   |-------|-------|
   | Name | `zone-1` |
   | DNS 1 | `8.8.8.8` |
   | DNS 2 | `8.8.4.4` |
   | Internal DNS 1 | `103.9.157.6` |
   | Hypervisor | `KVM` |
   | Network Type | `Advanced` |
   | Security Groups | **Unchecked** (we'll use VPC security) |
   | Domain | `ROOT` |
   | Local Storage Enabled | **Unchecked** |

4. **Click "OK"** → Zone is created

   Status shows: **Enabled** (green)

---

## Step 2: Create Pod (Physical Rack)

1. **Within Zone Page:**
   - Click on zone name **`zone-1`** (opens zone details)
   - Look for **Pods** section at bottom
   - Click **"Add Pod"**

2. **Pod Configuration Screen:**

   | Field | Value |
   |-------|-------|
   | Name | `pod-1` |
   | Reserved System Gateway | `10.10.10.1` |
   | Reserved System Netmask | `255.255.255.0` |
   | Reserved System Start IP | `10.10.10.10` |
   | Reserved System End IP | `10.10.10.50` |

   > **Why these IPs?** The management CIDR is `10.10.10.0/24`. We're reserving 10.10.10.10-50 for CloudStack internal system VMs (DHCP, console proxy, etc.)

3. **Click "OK"** → Pod created

---

## Step 3: Create Cluster (KVM)

1. **Within Pod Page:**
   - Click on pod **`pod-1`** (opens pod details)
   - Look for **Clusters** section
   - Click **"Add Cluster"**

2. **Cluster Configuration Screen:**

   | Field | Value |
   |-------|-------|
   | Hypervisor Type | `KVM` |
   | Cluster Name | `cluster-kvm-1` |
   | CPU Overcommit Ratio | `2` |
   | Memory Overcommit Ratio | `1` |

   > **Overcommit Ratios:** CPU can overcommit 2x (16 physical cores = 32 vCPU allocatable). Memory 1x = no overcommit (reserve all physical RAM).

3. **Click "OK"** → Cluster created

---

## Step 4: Add Hosts (KVM Compute Nodes)

### **Host 1: 103.9.159.151**

1. **Within Cluster Page:**
   - Click on **`cluster-kvm-1`**
   - Look for **Hosts** section
   - Click **"Add Host"**

2. **Host Configuration Screen:**

   | Field | Value |
   |-------|-------|
   | Hostname | `103.9.159.151` |
   | Username | `root` |
   | Password | `Admin@@3224@@` |
   | Host Tags | `kvm-compute-1` |

3. **Click "Next"** → CloudStack connects and discovers the host
   - Should show: **Hypervisor: KVM**, **Memory: 64512 MB**, **CPU cores: XX**
   - If error appears, double-check:
     - SSH connectivity: `ping 103.9.159.151`
     - SSH auth: `ssh root@103.9.159.151` (verify password works)
     - libvirtd running: Should be active from Ansible

4. **Click "OK"** → Host added, status shows **Up**

---

### **Host 2: 103.9.159.165**

**Repeat Step 4 process:**

| Field | Value |
|-------|-------|
| Hostname | `103.9.159.165` |
| Username | `root` |
| Password | `Admin@@3224@@` |
| Host Tags | `kvm-compute-2` |

---

### **Host 3: 103.9.159.188**

**Repeat Step 4 process:**

| Field | Value |
|-------|-------|
| Hostname | `103.9.159.188` |
| Username | `root` |
| Password | `Admin@@3224@@` |
| Host Tags | `kvm-compute-3` |

**After all 3 hosts added:**
- Cluster page should show:
  - **3 Hosts** in **Up** state
  - **CPU cores visible** (sum of all 3 hosts)
  - **Memory visible** (sum of all 3 hosts)

---

## Step 5: Add Primary Storage (NFS)

1. **Navigate:**
   - Left menu → **Infrastructure** → **Primary Storage**

2. **Click "Add Primary Storage"**

3. **Primary Storage Configuration:**

   | Field | Value |
   |-------|-------|
   | Scope | `Cluster` |
   | Hypervisor Type | `KVM` |
   | Zone | `zone-1` |
   | Pod | `pod-1` |
   | Cluster | `cluster-kvm-1` |
   | Name | `primary-storage-nfs-1` |
   | Storage Protocol | `NFS` |
   | Server | `103.9.157.6` |
   | Path | `/export/primary` |

   > **Note:** We'll create `/export/primary` on 103.9.157.6 before this, or CloudStack will auto-create it.

4. **Click "OK"** → Storage added, status shows **Up**

   If status shows **Down**, check:
   - NFS path exists: `ls -la /export/primary` on 103.9.157.6
   - Share exported: Check `/etc/exports` includes this path
   - Hosts can mount: On each KVM host, test `mount -t nfs 103.9.157.6:/export/primary /mnt`

---

## Step 6: Add Secondary Storage (NFS)

1. **Navigate:**
   - Left menu → **Infrastructure** → **Secondary Storage**

2. **Click "Add Secondary Storage"**

3. **Secondary Storage Configuration:**

   | Field | Value |
   |-------|-------|
   | Zone | `zone-1` |
   | NFS Server | `103.9.157.6` |
   | Path | `/export/secondary` |
   | Name | `secondary-storage-nfs-1` |

4. **Click "OK"** → Storage added, status shows **Up**

   *(This path already exists from Ansible playbook)*

---

## Step 7: Verify Infrastructure

1. **Go to Dashboard:**
   - Left menu → **Dashboard**
   - Should show:
     - **Zone:** 1
     - **Pod:** 1
     - **Cluster:** 1
     - **Hosts:** 3 (all Up)
     - **Primary Storage:** 1 (Up)
     - **Secondary Storage:** 1 (Up)

2. **Check Host Resources:**
   - **Infrastructure** → **Hosts**
   - All 3 hosts should show:
     - State: **Up**
     - Power State: **On**
     - Memory Available: ~60GB each
     - CPU Available: (based on your server specs)

---

## Troubleshooting

### Issue: Host stays in "Connecting" state
**Solution:**
```bash
# On each KVM host, verify:
sshpass -p 'Admin@@3224@@' ssh root@<HOST_IP> 'systemctl is-active libvirtd'
# Should return: active

# Check firewall allows CloudStack:
sshpass -p 'Admin@@3224@@' ssh root@<HOST_IP> 'ufw status | grep 16514'
# Should see 16514 rule (libvirt monitoring)
```

### Issue: Primary Storage stays "Connecting"
**Solution:**
```bash
# On management host, create missing export:
ssh root@103.9.157.6 'mkdir -p /export/primary && chown nobody:nogroup /export/primary && chmod 777 /export/primary'

# Update NFS export:
ssh root@103.9.157.6 'echo "/export/primary 10.10.0.0/16(rw,no_root_squash,sync)" >> /etc/exports && exportfs -ra'

# Test from each KVM host:
sshpass -p 'Admin@@3224@@' ssh root@103.9.159.151 'mount -t nfs 103.9.157.6:/export/primary /mnt && umount /mnt && echo "NFS works"'
```

### Issue: Zone/Pod/Cluster show "Disabled"
**Solution:**
- Click zone/pod/cluster → **Edit** → Check **"Enable zone/pod/cluster"** checkbox
- CloudStack requires explicit enable after creation

---

## What's Next?

Once **all hosts show "Up"** and **storage shows "Up"**:

1. **Create Network (Guest Network):**
   - **Infrastructure** → **Networks** → **Add Network**
   - CIDR: `10.20.0.0/16`, Gateway: `10.20.0.1`
   - DHCP range: `10.20.1.0-10.20.1.254`

2. **Create Service Offering:**
   - **Service Offerings** → **Compute** → **Add Service Offering**
   - Name: `tiny`, CPU: 1, Memory: 512MB
   - Name: `small`, CPU: 2, Memory: 2048MB

3. **Register Template:**
   - **Images** → **Templates** → **Add Template** (Linux cloud image like Ubuntu)
   - Or use CloudStack's built-in Tiny Cloud Linux template

4. **Deploy First VM:**
   - **Instances** → **Add Instance**
   - Select template, service offering, guest network
   - Click "Launch" to deploy

---

## Quick Reference: UI Menu Structure

```
Dashboard (top-level status)
├── Infrastructure
│   ├── Zones (click to expand and create Pod/Cluster)
│   ├── Hosts (view all hosts across zones)
│   ├── Primary Storage
│   ├── Secondary Storage
│   └── Networks
├── Service Offerings
│   ├── Compute
│   └── Disk
├── Images
│   ├── Templates
│   ├── ISOs
│   └── Snapshots
├── Instances
│   ├── Running VMs
│   └── Add Instance
└── Administration
    ├── Accounts
    ├── Users
    └── Settings (zone-level configs)
```

---

## Authentication Note

If login fails with default credentials:
1. Check MySQL database: `mysql -h 103.9.157.6 -u cloud -p<password> cloud`
2. Query user account: `SELECT * FROM user WHERE username='admin';`
3. Ask DevOps for actual credentials (may have been set during initial CloudStack deployment)

---

**Estimated Time:** 15-20 minutes to create all infrastructure objects
**Validation:** All 3 hosts + 2 storage = infrastructure ready for first VM deployment
