# CloudStack Plan Cho 3 Server

> Cap nhat: 2026-03-11  
> Muc tieu: Co mot cum CloudStack chay duoc production nho voi 3 server vat ly

---

## 1. Ket luan nhanh: Nen chon KVM hay VMware?

Voi bai toan hien tai chi co `3` server, khuyen nghi uu tien **KVM**.

### Vi sao nen chon KVM

- Khong ton phi license hypervisor.
- CloudStack + KVM la cap pho bien, tai lieu va cong dong nhieu.
- Van hanh don gian hon VMware stack (vCenter, ESXi licensing, cert, lifecycle).
- Phu hop giai doan dau khi can toi uu chi phi.

### Khi nao nen chon VMware

- Da co san license VMware + doi van hanh quen ESXi/vCenter.
- Can tiep tuc dung he sinh thai VMware hien huu va quy trinh enterprise da chuan hoa.

### Khuyen nghi thuc te cho ban

- Neu chua bi rang buoc license/nhan su: **chon KVM**.
- Neu cong ty da co VMware enterprise san: co the chon VMware, nhung tong chi phi van hanh thuong cao hon.

---

## 2. Voi 3 server thi lam duoc gi?

Co the dung de chay mot cum CloudStack production nho (SMB/internal), nhung can chap nhan gioi han sau:

- Chay duoc 1 zone, 1 pod, 1 cluster KVM.
- Chay duoc vai chuc VM nho-trung binh (tuy cau hinh tung may).
- Khong phu hop workload mission-critical can HA muc cao va DR day du.

### Cac mo hinh kha thi voi 3 server

1. **Mo hinh can bang (khuyen nghi)**
- `Node-1`: Management + DB + Secondary Storage (co backup).
- `Node-2`: KVM Compute.
- `Node-3`: KVM Compute.
- Uu diem: de lam, it thanh phan.
- Nhuoc diem: management va DB cung mot node.

2. **Mo hinh toi uu compute**
- `Node-1`: Management + DB.
- `Node-2`: KVM Compute.
- `Node-3`: KVM Compute + Secondary Storage.
- Uu diem: nhieu tai nguyen cho VM hon.
- Nhuoc diem: storage tren compute node, can theo doi I/O ky.

3. **Mo hinh pseudo-HA nhe**
- `Node-1`: Management.
- `Node-2`: DB + Secondary Storage.
- `Node-3`: KVM Compute.
- Uu diem: tach vai tro ro hon.
- Nhuoc diem: compute con 1 host, khong hop ly cho production co tai.

=> Voi 3 server, mo hinh so 1 thuong la tot nhat de bat dau.

---

## 3. Bang mua sam/cap phat tai nguyen (ban gui nha cung cap)

## 3.1 BOM cuc ngan gon

| Hang muc | So luong | Cau hinh de xuat toi thieu |
|---|---:|---|
| Server vat ly | 3 | 16-24 cores, RAM 128 GB, 2 x SSD/NVMe cho OS, 4 x disk cho data (neu dung local/Ceph) |
| NIC moi server | 2 | 10GbE khuyen nghi (toi thieu 1GbE) |
| Switch | 1-2 | Ho tro VLAN trunk |
| Public IP block | 1 block | toi thieu /28 |
| Secondary storage | 1 | NFS 2 TB tro len |
| Backup storage | 1 | 2-4 TB tro len |

## 3.2 VLAN/IP cap phat mau

| VLAN | Muc dich | Subnet mau |
|---|---|---|
| 10 | Management | 10.10.10.0/24 |
| 20 | Storage | 10.10.20.0/24 |
| 30 | Guest | 10.20.0.0/16 |
| 40 | Public | 203.0.113.0/28 |

---

## 4. Runbook tung buoc (tu 0 den VM dau tien)

Phan nay theo huong **KVM + CloudStack**.

## 4.1 Gia dinh

