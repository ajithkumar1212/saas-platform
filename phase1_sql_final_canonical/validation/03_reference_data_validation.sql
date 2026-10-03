\set ON_ERROR_STOP on
\set p_tenant_schema 'tenant_t001'

DO $$
DECLARE
    s text := :'p_tenant_schema';
    n integer;
    codes text[];
    type_codes text[];
BEGIN
    SELECT count(*) INTO n FROM public.application;
    IF n <> 3 THEN RAISE EXCEPTION 'FAIL: application catalog expected 3 rows, found %', n; END IF;

    SELECT array_agg(application_code ORDER BY application_code) INTO codes FROM public.application;
    IF codes <> ARRAY['DISTRIBUTION','E_COMMERCE','WAREHOUSE'] THEN
      RAISE EXCEPTION 'FAIL: application catalog codes are %, expected DISTRIBUTION,E_COMMERCE,WAREHOUSE', codes;
    END IF;

    EXECUTE format('SELECT array_agg(type_code ORDER BY type_code) FROM %I.reference_type', s) INTO type_codes;
    IF type_codes <> ARRAY['BUSINESS_TYPE','CHANNEL_TYPE','COMPANY_TYPE','ORGANIZATION_TYPE','STATUS','TAX_REGISTRATION_TYPE'] THEN
      RAISE EXCEPTION 'FAIL: reference type codes are %, expected canonical six codes', type_codes;
    END IF;

    EXECUTE format('SELECT count(*) FROM %I.reference_value', s) INTO n;
    IF n <> 42 THEN RAISE EXCEPTION 'FAIL: reference_value expected 42 rows, found %', n; END IF;

    EXECUTE format($q$
      SELECT count(*) FROM (
        SELECT reference_type_id, count(*) c FROM %I.reference_value GROUP BY reference_type_id
        HAVING (SELECT type_code FROM %I.reference_type rt WHERE rt.reference_type_id=reference_value.reference_type_id) = 'STATUS' AND count(*)<>4
           OR (SELECT type_code FROM %I.reference_type rt WHERE rt.reference_type_id=reference_value.reference_type_id) = 'CHANNEL_TYPE' AND count(*)<>7
           OR (SELECT type_code FROM %I.reference_type rt WHERE rt.reference_type_id=reference_value.reference_type_id) = 'BUSINESS_TYPE' AND count(*)<>9
           OR (SELECT type_code FROM %I.reference_type rt WHERE rt.reference_type_id=reference_value.reference_type_id) = 'TAX_REGISTRATION_TYPE' AND count(*)<>5
           OR (SELECT type_code FROM %I.reference_type rt WHERE rt.reference_type_id=reference_value.reference_type_id) = 'ORGANIZATION_TYPE' AND count(*)<>9
           OR (SELECT type_code FROM %I.reference_type rt WHERE rt.reference_type_id=reference_value.reference_type_id) = 'COMPANY_TYPE' AND count(*)<>8
      ) x$q$, s,s,s,s,s,s,s) INTO n;
    IF n <> 0 THEN RAISE EXCEPTION 'FAIL: reference value counts per type do not match canonical seed'; END IF;

    EXECUTE format($q$
      SELECT count(*) FROM (
        SELECT type_code, count(*) c FROM %I.reference_type GROUP BY type_code HAVING count(*)<>1
      ) x$q$, s) INTO n;
    IF n <> 0 THEN RAISE EXCEPTION 'FAIL: duplicate reference type codes detected'; END IF;

    EXECUTE format($q$
      SELECT count(*) FROM (
        SELECT reference_type_id, value_code, count(*) c FROM %I.reference_value GROUP BY reference_type_id, value_code HAVING count(*)<>1
      ) x$q$, s) INTO n;
    IF n <> 0 THEN RAISE EXCEPTION 'FAIL: duplicate reference value codes detected'; END IF;

    EXECUTE format($q$
      SELECT count(*) FROM %I.reference_value rv
      LEFT JOIN %I.reference_type rt ON rt.reference_type_id=rv.reference_type_id
      WHERE rt.reference_type_id IS NULL$q$, s, s) INTO n;
    IF n <> 0 THEN RAISE EXCEPTION 'FAIL: reference_value rows with missing reference_type detected: %', n; END IF;
END $$;

SELECT 'PASS: reference data and application catalog validated' AS result;
