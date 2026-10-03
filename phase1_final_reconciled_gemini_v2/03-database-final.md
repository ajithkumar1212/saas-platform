# Phase 1 Database Contract

## Authority and status

This document defines the Phase 1 database architecture and contract as reconciled against the canonical SQL package supplied with the project.

The canonical SQL package is the authoritative implementation artifact for the Phase 1 PostgreSQL schema and migration topology. It contains:

- Control-plane migrations for `public.tenant_registry` and `public.application`.
- Tenant V1 schema creation.
- Tenant V2 reference-data seeding.
- Tenant V3 `business_application` creation.
- Canonical tenant-schema SQL.
- Constraint, reference-data, cross-schema, tenant-isolation, provisioning, and direct-SQL validation scripts.
- Migration manifest, reconciliation report, deletion matrix, and replay runbook.

The older 37-table ERD/data-dictionary/DDL artifacts remain historical reference material. They are not the current 39/40-table canonical database contract.

The uploaded documentation ZIP does not contain the original `sources/business-requirement.md` file. Business semantics are therefore taken only from the finalized project documentation and available requirement material; this document does not invent business rules that are not supported by those sources.

## Phase 1 database topology

The platform uses a shared PostgreSQL database with:

```text
public
├── tenant_registry
└── application

tenant_<generated_schema>
├── 39 V1 tenant domain tables
├── business_application          -- V3
└── reference/application data
```

There is **one tenant schema per Tenant**.

All businesses and enabled applications belonging to a tenant use that tenant's single schema.

There is **no schema-per-application model**.

### Control-plane tables

The canonical control plane contains:

```text
public.tenant_registry
public.application
```

`tenant_registry` exists because the tenant schema does not exist when provisioning begins. It provides the trusted mapping:

```text
tenant_id
    ↓
tenant_code
    ↓
schema_name
    ↓
provisioning_status
```

The `application` table is the platform-owned catalog of selectable Business Applications.

The canonical catalog contains exactly:

```text
E_COMMERCE
DISTRIBUTION
WAREHOUSE
```

### Tenant-plane tables

Tenant migration V1 creates **39 domain tables**.

Tenant migration V3 creates:

```text
business_application
```

Therefore the final tenant domain-table count is:

```text
39 + 1 = 40
```

The validation package treats these as 40 tenant domain tables.

## Canonical migration topology

### Control plane

```text
public
  V1__create_tenant_registry.sql
      ↓
  V2__create_application_catalog.sql
      ↓
  V3__insert_application_catalog.sql
```

Flyway history:

```text
public.flyway_schema_history
```

### Tenant plane

For every tenant schema:

```text
<tenant_schema>
  V1__create_phase1_schema.sql
      ↓
  V2__insert_reference_data.sql
      ↓
  V3__create_business_application.sql
```

Flyway history:

```text
<tenant_schema>.flyway_schema_history
```

The control and tenant Flyway histories are independent.

Tenant migrations must never be executed against `public`.

Tenant V3 depends on the control-plane `public.application` table.

The canonical migration manifest establishes this dependency order.

## Tenant provisioning and schema ownership

Tenant provisioning follows:

```text
Create tenant registry record
        ↓
Generate trusted schema name
        ↓
CREATE SCHEMA
        ↓
Run tenant V1
        ↓
Run tenant V2
        ↓
Run tenant V3
        ↓
Bootstrap tenant/admin configuration
        ↓
Mark registry PROVISIONED
```

The schema name is generated and validated by the backend. Clients do not supply arbitrary PostgreSQL schema names.

The provisioning registry must not be marked `PROVISIONED` until required provisioning and bootstrap steps succeed.

Failed provisioning must remain retryable without creating cross-tenant state or incorrectly marking an incomplete tenant as provisioned.

## Tenant isolation

Tenant isolation is a database and application responsibility.

### Required rules

1. Every tenant receives its own PostgreSQL schema.
2. Tenant business tables are created within that tenant schema.
3. Tenant A must not have foreign-key dependencies on Tenant B.
4. Client-provided schema names are never trusted.
5. Tenant schema resolution is derived from trusted server-side tenant context and `tenant_registry`.
6. Connection/schema state must not leak between tenant requests.
7. Cross-schema references are permitted only where explicitly part of the platform design.

### Intentional cross-schema relationship

The canonical Phase 1 model contains one intentional application-catalog relationship:

```text
<tenant_schema>.business_application.application_id
        ↓
public.application.application_id
```

This is a control-plane dependency, not a tenant-to-tenant dependency.

The database validation package explicitly checks for tenant-to-tenant foreign keys and requires none.

## Hierarchy and entity model

The core hierarchy is:

```text
Tenant
└── Organization
    └── Company
        └── Business
```

Business-level foundation includes:

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

Store/channel and store/brand associations remain governed by the canonical constraints.

Business applications are represented separately:

```text
public.application
        ↓
tenant.business_application
        ↓
tenant.business
```

Inventory is treated as a shared capability rather than a selectable Phase 1 Application.