- OS cho KVM host: Ubuntu 22.04 LTS hoac Rocky Linux 9.
- CloudStack Management dang chay container nhu he hien tai.
- Co 3 host dat ten: `cs-node1`, `cs-node2`, `cs-node3`.

## 4.2 Phan bo vai tro

- `cs-node1`: CloudStack Management, MySQL, Secondary Storage (NFS).
- `cs-node2`: KVM Compute Host #1.
- `cs-node3`: KVM Compute Host #2.

## 4.3 Chuan bi he dieu hanh va mang (ca 3 node)

1. Dat hostname, DNS, NTP dong bo.
2. Cau hinh 2 NIC (hoac bond):
- NIC-MGMT: VLAN 10 (10.10.10.0/24)
- NIC-STORAGE/GUEST: VLAN 20/30 trunk
3. Tat SWAP neu can theo policy; set timezone.
4. Mo firewall cac port can cho management, agent, libvirt, NFS.

## 4.4 Cai KVM tren `cs-node2`, `cs-node3`

1. Cai package KVM/libvirt/bridge-utils/qemu.
2. Bat va enable libvirtd.
3. Tao Linux bridge (`br-mgmt`, `br-guest`) map vao NIC/VLAN.
4. Kiem tra `kvm-ok`, `virsh list`, bridge len dung IP.

## 4.5 Chuan bi Secondary Storage tren `cs-node1`

1. Tao volume cho secondary storage, vd `/export/secondary`.
2. Cai NFS server.
3. Export NFS cho subnet management/compute.
4. Test mount tu `cs-node2` va `cs-node3`.

## 4.6 Chuan bi Management + DB tren `cs-node1`

1. Dung stack hien tai trong source `cloudstack`.
2. Doi mat khau DB manh hon (`cloud/cloud` -> strong password).
3. Dam bao management API/UI truy cap on qua domain.
4. Backup dinh ky db + file config trong `docker/conf`.

## 4.7 Tao cau hinh trong CloudStack UI

1. Dang nhap admin vao CloudStack UI.
2. Tao `Zone` (Basic hoac Advanced; thuong chon Advanced).
3. Tao `Pod` voi dải management.
4. Tao `Cluster` loai KVM.
5. Add `Host`:
- `cs-node2` (KVM)
- `cs-node3` (KVM)
6. Add `Primary Storage`:
- Neu ban dau chua co Ceph/iSCSI: co the dung NFS de khoi dong nhanh.
7. Add `Secondary Storage`:
- NFS URL: `nfs://cs-node1/export/secondary`
8. Add traffic labels/network offerings theo VLAN plan.

## 4.8 Dua template va tao VM dau tien

1. Register/download template (VD Ubuntu cloud image).
2. Tao service offering (1 vCPU, 2 GB RAM de test).
3. Tao network guest (VLAN 30).
4. Deploy VM dau tien.
5. Kiem tra:
- VM nhan IP.
- Ping/SSH duoc.
- Console access duoc.

## 4.9 Kiem tra sau trien khai

- Tat 1 host compute, kiem tra VM deployment con hoat dong tren host con lai.
- Kiem tra I/O secondary storage.
- Kiem tra backup va restore DB (dry run).
- Kiem tra canh bao CPU/RAM/disk.

---

## 5. Lo trinh nang cap sau khi vuot qua 3 server

1. Them server thu 4: tach rieng DB khoi management.
2. Them server thu 5: thanh management node thu 2 (HA).
3. Chuyen primary storage sang Ceph/HA storage.
4. Bo sung LB cap doi va monitoring/logging day du.

---

## 6. Checklist chot de trien khai ngay

- Chon hypervisor: **KVM**.
- Chot role 3 node theo mo hinh can bang.
- Chot VLAN/IP plan.
- Chot secondary storage NFS.
- Chot chinh sach backup DB + config.
- Chot tieu chi capacity VM giai doan 1.

---

## 7. Capacity calculator (small/medium/large)

Muc tieu: nhap CPU/RAM cua tung server de uoc tinh nhanh so VM co the chay an toan.

