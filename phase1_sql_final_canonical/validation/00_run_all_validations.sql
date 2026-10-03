\set ON_ERROR_STOP on
\echo '=== Phase 1 canonical database validation ==='
\i 01_schema_validation.sql
\i 02_constraint_validation.sql
\i 03_reference_data_validation.sql
\i 04_cross_schema_validation.sql
\i 05_tenant_isolation_validation.sql
\i 06_provisioning_smoke_test.sql
\i 07_flyway_history_validation.sql
\echo '=== ALL PHASE 1 VALIDATIONS PASSED ==='
