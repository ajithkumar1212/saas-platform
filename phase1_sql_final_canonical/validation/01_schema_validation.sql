\set ON_ERROR_STOP on
\set p_tenant_schema 'tenant_t001'

-- Phase 1 schema validation. Standardized Flyway history table: flyway_schema_history.

DO $$
DECLARE
    tenant_schema text := :'p_tenant_schema';
    missing_count integer;
    domain_count integer;
    physical_count integer;
    public_domain_count integer;
    public_physical_count integer;
    t text;
    expected text[] := ARRAY[
        'tenant',
        'organization',
        'organization_legal_detail',
        'tax_registration',
        'business_tax_registration',
        'organization_address',
        'organization_contact',
        'company',
        'company_legal_detail',
        'company_address',
        'company_contact',
        'business',
        'brand',
        'store',
        'store_location',
        'warehouse',
        'warehouse_location',
        'pos',
        'website',
        'website_domain',
        'channel',
        'store_channel',
        'store_branding',
        'store_theme',
        'country',
        'state',
        'district',
        'user_account',
        'role',
        'permission',
        'role_permission',
        'user_access_scope',
        'hierarchy_change_history',
        'user_access_history',
        'organization_change_history',
        'user_account_history',
        'audit_log',
        'reference_type',
        'reference_value'
        ,'business_application'
    ];
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_namespace WHERE nspname = tenant_schema) THEN
        RAISE EXCEPTION 'FAIL: tenant schema % does not exist', tenant_schema;
    END IF;

    SELECT count(*) INTO missing_count
    FROM unnest(expected) e(t)
    WHERE NOT EXISTS (
        SELECT 1 FROM information_schema.tables it
        WHERE it.table_schema = tenant_schema
          AND it.table_name = e.t
          AND it.table_type = 'BASE TABLE'
    );
    IF missing_count <> 0 THEN
        RAISE EXCEPTION 'FAIL: % expected tenant domain tables are missing in %', missing_count, tenant_schema;
    END IF;

    SELECT count(*) INTO domain_count
    FROM information_schema.tables
    WHERE table_schema = tenant_schema
      AND table_type = 'BASE TABLE'
      AND table_name <> 'flyway_schema_history';
    IF domain_count <> 40 THEN
        RAISE EXCEPTION 'FAIL: expected 40 tenant domain tables, found % in %', domain_count, tenant_schema;
    END IF;

    SELECT count(*) INTO physical_count
    FROM information_schema.tables
    WHERE table_schema = tenant_schema
      AND table_type = 'BASE TABLE';
    IF physical_count <> 41 THEN
        RAISE EXCEPTION 'FAIL: expected 41 physical tenant tables including Flyway history, found %', physical_count;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema=tenant_schema AND table_name='flyway_schema_history' AND table_type='BASE TABLE'
    ) THEN
        RAISE EXCEPTION 'FAIL: % does not contain flyway_schema_history', tenant_schema;
    END IF;

    SELECT count(*) INTO public_domain_count
    FROM information_schema.tables
    WHERE table_schema='public' AND table_type='BASE TABLE'
      AND table_name IN ('tenant_registry','application');
    IF public_domain_count <> 2 THEN
        RAISE EXCEPTION 'FAIL: public control domain table count expected 2, found %', public_domain_count;
    END IF;

    SELECT count(*) INTO public_physical_count
    FROM information_schema.tables
    WHERE table_schema='public' AND table_type='BASE TABLE';
    IF public_physical_count <> 3 THEN
        RAISE EXCEPTION 'FAIL: expected exactly 3 public physical tables (tenant_registry, application, flyway_schema_history), found %', public_physical_count;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema='public' AND table_name='flyway_schema_history' AND table_type='BASE TABLE'
    ) THEN
        RAISE EXCEPTION 'FAIL: public does not contain flyway_schema_history';
    END IF;
END $$;

SELECT 'PASS: schema topology validated' AS result;
