# GPU Cloud Service Architecture (VNSO CloudStack)

## 1. Executive Summary

This document defines a GPU service architecture for CloudStack aligned with hyperscaler patterns (AWS EC2 GPU, Azure N-series, Google GCE GPU).

Current status in this repo:
- CloudStack core already includes vGPU capabilities and tests in the integration test suite.
- Custom GCP integration plugin currently has no dedicated GPU command/service implementation yet.

Conclusion:
- GPU foundation exists.
- Managed GPU cloud product layer is not complete yet.

## 2. Capability Parity Matrix

| Capability | AWS/Azure/GCP | Current VNSO CloudStack | Gap |
|---|---|---|---|
| GPU instance families (A100, L4, T4, etc.) | Full | Partial (infra-level vGPU support) | Product catalog missing |
| GPU scheduling policies | Full | Basic | No policy abstraction in product layer |
| Quota by GPU type | Full | Partial | Need API and UI integration |
| Driver/runtime lifecycle management | Full | Partial | Need golden image pipeline |
| GPU monitoring and alerting | Full | Basic | Need dashboard and SLO alerts |
| Spot/preemptible GPU | Full | Missing | Roadmap item |
| Multi-tenant isolation and billing | Full | Partial | Need usage metering by GPU profile |

## 3. Target Architecture

## 3.1 Control Plane
- CloudStack Management API + plugin extension `gpu-service`.
- Catalog API for GPU profiles:
  - `listGpuProfiles`
  - `createGpuInstance`
  - `startGpuInstance`
  - `stopGpuInstance`
  - `deleteGpuInstance`
- Quota and policy engine:
  - quota per account/domain by GPU profile.
  - approval workflow for high-cost SKUs.

## 3.2 Data Plane
- Hypervisors with certified NVIDIA drivers.
- Support modes:
  - Passthrough GPU.
  - vGPU profile.
  - MIG profile (if hardware supports).
- Golden templates:
  - CUDA image.
  - AI framework image (PyTorch/TensorFlow).

## 3.3 Observability
- GPU metrics exporter (DCGM exporter) on hosts.
- Prometheus scrape targets:
  - gpu_utilization
  - gpu_memory_used
  - gpu_temperature
  - gpu_power_draw
  - ecc_error_count
- Grafana dashboards:
  - fleet-level GPU capacity and saturation.
  - tenant-level usage and noisy-neighbor detection.

## 4. API and UI Design

## 4.1 API Contract (recommended)
- `createGpuInstance` inputs:
  - name
  - zoneid
  - gpuprofileid
  - serviceofferingid
  - templateid
  - networkid
  - assignpublicip
  - ipmode
- response:
  - instance id
  - profile id
  - host assignment
  - state
  - connection endpoint

## 4.2 UI/UX
- New section under VNSO Documents and GPU Services:
  - Profile catalog cards (cost/perf labels).
  - Capacity badges by zone.
  - One-click launch templates.
- Guardrails:
  - warn if requested profile has low available capacity.
  - show estimated monthly cost before submit.

## 5. Security Model
- RBAC for GPU operations (separate from standard VM roles).
- Strict image hardening:
  - signed templates.
  - CIS baseline for Linux images.
- Network policy defaults:
  - deny-all inbound except approved ports.
- Secrets:
  - no plaintext credentials in user-data.

## 6. Deployment Runbook

## 6.1 Host Preparation
1. Install host OS and patch baseline.
2. Install NVIDIA driver and validate with `nvidia-smi`.
3. Enable virtualization mode (passthrough/vGPU/MIG).
4. Register host into CloudStack cluster.
5. Run GPU discovery and verify profile inventory.

## 6.2 Control Plane
1. Deploy/enable GPU plugin module.
2. Apply DB migration for GPU metadata tables.
3. Register API permissions and UI sections.
4. Configure quota defaults.

## 6.3 Validation
1. Deploy one GPU VM per profile.
2. Run CUDA sample workload.
3. Verify metrics in Prometheus/Grafana.
4. Verify alarms and quota rejection paths.

## 7. Operational SLOs
- Provisioning success rate: >= 99.5%
- API p95 latency for list/create operations: <= 2s (list), <= 10s (submit)
- GPU saturation alert time: <= 2 minutes
- MTTR for failed provisioning: <= 30 minutes

## 8. Roadmap

Phase 1 (now):
- docs, API contract, test harness, monitoring baseline.

Phase 2:
- GPU plugin commands + DB schema + UI pages.

Phase 3:
- scheduler policies, quota, cost model, billing integration.

Phase 4:
- spot GPU, autoscaling, AI platform templates.
