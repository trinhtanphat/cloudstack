# Bulk Deployment Guide: 1000 EC2-like VMs and 1000 Services

## 1. Scope

This guide explains how to:
- Deploy 1000 EC2-like virtual machines in bulk.
- Deploy 1000 service instances from Service Catalog.
- Allocate and manage 1000 public IPs and/or shared IP + random port forwarding.

Target platform: Apache CloudStack with custom plugins:
- Bulk Provisioning Service
- DBaaS Service
- Service Catalog

## 2. Prerequisites

### 2.1 Software

- Linux control plane hosts (Ubuntu 22.04 LTS recommended)
- Java 17
- Maven 3.8+
- MySQL/MariaDB for CloudStack databases
- KVM hypervisors (or your supported hypervisor)
- Layer-3 capable network with public IP pool routing

### 2.2 CloudStack Plugins Required

- `bulkDeployVirtualMachines`
- `bulkAllocatePublicIpAddresses`
- `createDatabaseInstance`
- `deployCatalogItem`
- `listBulkJobs`

### 2.3 Capacity Readiness (before running bulk jobs)

- Enough hypervisor capacity (vCPU, RAM, storage IOPS)
- Enough templates cached in target zones
- Sufficient public IP pool if using dedicated IP mode
- API rate limits and async job queue sized for large bursts

## 3. Deployment Models

### 3.1 For 1000 EC2-like VMs

Model A: Dedicated public IP per VM
- API mode: `ipmode=STATIC_NAT`
- Requires approximately 1000 public IP addresses

Model B: Shared public IP + random high ports
- API mode: `ipmode=PORT_FORWARD`
- Requires fewer public IPs, maps random public ports to VM private ports

### 3.2 For 1000 Service Instances

Service Catalog deployment
- API: `deployCatalogItem`
- Supports quantity via `count`
- Typical use: stateless web/app services, managed stack templates

DBaaS deployment
- API: `createDatabaseInstance`
- Supports `ipmode` and random public port in PORT_FORWARD mode

## 4. UI Workflow

## 4.1 Deploy 1000 EC2-like VMs from UI

1. Open plugin view: Bulk Deploy VMs.
2. Set:
   - Number of VMs = 1000
   - Name prefix (for example: `customer-a`)
   - Zone, Service Offering, Template
   - Batch size (recommended start: 20 to 50)
3. Submit deployment.
4. Track progress in Bulk Job Progress view.

### 4.2 Deploy 1000 Service Instances from UI

Option A: Service Catalog modal/form
1. Open Service Catalog.
2. Choose a catalog item.
3. Set quantity (`count`) up to 1000.
4. Submit deployment.

Option B: Deploy Catalog Item form
1. Open Deploy Catalog Item action.
2. Fill catalog item, name prefix, zone, offering.
3. Set `count=1000`.
4. Submit and monitor async completion.

## 5. API Workflow (recommended for repeatable automation)

Use API keys and signed requests in production.

### 5.1 Deploy 1000 EC2-like VMs

Example parameters:
- `command=bulkDeployVirtualMachines`
- `count=1000`
- `nameprefix=prod-ec2`
- `zoneid=<ZONE_UUID>`
- `serviceofferingid=<SO_UUID>`
- `templateid=<TEMPLATE_UUID>`
- `assignpublicip=true`
- `ipmode=PORT_FORWARD` (or `STATIC_NAT`)
- `batchsize=50`

### 5.2 Allocate 1000 Public IP addresses

Example parameters:
- `command=bulkAllocatePublicIpAddresses`
- `count=1000`
- `zoneid=<ZONE_UUID>`
- `networkid=<NETWORK_UUID>`

### 5.3 Deploy 1000 Service Instances

Example parameters:
- `command=deployCatalogItem`
- `catalogitemid=wordpress` (example)
- `name=wordpress-prod`
- `zoneid=<ZONE_UUID>`
- `serviceofferingid=<SO_UUID>`
- `count=1000`
- `assignpublicip=false`

### 5.4 DBaaS with shared IP + random public port

Example parameters:
- `command=createDatabaseInstance`
- `name=orders-db`
- `dbengine=MYSQL`
- `zoneid=<ZONE_UUID>`
- `serviceofferingid=<SO_UUID>`
- `templateid=<TEMPLATE_UUID>`
- `assignpublicip=true`
- `ipmode=PORT_FORWARD`
- `allowedcidr=10.0.0.0/8`

### 5.5 Monitor Async Bulk Jobs

- `command=listBulkJobs`
- Filter by status: `PENDING`, `RUNNING`, `COMPLETED`, `PARTIAL`, `FAILED`

## 6. Rollout Strategy for 1000 + 1000

Use phased rollout, not one giant burst.

Phase plan:
1. Dry-run: 10 VMs + 10 services
2. Pilot: 100 + 100
3. Scale wave 1: 300 + 300
4. Scale wave 2: 600 + 600
5. Final fill to 1000 + 1000

At each phase validate:
- VM creation success rate
- API queue depth and latency
- Hypervisor CPU steal and memory pressure
- Storage latency and iowait
- Public IP and firewall rule usage

## 7. Infrastructure Sizing (starting point)

These are practical baseline estimates. Adjust after load testing.

### 7.1 Control Plane (CloudStack Management)

- 3x management nodes (HA)
- Per node: 16 vCPU, 64 GB RAM, NVMe SSD
- DB cluster: 3 nodes, each 16 vCPU, 64 GB RAM, NVMe
- Message bus / queue capacity tuned for async job bursts

### 7.2 Hypervisor Capacity (for 1000 VMs)

Assume average VM profile: 2 vCPU, 4 GB RAM.

Aggregate demand:
- vCPU: ~2000
- RAM: ~4 TB
- Plus overhead: 20-30%

Recommended cluster baseline:
- 20 to 30 hypervisor hosts
- Each host: 64 to 96 physical cores, 256 to 512 GB RAM
- Fast storage backend (NVMe/Ceph/enterprise SAN)
- 25GbE network recommended for east-west traffic at this scale

### 7.3 Public IP and Port Planning

For dedicated IP model:
- Reserve at least 1200 public IPs (1000 target + headroom)

For shared IP + random port model:
- Keep high-port range policy (for example 10000-65000)
- Track firewall/NAT rule limits per network device
- Capacity-plan for concurrent connection count and conntrack tables

## 8. Security and Operations

- Use `allowedcidr` to restrict database ingress wherever possible.
- Enforce password policy (length, complexity, rotation).
- Use centralized logging and SIEM for API and auth events.
- Enable fail2ban and SSH hardening on service templates.
- Apply backup and restore drills before production launch.

## 9. Validation Checklist

- All plugin tests pass in CI.
- Bulk jobs complete without elevated partial failure rates.
- NAT/firewall rule creation stays below infra limits.
- Hypervisor and storage metrics remain within SLO.
- Rollback procedures tested for failed deployment batches.

## 10. Known Operational Caveats

- Service Catalog deployment currently reports async deployment initiation; validate full provisioning path in your environment before commercial launch.
- For very large batches, API timeouts and job queue saturation can happen without phased rollout and queue tuning.

---

If you want, I can also generate:
- a ready-to-run bash script for signed CloudStack API calls,
- a phased canary execution script (10 -> 100 -> 300 -> 600 -> 1000),
- and an Excel-style capacity calculator in Markdown.
