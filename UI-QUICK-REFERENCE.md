# CloudStack UI Quick Reference Card

Print this or keep it open while setting up infrastructure!

---

## 🌐 Access & Login

**URL:** `http://103.9.157.6:28080/client/`

**Credentials:**
```
Username: admin
Password: password
```

**Dashboard Load Time:** 10-15 seconds (first time)

---

## 📍 Navigation Menu Structure

```
Left Sidebar (appears after login)
├── Dashboard              (status overview)
├── Infrastructure
│   ├── Zones              ← CREATE ZONE HERE
│   ├── Pods               (sub-section of zone)
│   ├── Clusters           (sub-section of pod)
│   ├── Hosts              (appears here after creation)
│   ├── Primary Storage    ← CREATE STORAGE HERE
│   ├── Secondary Storage  ← CREATE STORAGE HERE
│   └── Networks
├── Service Offerings
│   ├── Compute
│   └── Disk
├── Images
│   ├── Templates
│   ├── ISOs
│   └── Snapshots
├── Instances             ← LAUNCH VMs HERE (later)
├── Accounts
├── Administration
└── Settings
```

---

## 📝 Creation Order & Values

### **1️⃣ Create Zone**

Menu: **Infrastructure** → **Zones** → **Add Zone**

Fill these fields:

| Field | Value |
|-------|-------|
| Name | `zone-1` |
| DNS 1 | `8.8.8.8` |
| DNS 2 | `8.8.4.4` |
| Internal DNS | `103.9.157.6` |
| Hypervisor | `KVM` |
| Network Type | `Advanced` |
| Security Groups | Unchecked |
| Local Storage | Unchecked |

Click **OK** → Zone appears in list with status **Enabled**

---

### **2️⃣ Create Pod**

From zone **`zone-1`** page:

Click zone name → Look for **"Add Pod"** button

| Field | Value |
|-------|-------|
| Name | `pod-1` |
| Gateway | `10.10.10.1` |
| Netmask | `255.255.255.0` |
| Reserved Start | `10.10.10.10` |
| Reserved End | `10.10.10.50` |

Click **OK** → Pod created

---

### **3️⃣ Create Cluster**

From pod **`pod-1`** page:

Click pod name → Look for **"Add Cluster"** button

| Field | Value |
|-------|-------|
| Hypervisor Type | `KVM` |
| Cluster Name | `cluster-kvm-1` |
| CPU Overcommit | `2` |
| Memory Overcommit | `1` |

Click **OK** → Cluster created

---

### **4️⃣ Add Hosts (3 times)**

From cluster **`cluster-kvm-1`** page:

Click cluster name → Look for **"Add Host"** button

**For Host 1:**

| Field | Value |
|-------|-------|
| Host | `103.9.159.151` |
| Username | `root` |
| Password | `Admin@@3224@@` |
| Tags | `kvm-compute-1` |

Click **Next** → Auto-detects host (wait 10-30 seconds)

Click **OK** → Status becomes **Up** (wait 30 seconds)

**For Host 2:** Use `103.9.159.165` (root / Admin@@3224@@)

**For Host 3:** Use `103.9.159.188` (root / Admin@@3224@@)

---

### **5️⃣ Add Primary Storage**

Menu: **Infrastructure** → **Primary Storage** → **Add Primary Storage**

| Field | Value |
|-------|-------|
| Scope | `Cluster` |
| Hypervisor Type | `KVM` |
| Zone | `zone-1` |
| Pod | `pod-1` |
| Cluster | `cluster-kvm-1` |
| Name | `primary-storage-nfs-1` |
| Protocol | `NFS` |
| Server | `103.9.157.6` |
| Path | `/export/primary` |

Click **OK** → Storage added

Status shows **Up** after 30-60 seconds

---

### **6️⃣ Add Secondary Storage**

Menu: **Infrastructure** → **Secondary Storage** → **Add Secondary Storage**

| Field | Value |
|-------|-------|
| Zone | `zone-1` |
| NFS Server | `103.9.157.6` |
| Path | `/export/secondary` |

Click **OK** → Storage added

Status shows **Up** after 30-60 seconds

---

## ✅ Final Verification Checklist

Go to **Dashboard** and verify:

| Component | Expected Status |
|-----------|-----------------|
| Zones | 1 zone (`zone-1`) |
| Pods | 1 pod (`pod-1`) |
| Clusters | 1 cluster (`cluster-kvm-1`) |
| Hosts | 3 hosts - all **Up** |
| Primary Storage | 1 storage - **Up** |
| Secondary Storage | 1 storage - **Up** |

---

## 🔍 How to Check Host Status

**If host shows "Connecting":**
- Wait 30-60 seconds (detection takes time)
- Refresh page (F5)
- Check SSH connectivity: `ping 103.9.159.151`

**If host shows "Disconnected":**
- Click host name → check error text
- See troubleshooting section in START-HERE.md

---

## 📊 What You're Creating

```
Zone (Logic grouping)
  └── Pod (Physical rack)
      └── Cluster (Group of hypervisors)
          ├── Host 1 (103.9.159.151)
          ├── Host 2 (103.9.159.165)
          └── Host 3 (103.9.159.188)

Storage (Separate)
├── Primary Storage (VM disk I/O)
└── Secondary Storage (Templates & ISOs)
```

---

## ⏰ Time Estimates

| Task | Time |
|------|------|
| Create Zone | 2 minutes |
| Create Pod | 2 minutes |
| Create Cluster | 2 minutes |
| Add 3 Hosts | 10-15 minutes (includes detection time) |
| Add Primary Storage | 2 minutes |
| Add Secondary Storage | 2 minutes |
| **Total** | **20-25 minutes** |

---

## 🔑 Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Refresh page | F5 |
| Open browser console | F12 |
| Go back | Alt + ← |

---

## 💾 Passwords Reference

```
Management Host: 103.9.157.6
  (SSH root / Admin@@3224@@)

KVM Host 1: 103.9.159.151
  (SSH root / Admin@@3224@@)

KVM Host 2: 103.9.159.165
  (SSH root / Admin@@3224@@)

KVM Host 3: 103.9.159.188
  (SSH root / Admin@@3224@@)

CloudStack UI: admin / password
```

---

## 📞 Emergency Contacts

**If infrastructure doesn't show after creation:**

1. Check host is reachable:
   ```bash
   ping 103.9.159.151
   ```

2. Check libvirtd is running:
   ```bash
   sshpass -p 'Admin@@3224@@' ssh root@103.9.159.151 'systemctl is-active libvirtd'
   ```

3. Check firewall allows CloudStack:
   ```bash
   sshpass -p 'Admin@@3224@@' ssh root@103.9.159.151 'ufw status'
   ```

4. See detailed troubleshooting in: `START-HERE.md` → Troubleshooting section

---

**Created:** 2026-03-12
**CloudStack Version:** 4.23.0.0-SNAPSHOT
**Hypervisor:** KVM (Open Source)
