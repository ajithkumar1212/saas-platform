# ADR-004: Application Catalog and Business Application

- **Status:** Accepted for Phase 1
- **Type:** Business and data-model decision
- **Scope:** Business application enablement

## Context

The product flow requires a Business to enable one or more business applications, and different Businesses may select different combinations.

The original ADR snapshot described this model as unresolved because the earlier 37-table SQL did not contain application-model tables.

The reconciled Phase 1 baseline now explicitly includes:

- `public.application`
- tenant-scoped `business_application`

## Decision

The platform uses a platform-owned Application Catalog in the shared Control Plane.

Phase 1 selectable applications are exactly:

| Application code | Application |
|---|---|
| `E_COMMERCE` | E-Commerce |
| `DISTRIBUTION` | Distribution |
| `WAREHOUSE` | Warehouse Management |

A Business may have zero or more application enablements, subject to authorization and lifecycle rules.

The tenant association is represented by:

```text
tenant_schema.business_application
        |
        +-- business_id
        |
        +-- application_id --> public.application
```

The uniqueness rule is:

```text
(business_id, application_id)
```

A Business Application belongs to the tenant schema because it represents tenant/business enablement.

The Application Catalog belongs to the shared Control Plane because it is platform-owned.

Inventory is a shared capability and is not a selectable application.

Administration remains a Control Plane capability.

No application-feature/dependency tables are introduced in Phase 1.

## Phase 1 application model

```text
Control Plane
    |
    +-- application
          +-- E_COMMERCE
          +-- DISTRIBUTION
          +-- WAREHOUSE

Tenant Schema
    |
    +-- business_application
          |
          +-- Business
          +-- Application
```

### Implementation consequence

This acceptance unblocks the Phase 1 Application Catalog and Business Application API/UI resource model. Implementations must enforce authorization, lifecycle/status rules, duplicate protection and tenant/business ownership checks. No additional application dependency/entitlement tables are introduced by this ADR.

## Consequences

### Positive

- One Business can use multiple applications.
- Different Businesses in the same tenant can have different application combinations.
- Application ownership is separated from tenant enablement.
- Future applications can be added through catalog records without redesigning the tenant hierarchy.

### Negative

- Application enablement requires authorization and lifecycle rules.
- Cross-schema dependency exists from tenant `business_application` to the shared application catalog.
- Catalog lifecycle and entitlement rules require operational governance.

## Required behavior

Application enablement must verify:

- Business belongs to the authenticated tenant;
- application exists;
- caller has required permission;
- duplicate enablement is handled deterministically;
- application lifecycle/status is respected;
- enablement does not automatically grant broader privileges.

## Verification

The canonical Phase 1 SQL model must contain:

- `public.application`;
- exactly three Phase 1 application seed records;
- tenant `business_application`;
- unique `(business_id, application_id)`;
- foreign key from `business_application.application_id` to `public.application.application_id`.

The direct-SQL validation has verified the application catalog and cross-schema foreign-key model.

## Revisit criteria

Revisit this ADR when:

- application entitlements are introduced;
- application dependencies become required;
- application configuration needs a new persistence model;
- disable/archive behavior changes;
- subscription/billing must control application availability.
