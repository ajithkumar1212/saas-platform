\set ON_ERROR_STOP on

-- ============================================================================
-- XYZ Phase 1 - Consolidated Validation
-- Mode: DIRECT SQL REPLAY (NOT Flyway)
--
-- Validates both tenant_t001 and tenant_t002:
--   1. Schema topology
--   2. Primary/foreign/check constraints
--   3. Required indexes
--   4. Application catalog and reference data
--   5. Cross-schema application FK
--   6. Tenant isolation
--   7. Tenant provisioning state
--
-- IMPORTANT:
-- This script intentionally does NOT require flyway_schema_history because
-- the current database was built by executing SQL files directly with psql.
-- Flyway history must be validated separately after a real Flyway replay.
-- ============================================================================

\echo ''
\echo '============================================================'
\echo 'XYZ PHASE 1 - CONSOLIDATED DATABASE VALIDATION'
\echo '============================================================'
\echo ''

DO $$
DECLARE
    s text := 'tenant_t001';
    s2 text := 'tenant_t002';
    tenant_schemas text[] := ARRAY[s, s2];
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
        'reference_value',
        'business_application'
    ];
    required_fk text[] := ARRAY[
        'fk_organization_tenant_id',
        'fk_organization_legal_detail_organization_id',
        'fk_tax_registration_organization_id',
        'fk_tax_registration_country_id',
        'fk_tax_registration_state_id',
        'fk_business_tax_registration_business_organization',
        'fk_business_tax_registration_tax_organization',
        'fk_business_company_organization',
        'fk_store_business_brand',
        'fk_reference_value_reference_type_id',
        'fk_business_application_business',
        'fk_business_application_application'
    ];
    required_idx text[] := ARRAY[
        'uq_website_domain_primary',
        'uq_store_location_primary',
        'uq_warehouse_primary',
        'uq_business_tax_registration_primary',
        'uq_tax_registration_gstin',
        'uq_role_global_code',
        'uq_role_tenant_code',
        'uq_user_access_scope_user_id_role_id_scope_type_scope_id'
    ];
    t text;
    n bigint;
    missing_count bigint;
    physical_count bigint;
    public_count bigint;
    codes text[];
    type_codes text[];
