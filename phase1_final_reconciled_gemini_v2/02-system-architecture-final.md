# System Architecture

## Status and scope

**Status:** Phase 1 architecture baseline, with individual architecture decisions governed by the ADR status in `10-adr/`.

This document describes the logical and technical architecture required to support the Phase 1 product model. It distinguishes product intent from technical architecture decisions. An architecture statement must not be treated as an uploaded business requirement unless its source is explicitly identified.

The current Phase 1 baseline is:

- Multi-tenant, multi-business SaaS platform.
- Shared Control Plane and tenant/business Application Plane.
- Tenant → Organization → Company → Business hierarchy.
- PostgreSQL schema-per-tenant isolation.
- One tenant schema shared by that tenant's business applications; **no schema-per-application model**.
- Application Catalog in the public control schema and Business Application enablement in each tenant schema.
- Phase 1 application catalog contains `E_COMMERCE`, `DISTRIBUTION`, and `WAREHOUSE`.
- Inventory is a shared capability rather than a selectable Phase 1 application.
- The modular-monolith deployment model is **Accepted for Phase 1** under ADR-001.
- Standards-based OIDC authentication with Keycloak reference implementation is **Accepted for Phase 1** under ADR-003; the application owns tenant, role, permission, and hierarchy authorization.
- The backend technology stack and runtime baseline is **Accepted for Phase 1** under ADR-008 (Java 21 LTS, Spring Boot 3.3.x, Hibernate 6 schema-per-tenant multi-tenancy, and programmatic Flyway runner).

The canonical SQL package is the current database architecture reference for Phase 1. Its migration replay through Flyway remains a separate validation gate; direct-SQL database validation has been completed separately.

## Logical view

```text
Public Web
(www.xyz.com)
      │
      │ Sign Up / Login
      ▼
Authenticated Application
(app.xyz.com)
      │
      ├──────────────────────────────────────────────┐
      │                                              │
      ▼                                              ▼
Control Plane                                  Application Plane
      │                                              │
      ├─ Tenant registry                             ├─ Business workspace
      ├─ Identity / access boundary                  ├─ Store / Brand / Channel
      ├─ Organization / Company / Business admin     ├─ Store → Warehouse / POS / Website
      ├─ Application catalog                         └─ Enabled business domains
      ├─ Platform configuration
      └─ Audit
      │
      ▼
PostgreSQL
public schema
      │
      ├─ tenant_registry
      └─ application
      │
      ├──────────── tenant schema A ────────────┐
      │                                         │
      │  tenant + hierarchy + users +           │
      │  store foundation + reference data +    │
      │  business_application                   │
      │                                         │
      └──────────── tenant schema B ────────────┘
```

The logical separation is a product and architecture boundary. It does not require separate deployables for every plane or business domain.

## Product hierarchy and tenancy boundary

The business hierarchy is:

```text
Tenant
└── Organization
    └── Company
        └── Business
```

### Tenant

Tenant is the technical data-isolation boundary. Each tenant has a generated PostgreSQL schema. All businesses belonging to that tenant use the same tenant schema.

The tenant schema must be selected from trusted server-side tenant context and registry state. A client must never be allowed to supply an arbitrary PostgreSQL schema name through a URL, header, request body, or equivalent input.

### Organization

Organization belongs to a Tenant and groups Companies.

### Company

Company belongs to an Organization and can contain multiple Businesses.

### Business

Business is the operational workspace at which enabled business applications may vary. Business applications remain within the tenant's single schema boundary.

Authorization is not established by an identifier alone. The application must verify that the authenticated principal has access to the requested tenant and hierarchy scope.

## Control Plane and Application Plane

### Control Plane

The Control Plane contains platform-wide capabilities and administration boundaries, including:

- Tenant registry and provisioning state.
- Identity/access boundary.
- Organization, Company, and Business administration.
- Application Catalog.
- Platform configuration.
- Audit and history.
- Subscription/billing capabilities where later phases require them.

Administration and identity/access are platform capabilities rather than selectable Business Applications.

### Application Plane

The Application Plane contains Business workspaces and the business domains enabled for those Businesses.

The current Phase 1 database model supports the Business Application relationship:

```text
public.application
        │
        │ application_id
        ▼
tenant_schema.business_application
        │
        ▼
tenant_schema.business
```

This creates a cross-schema dependency from a tenant's `business_application` table to the public Application Catalog. It does **not** create a tenant-to-tenant dependency.

## Application model

The canonical Phase 1 SQL package contains:

### Public control-plane catalog

```text
public.application
```

with exactly these seeded application codes:

