# CloudStack Docker Setup - Complete Guide

## ✅ What's Been Done

### 1. **Infrastructure Setup**
- ✅ Docker Dockerfile created (`Dockerfile.prod`)
  - Multi-stage build: Maven stage + Tomcat stage
  - Automatically builds CloudStack WAR from source
  - ~450MB final image size

- ✅ Docker Compose configuration (`docker-compose.prod.yml`)
  - CloudStack Management Server on port 28080 (internal to container)
  - MySQL 8.0 database on port 3307 (for host access)
  - Automatic health checks
  - Persistent volumes for data

- ✅ Nginx reverse proxy updated
  - Domain: `cloudstack.vnso.vn` with wildcard SSL
  - Paths:
    - `/` → CloudStack UI (host:25050)
    - `/client/api/` → CloudStack API (container:28080)

### 2. **Files Created**
```
/root/cloudstack/
├── Dockerfile.prod (multi-stage: Maven build + Tomcat)
├── docker-compose.prod.yml (MySQL + Management Server)
└── scripts/vnso/docker-start.sh (startup helper script)
```

### 3. **Current Status**
- Building Docker image (in progress)
- MySQL container ready to start
- Nginx proxy configured and validated
- UI still running on port 25050

---

## 🚀 Quick Start

###  After image build completes:
```bash
cd /root/cloudstack

# Start all services
docker compose -f docker-compose.prod.yml up -d

# Check status
docker compose -f docker-compose.prod.yml ps

# View logs
docker logs cloudstack-mgmt -f
docker logs cloudstack-db -f
```

### Access CloudStack
- **HTTPS Domain**: https://cloudstack.vnso.vn
  - Dashboard: `/`
  - API: `/client/api/`
- **Direct (host)**:
  - UI: http://127.0.0.1:25050
  - Management API: http://127.0.0.1:28080/client/api/

### Database Access
```
Host: localhost:3307
Database: cloud
User: cloud / cloud
Root: root / BUr6^xNdU+5$%aL+At2W5mSdK88-rf
```

### Admin API Key Rotation
```bash
cd /root/cloudstack
./scripts/vnso/rotate-admin-api-key.sh
```

This writes the active admin API keypair to `docker/secrets/admin-api-key.env` with `0600` permissions and rotates any older active keypairs for the admin user out of service.

---

## 📋 Architecture

```
┌─────────────────────────────────────────┐
│        Nginx Reverse Proxy (Docker)     │
│       Port 443 (HTTPS)                  │
│     cloudstack.vnso.vn                  │
└────────────┬─────────────────────────────┘
             │
    ┌────────┴──────────┐
    │                   │
┌───▼────────────┐  ┌──▼──────────────┐
│  UI Container  │  │ Backend Docker  │
│ (npm dev)      │  │  (Tomcat)       │
│ :25050         │  │  :28080         │
│ on Host        │  │ in Container    │
└────────────────┘  └─────────────────┘
                         │
                    ┌────▼────────────┐
                    │ MySQL (Docker)  │
                    │ :3307           │
                    │ in Container    │
                    └─────────────────┘
```

---

## ⚡ Key Features

1. **Sạch hơn** - Everything containerized except UI dev server
2. **Tự động xử lý** - Docker Compose manages both services
3. **Health checks** - Automatic restart if service crashes
4. **Persistent data** - MySQL data survives restarts
5. **Single command startup** - `docker compose up -d`

---

## 🔧 Troubleshooting

### If Management Server won't start
```bash
# Check logs
docker logs cloudstack-mgmt -f

# Common issues:
# - MySQL not ready yet (wait 30-60s)
# - Spring context initialization (see logs for specific beans)
# - Port conflict (check docker ps)
```

### If API endpoint returns 502
```bash
# Check backend is running
docker logs cloudstack-mgmt | grep -i "started\|error\|listening"

# Verify port binding
netstat -tlnp | grep 28080
```

### To restart services
```bash
# Stop all
docker compose -f docker-compose.prod.yml down

# Start fresh
docker compose -f docker-compose.prod.yml up -d
```

---

## 📝 Notes

- **First start**: May take 2-3 minutes (Tomcat startup + Spring initialization)
- **Database initialization**: Happens automatically on container start
- **Logs location**: Docker logs command (see Troubleshooting)
- **WAR file**: Built inside Docker image, not cached locally

---

## ✅ Testing Checklist

After startup, verify:
```bash
# 1. UI is accessible
curl -I http://127.0.0.1:25050

# 2. API endpoint exists
curl -I http://127.0.0.1:28080/client/api/

# 3. Domain proxy works
curl -kI https://cloudstack.vnso.vn

# 4. Database is accessible
mysql -h localhost -P 3307 -u cloud -p'cloud' -e "SELECT VERSION();"

# 5. Docker services healthy
docker compose -f docker-compose.prod.yml ps
```

---

*Setup completed on: 2026-03-10*
