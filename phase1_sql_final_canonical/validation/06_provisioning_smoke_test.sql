\set ON_ERROR_STOP on
\set p_tenant_schema 'tenant_t001'

DO $$
DECLARE
    s text := :'p_tenant_schema';
    reg public.tenant_registry%ROWTYPE;
    n integer;
    tenant_id uuid;
BEGIN
    SELECT * INTO reg FROM public.tenant_registry WHERE schema_name=s;
    IF NOT FOUND THEN RAISE EXCEPTION 'FAIL: no tenant_registry row for schema %', s; END IF;
    IF reg.provisioning_status <> 'PROVISIONED' THEN
      RAISE EXCEPTION 'FAIL: tenant % provisioning_status is %, expected PROVISIONED', s, reg.provisioning_status;
    END IF;

    EXECUTE format('SELECT count(*), max(tenant_id) FROM %I.tenant', s) INTO n, tenant_id;
    IF n <> 1 THEN RAISE EXCEPTION 'FAIL: tenant domain table in % expected exactly 1 row, found %', s, n; END IF;
    IF tenant_id <> reg.tenant_id THEN
      RAISE EXCEPTION 'FAIL: tenant_registry tenant_id % does not match % tenant row %', reg.tenant_id, s, tenant_id;
    END IF;
END $$;

SELECT 'PASS: tenant provisioning smoke test validated' AS result;
