# Project Context — SaaS E-Commerce Platform

**Status:** Phase 1 documentation baseline; database package and direct-SQL validation are complete for the current Phase 1 schema baseline. Architecture decisions remain governed by their individual ADR status unless explicitly finalized.

**Precedence:** Approved business requirements → approved decisions in this context → detailed docs/ADRs → implementation. Where an ADR is explicitly marked Proposed/Open, it does not become approved merely because another document references it.

**Source/validation note:** The current documentation ZIP does not contain a `sources/` directory or the authoritative business-requirement file. The canonical Phase 1 SQL package is present separately and is the current database reference for this documentation reconciliation. The SQL package defines 39 tenant baseline tables in tenant V1, adds `business_application` in tenant V3 for a total of 40 tenant domain tables, and defines two public control-plane tables: `tenant_registry` and `application`. Direct-SQL validation has been executed successfully against the Phase 1 validation database. Flyway replay has not been executed in the current Windows validation environment and remains **Deferred**.

## Product and terminology

The product is a multi-tenant, multi-business SaaS platform with a shared Control Plane and business-facing Application Plane. Preserve these terms:

- **Tenant** is the technical isolation boundary and the owner of a tenant data schema.
- **Organization** belongs to a Tenant; an Organization can contain Companies.
- **Company** belongs to an Organization and can contain Businesses.
- **Business** is the operational unit that can use one or more business applications.
- **Application Catalog** is the platform-level catalog of available applications. The current canonical SQL package contains `public.application` and seeds exactly `E_COMMERCE`, `DISTRIBUTION`, and `WAREHOUSE`.
- **Business Application** represents application enablement for a Business. The current canonical SQL package contains tenant-level `business_application` and enforces a Business-to-Application relationship through `public.application`. ADR-004 is accepted for Phase 1. The platform-owned Application Catalog and tenant-level Business Application model are approved for Phase 1; implementation must enforce authorization and lifecycle rules without changing the canonical schema.
- **Store**, **Brand**, and **Channel** are distinct business/store concepts. Store/Brand and Store/Channel relationships remain separate. Store/Brand same-business integrity is database-enforced; Store/Channel same-business integrity is an application-service validation rule because the current DDL has no shared Business key.
- **Inventory** is a shared domain capability and is not a separate selectable application in the current Phase 1 application catalog.
- Administration, identity, roles, permissions, subscriptions, billing, and audit are platform/control capabilities, not selectable Business Applications in Phase 1.

Expected setup journey:

```text
www.xyz.com → Sign Up / Login → app.xyz.com → Tenant → Organization → Company
            → Business → application configuration → manage Business
```

The platform is intended to support multiple businesses under a company and different application combinations by business. The current Phase 1 catalog is limited to the three seeded application codes above.

## Phase 1 intent and boundary

Phase 1 establishes the onboarding and tenant/org/company/business/store foundation together with the shared control capabilities and application-enablement data model present in the canonical SQL package. It does not imply that all later commerce workflows are implemented merely because their domains appear in the broader product model.

The implementation sequence is database/common persistence foundation, entities/repositories/services, DTOs and mapping, validation/error handling, security, APIs, tests, and then React UI. This documentation baseline does not by itself authorize source-code generation or expansion into later phases.

## Source-of-truth and status labels

Use the following status language consistently:

- **Requirement** — a statement supported by an approved business/product requirement.
- **Finalized database decision** — a schema/data-model decision represented by the current canonical SQL package and validated database state.
- **Architecture decision** — a technical decision recorded in an ADR. Its approval status is controlled by that ADR.
- **Open** — an unresolved business or technical question that must be decided before the affected implementation boundary is frozen.
- **Deferred validation** — a validation activity that has not yet been executed; it must not be described as passed.

The current canonical SQL package establishes the following database baseline:

- PostgreSQL.
- 39 tenant domain tables in tenant V1.
- `business_application` added by tenant V3, giving 40 tenant domain tables after all tenant migrations.
- `public.tenant_registry` and `public.application` as the two public control-plane domain tables.
- Three application catalog seed records: `E_COMMERCE`, `DISTRIBUTION`, `WAREHOUSE`.
- Six reference-data types and 42 reference values in each tenant schema.
- Application-generated UUID identifiers and Spring/JPA-managed audit timestamps mapped to PostgreSQL `TIMESTAMPTZ` remain the intended application persistence model.
- No blanket cascading deletes; the packaged deletion matrix defines a hybrid lifecycle/deletion policy.
- Tenant `business_application` references the control-plane application catalog; tenant-to-tenant business foreign keys are prohibited.