## Tax registration model

The canonical V1 schema contains:

```text
tax_registration
business_tax_registration
```

### `tax_registration`

A tax registration belongs to an Organization.

The canonical structure includes:

- `tax_registration_id`
- `organization_id`
- `registration_type`
- `registration_number`
- `gstin`
- `country_id`
- `state_id`
- `status`
- `valid_from`
- `valid_until`
- description/audit fields

`valid_from` is required.

`valid_until` is optional and, when supplied, must be greater than or equal to `valid_from`.

### `business_tax_registration`

This table associates a Business with a tax registration.

Its primary key is:

```text
(business_id, tax_registration_id)
```

It also carries the Organization context required for same-hierarchy integrity.

The canonical structure includes:

- `business_id`
- `tax_registration_id`
- `organization_id`
- `is_primary`
- `valid_from`
- `valid_until`
- description/audit fields

Tax registration is retained as legal/tax history and is not part of normal physical deletion.

## Identifiers and timestamps

### UUIDs

All entity identifiers use PostgreSQL `uuid`.

IDs are generated by the application.

The canonical SQL does not introduce database-generated UUID defaults.

Entity-specific primary-key names are retained.

### Time

Application timestamps use Java `Instant` and PostgreSQL `TIMESTAMPTZ`.

The canonical SQL does not introduce implicit database `now()` defaults for audit timestamps.

Created/updated timestamps and actors are application-managed.

## Constraints and integrity

The canonical static baseline is:

```text
Tenant V1 domain tables       39
Tenant V3 extension            1
Final tenant domain tables    40

Tenant V1 foreign keys        66
Final tenant foreign keys     68

Tenant CHECK constraints      14
Explicit tenant indexes       29

Control domain tables          2
Application catalog rows       3
Reference types                6
Reference values              42
```

The canonical validation package checks these structural expectations.

### Key integrity rules

The database includes integrity rules for:

- primary keys
- unique tenant/business/company/organization codes
- same-business Store/Brand integrity (database-enforced)
- Store/Channel same-business integrity (application-service enforced; not a database constraint)
- composite Role/Permission integrity
- user access-scope type allowlist
- latitude/longitude ranges
- effective/valid date ranges
- primary website domain rules
- primary store location rules
- primary warehouse rules
- tax registration validity ranges

Exact constraint and index names are defined by the canonical SQL package.

## Reference data

Tenant V2 seeds exactly six reference types:

```text
STATUS
CHANNEL_TYPE
BUSINESS_TYPE
TAX_REGISTRATION_TYPE
ORGANIZATION_TYPE
COMPANY_TYPE
```

The canonical validation baseline expects:

```text
6 reference types
42 reference values
```

The application catalog is independent of the generic reference-value mechanism and is represented by `public.application`.

ADR-004 is **Accepted for Phase 1** and the Application Catalog / Business Application model is part of the approved Phase 1 contract.

## Deletion and lifecycle policy

Phase 1 uses a hybrid deletion strategy.

Core business and governance records are normally retained and transitioned through lifecycle/status/history.

The default database behavior is restrictive:

```text
No blanket ON DELETE CASCADE
No blanket ON DELETE SET NULL
```

Physical deletion is limited to explicitly approved transient/configuration associations where historical retention is not required.

Examples:

```text
Tenant                    → archive/deactivate
Organization              → deactivate/archive
Company                   → deactivate/archive
Business                  → deactivate/archive
Brand                     → deactivate/archive
Store                     → deactivate/archive
Warehouse                 → deactivate/archive
User account              → disable
Role                      → deactivate
Permission                → deactivate
Tax registration          → deactivate/end validity
Audit/history             → retain
```

Configuration associations may be physically removed where permitted by the deletion matrix.

The canonical `DELETION_MATRIX.md` is the detailed Phase 1 lifecycle reference.

## Audit and history

The Phase 1 model contains history/audit structures for:

- hierarchy changes
- user access changes
- organization changes
- user account changes
- audit events

Historical records are retained by default.

Deletion of core business records must not bypass historical/audit requirements.

## Canonical Phase 1 tenant table catalog

The final tenant schema contains 40 domain tables: 39 from V1 and `business_application` from V3. Column-level definitions are governed by the canonical PostgreSQL DDL/data dictionary.

