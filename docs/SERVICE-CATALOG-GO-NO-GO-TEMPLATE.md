# Service Catalog Go/No-Go Template

Date/Time Cutover: ____________________

Release Version/Tag: ____________________

Environment: [ ] Staging  [ ] Production

Change Owner: ____________________

Ops Lead: ____________________

Tech Lead: ____________________

## 1. Scope Confirmation

- [ ] `deployCatalogItem` uses real deployment flow (DB delegation + generic VM + batch async)
- [ ] API params exposed and validated: `templateId`, `ipMode`, `privatePorts`, `allowedCidr`
- [ ] UI forms updated:
- [ ] `ui/src/views/plugins/catalog/ServiceCatalog.vue`
- [ ] `ui/src/views/plugins/catalog/DeployCatalogItem.vue`
- [ ] `ui/src/views/plugins/dbaas/CreateDatabaseInstance.vue`
- [ ] `ui/src/views/plugins/bulk/BulkDeployVMs.vue`

## 2. Build and Test Gate

- [ ] Backend tests passed:
- [ ] `plugins/integrations/bulk-service`
- [ ] `plugins/integrations/dbaas-service`
- [ ] `plugins/integrations/service-catalog`
- [ ] Checkstyle passed (0 violations)
- [ ] UI lint passed for modified files
- [ ] UI production build passed (Node 18 requires `NODE_OPTIONS=--openssl-legacy-provider`)

## 3. Deployment Gate

- [ ] Deploy artifacts in dependency order (`dbaas-service` before `service-catalog`)
- [ ] Restart management service completed
- [ ] Plugin registration verified (commands visible)
- [ ] Rollback artifacts prepared and version-pinned

## 4. Functional Gate (Smoke)

- [ ] Generic single deploy succeeds
- [ ] DB catalog deploy delegates to DBaaS and succeeds
- [ ] PORT_FORWARD deploy succeeds with `networkId`
- [ ] Batch deploy (`count > 1`) starts and returns deploying status
- [ ] Validation behavior confirmed:
- [ ] invalid `count` is rejected
- [ ] invalid `ipMode` is rejected
- [ ] invalid CIDR is rejected

## 5. UI Checklist Gate

ServiceCatalog deploy modal:
- [ ] `templateid` visible and searchable
- [ ] `ipmode` has `STATIC_NAT` and `PORT_FORWARD`
- [ ] `privateports` auto-filled from selected `item.defaultport`
- [ ] `privateports` shown only when `ipmode=PORT_FORWARD`
- [ ] `allowedcidr` shown for static NAT flow

DeployCatalogItem form:
- [ ] fields visible: `templateid`, `ipmode`, `privateports` (conditional), `allowedcidr`

CreateDatabaseInstance form:
- [ ] `templateid` required and searchable
- [ ] `ipmode` disabled when `assignpublicip=false`
- [ ] `allowedcidr` defaults to `0.0.0.0/0`

BulkDeployVMs form:
- [ ] `ipmode` visible
- [ ] `privateports` shown when `assignpublicip=true` and `ipmode=PORT_FORWARD`

## 6. Risk Acknowledgement

- [ ] High: Batch context handling validated (explicit account propagation)
- [ ] Medium: PORT_FORWARD requires `networkId` and behavior documented
- [ ] Medium: Generic VM flow integration test gap accepted for this release
- [ ] Low: Executor pool size and deploy timeout are configurable and verified
- [ ] Low: Batch status persistence limitations accepted
- [ ] Low: Template fallback behavior documented

## 7. Observability and Rollback Gate

- [ ] 24h monitoring owner assigned
- [ ] Alert/watch list prepared (IP allocation, NAT, PF conflicts, capacity)
- [ ] Rollback trigger conditions defined
- [ ] Rollback execution owner assigned

## Decision

Go-Live Decision: [ ] GO  [ ] NO-GO

Decision Time: ____________________

Blocking Issues (if NO-GO):

1. ______________________________________________
2. ______________________________________________
3. ______________________________________________

## Sign-Off

Change Owner: ____________________  Date: __________

Ops Lead: ____________________      Date: __________

Tech Lead: ____________________     Date: __________

Product Owner: ____________________ Date: __________