```text
E_COMMERCE
DISTRIBUTION
WAREHOUSE
```

### Tenant application enablement

Each tenant schema contains:

```text
business_application
```

which associates a Business with an Application Catalog entry.

The database representation is therefore established in the canonical SQL package. Detailed business semantics remain governed by the current ADR status and product documentation, including ownership, entitlement/dependency rules, enablement lifecycle, and disable behavior. Architecture documentation must not invent those rules.

Inventory is treated as a shared capability rather than a separate selectable application in the current Phase 1 product model.

## Recommended deployment architecture

### Modular monolith

ADR-001 proposes starting with one deployable modular monolith with explicit domain boundaries and internal APIs.

**ADR status: Proposed; confirm with project owner.**

Therefore this document treats modular monolith as the recommended initial deployment architecture, not as an already-approved irreversible decision.

The proposed logical modules are:

```text
Identity / Access
Tenant / Provisioning
Organization
Company
Business
Store Foundation
Application Access
Audit
Shared Platform Foundation
```

A module should own its data access and expose stable application-level interfaces. Cross-module writes should go through application services, and circular dependencies should be avoided.

Module extraction into independent services remains a future option when measurable operational or scaling requirements justify it.

### Technology baseline

The project documentation and canonical implementation discussion identify the following technology direction:

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Flyway
- MapStruct
- React + TypeScript

Exact framework/library versions and deployment infrastructure remain implementation/operations decisions and must not be inferred as business requirements.

## Tenancy model

The Phase 1 tenancy model is **shared control schema + one generated PostgreSQL schema per tenant**.

```text
PostgreSQL
├── public
│   ├── tenant_registry
│   └── application
│
├── tenant_<generated>
│   ├── tenant
│   ├── organization
│   ├── company
│   ├── business
│   ├── business_application
│   └── remaining tenant domain tables
│
└── tenant_<generated>
    └── ...
```

### Tenant schema resolution

The application must:

1. Authenticate the request.
2. Establish trusted tenant identity/context.
3. Resolve the tenant schema through server-side registry state.
4. Validate that the principal is allowed to access the requested hierarchy scope.
5. Select the tenant schema transaction-locally.
6. Ensure the database connection/pool does not leak tenant schema state between requests.

A schema name supplied by a client is never an authorization mechanism.

### Tenant isolation

The database contract prohibits tenant-to-tenant business foreign keys.

The only intentional cross-schema relationship in the Phase 1 tenant model is:

```text
<tenant_schema>.business_application
        └── application_id
                ↓
public.application.application_id
```

Tenant A must never obtain a foreign-key dependency on Tenant B.

## Domain boundaries

| Domain | Primary responsibility |
|---|---|
| Identity and Access | Principal mapping, users, roles, permissions, access scopes, access history |
| Tenant / Provisioning | Tenant registry, tenant lifecycle, schema provisioning and migration orchestration |
| Organization / Company / Business | Hierarchy lifecycle and hierarchy history |
| Store Foundation | Store, Brand, Channel and their approved relationships |
| Application Access | Application Catalog and Business Application enablement |
| Audit | Audit events, history and security/business traceability |
| Shared Platform Foundation | Common identifiers, auditing, validation, error handling and platform infrastructure |

The exact internal module boundaries may evolve if ADR-001 or later implementation decisions change the deployment architecture.

## Database architecture

The canonical Phase 1 SQL package establishes:

```text
Control plane
    2 domain tables
    ├── public.tenant_registry
    └── public.application

Tenant plane
    V1 = 39 domain tables
    V3 = +1 business_application
    --------------------------------
    Total = 40 tenant domain tables
```

The canonical package also defines:

- 66 baseline tenant foreign keys.
- 68 tenant foreign keys after `business_application`.
- 14 tenant CHECK constraints.
- 29 explicit tenant indexes after Tenant V3 (`business_application` adds `ix_business_application_application_id`).
- 6 reference types.
- 42 reference values.
- 3 application catalog seed records.

The canonical package is the database reference for this Phase 1 architecture.

## Migration architecture

Migration ownership is separated between control and tenant schemas.

### Control plane

```text
public
V1 → create_tenant_registry
V2 → create_application_catalog
V3 → insert_application_catalog
```

### Tenant plane

```text
each tenant schema
V1 → create_phase1_schema
V2 → insert_reference_data
V3 → create_business_application
```

The dependency order is:

```text
Control V1
   ↓
Control V2
   ↓
Control V3
   ↓
Tenant V1
   ↓
Tenant V2
   ↓
Tenant V3
```

Tenant V3 depends on `public.application`.

