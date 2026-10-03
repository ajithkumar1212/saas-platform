# ADR-005: PostgreSQL, UUIDs, Auditing, and Migration Strategy

- **Status:** Accepted baseline; canonical artifacts are the implementation contract
- **Type:** Data architecture decision
- **Scope:** Phase 1 persistence and migrations

## Context

The Phase 1 platform uses PostgreSQL and a schema-per-tenant isolation model.

The reconciled database baseline establishes:

- application-generated UUIDs;
- `TIMESTAMPTZ` for instants;
- Spring/JPA audit ownership;
- no database UUID/time generation defaults;
- no blanket cascading deletes;
- explicit keys, constraints, indexes, and checks;
- a hybrid deletion policy;
- separate Control Plane and tenant migrations.

## Decision

Use PostgreSQL as the Phase 1 relational database.

### UUIDs

Generate UUIDs in application code.

Do not introduce database-generated UUID defaults unless separately approved.

### Time and auditing

Use Java `Instant` mapped to PostgreSQL `TIMESTAMPTZ`.

Spring/JPA/application auditing owns timestamps and actors.

Do not add database `now()` defaults for application audit fields.

### Deletion

Do not apply blanket cascading deletes.

Use the approved hybrid deletion behavior:

- preserve business history where required;
- archive/soft-delete where required;
- physically delete only where explicitly allowed;
- protect audit evidence from ordinary delete paths.

### Constraints and indexes

The canonical SQL is the database contract.

Do not replace or weaken approved:

- primary keys;
- foreign keys;
- unique constraints;
- indexes;
- check constraints;
- same-business Store/Brand integrity;
- access-scope allowlist;
- tax-registration integrity;
- effective-date rules.

### Migration authority

Flyway is the migration authority.

Migration topology:

```text
Control Plane
  V1__create_tenant_registry
  V2__create_application_catalog
  V3__insert_application_catalog

Tenant
  V1__create_phase1_schema
  V2__insert_reference_data
  V3__create_business_application
```

Control migrations run once against the shared Control Plane schema.

Tenant migrations run against every provisioned tenant schema.

## Consequences

- Database structure is version-controlled.
- Tenant provisioning must be migration-aware.
- Migration rollout must account for all tenant schemas.
- Destructive rollback cannot be assumed.
- DDL changes require coordinated review of ERD, data dictionary, API, QA, security, and implementation documents.

## Verification

The direct-SQL validation has passed the canonical Phase 1 schema, constraints, reference data, cross-schema application dependency, and tenant-isolation checks.

Flyway replay has **not** been executed and therefore is not represented as passed.

## Revisit criteria

Revisit this ADR if:

- PostgreSQL is replaced;
- identifier strategy changes;
- audit ownership changes;
- deletion policy changes;
- migration authority changes;
- tenant isolation strategy changes.