### 7.1 Input can nhap

- `C2`, `C3`: so core cua `cs-node2`, `cs-node3` (compute hosts).
- `R2`, `R3`: tong RAM (GB) cua `cs-node2`, `cs-node3`.
- `RsvCPU`: ti le reserve CPU cho host/system VM (khuyen nghi `20%`).
- `RsvRAM`: ti le reserve RAM cho host/system VM (khuyen nghi `20%`).
- `OvsCPU`: he so oversubscribe vCPU (goi y: `2.0` cho workload thong thuong).

### 7.2 Cong thuc

1. Tong core compute:

$$
C_{total} = C2 + C3
$$

2. vCPU kha dung (sau reserve + oversubscribe):

$$
vCPU_{usable} = C_{total} \times (1 - RsvCPU) \times OvsCPU
$$

3. Tong RAM compute:

$$
RAM_{total} = R2 + R3
$$

4. RAM kha dung:

$$
RAM_{usable} = RAM_{total} \times (1 - RsvRAM)
$$

5. So VM toi da theo profile `p`:

$$
VM_{max,p} = \min\left(\left\lfloor\frac{vCPU_{usable}}{vCPU_p}\right\rfloor,\left\lfloor\frac{RAM_{usable}}{RAM_p}\right\rfloor\right)
$$

### 7.3 Profile VM de tinh nhanh

| Profile | vCPU/VM | RAM/VM |
|---|---:|---:|
| Small | 1 | 2 GB |
| Medium | 2 | 4 GB |
| Large | 4 | 8 GB |

### 7.4 Vi du tinh mau (de tham chieu)

Gia su:
- `cs-node2`: 24 cores, 128 GB RAM
- `cs-node3`: 24 cores, 128 GB RAM
- `RsvCPU = 20%`, `RsvRAM = 20%`, `OvsCPU = 2.0`

Tinh:
- `C_total = 48`
- `vCPU_usable = 48 x 0.8 x 2.0 = 76.8` ~ `76 vCPU`
- `RAM_total = 256 GB`
- `RAM_usable = 256 x 0.8 = 204.8 GB` ~ `204 GB`

Bang ket qua:

| Profile | Theo CPU | Theo RAM | VM uoc tinh an toan |
|---|---:|---:|---:|
| Small (1vCPU/2GB) | 76 | 102 | **76 VM** |
| Medium (2vCPU/4GB) | 38 | 51 | **38 VM** |
| Large (4vCPU/8GB) | 19 | 25 | **19 VM** |

Ghi chu:
- So lieu tren la muc ly thuyet cho workload deu.
- Neu workload I/O cao (DB, cache, log-heavy), nen giam 20-30%.
- Neu can SLA on dinh, nen giam tiep de chua headroom failover.

### 7.5 Bang de dien nhanh (copy va dien so)

| Tham so | Gia tri cua ban |
|---|---|
| C2 (cores) | ... |
| C3 (cores) | ... |
| R2 (GB RAM) | ... |
| R3 (GB RAM) | ... |
| RsvCPU | 0.20 |
| RsvRAM | 0.20 |
| OvsCPU | 2.0 |

| Ket qua | Gia tri |
|---|---:|
| vCPU_usable | ... |
| RAM_usable (GB) | ... |
| VM Small | ... |
| VM Medium | ... |
| VM Large | ... |

---

## 8. Muc tieu test cho cum 3 server

Muc tieu test duoc chia theo 3 muc: P0 (bat buoc), P1 (on dinh), P2 (san sang van hanh).

### 8.1 P0 - Bat buoc truoc khi go-live

1. Management UI/API hoat dong on:
- Dang nhap UI thanh cong.
- Goi API list zones/hosts/templates thanh cong.

2. Compute host san sang:
- Ca `cs-node2`, `cs-node3` o trang thai `Up` trong CloudStack.
- Deploy duoc VM tren ca 2 host.

