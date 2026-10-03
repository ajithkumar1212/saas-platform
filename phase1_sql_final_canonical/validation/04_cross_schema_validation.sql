\set ON_ERROR_STOP on
\set p_tenant_schema 'tenant_t001'

DO $$
DECLARE
    s text := :'p_tenant_schema';
    n integer;
BEGIN
    SELECT count(*) INTO n
    FROM pg_constraint c
    JOIN pg_class src ON src.oid=c.conrelid
    JOIN pg_namespace sns ON sns.oid=src.relnamespace
    JOIN pg_class tgt ON tgt.oid=c.confrelid
    JOIN pg_namespace tns ON tns.oid=tgt.relnamespace
    WHERE c.contype='f'
      AND sns.nspname=s
      AND src.relname='business_application'
      AND tns.nspname='public'
      AND tgt.relname='application';
    IF n <> 1 THEN RAISE EXCEPTION 'FAIL: expected exactly one business_application → public.application FK, found %', n; END IF;

    EXECUTE format($q$
      SELECT count(*) FROM %I.business_application ba
      LEFT JOIN public.application a ON a.application_id=ba.application_id
      WHERE a.application_id IS NULL$q$, s) INTO n;
    IF n <> 0 THEN RAISE EXCEPTION 'FAIL: % orphan business_application rows reference missing public.application rows', n; END IF;
END $$;

SELECT 'PASS: cross-schema application reference validated' AS result;