| # | Table | Responsibility |
|---:|---|---|
| 1 | `tenant` | Tenant identity/lifecycle |
| 2 | `organization` | Tenant organization |
| 3 | `organization_legal_detail` | Organization legal details |
| 4 | `organization_address` | Organization addresses |
| 5 | `organization_contact` | Organization contacts |
| 6 | `company` | Company |
| 7 | `company_legal_detail` | Company legal details |
| 8 | `company_address` | Company addresses |
| 9 | `company_contact` | Company contacts |
| 10 | `business` | Operational Business |
| 11 | `brand` | Business brands |
| 12 | `store` | Business stores |
| 13 | `store_location` | Store locations |
| 14 | `warehouse` | Store-owned warehouse |
| 15 | `warehouse_location` | Warehouse location |
| 16 | `pos` | Store POS |
| 17 | `website` | Store website |
| 18 | `website_domain` | Website domains |
| 19 | `channel` | Business channel |
| 20 | `store_channel` | Store-channel association |
| 21 | `store_branding` | Store branding |
| 22 | `store_theme` | Store theme |
| 23 | `country` | Country reference |
| 24 | `state` | State reference |
| 25 | `district` | District reference |
| 26 | `user_account` | Tenant user account |
| 27 | `role` | Roles |
| 28 | `permission` | Permissions |
| 29 | `role_permission` | Role-permission association |
| 30 | `user_access_scope` | User access scope |
| 31 | `hierarchy_change_history` | Hierarchy history |
| 32 | `user_access_history` | Access history |
| 33 | `organization_change_history` | Organization history |
| 34 | `user_account_history` | User history |
| 35 | `audit_log` | Domain audit |
| 36 | `reference_type` | Reference types |
| 37 | `reference_value` | Reference values |
| 38 | `tax_registration` | Tax registration |
| 39 | `business_tax_registration` | Business tax registration |
| 40 | `business_application` | Business application enablement |

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

`store_channel` has no `business_id`. Same-business validation is therefore an application-service rule: the service must verify that the Store and Channel resolve to the same Business before persisting the association.

## Application Catalog and Business Application

The database representation is finalized in the canonical SQL package.

### Platform catalog

```text
public.application
```

Seeded applications:

```text
E_COMMERCE
DISTRIBUTION
WAREHOUSE
```

### Business enablement

```text
tenant.business_application
```

A Business may have application enablement records through this table.

The database relationship is therefore:

```text
Business
   │
   └── business_application
            │
            └── application
                 (public control plane)
```

Detailed product semantics such as entitlement rules, dependency rules, enable/disable workflow, and commercial subscription behavior remain governed by the current PRD/ADR status and must not be inferred solely from the database schema.

## Database validation status

The direct-SQL Phase 1 validation has been executed successfully against the validation database.

The validated result includes:

```text
public.application              3
public.tenant_registry          2
tenant_t001 domain tables      40
tenant_t002 domain tables      40
tenant_t001 reference values   42
tenant_t002 reference values   42
```

The consolidated direct-SQL validation reported:

```text
PASS: ALL DIRECT-SQL PHASE 1 VALIDATIONS PASSED
```

The validation also covered:

- expected public tables
- expected tenant tables
- primary-key counts
- foreign-key counts
- CHECK constraints
- required FK names
- required indexes
- reference types and values
- application catalog
- Business Application cross-schema relationship
- absence of tenant-to-tenant foreign keys
- absence of blanket cascade actions
- tenant registry mapping
- tenant provisioning status
- tenant identity mapping
- two-tenant isolation

### Flyway replay status

Flyway replay validation is **deferred**.

The current Windows validation environment did not execute a clean Flyway replay. Therefore:

```text
Direct SQL database validation   PASS
Flyway replay validation         DEFERRED
```

This document must not state that Flyway runtime replay has passed.

Before production migration execution, a clean-database Flyway replay remains a required verification gate.

## Historical artifacts and reconciliation

The project also contains earlier database artifacts describing a 37-table model.

Those artifacts are retained as historical reference.

The current canonical model supersedes their table-count statements where they conflict with the canonical SQL package:

```text
Historical baseline      37
Canonical tenant V1      39
Canonical tenant final   40
```

The two additional V1 tables relative to the historical 37-table bundle are:

```text
tax_registration
business_tax_registration
```

The V3 addition is:

```text
business_application
```

The control plane separately adds:

```text
tenant_registry
application
```

## Implementation freeze rules

Before database implementation changes are accepted:

1. The canonical SQL package remains the database source of truth.
2. Entity mappings must match canonical table/column/constraint names.
3. No new database-generated UUID/timestamp defaults may be introduced without an explicit decision.
4. No blanket cascading foreign keys may be introduced.
5. Tenant-to-tenant foreign keys are prohibited.
6. Tenant schema resolution must remain server-controlled.
7. Control-plane and tenant-plane migration ownership must remain separated.
8. Changes to the 39-table V1 baseline or V3 `business_application` extension require coordinated updates to the database contract, API, architecture, tests, and relevant ADRs.
9. Tax-registration ownership/cardinality must not be changed implicitly.
10. The deletion matrix remains the lifecycle reference.
11. Flyway replay must be completed against a clean database before production migration use.

## Current database contract status

```text
Canonical schema                  FINAL
Control-plane catalog             FINAL
Tenant V1 = 39 tables             FINAL
Tenant V3 = +1 table              FINAL
Final tenant = 40 tables          FINAL
Reference data                    FINAL
Deletion policy                   FINAL
Direct-SQL validation              PASS
Flyway replay validation           DEFERRED
```

This document is the Phase 1 database contract. Changes after this point should be treated as controlled architecture/database changes and reconciled with the dependent API, security, testing, DevOps, UI, implementation plan, and ADR documents.
