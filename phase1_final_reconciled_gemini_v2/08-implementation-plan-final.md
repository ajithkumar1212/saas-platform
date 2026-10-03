# Phase 1 Implementation Plan

**Status: FINALIZED**

This is the finalized Phase 1 implementation sequence. It preserves the source gate-based structure and reconciles it with the finalized database, API, UI, QA, and DevOps baselines.

## Gates

1. **Gate 0 — Contract and implementation freeze:** confirm requirements, canonical database/migration package, tenant boundary, application catalog, Inventory as shared capability, authentication/authorization boundary, API/UI/QA/DevOps baselines, and tech stack decisions. **COMPLETED / FROZEN:** Full technology stack finalized across ADR-001 (Modular Monolith with domain-package layout), ADR-003 (OIDC / Keycloak), ADR-008 (Java 21 LTS, Spring Boot 3.5.x, Spring Data JPA / Hibernate 6, programmatic Flyway 10.x, Testcontainers PostgreSQL, ArchUnit, JaCoCo), ADR-009 (Next.js 14+, Tailwind CSS, shadcn/ui + Radix UI), and ADR-010 (Docker OCI, Docker Compose dev, Kubernetes/ECS, NGINX Ingress, Managed PostgreSQL 16).
2. **Gate 1 — Database and migration foundation:** implement `public.tenant_registry`, `public.application`, tenant V1–V3 migrations, reference data, application catalog, provisioning prerequisites, and tenant-isolation validation. The finalized tenant schema is 40 domain tables: 39 from V1 plus `business_application` from V3. Direct-SQL validation has passed; Flyway replay remains deferred.
3. **Gate 2 — Backend foundation and domains:** implement persistence, tenant context, transaction/error handling, authentication boundary, authorization, provisioning, Organization → Company → Business, Business Applications, Store/Brand/Channel, Warehouse/POS/Website, tax/reference data, audit and history.
4. **Gate 3 — Verification:** execute unit, PostgreSQL/integration, migration, API-contract, security, provisioning, tenant-isolation, E2E, UI/accessibility, and resilience/performance verification defined by `06-testing-qa.md`.
5. **Gate 4 — UI and operations:** implement public-to-app onboarding and workspace flows, CI/CD, telemetry, backup/restore checks, deployment controls, and operational runbooks defined by `07-devops.md`.

## Implementation principles

- PostgreSQL schema-per-tenant is the isolation boundary.
- `public` is the control plane; each tenant gets one generated schema.
- Never accept a tenant schema name from client input.
- Applications share the tenant schema; do not create a schema per application.
- Phase 1 selectable applications are `E_COMMERCE`, `DISTRIBUTION`, and `WAREHOUSE`.
- Inventory is a shared capability, not a selectable application.
- Start with the modular-monolith deployment baseline unless an approved ADR changes it.
- Treat documented, implemented, tested, and passed as separate states.
- Do not introduce later-phase product scope through implementation convenience.

## Canonical database/migration sequence

```text
control/
  V1__create_tenant_registry.sql
  V2__create_application_catalog.sql
  V3__insert_application_catalog.sql

tenant/
  V1__create_phase1_schema.sql
  V2__insert_reference_data.sql
  V3__create_business_application.sql
```

Provisioning sequence:

```text
Sign Up → tenant_registry → generate schema → CREATE SCHEMA
→ tenant V1 → V2 → V3 → tenant row → tenant administrator
→ verify schema → PROVISIONED
```

Provisioning must be retry-safe, observable, auditable, and must never mark a tenant ready before schema verification.

## Backend workstream order

```text
Platform foundation
→ tenant context / persistence
→ identity & authorization
→ tenant provisioning
→ Organization
→ Company
→ Business
→ Business Applications
→ Store / Brand / Channel
→ Warehouse / POS / Website
→ Tax / reference data
→ Audit / history
→ API hardening
```

Business workspace route: `app.xyz.com/business/{businessId}`.

Authorization must enforce tenant and hierarchy scope server-side. UI checks are not sufficient.

## Workstreams and dependencies

