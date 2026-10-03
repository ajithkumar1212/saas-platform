\set ON_ERROR_STOP on
\set p_tenant_schema 'tenant_t001'

DO $$
DECLARE
    s text := :'p_tenant_schema';
    n integer;
    required_fk text[] := ARRAY[
      'fk_organization_tenant_id','fk_organization_legal_detail_organization_id',
      'fk_tax_registration_organization_id','fk_tax_registration_country_id','fk_tax_registration_state_id',
      'fk_business_tax_registration_business_organization','fk_business_tax_registration_tax_organization',
      'fk_business_company_organization','fk_store_business_brand','fk_reference_value_reference_type_id',
      'fk_business_application_business','fk_business_application_application'
    ];
    idx text[] := ARRAY[
      'uq_website_domain_primary','uq_store_location_primary','uq_warehouse_primary',
      'uq_business_tax_registration_primary','uq_tax_registration_gstin',
      'uq_role_global_code','uq_role_tenant_code','uq_user_access_scope_user_id_role_id_scope_type_scope_id'
    ];
    x text;
BEGIN
    SELECT count(*) INTO n FROM pg_constraint c JOIN pg_namespace ns ON ns.oid=c.connamespace
      WHERE ns.nspname=s AND c.contype='p' AND c.conrelid <> format('%I.flyway_schema_history',s)::regclass;
    IF n <> 40 THEN RAISE EXCEPTION 'FAIL: tenant PK count expected 40, found %', n; END IF;

    SELECT count(*) INTO n FROM pg_constraint c JOIN pg_namespace ns ON ns.oid=c.connamespace
      WHERE ns.nspname=s AND c.contype='f';
    IF n <> 68 THEN RAISE EXCEPTION 'FAIL: tenant FK count expected 68 (66 baseline + 2 business_application), found %', n; END IF;

    SELECT count(*) INTO n FROM pg_constraint c JOIN pg_namespace ns ON ns.oid=c.connamespace
      WHERE ns.nspname=s AND c.contype='c';
    IF n <> 14 THEN RAISE EXCEPTION 'FAIL: tenant CHECK count expected 14, found %', n; END IF;

    FOREACH x IN ARRAY required_fk LOOP
      IF NOT EXISTS (
        SELECT 1 FROM pg_constraint c JOIN pg_namespace ns ON ns.oid=c.connamespace
        WHERE ns.nspname=s AND c.conname=x AND c.contype='f'
      ) THEN RAISE EXCEPTION 'FAIL: required FK % is missing', x; END IF;
    END LOOP;

    FOREACH x IN ARRAY idx LOOP
      IF NOT EXISTS (
        SELECT 1 FROM pg_indexes WHERE schemaname=s AND indexname=x
      ) THEN RAISE EXCEPTION 'FAIL: required index % is missing', x; END IF;
    END LOOP;

    IF EXISTS (
      SELECT 1 FROM pg_constraint c
      JOIN pg_namespace src ON src.oid=c.connamespace
      JOIN pg_class tgt ON tgt.oid=c.confrelid
      JOIN pg_namespace dst ON dst.oid=tgt.relnamespace
      WHERE c.contype='f' AND src.nspname=s AND dst.nspname LIKE 'tenant_%' AND dst.nspname<>s
    ) THEN
      RAISE EXCEPTION 'FAIL: cross-tenant foreign key detected';
    END IF;

    IF EXISTS (
      SELECT 1 FROM pg_constraint c JOIN pg_namespace ns ON ns.oid=c.connamespace
      WHERE ns.nspname=s AND (pg_get_constraintdef(c.oid) ILIKE '%ON DELETE CASCADE%' OR pg_get_constraintdef(c.oid) ILIKE '%ON UPDATE CASCADE%')
    ) THEN
      RAISE EXCEPTION 'FAIL: cascade action found in tenant FK constraints';
    END IF;
END $$;

SELECT 'PASS: constraints and indexes validated' AS result;
