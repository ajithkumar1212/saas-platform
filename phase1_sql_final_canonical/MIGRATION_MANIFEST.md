# Phase 1 Canonical SQL Migration Manifest

## Control plane

| Version | Migration | Target | History |
|---|---|---|---|
| V1 | create_tenant_registry | `public` | `public.flyway_schema_history` |
| V2 | create_application_catalog | `public` | `public.flyway_schema_history` |
| V3 | insert_application_catalog | `public` | `public.flyway_schema_history` |

## Tenant plane

| Version | Migration | Target | History |
|---|---|---|---|
| V1 | create_phase1_schema | each tenant schema | `<tenant_schema>.flyway_schema_history` |
| V2 | insert_reference_data | each tenant schema | `<tenant_schema>.flyway_schema_history` |
| V3 | create_business_application | each tenant schema | `<tenant_schema>.flyway_schema_history` |

Control and tenant Flyway histories are independent. Tenant migrations must never be pointed at `public`.

Dependency: control V1 → V2 → V3, then tenant V1 → V2 → V3. Tenant V3 depends on `public.application`.
