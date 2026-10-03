# DevOps and Operations

## 1. Status and scope

**Status: FINALIZED — Phase 1**

This document defines the DevOps, deployment, CI/CD, database operations, observability, reliability, security-operation, backup/restore, and production-readiness baseline for the XYZ SaaS platform.

It is reconciled with the finalized Phase 1 architecture, database, API, UI, and QA documents. It distinguishes established project decisions from operational requirements and open implementation choices. Provider-specific technologies are not treated as approved unless the project baseline explicitly establishes them.

---

## 2. DevOps objectives

The operating model shall provide:

1. Repeatable builds and deployments.
2. Isolated local, development, test, staging, and production environments.
3. Safe tenant-schema provisioning and migration.
4. Strong tenant isolation during deployment and database operations.
5. Immutable and traceable application artifacts.
6. Controlled database changes.
7. Secure configuration and secret management.
8. Application, database, migration, and provisioning observability.
9. Production PostgreSQL backup and restore capability.
10. Controlled release, rollback, and forward-fix procedures.
11. Auditable production changes.
12. A controlled path from the initial modular monolith toward later decomposition only when an operational need is demonstrated.

---

## 3. Current deployment baseline

The platform deployment architecture is the **accepted modular monolith (ADR-001, ADR-008)** with a single executable Spring Boot 3.3.x backend application running on Java 21 LTS (Eclipse Temurin) paired with a Next.js 14+ frontend application (ADR-009).

### 3.1 Containerization & Infrastructure Baseline (ADR-010)

- **Container Standards:** Standardized multi-stage OCI/Docker images:
  - Backend: `eclipse-temurin:21-jre-alpine` runtime image, unprivileged user.
  - Frontend: `node:20-alpine` Next.js standalone runtime image.
- **Local Development Orchestration:** Docker Compose (`docker-compose.yml`) running:
  - `db`: PostgreSQL 16 (port 5432) initialized with canonical schema.
  - `idp`: Keycloak OIDC server (port 8080) for authentication (ADR-003).
  - `backend`: Spring Boot 3.3 application (port 8081).
  - `frontend`: Next.js application (port 3000).
- **Production Runtime Orchestration:** Kubernetes (or managed container services: AWS ECS Fargate, GCP Cloud Run, Azure Container Apps) with NGINX Ingress Controller terminating TLS and routing:
  - `www.xyz.com` $\rightarrow$ Next.js public routes;
  - `app.xyz.com` $\rightarrow$ Next.js authenticated SaaS workspace;
  - `app.xyz.com/api/v1/*` $\rightarrow$ Spring Boot modular monolith backend;
  - `auth.xyz.com/*` $\rightarrow$ Keycloak / OIDC IdP.
- **Database Infrastructure:** Managed PostgreSQL 16+ (AWS RDS PostgreSQL / GCP Cloud SQL) with Multi-AZ replication and automated continuous WAL archiving for Point-in-Time Recovery (PITR).

```text
www.xyz.com (Next.js Public)        app.xyz.com (Next.js SaaS App)
        │                                        │
        ▼                                        ▼
    ┌────────────────────────────────────────────────────────┐
    │          NGINX Ingress / Cloud Load Balancer           │
    └────────────────────────────────────────────────────────┘
                    │                           │
         /api/v1/*  ▼                           ▼  /auth/*
    ┌─────────────────────────┐     ┌─────────────────────────┐
    │   Spring Boot Backend   │     │    Keycloak OIDC IdP    │
    │  (Modular Monolith API) │     │  (Auth & Token Issuer)  │
    └─────────────────────────┘     └─────────────────────────┘
                    │
                    ▼
    ┌─────────────────────────────────────────────────────────┐
    │          Managed PostgreSQL 16 (Multi-AZ)               │
    │  public: tenant_registry, application catalog           │
    │  tenant_<generated>: 40 domain tables                   │
    └─────────────────────────────────────────────────────────┘
```

---

## 4. Environment model

| Environment | Purpose |
|---|---|
| Local | Developer implementation and debugging |
| Development | Shared integration of active changes |
| Test | Automated functional, integration, security, and migration validation |
| Staging | Production-like release verification |
| Production | Live tenant workloads |

### Environment rules

- Production data must not be copied into lower environments unless an approved anonymization/synthetic-data process exists.
- Each environment shall have separate configuration and credentials.
- Database credentials shall use least privilege.
- Secrets shall never be committed to source control.
- Environment-specific configuration shall be externalized.
- Tenant schema names shall be generated and resolved server-side; clients must never select an arbitrary schema.
- Production access shall be restricted and auditable.
- Staging shall be sufficiently representative to validate application and migration behavior.

