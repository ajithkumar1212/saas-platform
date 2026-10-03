# Phase 1 Canonical Replay Runbook

This package is **not executed by this artifact generation step**. It is prepared for a later clean PostgreSQL replay.

## Required execution model

1. Create a clean PostgreSQL database.
2. Run control Flyway against `public` using `migration/control`.
3. For each tenant, create the generated tenant schema server-side.
4. Run tenant Flyway against that tenant schema using `migration/tenant`.
5. Bootstrap the tenant-domain `tenant` row and administrator through the provisioning service.
6. Mark `public.tenant_registry.provisioning_status = 'PROVISIONED'` only after successful validation/bootstrap.
7. Run `validation/00_run_all_validations.sql` with `psql -v p_tenant_schema=tenant_t001 -v p_second_tenant_schema=tenant_t002 -f 00_run_all_validations.sql` from the validation directory.

The validation scripts use `ON_ERROR_STOP` and raise PostgreSQL exceptions on failed assertions.
