# ADR Index — Phase 1

## Status

**FINALIZED — ADR baseline**

This directory contains the Phase 1 Architecture Decision Records (ADRs) for the current platform baseline.

The ADRs are the decision record for architecture/data choices. They do not replace the PRD, architecture, database, API, QA, DevOps, or security documents.

## ADR register

| ADR | Decision | Status |
|---|---|---|
| ADR-001 | Modular Monolith for Initial Delivery | Accepted for Phase 1 |
| ADR-002 | Schema-per-Tenant Isolation | Accepted baseline |
| ADR-003 | Authentication and Identity Boundary | Accepted for Phase 1 (Keycloak reference) |
| ADR-004 | Application Catalog and Business Application | Accepted for Phase 1 |
| ADR-005 | PostgreSQL, UUIDs, Auditing, and Migration Strategy | Accepted baseline |
| ADR-006 | Tenant Provisioning and Migration Runner | Accepted baseline |
| ADR-007 | Data Deletion and Retention Policy | Accepted baseline; retention periods open |
| ADR-008 | Technology Stack, Framework, and Runtime Baseline | Accepted for Phase 1 (Java 21, Spring Boot 3.3) |
| ADR-009 | Frontend Framework, UI Design System, and Client Architecture | Accepted for Phase 1 (Next.js, Tailwind, shadcn/ui) |
| ADR-010 | Deployment Topology, Containerization, and Infrastructure Baseline | Accepted for Phase 1 (Docker OCI, Kubernetes/ECS, NGINX) |

## Important reconciliation note

The original uploaded ADR snapshot contained ADR-001 through ADR-005.

ADR-006, ADR-007, ADR-008, ADR-009, and ADR-010 were not present in that initial snapshot. They are now formalized because the finalized Phase 1 database, architecture, UI, DevOps, security, and implementation documents contain explicit decisions for tenant provisioning/migration, deletion policy, the Java 21 / Spring Boot 3.3 backend stack, the Next.js / shadcn/ui frontend stack, and the Docker / Kubernetes deployment baseline.

They are therefore marked as **newly formalized ADRs**, not presented as if they existed in the original source snapshot.

## Decision authority

Where a later finalized project document explicitly records an accepted decision that was previously marked open in the original ADR snapshot, this ADR package records the reconciled current baseline.

Examples:

- ADR-001 is now accepted establishing the single-deployable modular monolith.
- ADR-003 is now accepted establishing the standards-based OIDC identity boundary with Keycloak reference.
- ADR-004 is now accepted because the finalized database and implementation plan explicitly define the Application Catalog and Business Application model.
- ADR-005 is now accepted because the canonical database baseline establishes the PostgreSQL/UUID/audit/deletion/migration strategy.
- ADR-002 is now the accepted schema-per-tenant baseline because the canonical SQL and direct-SQL validation establish that model.
- ADR-008 is now accepted establishing the Java 21 LTS, Spring Boot 3.3.x, Hibernate schema multi-tenancy, and programmatic Flyway runner baseline.
- ADR-009 is now accepted establishing the Next.js (App Router), TypeScript, Tailwind CSS, and shadcn/ui + Radix UI frontend stack.
- ADR-010 is now accepted establishing the Docker containerization, Docker Compose dev environment, and Kubernetes / NGINX Ingress production infrastructure.

Open decisions remain explicitly open. They are not silently converted into implementation choices.

## Cross-document dependencies

```text
ADR-001 Modular Monolith
       |
       +---- ADR-002 Tenant Isolation
       |
       +---- ADR-003 Authentication
       |
       +---- ADR-004 Application Model
       |
       +---- ADR-005 Database Strategy
       |            |
       |            +---- ADR-006 Tenant Provisioning
       |            |
       |            +---- ADR-007 Deletion Policy
       |
       +---- ADR-008 Technology Stack & Runtime (Java 21 / Spring Boot 3.3)
       |
       +---- ADR-009 Frontend UI Stack (Next.js / Tailwind / shadcn)
       |
       +---- ADR-010 Deployment & Infrastructure (Docker / Kubernetes / NGINX)
```

## Rules for future ADR changes

A change to any of the following requires ADR review:

- deployment topology;
- tenant isolation;
- authentication/identity;
- authorization boundary;
- application model;
- database strategy;
- migration authority;
- tenant provisioning;
- deletion/retention behavior.

A decision change must be reflected consistently in affected architecture, database, API, UI, QA, DevOps, and security documents.

## Phase 1 ADR finalization rule

No implementation change may silently override an accepted ADR.

If a requirement or implementation constraint contradicts an accepted ADR, update the ADR first or explicitly document the exception.
