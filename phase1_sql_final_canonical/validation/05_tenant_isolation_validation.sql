\set ON_ERROR_STOP on
\set p_tenant_schema 'tenant_t001'
\set p_second_tenant_schema 'tenant_t002'

DO $$
DECLARE
    s text := :'p_tenant_schema';
    s2 text := :'p_second_tenant_schema';
    n integer;
BEGIN
    SELECT count(*) INTO n FROM public.tenant_registry WHERE schema_name=s;
    IF n <> 1 THEN RAISE EXCEPTION 'FAIL: tenant_registry must contain exactly one row for schema %, found %', s, n; END IF;

    SELECT count(*) INTO n
    FROM pg_constraint c
    JOIN pg_namespace src ON src.oid=c.connamespace
    JOIN pg_class tgt ON tgt.oid=c.confrelid
    JOIN pg_namespace dst ON dst.oid=tgt.relnamespace
    WHERE c.contype='f'
      AND src.nspname=s
      AND dst.nspname LIKE 'tenant_%'
      AND dst.nspname<>s;
    IF n <> 0 THEN RAISE EXCEPTION 'FAIL: tenant % has % cross-tenant FKs', s, n; END IF;

    IF EXISTS (SELECT 1 FROM pg_namespace WHERE nspname=s2) THEN
      SELECT count(*) INTO n
      FROM pg_constraint c
      JOIN pg_namespace src ON src.oid=c.connamespace
      JOIN pg_class tgt ON tgt.oid=c.confrelid
      JOIN pg_namespace dst ON dst.oid=tgt.relnamespace
      WHERE c.contype='f'
        AND ((src.nspname=s AND dst.nspname=s2) OR (src.nspname=s2 AND dst.nspname=s));
      IF n <> 0 THEN RAISE EXCEPTION 'FAIL: cross-tenant FK exists between % and %', s, s2; END IF;
    END IF;
END $$;

SELECT 'PASS: tenant schema isolation checks validated' AS result;
