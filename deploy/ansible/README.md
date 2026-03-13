# CloudStack 4-Server Ansible Quickstart

Muc tieu: Ban chi can them SSH key la chay duoc setup co ban cho 1 management + 3 KVM hosts.

## 1) Chuan bi may dieu phoi (run Ansible)

Neu ban dang gap loi nhu sau tren Ubuntu moi / Python 3.12 host:

```text
ModuleNotFoundError: No module named 'ansible.module_utils.six.moves'
```

thi nguyen nhan thuong la may dieu phoi dang dung `ansible 2.10.x` qua cu so voi remote Python 3.12.
Khac phuc bang cach dung `ansible-core` moi hon trong virtualenv.

```bash
apt update
apt install -y openssh-client python3-pip python3-venv
python3 -m venv /root/cloudstack/deploy/ansible/.venv
/root/cloudstack/deploy/ansible/.venv/bin/pip install --upgrade pip
/root/cloudstack/deploy/ansible/.venv/bin/pip install 'ansible-core>=2.17,<2.18'
```

Sau do, o cac lenh ben duoi, thay `ansible` bang:

```bash
/root/cloudstack/deploy/ansible/.venv/bin/ansible
```

va thay `ansible-playbook` bang:

```bash
/root/cloudstack/deploy/ansible/.venv/bin/ansible-playbook
```

## 2) Tao SSH key va copy sang 4 host

```bash
ssh-keygen -t ed25519 -C "cloudstack-ansible" -f ~/.ssh/id_ed25519 -N ""
ssh-copy-id root@103.9.157.6
ssh-copy-id root@103.9.159.151
ssh-copy-id root@103.9.159.165
ssh-copy-id root@103.9.159.188
```

## 3) Kiem tra ket noi

```bash
cd /root/cloudstack/deploy/ansible
/root/cloudstack/deploy/ansible/.venv/bin/ansible -i inventory.ini all -m ping
```

## 4) Chinh bien trien khai (neu can)

Sua file:
- /root/cloudstack/deploy/ansible/group_vars/all.yml

Thong so can de y:
- cloudstack_domain
- management_private_ip
- secondary_storage_path
- nfs_export_cidr

## 5) Chay playbook

```bash
cd /root/cloudstack/deploy/ansible
/root/cloudstack/deploy/ansible/.venv/bin/ansible-playbook -i inventory.ini playbooks/site.yml
```

## 5.1) Neu virtualenv chua tao duoc

Neu ban thay loi `ensurepip is not available`, chay them:

```bash
apt install -y python3-venv
python3 -m venv /root/cloudstack/deploy/ansible/.venv
/root/cloudstack/deploy/ansible/.venv/bin/pip install --upgrade pip
/root/cloudstack/deploy/ansible/.venv/bin/pip install 'ansible-core>=2.17,<2.18'
```

## 6) Sau khi playbook xong

1. Tren management host, deploy CloudStack service:
```bash
cd /root/cloudstack
docker compose -f docker-compose.prod.yml up -d
```

2. Kiem tra endpoint:
```bash
curl -I http://103.9.157.6:28080/client/
```

3. Vao UI CloudStack de:
- Tao Zone/Pod/Cluster
- Add 3 KVM hosts
- Add primary storage
- Add secondary storage: nfs://<management-ip>/export/secondary
- Tao guest network va deploy VM test

## 7) Neu ban muon chay script copy-paste thay cho Ansible

- Management host:
  - /root/cloudstack/deploy/scripts/01-management-setup.sh
- Moi KVM host:
  - /root/cloudstack/deploy/scripts/02-kvm-host-setup.sh
- Verify endpoint:
  - /root/cloudstack/deploy/scripts/03-quick-verify.sh

## 7.1) Playbook nang cao de tao bridge mang tu dong

Tep da tao san:
- /root/cloudstack/deploy/ansible/playbooks/network-bridges.yml
- /root/cloudstack/deploy/ansible/templates/netplan-bridges.yaml.j2
- /root/cloudstack/deploy/ansible/host_vars.example.yml

Cach dung:

1. Tao file rieng cho tung host KVM:

```bash
cd /root/cloudstack/deploy/ansible
cp host_vars.example.yml host_vars/103.9.159.151.yml
cp host_vars.example.yml host_vars/103.9.159.165.yml
cp host_vars.example.yml host_vars/103.9.159.188.yml
```

2. Sua dung ten NIC tung may, vi du `eno1`, `eno2`, `ens3`.

3. Chay playbook bridge:

```bash
cd /root/cloudstack/deploy/ansible
/root/cloudstack/deploy/ansible/.venv/bin/ansible-playbook -i inventory.ini playbooks/network-bridges.yml
```

Canh bao:
- Playbook nay sua netplan tren remote host.
- Neu dien sai ten NIC, host co the mat mang.
- Hay test tung host mot neu ban chua chac ten card:

```bash
/root/cloudstack/deploy/ansible/.venv/bin/ansible -i inventory.ini 103.9.159.151 -m shell -a 'ip -br a'
```

## 8) Luu y

- Playbook nay setup he thong nen can root access.
- Nginx TLS duoc template san, ban can dat dung file cert:
  - /etc/nginx/ssl/cert.crt
  - /etc/nginx/ssl/cert.key
- Khong expose truc tiep management backend ra Internet neu khong can thiet.