The canonical SQL package also contains control and tenant Flyway migrations V1–V3, a canonical schema, reconciliation report, deletion matrix, runbook, and validation scripts. The direct-SQL validation script has passed the current validation database checks for schema shape, constraints, reference data, application catalog, cross-schema relationships, tenant isolation, and provisioning fixture state. Flyway history/replay validation remains deferred because the migrations have not been replayed through Flyway in the current validation environment.

## Architecture baseline

The conceptual separation between Control Plane and Application Plane is part of the intended product. Schema-per-tenant is the database isolation model: one generated schema per tenant, shared by all business applications belonging to that tenant; there is no schema per application.

The canonical SQL package and current documentation baseline support:

- PostgreSQL as the database platform.
- Schema-per-tenant isolation with a public control plane.
- Tenant registry state in `public.tenant_registry`.
- Application catalog in `public.application`.
- Tenant business/application enablement in `business_application`.
- Server-controlled tenant/schema resolution; a request must never be allowed to select an arbitrary tenant schema.

The modular-monolith delivery model remains **Proposed** under ADR-001 and must not be described elsewhere as an approved architecture decision. The authentication ADR remains **Proposed** and does not itself approve a specific identity provider. The API contract may document the currently requested Keycloak OIDC/JWT contract boundary, but that does not change ADR-003's status.

## Non-negotiable data rules

- Do not change a canonical DDL column, key, constraint, index, table count, or migration without an explicit approved database decision and synchronized documentation updates.
- UUID primary keys are generated by application code; Java `Instant` maps to PostgreSQL `TIMESTAMPTZ`. Spring/JPA auditing owns created/updated timestamps and actors; do not add database `now()` defaults unless separately approved.
- Preserve entity-specific identifier column names and composite keys from the canonical schema.
- No blanket cascading deletes. Apply the packaged hybrid deletion policy: lifecycle/archive or disable core business/governance records; allow physical deletion only for explicitly approved transient/configuration associations and only where retained references permit it.
- Preserve GST/tax registration as legal/business data at the entity scope defined by the canonical schema. Do not collapse or expand registration cardinality without an approved database decision.
- `user_access_scope.scope_type` values in the verified artifact are `ORGANIZATION`, `COMPANY`, `BUSINESS`, `BRAND`, `STORE`, and `CHANNEL`. Its polymorphic target requires application-level existence, tenant ownership, and authorization checks.
- Tenant isolation is mandatory: tenant schemas may reference the public application catalog where explicitly defined, but must not contain tenant-to-tenant business foreign keys.
- Tenant schema names are trusted infrastructure data resolved from tenant registry state; they are never supplied by an untrusted request as an arbitrary SQL identifier.

## Current implementation-freeze decisions

The following are frozen for the current Phase 1 database/documentation baseline:

1. Tenant is the technical isolation boundary.
2. One schema is used per tenant, shared by that tenant's applications.
3. The tenant hierarchy is Tenant → Organization → Company → Business.
4. Store, Brand, and Channel remain distinct concepts.
5. Inventory is a shared capability rather than a selectable Phase 1 application.
6. The Phase 1 application catalog contains exactly `E_COMMERCE`, `DISTRIBUTION`, and `WAREHOUSE` in the canonical SQL package.
7. Tenant application enablement is represented by `business_application` and references the public application catalog.
8. The canonical tenant baseline is 39 tables plus `business_application`, for 40 tenant domain tables after tenant V3.
9. The control plane contains `tenant_registry` and `application` as its Phase 1 domain tables.
10. The packaged hybrid deletion policy and restrictive foreign-key behavior are the baseline.
11. Direct-SQL Phase 1 validation is complete and passed; Flyway replay validation is deferred.

These database facts do not by themselves approve proposed technical choices recorded in other ADRs. ADR-004 is accepted for the Phase 1 Application Catalog / Business Application model.

## Open decisions before implementation freeze

1. Reconcile the authoritative business requirement and complete requirement-to-document traceability; the current documentation ZIP does not contain the `sources/` directory.
3. Finalize ADR-003's identity-provider selection and provider-specific token/identity contract. The current API documentation may describe the requested Keycloak OIDC/JWT boundary without treating ADR-003 as approved.
4. Finalize ADR-001's modular-monolith decision if the project owner intends it to be the delivery architecture; until then it remains Proposed.
5. Complete Flyway replay validation against a fresh database and confirm migration history/ordering at runtime.
6. Freeze remaining API field-level schemas, lifecycle semantics, pagination limits, idempotency retention, concurrency wire format, and OpenAPI tooling where they remain marked Open in `04-api.md`.
7. Finalize operational details such as provisioning concurrency/retry behavior, audit retention, and other items explicitly marked Open in the detailed documents.

## Document map

See [`README.md`](README.md) for the index and reading order. Individual ADR status controls proposed technical choices; this context is the master glossary, precedence guide, and Phase 1 cross-document baseline.
