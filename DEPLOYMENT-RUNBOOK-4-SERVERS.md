# CloudStack Deployment Runbook - 4 Servers

Cap nhat: 2026-03-11

## 1) Hien trang va ket luan nhanh

Servers:
- 103.9.157.6 (96 GB RAM) - dang co source cloudstack
- 103.9.159.151 (64 GB RAM)
- 103.9.159.165 (48 GB RAM)
- 103.9.159.188 (64 GB RAM)

Kien truc de xuat (khuyen nghi):
- 103.9.157.6: Management + DB (va co the build source)
- 103.9.159.151: KVM Host #1
- 103.9.159.165: KVM Host #2
- 103.9.159.188: KVM Host #3

LB la gi?
- LB = Load Balancer (can bang tai), dung de nhan traffic domain cloudstack.vnso.vn va chuyen vao Management API/UI.
- Giai doan dau, co the chay Nginx reverse proxy ngay tren 103.9.157.6 (khong bat buoc tach may rieng).

## 2) Kha nang toi da voi bo 4 server nay

Voi 3 KVM host (64 + 48 + 64 GB), reserve 20%, oversubscribe CPU 2.0:
- Co the van hanh cum production nho.
- Muc deploy an toan ban dau:
  - Small (1 vCPU/2GB): 25-40 VM
  - Medium (2 vCPU/4GB): 12-20 VM
  - Large (4 vCPU/8GB): 6-10 VM

So luong thuc te phu thuoc IOPS storage va workload tung VM.

## 3) Pre-check truoc trien khai

Tren may local dieu phoi (hoac 103.9.157.6), check SSH:

```bash
for ip in 103.9.157.6 103.9.159.151 103.9.159.165 103.9.159.188; do
  echo "== $ip =="
  ping -c 1 -W 1 $ip && echo ok || echo fail
  timeout 3 bash -lc "</dev/tcp/$ip/22" && echo ssh-open || echo ssh-closed
done
```

Can chot:
- OS tren 3 host KVM: Ubuntu 22.04 LTS hoac Rocky 9
- VLAN/subnet: Management, Storage, Guest, Public
- DNS A record cloudstack.vnso.vn
- Firewall policy

## 4) Mang de xuat

VLAN/subnet mau:
- VLAN10 Management: 10.10.10.0/24
- VLAN20 Storage: 10.10.20.0/24
- VLAN30 Guest: 10.20.0.0/16
- Public: theo ISP cap (cac IP 103.9.157.6, 103.9.159.151, 103.9.159.165, 103.9.159.188)

Khuyen nghi:
- Khong expose truc tiep backend management port ra Internet.
- Chi expose qua Nginx/LB (443).

## 5) Phan bo role chi tiet

Option A (khuyen nghi):
- 103.9.157.6: CloudStack Management + MySQL + Nginx reverse proxy
- 103.9.159.151: KVM host
- 103.9.159.165: KVM host
- 103.9.159.188: KVM host

Option B (neu muon tach edge):
- 103.9.157.6: Management + MySQL
- 103.9.159.151: KVM host
- 103.9.159.165: KVM host
- 103.9.159.188: KVM host + Nginx reverse proxy

## 6) Quy trinh trien khai tung buoc

### Buoc 1 - Chuan bi 103.9.157.6 (Management)

1. Pull source cloudstack fork.
2. Build artifact (neu can):
```bash
cd /root/cloudstack
mvn -f client/pom.xml package -DskipTests
```
3. Start management (theo stack dang dung):
```bash
cd /root/cloudstack
docker compose -f docker-compose.prod.yml up -d
```
4. Kiem tra:
```bash
docker ps | grep cloudstack
curl -I http://127.0.0.1:28080/client/
```

### Buoc 2 - Cai KVM tren 3 host compute

Thuc hien tren 103.9.159.151, 103.9.159.165, 103.9.159.188.

Ubuntu:
```bash
apt update
apt install -y qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils virtinst
systemctl enable --now libvirtd
```

Kiem tra:
```bash
kvm-ok || true
virsh list --all
ip a
```

### Buoc 3 - Cau hinh bridge

Toi thieu can 2 bridge:
- br-mgmt (management traffic)
- br-guest (guest/public trunk)

Sau khi cau hinh xong, xac nhan host van SSH on dinh va bridge co IP dung.

### Buoc 4 - Secondary Storage (NFS)

