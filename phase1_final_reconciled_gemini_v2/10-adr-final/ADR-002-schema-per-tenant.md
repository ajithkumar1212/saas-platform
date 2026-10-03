# ADR-002: Schema-per-Tenant Isolation

- **Status:** Accepted baseline; retain as Phase 1 architecture decision
- **Type:** Architecture decision
- **Scope:** PostgreSQL tenant isolation

## Context

The project requires a multi-tenant SaaS platform in which a tenant owns organization, company, business, access, and application data.

The canonical Phase 1 database model uses PostgreSQL with a shared Control Plane schema and one generated schema for each tenant.

The original ADR snapshot described this decision as proposed pending verification. The canonical SQL and direct-SQL validation have since established the schema-per-tenant model as the current Phase 1 baseline.

## Decision

Use:

```text
PostgreSQL
|
+-- public / Control Plane
|     +-- tenant_registry
|     +-- application
|
+-- tenant_t001
|     +-- tenant domain model
|
+-- tenant_t002
      +-- tenant domain model
```

Each tenant receives one generated PostgreSQL schema.

All business applications belonging to that tenant use the same tenant schema.

Do not create a PostgreSQL schema per application.

Tenant schema selection is allowed only from trusted authenticated tenant context and server-side tenant registry state.

A client must never provide an authoritative schema name.

## Security rules

The application must:

1. authenticate the principal;
2. resolve tenant identity;
3. resolve trusted tenant registry state;
4. verify tenant/provisioning status;
5. resolve the generated schema;
6. establish schema context transaction-locally;
7. execute tenant data access;
8. safely reset connection/session state before connection reuse.

Application-level authorization must separately verify tenant membership and hierarchy/business access.

## Consequences

### Positive

- Strong physical namespace separation between tenant datasets.
- Reduced risk of accidental cross-tenant table access.
- Tenant-specific database operations remain possible.
- Business applications share a consistent tenant isolation boundary.

### Negative

- Tenant-aware connection/schema switching is required.
- Connection-pool hygiene is security-critical.
- Tenant migrations must be applied across every provisioned tenant.
- Backup/restore, tenant movement, reporting, and operational tooling require explicit tenant-aware design.
- Cross-tenant reporting cannot assume ordinary tenant-schema queries.

## Verification

Phase 1 verification must include:

- tenant schemas exist as expected;
- no cross-tenant foreign keys;
- schema resolution cannot be controlled by request input;
- connection-pool schema reset behavior is tested;
- cross-tenant access tests pass.

The project's direct-SQL Phase 1 validation has passed the tenant-isolation checks for the validated database fixtures.

## Revisit criteria

Revisit this ADR only if:

- canonical database requirements change;
- tenant scale/operational requirements materially change;
- backup/restore requirements become incompatible;
- reporting/analytics requirements require another isolation model;
- a documented security or operational requirement demonstrates that the model is insufficient.