3. Storage thong suot:
- Secondary storage mount on.
- Upload/register template thanh cong.
- Tao volume va attach VM thanh cong.

4. Network thong suot:
- VM nhan IP dung network offering.
- VM ping gateway, DNS, Internet (neu co NAT/public).
- SSH vao VM duoc.

5. Security co ban:
- Da doi mat khau DB mac dinh.
- Port management khong public truc tiep (chi qua proxy/LB/VPN).

### 8.2 P1 - Test on dinh va kha nang chiu loi

1. Host failure test:
- Tat 1 compute host, xac minh host con lai van deploy duoc VM moi.
- Kiem tra canh bao va event log dung.

2. Stress nhe:
- Deploy hang loat 10-20 VM lien tiep.
- Kiem tra thoi gian boot trung binh, ty le fail deploy.

3. Storage performance sanity:
- Chay fio co ban tren 1-2 VM test.
- Theo doi latency/read-write co nam trong nguong cho phep.

4. Snapshot/restore:
- Tao snapshot volume.
- Restore volume tu snapshot thanh cong.

### 8.3 P2 - San sang van hanh

1. Backup/restore DB:
- Backup dinh ky (daily).
- Thu restore vao environment test va xac nhan data dung.

2. Monitoring/alert:
- Co metric CPU/RAM/disk cho host va container chinh.
- Co alert khi disk > 80%, RAM > 85%, host down.

3. Runbook su co:
- Co tai lieu xu ly: host down, storage full, API 5xx, DB restart.
- Co nguoi truc va SLA phan hoi.

### 8.4 Tieu chi pass de chot go-live (de xac nhan voi team)

- P0: Dat `100%`.
- P1: Dat >= `90%` testcase, khong loi nghiem trong mo.
- P2: Co backup restore test thanh cong va dashboard monitoring co alert.

### 8.5 Mau test plan 7 ngay

| Ngay | Muc tieu | Dau ra |
|---|---|---|
| Day 1 | Hoan tat setup zone/pod/cluster/storage | Co 2 host `Up`, co template |
| Day 2 | Deploy VM co ban + network | VM SSH duoc |
| Day 3 | Batch deploy 10-20 VM | So lieu thoi gian deploy |
| Day 4 | Snapshot/restore test | Bao cao pass/fail |
| Day 5 | Host failure test | Bao cao impact + recovery |
| Day 6 | Backup/restore DB test | Bien ban restore thanh cong |
| Day 7 | Chot tuning + go/no-go | Checklist go-live ky duyet |

---

## 9. Cau hinh de xuat cho dung bo 3 server cua ban

Thong so ban cung cap:

| Node | Model | CPU | RAM | Disk |
|---|---|---|---:|---|
| Server A | BL460c G8 | E5-2680 x1 (8 cores) | 64 GB | SSD 250 GB x1 |
| Server B | BL460c G8 | E5-2680 x1 (8 cores) | 16 GB | SSD 250 GB x1 |
| Server C | BL460c G8 | E5-2680 x1 (8 cores) | 64 GB | SSD 250 GB x1 |

Public IP hien co:
- `103.9.159.186`
- `103.9.159.187`
- `103.9.159.188`

### 9.1 Kien truc nen dung ngay

Voi bo tai nguyen nay, nen chia vai tro nhu sau:

- `Server B (16 GB RAM)`: Management + DB + Secondary Storage.
- `Server A (64 GB RAM)`: KVM Compute Host #1.
- `Server C (64 GB RAM)`: KVM Compute Host #2.

Ly do:
- Node 16 GB khong phu hop lam compute host cho VM tenant.
- 2 node 64 GB se dung lam compute de giu suc chua VM.

### 9.2 Gan 3 IP public

Phuong an khuyen nghi:

- `103.9.159.186`: API/UI CloudStack (Nginx/LB endpoint) - domain `cloudstack.vnso.vn`.
- `103.9.159.187`: Public IP cho Virtual Router/EIP pool (tenant).
- `103.9.159.188`: Du phong (LB them, NAT bo sung, hoac service management khac).