Co the dat tren 103.9.157.6 giai doan dau:
```bash
apt install -y nfs-kernel-server
mkdir -p /export/secondary
chown -R nobody:nogroup /export/secondary
echo "/export/secondary 10.10.0.0/16(rw,sync,no_subtree_check,no_root_squash)" >> /etc/exports
exportfs -rav
systemctl restart nfs-kernel-server
```

### Buoc 5 - Cau hinh trong CloudStack UI

1. Login admin
2. Tao Zone (Advanced)
3. Tao Pod
4. Tao Cluster (KVM)
5. Add 3 hosts KVM
6. Add Primary Storage
7. Add Secondary Storage: nfs://<ip-nfs>/export/secondary
8. Tao Network Offering
9. Tao guest network

### Buoc 6 - Domain va reverse proxy

DNS:
- cloudstack.vnso.vn -> IP public endpoint (LB/Nginx)

Nginx toi thieu:
- /client/ -> http://127.0.0.1:28080/client/
- /client/api/ -> http://127.0.0.1:28080/client/api/

### Buoc 7 - Tao VM dau tien

1. Register template (Ubuntu)
2. Tao service offering (small)
3. Deploy VM
4. Test SSH, ping, Internet egress

## 7) Bao mat bat buoc

- Doi ngay mat khau DB mac dinh cloud/cloud.
- Mo duy nhat 443 public; han che 28080 chi noi bo.
- Bat backup DB hằng ngay.
- Luu file secret o quyen 600.

## 8) Muc tieu test go-live

P0 (bat buoc):
- Management UI/API on
- 3 host KVM o trang thai Up
- Deploy thanh cong >= 3 VM test
- VM co network va SSH duoc

P1 (on dinh):
- Deploy lo 10 VM khong fail nghiem trong
- Snapshot/restore 1 VM thanh cong
- Tat 1 host compute, host con lai van deploy duoc VM moi

P2 (van hanh):
- Backup/restore DB thu nghiem thanh cong
- Co canh bao CPU/RAM/Disk
- Co runbook xu ly su co

## 9) De xuat nang cap tiep

- Them 1 node rieng cho DB de giam rui ro.
- Them storage rieng >= 1-2 TB cho secondary/backup.
- Neu can HA management, them 1 management node nua + LB active/standby.
- Mo rong pool public IP neu can cap EIP cho nhieu VM.

## 10) Ghi chu ve SSH tu dong

Tu moi truong hien tai, da xac nhan 4 may mo cong SSH.
De trien khai tu dong full, nen dung 1 trong 2 cach:
- SSH key-based auth (khuyen nghi)
- Hoac cai cong cu auth non-interactive (sshpass/ansible vault) trong moi truong dieu phoi

Khi da co key SSH, co the chay provisioning script/ansible de deploy dong loat.

## 11) Ban "khong ranh ky thuat" - lam theo dung 6 lenh

Neu ban muon lam nhanh va de nhat, chi can theo dung thu tu sau tren may dieu phoi:

1) Cai Ansible:
```bash
apt update && apt install -y ansible openssh-client
```

2) Tao SSH key:
```bash
ssh-keygen -t ed25519 -C "cloudstack-ansible" -f ~/.ssh/id_ed25519 -N ""
```

3) Copy key sang 4 server:
```bash
ssh-copy-id root@103.9.157.6
ssh-copy-id root@103.9.159.151
ssh-copy-id root@103.9.159.165
ssh-copy-id root@103.9.159.188
```

4) Test ket noi:
```bash
cd /root/cloudstack/deploy/ansible
ansible -i inventory.ini all -m ping
```

5) Chay auto setup:
```bash
cd /root/cloudstack/deploy/ansible
ansible-playbook -i inventory.ini playbooks/site.yml
```

6) Chay management service:
```bash
cd /root/cloudstack
docker compose -f docker-compose.prod.yml up -d
```

Sau 6 lenh tren, ban vao CloudStack UI de add Zone/Pod/Cluster/Hosts/Storage va deploy VM dau tien.

Tai lieu/tep da tao san:
- Scripts copy-paste:
  - /root/cloudstack/deploy/scripts/01-management-setup.sh
  - /root/cloudstack/deploy/scripts/02-kvm-host-setup.sh
  - /root/cloudstack/deploy/scripts/03-quick-verify.sh
- Ansible:
  - /root/cloudstack/deploy/ansible/inventory.ini
  - /root/cloudstack/deploy/ansible/group_vars/all.yml
  - /root/cloudstack/deploy/ansible/playbooks/site.yml
  - /root/cloudstack/deploy/ansible/README.md