| Workstream | Primary output | Depends on |
|---|---|---|
| W1 Contract baseline | Frozen Phase 1 contract | Gate 0 |
| W2 Database/migrations | Verified schema and migrations | W1 |
| W3 Platform foundation | Tenant context, persistence, errors, audit foundation | W2 |
| W4 Identity/access | Authentication and authorization foundation | W3 |
| W5 Tenant provisioning | Automated tenant creation/migration workflow | W2–W4 |
| W6 Organization/company/business | Hierarchy services and APIs | W3–W4 |
| W7 Application catalog | Catalog and Business Application enablement | W2, W6 |
| W8 Store/brand/channel | Business operational foundation | W6–W7 |
| W9 Warehouse/POS/website | Operational setup foundation | W6, W8 |
| W10 Tax/reference/audit | Supporting data and history | W2, W6 |
| W11 API hardening | Contract/error/idempotency/concurrency behavior | W3–W10 |
| W12 UI | Onboarding and workspace flows | W4–W11 |
| W13 QA/security | Verification evidence | W3–W12 |
| W14 DevOps | CI/CD, telemetry, backup, runbooks | W2, W13 |
| W15 Release acceptance | Production-readiness evidence | All required workstreams |
```

## Verification requirements

Mandatory verification covers tenant isolation, authorization scope, hierarchy integrity, database constraints/reference data, provisioning retry/failure states, API error/validation behavior, audit behavior, security leakage, and UI onboarding/accessibility. Cross-tenant IDOR, forged tenant context, scope escalation, schema/SQL injection, and secret/PII leakage are release-blocking security concerns unless formally accepted.

## CI/CD and operations

CI shall cover formatting/static analysis, unit and integration tests, migration compatibility, API contract validation, dependency/security/secret scans, and immutable artifact creation. Promotion should use the same artifact across environments.

Production readiness additionally requires health/readiness checks, observability, secure configuration/secrets, backup, restore exercise, deployment/rollback or forward-fix procedures, tenant migration controls, and operational runbooks. Cloud provider, runtime, artifact registry, infrastructure-as-code tool, RPO/RTO, alert thresholds, and other explicitly open DevOps decisions must be approved before production rather than assumed here.

## Milestones

- **M0:** Contract frozen.
- **M1:** Database foundation ready.
- **M2:** Backend foundation ready.
- **M3:** Core business management ready.
- **M4:** Phase 1 operational foundation ready.
- **M5:** Verification ready.
- **M6:** Production-readiness ready.
- **M7:** Phase 1 accepted.

No calendar dates or effort estimates are invented by this plan.

## Definition of done

An implementation item is complete only when code, required tests, database/API/UI synchronization, authorization, audit behavior where required, safe telemetry, review, and CI quality gates are satisfied.

## Phase 1 completion criteria

Product: tenant onboarding, tenant administrator access, Organization, Company, Business, Business workspace, approved Business Applications, Store/Brand/Channel foundation, approved Warehouse/POS/Website foundation, and administration/access controls.

Database: canonical schema, control-plane objects, tenant provisioning, reference data, constraints/indexes, cross-tenant isolation, migration process, and Flyway replay validation before claiming migration readiness.

Security: authentication, authorization, non-forgeable tenant context, cross-tenant tests, sensitive-data protection, and auditability.

Quality: required unit, integration, migration, API, security, provisioning, E2E, UI/accessibility, and applicable resilience/performance verification.

Operations: CI/CD gates, immutable artifact, secure configuration/secrets, monitoring/alerting, backup, restore exercise, runbooks, release and rollback/forward-fix procedures, and operational ownership.

## Change control and scope boundary

Changes to tenant isolation, hierarchy, database schema, application catalog, Business Application model, authentication, authorization, API contracts, UI onboarding, deletion behavior, tax semantics, or deployment architecture require impact assessment against the authoritative documents and ADRs. This plan does not silently redefine those contracts.

Later customer, cart, checkout, installment, wallet, payment, order, fulfillment, delivery, return/exchange, and later catalog/inventory workflows remain outside this Phase 1 plan unless separately approved.

## Current evidence

The project has recorded `PASS: ALL DIRECT-SQL PHASE 1 VALIDATIONS PASSED`, including the two validated tenant schemas and application/reference-data checks. Flyway replay has not been executed in the current environment and must not be represented as passed.

**Final status: `08-implementation-plan.md` — FINALIZED.**

`09-security.md` is intentionally not reviewed or changed as part of this finalization step.
