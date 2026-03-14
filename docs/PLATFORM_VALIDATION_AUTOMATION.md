# Platform Validation, Pentest, and Monitoring Automation

## 1. Scope

This document defines automated quality gates for CloudStack platform operations:
- Functional smoke tests.
- Security baseline and pentest checks.
- Monitoring and observability readiness checks.

## 2. What Exists vs What Is Missing

Existing in repo:
- Core CloudStack integration tests are available.
- Multiple custom plugins and UI sections are present.

Missing for release-grade operations:
- Unified one-command quality gate runner.
- Baseline pentest automation tied to deployment URL.
- Standardized monitor health checks for production rollout.

## 3. Scripts Added

Under scripts/vnso:
- run-functional-smoke.sh
- run-pentest-baseline.sh
- run-monitoring-check.sh
- run-platform-quality-gates.sh

## 4. How To Run

Example:

```bash
cd /root/cloudstack
TARGET_URL=https://cloudstack.vnso.vn scripts/vnso/run-platform-quality-gates.sh
```

Optional environment variables:
- TARGET_URL: public endpoint (default: https://cloudstack.vnso.vn)
- API_HEALTH_PATH: API health endpoint path (default: /client/api)
- MONITOR_ENDPOINT: Prometheus endpoint (optional)
- FAIL_ON_WARN: set to 1 to fail on warnings

## 5. Functional Smoke Coverage

Checks:
- HTTP reachability of `/`, `/client/`, `/documents/`.
- TLS handshake and certificate retrieval.
- Sidebar route endpoint response shape.
- Basic header checks and redirect behavior.

## 6. Pentest Baseline Coverage

Checks:
- Security headers presence:
  - strict-transport-security
  - content-security-policy
  - x-frame-options
  - x-content-type-options
  - referrer-policy
  - permissions-policy
- TLS protocol and cipher quick scan (`nmap` if available).
- OWASP ZAP baseline scan (`docker` if available).

## 7. Monitoring Readiness Coverage

Checks:
- Container health (`docker ps`) for core services.
- Target endpoint latency and status.
- Prometheus endpoint health if configured.

## 8. CI/CD Recommendation

Pipeline stages:
1. build
2. unit test
3. integration smoke
4. pentest baseline
5. monitoring readiness
6. deploy

Gate policy:
- fail on hard errors.
- warning budget for non-blocking issues in pre-prod only.

## 9. KPI Targets

- Functional smoke pass rate: >= 99%
- Pentest high severity findings: 0
- Monitoring readiness pass rate: >= 95%
- Mean validation runtime: <= 10 minutes

## 10. Next Enhancements

- Add Playwright UI flow for login, document navigation, and service forms.
- Add authenticated API functional tests using scoped API keys.
- Add SLO assertions from real Prometheus query results.