BEGIN
    --------------------------------------------------------------------------
    -- 0. Public control-plane topology
    --------------------------------------------------------------------------
    SELECT count(*) INTO public_count
    FROM information_schema.tables
    WHERE table_schema = 'public'
      AND table_type = 'BASE TABLE';

    IF public_count <> 2 THEN
        RAISE EXCEPTION
            'FAIL [PUBLIC]: expected exactly 2 domain tables (application, tenant_registry), found %',
            public_count;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'application'
          AND table_type = 'BASE TABLE'
    ) THEN
        RAISE EXCEPTION 'FAIL [PUBLIC]: public.application is missing';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'tenant_registry'
          AND table_type = 'BASE TABLE'
    ) THEN
        RAISE EXCEPTION 'FAIL [PUBLIC]: public.tenant_registry is missing';
    END IF;

    --------------------------------------------------------------------------
    -- 1-7. Validate each tenant
    --------------------------------------------------------------------------
    FOREACH t IN ARRAY tenant_schemas LOOP

        s := t;

        IF NOT EXISTS (
            SELECT 1 FROM pg_namespace WHERE nspname = s
        ) THEN
            RAISE EXCEPTION 'FAIL [%]: tenant schema does not exist', s;
        END IF;

        ----------------------------------------------------------------------
        -- 1. Schema topology: exact 40 domain tables
        ----------------------------------------------------------------------
        SELECT count(*) INTO missing_count
        FROM unnest(expected) e(table_name)
        WHERE NOT EXISTS (
            SELECT 1
            FROM information_schema.tables it
            WHERE it.table_schema = s
              AND it.table_name = e.table_name
              AND it.table_type = 'BASE TABLE'
        );

        IF missing_count <> 0 THEN
            RAISE EXCEPTION
                'FAIL [%]: % expected domain tables are missing',
                s, missing_count;
        END IF;

        SELECT count(*) INTO n
        FROM information_schema.tables
        WHERE table_schema = s
          AND table_type = 'BASE TABLE';

        IF n <> 40 THEN
            RAISE EXCEPTION
                'FAIL [%]: expected exactly 40 domain tables, found %',
                s, n;
        END IF;

        ----------------------------------------------------------------------
        -- 2. Primary / foreign / check constraints
        ----------------------------------------------------------------------
        SELECT count(*) INTO n
        FROM pg_constraint c
        JOIN pg_namespace ns ON ns.oid = c.connamespace
        WHERE ns.nspname = s
          AND c.contype = 'p';

        IF n <> 40 THEN
            RAISE EXCEPTION
                'FAIL [%]: expected 40 primary keys, found %',
                s, n;
        END IF;

        SELECT count(*) INTO n
        FROM pg_constraint c
        JOIN pg_namespace ns ON ns.oid = c.connamespace
        WHERE ns.nspname = s
          AND c.contype = 'f';

        IF n <> 68 THEN
            RAISE EXCEPTION
                'FAIL [%]: expected 68 foreign keys, found %',
                s, n;
        END IF;

        SELECT count(*) INTO n
        FROM pg_constraint c
        JOIN pg_namespace ns ON ns.oid = c.connamespace
        WHERE ns.nspname = s
          AND c.contype = 'c';

        IF n <> 14 THEN
            RAISE EXCEPTION
                'FAIL [%]: expected 14 CHECK constraints, found %',
                s, n;
        END IF;

        FOREACH t IN ARRAY required_fk LOOP
            IF NOT EXISTS (
                SELECT 1
                FROM pg_constraint c
                JOIN pg_namespace ns ON ns.oid = c.connamespace
                WHERE ns.nspname = s
                  AND c.conname = t
                  AND c.contype = 'f'
            ) THEN
                RAISE EXCEPTION
                    'FAIL [%]: required FK % is missing',
                    s, t;
            END IF;
        END LOOP;

        ----------------------------------------------------------------------
        -- 3. Required indexes
        ----------------------------------------------------------------------
        FOREACH t IN ARRAY required_idx LOOP
            IF NOT EXISTS (
                SELECT 1
                FROM pg_indexes
                WHERE schemaname = s
                  AND indexname = t
            ) THEN
                RAISE EXCEPTION
                    'FAIL [%]: required index % is missing',
                    s, t;
            END IF;
        END LOOP;

        ----------------------------------------------------------------------
        -- 4. No cross-tenant FKs and no cascade actions
        ----------------------------------------------------------------------
        IF EXISTS (
            SELECT 1
            FROM pg_constraint c
            JOIN pg_namespace src ON src.oid = c.connamespace
            JOIN pg_class tgt ON tgt.oid = c.confrelid
            JOIN pg_namespace dst ON dst.oid = tgt.relnamespace
            WHERE c.contype = 'f'
              AND src.nspname = s
              AND dst.nspname LIKE 'tenant_%'
              AND dst.nspname <> s
        ) THEN
            RAISE EXCEPTION
                'FAIL [%]: cross-tenant foreign key detected',
                s;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM pg_constraint c
            JOIN pg_namespace ns ON ns.oid = c.connamespace
            WHERE ns.nspname = s
              AND (
                  pg_get_constraintdef(c.oid) ILIKE '%ON DELETE CASCADE%'
                  OR pg_get_constraintdef(c.oid) ILIKE '%ON UPDATE CASCADE%'
              )
        ) THEN
            RAISE EXCEPTION
                'FAIL [%]: cascade action found in tenant FK constraints',
                s;
        END IF;

        ----------------------------------------------------------------------
        -- 5. Reference data
        ----------------------------------------------------------------------
        EXECUTE format(
            'SELECT array_agg(type_code ORDER BY type_code) FROM %I.reference_type',
            s
        ) INTO type_codes;

        IF type_codes <> ARRAY[
            'BUSINESS_TYPE',
            'CHANNEL_TYPE',
            'COMPANY_TYPE',
            'ORGANIZATION_TYPE',
            'STATUS',
            'TAX_REGISTRATION_TYPE'
        ] THEN
            RAISE EXCEPTION
                'FAIL [%]: reference type codes are %, expected canonical six codes',
                s, type_codes;
        END IF;

        EXECUTE format(
            'SELECT count(*) FROM %I.reference_type',
            s
        ) INTO n;

        IF n <> 6 THEN
            RAISE EXCEPTION
                'FAIL [%]: expected 6 reference types, found %',
                s, n;
        END IF;

        EXECUTE format(
            'SELECT count(*) FROM %I.reference_value',
            s
        ) INTO n;

        IF n <> 42 THEN
            RAISE EXCEPTION
                'FAIL [%]: expected 42 reference values, found %',
                s, n;
        END IF;

        EXECUTE format($q$
            SELECT count(*)
            FROM (
                SELECT rt.type_code, count(rv.reference_value_id) AS value_count
                FROM %I.reference_type rt
                LEFT JOIN %I.reference_value rv
                  ON rv.reference_type_id = rt.reference_type_id
                GROUP BY rt.type_code
                HAVING
                    (rt.type_code = 'STATUS' AND count(rv.reference_value_id) <> 4)
                 OR (rt.type_code = 'CHANNEL_TYPE' AND count(rv.reference_value_id) <> 7)
                 OR (rt.type_code = 'BUSINESS_TYPE' AND count(rv.reference_value_id) <> 9)
                 OR (rt.type_code = 'TAX_REGISTRATION_TYPE' AND count(rv.reference_value_id) <> 5)
                 OR (rt.type_code = 'ORGANIZATION_TYPE' AND count(rv.reference_value_id) <> 9)
                 OR (rt.type_code = 'COMPANY_TYPE' AND count(rv.reference_value_id) <> 8)
            ) x
        $q$, s, s) INTO n;

        IF n <> 0 THEN
            RAISE EXCEPTION
                'FAIL [%]: reference value counts per type do not match canonical seed',
                s;
        END IF;

        EXECUTE format($q$
            SELECT count(*)
            FROM (
                SELECT type_code, count(*) AS c
                FROM %I.reference_type
                GROUP BY type_code
                HAVING count(*) <> 1
            ) x
        $q$, s) INTO n;

        IF n <> 0 THEN
            RAISE EXCEPTION
                'FAIL [%]: duplicate reference type codes detected',
                s;
        END IF;

        EXECUTE format($q$
            SELECT count(*)
            FROM (
                SELECT reference_type_id, value_code, count(*) AS c
                FROM %I.reference_value
                GROUP BY reference_type_id, value_code
                HAVING count(*) <> 1
            ) x
        $q$, s) INTO n;

        IF n <> 0 THEN
            RAISE EXCEPTION
                'FAIL [%]: duplicate reference value codes detected',
                s;
        END IF;

        EXECUTE format($q$
            SELECT count(*)
            FROM %I.reference_value rv
            LEFT JOIN %I.reference_type rt
              ON rt.reference_type_id = rv.reference_type_id
            WHERE rt.reference_type_id IS NULL
        $q$, s, s) INTO n;

        IF n <> 0 THEN
            RAISE EXCEPTION
                'FAIL [%]: reference_value rows with missing reference_type: %',
                s, n;
        END IF;

        ----------------------------------------------------------------------
        -- 6. Cross-schema application reference
        ----------------------------------------------------------------------
        SELECT count(*) INTO n
        FROM pg_constraint c
        JOIN pg_class src ON src.oid = c.conrelid
        JOIN pg_namespace sns ON sns.oid = src.relnamespace
        JOIN pg_class tgt ON tgt.oid = c.confrelid
        JOIN pg_namespace tns ON tns.oid = tgt.relnamespace
        WHERE c.contype = 'f'
          AND sns.nspname = s
          AND src.relname = 'business_application'
          AND tns.nspname = 'public'
          AND tgt.relname = 'application';

        IF n <> 1 THEN
            RAISE EXCEPTION
                'FAIL [%]: expected exactly one business_application -> public.application FK, found %',
                s, n;
        END IF;

        EXECUTE format($q$
            SELECT count(*)
            FROM %I.business_application ba
            LEFT JOIN public.application a
              ON a.application_id = ba.application_id
            WHERE a.application_id IS NULL
        $q$, s) INTO n;

        IF n <> 0 THEN
            RAISE EXCEPTION
                'FAIL [%]: orphan business_application rows reference missing public.application rows: %',
                s, n;
        END IF;

        ----------------------------------------------------------------------
        -- 7. Provisioning smoke test
        ----------------------------------------------------------------------
        SELECT count(*) INTO n
        FROM public.tenant_registry
        WHERE schema_name = s;

        IF n <> 1 THEN
            RAISE EXCEPTION
                'FAIL [%]: tenant_registry must contain exactly one row, found %',
                s, n;
        END IF;

        EXECUTE format(
            'SELECT count(*) FROM %I.tenant',
            s
        ) INTO n;

        IF n <> 1 THEN
            RAISE EXCEPTION
                'FAIL [%]: tenant table expected exactly 1 row, found %',
                s, n;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM public.tenant_registry r
            WHERE r.schema_name = s
              AND r.provisioning_status <> 'PROVISIONED'
        ) THEN
            RAISE EXCEPTION
                'FAIL [%]: provisioning_status is not PROVISIONED',
                s;
        END IF;

        EXECUTE format($q$
            SELECT count(*)
            FROM public.tenant_registry r
            JOIN %I.tenant t
              ON t.tenant_id = r.tenant_id
            WHERE r.schema_name = $1
        $q$, s) INTO n USING s;

        IF n <> 1 THEN
            RAISE EXCEPTION
                'FAIL [%]: tenant_registry.tenant_id does not match tenant.tenant_id',
                s;
        END IF;

    END LOOP;

    --------------------------------------------------------------------------
    -- 8. Public application catalog
    --------------------------------------------------------------------------
    SELECT count(*) INTO n
    FROM public.application;

    IF n <> 3 THEN
        RAISE EXCEPTION
            'FAIL [PUBLIC]: application catalog expected 3 rows, found %',
            n;
    END IF;

    SELECT array_agg(application_code ORDER BY application_code)
    INTO codes
    FROM public.application;

    IF codes <> ARRAY['DISTRIBUTION','E_COMMERCE','WAREHOUSE'] THEN
        RAISE EXCEPTION
            'FAIL [PUBLIC]: application catalog codes are %, expected DISTRIBUTION,E_COMMERCE,WAREHOUSE',
            codes;
    END IF;

    --------------------------------------------------------------------------
    -- 9. Tenant-to-tenant FK isolation
    --------------------------------------------------------------------------
    -- IMPORTANT: compare the two fixed tenant schema names, not the
    -- mutable loop variable used above.
    SELECT count(*) INTO n
    FROM pg_constraint c
    JOIN pg_class src ON src.oid = c.conrelid
    JOIN pg_namespace sns ON sns.oid = src.relnamespace
    JOIN pg_class tgt ON tgt.oid = c.confrelid
    JOIN pg_namespace tns ON tns.oid = tgt.relnamespace
    WHERE c.contype = 'f'
      AND (
          (sns.nspname = 'tenant_t001' AND tns.nspname = 'tenant_t002')
       OR (sns.nspname = 'tenant_t002' AND tns.nspname = 'tenant_t001')
      );

    IF n <> 0 THEN
        RAISE EXCEPTION
            'FAIL [ISOLATION]: cross-tenant FK exists between tenant_t001 and tenant_t002: %',
            n;
    END IF;

