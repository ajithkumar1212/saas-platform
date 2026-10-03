\set ON_ERROR_STOP on
\set p_tenant_schema 'tenant_t001'

DO $$
DECLARE
    s text := :'p_tenant_schema';
    n integer;
    versions text[];
BEGIN
    SELECT count(*) INTO n FROM public.flyway_schema_history WHERE success;
    IF n <> 3 THEN RAISE EXCEPTION 'FAIL: public Flyway successful migration count expected 3, found %', n; END IF;

    SELECT array_agg(version ORDER BY installed_rank) INTO versions FROM public.flyway_schema_history WHERE success;
    IF versions <> ARRAY['1','2','3'] THEN RAISE EXCEPTION 'FAIL: public Flyway versions are %, expected 1,2,3', versions; END IF;

    EXECUTE format('SELECT count(*) FROM %I.flyway_schema_history WHERE success', s) INTO n;
    IF n <> 3 THEN RAISE EXCEPTION 'FAIL: tenant % Flyway successful migration count expected 3, found %', s, n; END IF;

    EXECUTE format('SELECT count(*) FROM %I.flyway_schema_history WHERE success AND version IN (''1'',''2'',''3'')', s) INTO n;
    IF n <> 3 THEN RAISE EXCEPTION 'FAIL: tenant % does not contain successful versions 1,2,3', s; END IF;
END $$;

SELECT 'PASS: control/tenant Flyway histories are separated and complete' AS result;
