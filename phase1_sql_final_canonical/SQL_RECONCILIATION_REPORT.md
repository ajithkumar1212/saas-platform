# Phase 1 SQL Reconciliation Report

## Canonical source

The canonical tenant schema is the latest `phase1_step4_final_postgresql_ddl_v6_tax_cleanup.sql`. It was byte-compared against the previous tenant V1 migration source before packaging; they matched. The final package uses that schema as `tenant V1`.

## Corrections applied

1. Removed explicit `BEGIN`/`COMMIT` from tenant V2 so Flyway owns the transaction boundary.
2. Replaced invalid dynamic identifiers such as `:p_tenant_schema.reference_type` with `format('%I.%I', ...)` + `EXECUTE`.
3. Removed reliance on `current_schema()` for tenant validation; tenant schema is an explicit psql variable.
4. Standardized Flyway history table name to `flyway_schema_history`.
5. Corrected physical table expectations: 40 tenant domain tables + 1 Flyway history table = 41 physical tenant tables; 2 public domain tables + 1 Flyway history table = at least 3 physical public tables.
6. Converted validation scripts to assertion-style checks using `RAISE EXCEPTION` and `ON_ERROR_STOP`.
7. Added Flyway history/version separation validation.
8. Added stronger provisioning-state and tenant-id mapping validation.
9. Added optional second-tenant cross-schema FK validation.

## Static baseline

- Tenant domain tables: 39 in V1
- Tenant V3 table: 1 (`business_application`)
- Tenant domain tables after V3: 40
- Tenant baseline FKs: 66
- Tenant FKs after V3: 68
- Tenant CHECK constraints: 14
- Explicit tenant indexes: 28
- Control domain tables: 2 (`tenant_registry`, `application`)
- Application seed records: 3
- Reference types: 6
- Reference values: 42

## Not claimed yet

No live PostgreSQL execution was performed. Parser acceptance, actual Flyway behavior, PostgreSQL catalog state, two-tenant data isolation, and provisioning concurrency remain runtime validation gates.