END $$;

\echo ''
\echo '============================================================'
\echo 'PASS: ALL DIRECT-SQL PHASE 1 VALIDATIONS PASSED'
\echo '============================================================'
\echo ''

-- Final human-readable summary.
SELECT
    'PASS' AS status,
    'public.application' AS object,
    (SELECT count(*) FROM public.application) AS row_count
UNION ALL
SELECT
    'PASS',
    'public.tenant_registry',
    (SELECT count(*) FROM public.tenant_registry)
UNION ALL
SELECT
    'PASS',
    'tenant_t001.domain_tables',
    (SELECT count(*) FROM information_schema.tables
     WHERE table_schema = 'tenant_t001'
       AND table_type = 'BASE TABLE')
UNION ALL
SELECT
    'PASS',
    'tenant_t002.domain_tables',
    (SELECT count(*) FROM information_schema.tables
     WHERE table_schema = 'tenant_t002'
       AND table_type = 'BASE TABLE')
UNION ALL
SELECT
    'PASS',
    'tenant_t001.reference_values',
    (SELECT count(*) FROM tenant_t001.reference_value)
UNION ALL
SELECT
    'PASS',
    'tenant_t002.reference_values',
    (SELECT count(*) FROM tenant_t002.reference_value);

\echo ''
\echo 'NOTE: Flyway history is intentionally not checked by this script.'
\echo 'This database was populated by direct SQL execution.'
\echo 'Run a separate Flyway replay to validate flyway_schema_history.'
\echo ''