Luu y quan trong:
- Chi co 3 IP public thi kha nang cap EIP se rat han che.
- Giai doan dau nen uu tien mo hinh private guest + SNAT.

### 9.3 Mang/VLAN toi thieu de chay on

| VLAN | Muc dich | Goi y subnet |
|---|---|---|
| 10 | Management | 10.10.10.0/24 |
| 20 | Storage | 10.10.20.0/24 |
| 30 | Guest private | 10.20.0.0/16 |
| 40 | Public | 103.9.159.184/29 hoac theo subnet ISP cap |

Neu chua tach duoc nhieu NIC/VLAN ngay:
- Van co the chay trunk tren 1 uplink, nhung phai tach VLAN logic ro rang.

### 9.4 Capacity thuc te voi 2 compute node (64 GB + 64 GB)

Gia su:
- Moi compute node: 8 cores, 64 GB RAM
- Reserve 20% cho host/system
- CPU oversubscribe 2.0

Tinh nhanh:
- Tong cores compute: `16`
- vCPU usable: `16 x 0.8 x 2.0 = 25.6` ~ `25`
- RAM usable: `(64+64) x 0.8 = 102.4 GB` ~ `102 GB`

Uoc tinh VM an toan:

| Profile | Dinh nghia | So VM uoc tinh |
|---|---|---:|
| Small | 1 vCPU / 2 GB | ~25 VM |
| Medium | 2 vCPU / 4 GB | ~12 VM |
| Large | 4 vCPU / 8 GB | ~6 VM |

Khuyen nghi thuc te:
- Ban dau chi nen target `15-20 VM small` hoac `8-10 VM medium` de giu headroom.

### 9.5 Runbook cau hinh cu the cho bo 3 server nay

1. Chot IP quan tri noi bo:
- `Server B`: 10.10.10.11
- `Server A`: 10.10.10.21
- `Server C`: 10.10.10.22

2. Tren `Server A/C`:
- Cai KVM + libvirt + bridge.
- Tao bridge management (vi du `br-mgmt`) va guest trunk (vi du `br-guest`).

3. Tren `Server B`:
- Chay management stack CloudStack hien tai.
- Chuyen DB password sang mat khau manh.
- Tao NFS secondary storage (vi du `/export/secondary`).

4. Tren CloudStack UI:
- Tao Zone (Advanced).
- Tao Pod (management network 10.10.10.0/24).
- Tao Cluster (KVM).
- Add 2 hosts: `Server A`, `Server C`.
- Add Secondary Storage: `nfs://10.10.20.11/export/secondary` (doi IP dung theo host that).
- Add Primary Storage: NFS toi thieu de khoi dong nhanh.

5. Public endpoint:
- Gan DNS `cloudstack.vnso.vn` -> `103.9.159.186`.
- Nginx reverse proxy vao management API/UI.

6. Guest network:
- Tao network offering co SNAT.
- Dung `103.9.159.187` lam IP public dau tien cho VR/NAT.
- `103.9.159.188` de du phong/floating.

7. Deploy VM test:
- 3 VM small, 2 VM medium.
- Test SSH, outbound Internet, snapshot, stop/start/reboot.

### 9.6 Nhung gioi han can biet ngay

- Chua co HA management/db that su (vi dang gom tren node 16 GB).
- Storage 250 GB moi node rat han che, khong hop luu nhieu template/snapshot.
- 3 IP public se nhanh het neu cap EIP theo VM.

### 9.7 Nang cap toi thieu nen lam tiep

1. Them 1 server nua de tach DB rieng.
2. Them dung luong storage (NAS/Ceph/NFS) it nhat 1-2 TB.
3. Mua them block IP public neu can EIP nhieu hon.
4. Them monitoring + backup restore dinh ky.

---

Tai lieu nay de xuat theo hien trang he thong va muc tieu toi uu chi phi ban dau.