Control and tenant Flyway histories are independent. Tenant migrations must target the tenant schema and must never be executed against `public`.

The canonical migration manifest is the authoritative description of this migration split.

### Provisioning boundary

Tenant provisioning follows the architectural boundary:

```text
Create tenant registry record
        ↓
Generate trusted tenant schema name
        ↓
Create tenant schema
        ↓
Run tenant migrations V1 → V2 → V3
        ↓
Bootstrap tenant/admin configuration
        ↓
Mark tenant provisioning successful
```

The registry/provisioning state must not be marked `PROVISIONED` until required provisioning steps succeed.

Failure handling and retry behavior must preserve tenant isolation and avoid partially initialized tenants.

## Security architecture

Authentication and authorization are separate concerns.

### Authentication

ADR-003 proposes an external standards-based OIDC identity provider for credential authentication, MFA, and sessions.

**ADR-003 status: Proposed; identity provider not selected.**

Therefore this architecture does not freeze a provider-specific schema, claim set, or vendor implementation.

### Authorization

The application remains responsible for:

- Mapping the authenticated principal to platform user/account records.
- Resolving Tenant membership.
- Evaluating roles and permissions.
- Evaluating hierarchy/access scopes.
- Enforcing Business/Store/Brand/Channel scope where applicable.
- Preventing cross-tenant access.
- Preventing identifier-based authorization bypasses.

A valid JWT or authenticated subject does not by itself authorize access to arbitrary tenant or business data.

## Canonical Store resource hierarchy

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

Warehouse, POS, and Website are Store-owned resources in the database. Business-level queries may aggregate them, but `business_id` is not their owning foreign key.

## Store and business foundation

The Phase 1 store foundation treats these as distinct concepts:

```text
Business
├── Store
├── Brand
└── Channel
```

The canonical database enforces same-business integrity for Store/Brand. Store/Channel same-business integrity is enforced by the application service because the current `store_channel` DDL contains `store_id` and `channel_id` but no shared `business_id` key.

The schema also represents the Phase 1 foundation for:

- Store Location
- Warehouse
- Warehouse Location
- POS
- Website
- Website Domain

Their detailed operational workflows belong to the relevant API/UI/product documents and must not be expanded here beyond the supported architecture boundary.

## Data lifecycle and integrity

The database architecture follows these principles:

- Application code generates UUID identifiers.
- Java `Instant` maps to PostgreSQL `TIMESTAMPTZ`.
- Spring/JPA owns created/updated timestamps and actors.
- Database-generated timestamp/UUID defaults are not introduced unless explicitly approved.
- Blanket cascading deletes are not used.
- The approved hybrid deletion/lifecycle policy governs archival, soft deletion, and physical deletion.
- Restrictive relational behavior is the default.
- Finalized keys, constraints, and indexes must not be changed without an explicit database decision.
- Tax/legal registration data must remain at the approved entity scope and cardinality.

## Resilience and observability

The following are architecture/operations requirements or recommendations rather than business requirements:

- Propagate correlation/trace identifiers through synchronous and asynchronous processing.
- Emit structured logs and metrics.
- Do not log credentials, tokens, raw secrets, or unnecessarily exposed sensitive tax/legal values.
- Make provisioning operations retry-safe and idempotent where applicable.
- Provide application health and readiness checks.
- Maintain database backup and restore procedures.
- Monitor migration and provisioning outcomes.
- Test tenant isolation and failure/recovery scenarios.

Exact cloud provider, regions, SLOs, RTO/RPO, queueing infrastructure, and production topology require explicit operations/ADR decisions.

## Finalized Technology Stack Baseline (ADR-008, ADR-009, ADR-010)

The platform implementation is locked to the following enterprise stack:

- **Backend Framework & Language:** **Spring Boot 3.5.x** on **Java 21 LTS** (Eclipse Temurin), managed via Apache Maven 3.9+. Spring Boot controls curated compatible versions of Spring Framework 6.x, Spring Security, Hibernate, Jackson, Micrometer, Tomcat, and Flyway.
- **Architecture Pattern (ADR-001):** Modular Monolith structured strictly by domain packages (`com.xyz.platform.organization`, `company`, `business`, `store`, `warehouse`, etc.) rather than flat technical layers (`controller/`, `service/`).
- **Persistence Responsibilities:**
  - **Spring Data JPA / Hibernate 6.x:** Exclusively for tenant business domain entities (`Organization`, `Company`, `Business`, `Store`, `Brand`, `Channel`, `Website`, `Warehouse`, `POS`, etc.).
  - **Spring `JdbcTemplate`:** Exclusively for control-plane and provisioning infrastructure (`public.tenant_registry`, `CREATE SCHEMA`, schema introspection, migration runner metadata). Arbitrary business services cannot execute schema DDL or alter search paths.