---

## 5. Source control

The source repository is authoritative for application code, migration scripts, infrastructure code, operational scripts, and configuration templates.

Required controls:

- Material changes use pull requests/review.
- Database migration changes receive explicit review.
- Changes affecting tenant isolation, authorization, provisioning, or security receive appropriate architecture/security review.
- Secrets, private keys, passwords, tokens, and production credentials are never committed.
- Releases are traceable to an exact source revision.

The exact branching strategy is not fixed by the current project baseline.

---

## 6. CI pipeline

The minimum CI flow is:

```text
Checkout
  |
  +--> Formatting / static analysis
  +--> Unit tests
  +--> Repository / database integration tests
  +--> Migration validation
  +--> API contract validation
  +--> Dependency / vulnerability scan
  +--> Secret scan
  +--> Build
  +--> Immutable artifact
```

Required quality gates enforced via `mvn clean verify` include:
1. Code formatting and static style checks (**Spotless** / **Checkstyle**);
2. Domain unit tests with assertions (**JUnit 5**, **Mockito**, **AssertJ**);
3. Architecture boundary verification (**ArchUnit** ensuring strict package decoupling);
4. Multi-tenant database and repository integration tests using **Testcontainers PostgreSQL** (real PG instance; H2 prohibited);
5. Programmatic Flyway migration validation against ephemeral clean PostgreSQL schemas;
6. API contract validation against OpenAPI models (**MockMvc** / **REST Assured**);
7. Code coverage thresholds verified by **JaCoCo** (85%+ branch coverage);
8. Dependency vulnerability scanning (Trivy / OWASP Dependency Check) and secret detection (GitLeaks).

A failed required quality gate blocks release promotion.

---

## 7. Artifact management

Build artifacts shall be immutable and traceable to:

- source revision;
- application version;
- build identifier;
- build timestamp;
- dependency metadata; and
- the migration version expected by the release.

The same approved artifact should be promoted through environments rather than rebuilt independently for each environment.

The artifact registry technology is an open infrastructure decision.

---

## 8. CD and release promotion

Preferred flow:

```text
Release candidate
      |
      v
CI quality gates
      |
      v
Immutable artifact
      |
      v
Development -> Test -> Staging -> Production approval -> Production
```

Before production deployment, verify artifact identity, migration compatibility, backup readiness, configuration/secrets, health/readiness checks, observability, recovery procedure, release ownership, and tenant migration/provisioning impact.

The exact approval mechanism is an open operational decision.

---

## 9. Database migration authority and topology

Flyway is the intended migration authority in the current project baseline. **Flyway replay has not yet been executed in the user's environment**, so this document does not claim Flyway validation success.

Canonical migration topology:

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

Rules:

- Control migrations execute against `public`.
- Tenant migrations execute against the target generated tenant schema.
- Tenant migration SQL is schema-relative and must never accidentally run against `public`.
- `public.application` must exist before tenant V3 creates `business_application`.
- Migration versions are immutable after release.
- Do not edit an already-applied migration to repair production state; use a new forward migration.
- Prefer expand/contract changes.
- Destructive down-migrations are not the default recovery mechanism.

A production schema change requires migration review, ordering validation, empty-database validation, upgrade-path validation where applicable, tenant provisioning validation, failure/retry analysis, and backup readiness.

---

## 10. Tenant provisioning operations

Baseline flow:

```text
Sign Up
  -> create public.tenant_registry record
  -> generate tenant code + schema name
  -> CREATE SCHEMA
  -> tenant V1
  -> tenant V2
  -> tenant V3
  -> create tenant row
  -> create tenant administrator
  -> mark registry PROVISIONED
```

Provisioning must be idempotent, observable, retry-safe, auditable, and restricted to the intended tenant schema.

A failed provisioning operation must remain visibly failed/intermediate. Operators must not mark a tenant `PROVISIONED` without verifying actual schema state.

Operational state should identify the tenant, target schema, completed migration stage, failure stage, retry safety, and remediation requirement.

---

## 11. Tenant migration rollout

Tenant migrations are a fleet operation:

```text
Validate migration
      |
      v
Test representative tenant
      |
      v
Deploy compatible application
      |
      v
Migrate tenant schemas in controlled batches
      |
      v
Observe failures / DB load
      |
      v
Continue rollout
```

A staged/canary tenant rollout is preferred before broad rollout.

Exact batch size, concurrency, maintenance window, and scheduler implementation remain open decisions.

