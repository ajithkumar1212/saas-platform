# Phase 1 Validation Checklist

| Area | Expected |
|---|---|
| Control tables | `public.tenant_registry`, `public.application` |
| Tenant baseline | 39 tables |
| Tenant application extension | `business_application` |
| Tenant total | 40 tables |
| One-tenant physical total | 42 tables |
| Tenant baseline FKs | 66 |
| Application catalog | E_COMMERCE, DISTRIBUTION, WAREHOUSE |
| Cross-schema FK | tenant `business_application` → `public.application` |
| Reference data | 6 reference types and their values |
| Tenant isolation | no tenant-to-tenant business FK |
| Registry | tenant → generated schema mapping |
| Provisioning state | only PROVISIONED after successful migration/bootstrap |

Run the scripts in `database/validation/01` through `06` after replay.