- **Multi-Tenancy Execution Pipeline (ADR-002):** Centralized behind `TenantContext` $\rightarrow$ `TenantIdentifierResolver` $\rightarrow$ `MultiTenantConnectionProvider`. Schema routing is never executed casually by application code; the schema name is resolved server-side from the trusted `tenant_registry`—never from raw client HTTP headers.
- **Migration & Provisioning Runner (ADR-005, ADR-006):** Programmatic Flyway 10.x with two distinct lifecycles:
  - *Control Plane:* Runs on application startup against the `public` schema (`V1`–`V3`).
  - *Tenant Plane:* Programmatically invoked on-demand by the Provisioning Service during signup/provisioning (`V1`–`V3`). Tenant migrations are **never** blindly run on application boot.
- **Testing & Quality Tooling:** JUnit 5, Mockito, AssertJ, Spring Boot Test, **Testcontainers PostgreSQL** (mandatory real PostgreSQL testing for multi-tenancy and Flyway), REST Assured / MockMvc, **ArchUnit** (programmatic package isolation enforcement), JaCoCo (85%+ coverage), and Spotless/Checkstyle.
- **Authentication & Identity (ADR-003):** Spring Security 6.x OAuth2 Resource Server validating JWT Bearer tokens; Keycloak as official reference/self-hosted IdP.
- **API Documentation & Contracts:** SpringDoc OpenAPI 3.0 exposing `/v3/api-docs` and Swagger UI conforming to `04-api-final.md`.
- **Frontend UI Stack (ADR-009):** Next.js (App Router), React 18+, TypeScript (Strict), Tailwind CSS, shadcn/ui + Radix UI accessible primitives (targeting WCAG 2.2 AA), Lucide React, and TanStack Query v5.
- **Deployment & Infrastructure (ADR-010):** Multi-stage OCI/Docker container images, Docker Compose for local/dev (`db`, `idp`, `backend`, `frontend`), Kubernetes / ECS / Cloud Run with NGINX Ingress for production, and Managed PostgreSQL 16+ with Multi-AZ and automated continuous WAL archiving (PITR).

## Architecture constraints and non-goals

This architecture does **not** imply:

- one database schema per application;
- one deployable service per business domain;
- that every broader commerce capability is part of Phase 1;
- that a client-provided schema name is trusted;
- that detailed Application Catalog entitlement/dependency semantics beyond Phase 1 are finalized;
- that Flyway replay has been runtime-validated across CI environments.

## Architecture decision status

| Decision | Current status |
|---|---|
| Control Plane / Application Plane conceptual separation | Product/architecture baseline |
| PostgreSQL schema-per-tenant | Accepted baseline — ADR-002 |
| Modular monolith | **Accepted for Phase 1 — ADR-001** |
| External OIDC identity boundary | **Accepted for Phase 1 — ADR-003 (Keycloak reference)** |
| Application Catalog / Business Application semantics | **Accepted for Phase 1 — ADR-004** |
| PostgreSQL / UUID / auditing / migration strategy | **Accepted baseline — ADR-005** |
| Tenant Provisioning and Migration Runner | **Accepted baseline — ADR-006** |
| Data Deletion and Retention Policy | **Accepted baseline — ADR-007** |
| Backend Tech Stack, Framework & Runtime | **Accepted for Phase 1 — ADR-008 (Java 21, Spring Boot 3.3)** |
| Frontend UI Framework & Design System | **Accepted for Phase 1 — ADR-009 (Next.js, Tailwind, shadcn/ui)** |
| Deployment Topology, Containers & Infrastructure | **Accepted for Phase 1 — ADR-010 (Docker, Kubernetes, NGINX, PG16)** |
| Flyway migration topology in canonical SQL package | Defined by canonical package; runtime replay validation deferred |

## Architecture freeze conditions

Before Phase 1 implementation is considered architecturally frozen:

1. Open ADRs that affect implementation are explicitly resolved or accepted as intentionally deferred.
2. The canonical database contract remains synchronized with architecture and API documentation.
3. Tenant provisioning and schema-selection behavior are defined and tested.
4. Authentication provider and token/identity mapping are finalized before provider-specific implementation.
5. Application Catalog / Business Application behavior follows accepted ADR-004 before implementing entitlement or selection workflows.
6. Flyway replay is executed against a clean PostgreSQL environment before production migration use.
7. API, security, testing, and UI documents remain consistent with the final architecture.