---

## 12. PostgreSQL backup and restore

Production PostgreSQL shall have automated backups, defined retention, backup-integrity monitoring, restore testing, documented recovery procedures, protected backup credentials, and restricted backup access.

A backup that has never been restored must not be considered operationally proven.

Restore exercises shall verify:

1. Backup discovery.
2. Restoration into an isolated environment.
3. Control-plane recovery.
4. Tenant-schema recovery.
5. Application connectivity after restore.
6. Continued tenant isolation.
7. Migration/version state after restore.
8. Operator recovery readiness.

RPO and RTO are not fixed in this document and require explicit production approval.

---

## 13. Health and readiness

Operational health shall distinguish application/process health, database connectivity, critical dependency health, and readiness to receive traffic.

A failed readiness state must prevent traffic from being routed to an instance that cannot safely serve requests.

Health endpoints must not disclose secrets, credentials, connection strings, tokens, or sensitive tenant data.

---

## 14. Logging

Logs shall be structured and correlated. Where appropriate they should contain timestamp, severity, application/environment identifier, correlation/trace ID, operation, outcome, and non-sensitive identifiers.

Never log passwords, access/refresh tokens, private keys, database credentials, raw secrets, full tax identifiers, or unnecessary sensitive personal data.

Log retention and centralized logging technology remain open decisions.

---

## 15. Metrics and observability

Minimum telemetry:

### Application
- request count;
- request latency;
- error rate;
- HTTP status distribution;
- availability;
- background-job failures where applicable.

### Database
- connection-pool health;
- connection acquisition failures;
- query/transaction failures;
- database availability;
- migration outcomes.

### Multi-tenancy
- tenant provisioning success/failure and duration;
- tenant migration success/failure;
- tenant schema version/state;
- rejected tenant-context attempts;
- tenant-scoped operational failures.

### Security and audit
- authorization denials;
- authentication failures;
- security-sensitive changes;
- audit-write failures.

Correlation IDs should connect API activity, background work, database operations, and audit events where technically appropriate.

---

## 16. Alerting

Production alerts should cover at least:

- application unavailability;
- sustained elevated error rate;
- sustained latency degradation;
- database unavailability;
- connection-pool exhaustion;
- migration failure;
- tenant provisioning failure;
- backup failure;
- restore verification failure;
- critical security events;
- audit-write failure.

Exact thresholds, notification channels, escalation policy, and on-call ownership require explicit approval before production.

---

## 17. Security operations

DevOps controls shall reinforce the project security model:

- least-privilege infrastructure and database access;
- separate credentials per environment;
- secrets outside source control;
- credential/key rotation procedures;
- restricted production access;
- auditable administrative actions;
- no client-controlled tenant schema selection;
- restricted database network access;
- CI security and dependency scans;
- appropriate log/telemetry redaction.

Identity-provider hosting and exact Keycloak deployment architecture depend on the final authentication/deployment decisions.

---

## 18. Infrastructure as code

Infrastructure should be reproducible through version-controlled infrastructure-as-code.

The IaC technology is not an approved project decision in the current source set.

Where IaC is used:

- changes go through code review;
- production changes are traceable to source revision;
- secrets are not embedded in templates;
- state is protected;
- environments are separated;
- destructive operations receive explicit review;
- drift detection should be used where supported.

---

## 19. Network and runtime deployment

Final runtime and network topology remain open. The production design shall nevertheless provide controlled ingress and restricted database access:

```text
Internet
   |
Public DNS / edge
   |
Application entry point
   |
Application runtime
   +---- PostgreSQL
   +---- Identity provider
   +---- External integrations
```

PostgreSQL must not be directly exposed to the public internet. Administrative access must be controlled and auditable. Environment/network boundaries must be explicit.

Cloud provider, load balancer, container runtime, orchestration platform, VPC/network topology, and region are open decisions.

---

## 20. Domain and DNS operations

The principal web entry points are:

```text
www.xyz.com  -> public marketing / signup / login entry
app.xyz.com  -> authenticated SaaS application
```

Business workspace URL pattern:

```text
app.xyz.com/business/{businessId}
```

Custom store/website domains shall validate ownership before activation, protect verification tokens, maintain auditable state, and support controlled SSL/certificate lifecycle management.

DNS provider, certificate automation, and exact custom-domain architecture remain open decisions.

---

## 21. Rollback and forward-fix

Application rollback should redeploy a previously approved immutable artifact, subject to database compatibility.

