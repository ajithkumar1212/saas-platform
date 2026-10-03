# ADR-006: Tenant Provisioning and Migration Runner

- **Status:** Accepted baseline
- **Type:** Architecture and operations decision
- **Scope:** Tenant lifecycle and database provisioning

## Context

A new customer signs up through the public SaaS entry point. The platform must automatically create and isolate the tenant rather than requiring manual database-schema creation.

The schema-per-tenant model requires tenant-specific migrations to be executed after the tenant schema is created.

## Decision

Tenant provisioning is an application-controlled, automated workflow.

The canonical sequence is:

```text
Tenant Registration
        |
        v
Create tenant_registry record
        |
        v
Generate tenant ID/code/schema name
        |
        v
CREATE SCHEMA
        |
        v
Tenant V1
        |
        v
Tenant V2
        |
        v
Tenant V3
        |
        v
Create tenant row / bootstrap data
        |
        v
Create initial Tenant Administrator
        |
        v
Verify schema and required state
        |
        v
Mark PROVISIONED
```

The shared Control Plane migrations are established separately before tenant application enablement can reference the Application Catalog.

## Registry

`public.tenant_registry` is the authoritative Control Plane record used before a tenant schema exists.

The registry records:

- tenant ID;
- tenant code;
- tenant name;
- generated schema name;
- tenant status;
- provisioning status;
- audit timestamps/actors.

## Security rules

- Tenant IDs and schema names are generated server-side.
- User input must never become an arbitrary schema identifier.
- Provisioning credentials require only the privileges needed for the workflow.
- Tenant schema creation and migration failures must be observable.
- A tenant is not considered `PROVISIONED` until required schema verification succeeds.
- Provisioning must be retry-safe and idempotent.
- Failure state must be retained so operations can diagnose and retry safely.

## Migration execution

The migration runner must keep Control Plane and tenant migration contexts separate.

Tenant SQL is executed against the intended tenant schema and must not depend on an untrusted search path.

The canonical migration manifest remains authoritative.

## Consequences

### Positive

- Automated onboarding.
- Consistent tenant schema creation.
- Repeatable tenant provisioning.
- Centralized provisioning state.
- Retry-safe operational behavior.

### Negative

- Provisioning is a security-sensitive workflow.
- Database DDL privileges are required somewhere in the operational path.
- Tenant migrations must be coordinated across the tenant fleet.
- Partial failure requires explicit recovery procedures.

## Verification

Before production:

- empty-database provisioning succeeds;
- repeated provisioning requests are idempotent;
- failed migrations produce a non-ready tenant;
- schema verification occurs before `PROVISIONED`;
- cross-tenant schema access is impossible;
- provisioning actions are audited.

The direct-SQL validation confirms the resulting registry/schema/application relationships for the validated fixtures. Flyway replay remains deferred.
