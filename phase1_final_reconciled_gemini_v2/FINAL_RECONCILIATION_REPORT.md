# Phase 1 Gemini Audit — Final Reconciliation Report

## Final status

**FINAL — Gemini/Antigravity audit reconciled.**

The audit report identified genuine documentation/API inconsistencies, but the canonical PostgreSQL DDL does not require structural changes for CM-01 through CM-05.

## Decisions

| Finding | Final classification | Final fix | DDL change |
|---|---|---|---:|
| CM-01 Warehouse/POS/Website parentage | Documentation/API mismatch | Store is the parent; update docs/routes/QA | No |
| CM-02 Store/Channel same-business integrity | Documentation overstatement | Enforce at application-service layer | No |
| CM-03 ADR-004 status | Documentation mismatch | ADR-004 is Accepted for Phase 1; propagate status and unblock catalog routes | No |
| CM-04 `password_hash` | Authentication ambiguity | Retain nullable field; prohibit external-IdP password storage pending ADR-003 | No |
| CM-05 `audit_log.outcome` | Audit contract mismatch | Remove generic outcome from domain audit; use security/operational telemetry | No |
| Table documentation omissions | Documentation gap | Add complete 40-table catalog and data dictionary | No |
| Explicit index count | Documentation defect | Correct final tenant explicit index count to 29 | No |

## CM-01

Canonical hierarchy:

```text
Business
├── Brand
├── Store
│   ├── Store Location
│   ├── Warehouse
│   │   └── Warehouse Location
│   ├── POS
│   └── Website
│       └── Website Domain
└── Channel
```

Warehouse/POS/Website use `store_id` in the canonical schema. Business-level aggregation is permitted but does not imply a Business FK.

## CM-02

`store_channel` contains `store_id` and `channel_id`, not `business_id`. The service must resolve both records and verify that they belong to the same Business before persisting the association. Cross-business association is rejected.

## CM-03

ADR-004 is accepted for Phase 1. The platform-owned application catalog and tenant `business_application` model are approved. Application catalog and Business Application API/UI behavior is therefore no longer blocked by ADR-004.

## CM-04

`user_account.password_hash` remains nullable because ADR-003 is still Proposed. It is not an external identity-provider password store and must not be populated for an external IdP account. Any local-password authority requires a separate approved authentication decision.

## CM-05

`audit_log` remains unchanged. Domain audit records capture actor, action, target, time, tenant, correlation and change context. Authentication failures, authorization denials, token rejection, tenant-resolution failures, rate limiting and provisioning failures belong in security/operational telemetry.

## Documentation corrections

The final package now contains:

- explicit Store ownership for Warehouse/POS/Website;
- application-service Store/Channel integrity rule;
- accepted ADR-004 status;
- unblocked Application Catalog routes;
- authentication/password storage boundary;
- audit semantic boundary;
- 40-table database catalog/data dictionary;
- corrected explicit index count of 29.

## Structural baseline

- Public control tables: 2
- Tenant V1 tables: 39
- Tenant V3 addition: 1
- Final tenant domain tables: 40
- Tenant V1 foreign keys: 66
- Final tenant foreign keys: 68
- Tenant CHECK constraints: 14
- Tenant explicit indexes after V3: 29
- Application catalog rows: 3
- Reference types: 6
- Reference values: 42
- Cross-tenant foreign keys: 0

## Validation evidence

Direct-SQL Phase 1 validation previously passed.

Flyway replay remains **DEFERRED** and is not represented as passed.

## DDL decision

No new migration is required by this reconciliation. The canonical database structure remains unchanged.

**Final reconciliation: COMPLETE.**