Database recovery should normally use forward-compatible migrations and corrective releases rather than destructive down-migrations.

Preferred compatibility pattern:

```text
Expand -> deploy compatible application -> migrate/backfill -> switch behavior -> contract later
```

The release process must prevent application rollback from leaving the database incompatible with the application.

---

## 22. Disaster recovery

Recovery procedures shall cover application deployment failure, PostgreSQL outage, corrupted/unavailable tenant schema, tenant provisioning failure, tenant migration failure, identity-provider outage where applicable, application-runtime loss, and backup restoration.

Exact DR topology, RPO, RTO, multi-region strategy, and failover mechanism are not finalized.

---

## 23. Operational runbooks

Before production, runbooks should exist for:

1. Application deployment.
2. Application rollback.
3. Database migration.
4. Tenant provisioning failure.
5. Tenant migration failure.
6. Database restore.
7. Backup failure.
8. High database connection usage.
9. Elevated API error rate.
10. Authentication/identity outage.
11. Domain verification failure.
12. Security incident escalation.
13. Audit pipeline failure.

Each runbook should define the trigger, prerequisites, diagnostics, safe remediation, post-remediation validation, escalation path, and rollback/forward-fix considerations.

---

## 24. Production-readiness checklist

### Application
- [ ] Reproducible build.
- [ ] Immutable artifact.
- [ ] Health/readiness checks.
- [ ] Required observability.
- [ ] Externalized configuration.

### Database
- [ ] Canonical DDL validated.
- [ ] Migration scripts reviewed.
- [ ] Tenant isolation validated.
- [ ] Provisioning flow validated.
- [ ] Backup configured.
- [ ] Restore exercise completed.
- [ ] Migration failure handling tested.

### Security
- [ ] Secrets externalized.
- [ ] Least-privilege production credentials.
- [ ] Security/dependency scans active.
- [ ] Logs redacted.
- [ ] Production access restricted and auditable.

### Operations
- [ ] Monitoring active.
- [ ] Alerts configured.
- [ ] Runbooks available.
- [ ] On-call ownership defined.
- [ ] Release approval defined.
- [ ] Recovery objectives approved.

---

## 25. Current validation evidence

The project has direct-SQL validation evidence for the Phase 1 database package:

```text
PASS: ALL DIRECT-SQL PHASE 1 VALIDATIONS PASSED
```

The validated database included `public.application` (3 records), `public.tenant_registry` (2 records), 40 domain tables in each of `tenant_t001` and `tenant_t002`, 42 reference values per validated tenant, required application-catalog cross-schema foreign keys, and no cross-tenant foreign keys.

The direct-SQL validator intentionally did not validate Flyway history. Flyway replay remains deferred because Flyway has not yet been executed in the user's environment. No Flyway success claim is made here.

---

## 26. Open operational decisions

| Decision | Status |
|---|---|
| Cloud provider | Open |
| Cloud region(s) | Open |
| Deployment runtime | Open |
| Container/orchestration platform | Open |
| PostgreSQL hosting | Open |
| Artifact registry | Open |
| Infrastructure-as-code tool | Open |
| DNS provider | Open |
| Certificate/SSL automation | Open |
| Identity-provider hosting | Open |
| Email delivery provider | Open |
| Centralized logging platform | Open |
| Metrics/monitoring platform | Open |
| Backup retention | Open |
| RPO/RTO | Open |
| Production alert thresholds | Open |
| On-call ownership | Open |
| Release approval policy | Open |
| Network topology | Open |
| Multi-region/DR strategy | Open |

Material choices shall be recorded in ADRs or approved operational design documents before production implementation.

---

## 27. Acceptance criteria

`07-devops.md` is complete for the current Phase 1 documentation baseline when:

1. Environment separation is defined.
2. CI quality gates are defined.
3. Immutable artifact promotion is defined.
4. Database migration ownership is defined.
5. Tenant provisioning and migration operations are defined.
6. Backup and restore responsibilities are defined.
7. Logging, metrics, correlation, and alerting requirements are defined.
8. Security-operation requirements are defined.
9. Rollback/forward-fix principles are defined.
10. Production-readiness criteria are defined.
11. Current validation evidence is recorded without overstating execution.
12. Open infrastructure decisions are explicit.
13. No later document is required to silently resolve an open DevOps decision.

---

## 28. Final status

**`07-devops.md` — FINALIZED**

This document is complete at the operational-policy and architecture level without inventing provider-specific infrastructure choices that have not been approved.

**Next document in sequence:** `08-implementation-plan.md` — review only after this document is accepted as finalized.
