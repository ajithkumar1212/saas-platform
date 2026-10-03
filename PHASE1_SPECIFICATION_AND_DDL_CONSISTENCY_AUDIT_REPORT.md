# Phase 1 Specification & PostgreSQL DDL Consistency Audit Report

**Audit Target:** Multi-Tenant SaaS Platform Specification vs. Canonical PostgreSQL DDL / Flyway Migrations  
**Target Path:** `D:\saas\`  
**Date:** October 2026  
**Auditor:** Antigravity (Advanced Agentic Architecture & Database Verification)  

---

## 1. EXECUTIVE RESULT

- **Overall Consistency:** **FAIL**

While the database topology (schema-per-tenant, control-plane vs. tenant-schema separation), reference data seeding (6 types, 42 values), application catalog seed data (`E_COMMERCE`, `DISTRIBUTION`, `WAREHOUSE`), and tenant table counts (39 in V1 + 1 in V3 = 40) demonstrate strong alignment between the canonical SQL package and the recent documentation updates, several **critical architectural contradictions, entity hierarchy parentage mismatches, and unimplemented database integrity claims** exist between the documentation set and the canonical DDL.

---

## 2. CRITICAL MISMATCHES

| ID | Document Source | DDL Source | Issue Description | Severity |
|---|---|---|---|---|
| **CM-01** | [`03-database-final.md` (lines 207-217)](docs/03-database-final.md#L207-L217)<br>[`04-api-final.md` (lines 40-47, 184-186)](docs/04-api-final.md#L40-L47) | [`V1__create_phase1_schema.sql` (lines 292-308, 334-350, 352-366)](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L292-L308) | **Hierarchy Parentage & FK Mismatch (Warehouse, POS, Website):**<br>`03-database-final.md` and `04-api-final.md` model Warehouse, POS, and Website as direct children of `Business`, specifying endpoints `/businesses/{businessId}/warehouses`, `/pos`, `/websites`. In the DDL, `warehouse`, `pos`, and `website` do not have a `business_id` column; they strictly carry `store_id uuid NOT NULL` referencing `store(store_id)`. Conversely, [`06-testing-qa-final.md` (lines 175-181)](docs/06-testing-qa-final.md#L175-L181) contradicts documents 03 and 04 by nesting them under `Store`. | **Critical** |
| **CM-02** | [`02-system-architecture-final.md` (line 401)](docs/02-system-architecture-final.md#L401)<br>[`03-database-final.md` (line 338)](docs/03-database-final.md#L338)<br>[`06-testing-qa-final.md` (line 193)](docs/06-testing-qa-final.md#L193) | [`V1__create_phase1_schema.sql` (lines 405-416, 749-750)](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L405-L416) | **Unimplemented Same-Business Integrity for Store/Channel:**<br>The documentation repeatedly asserts that the database enforces same-business integrity for Store/Channel associations ("composite Store/Channel association integrity"). However, DDL table `store_channel` contains only `(store_id, channel_id)` referencing `store` and `channel` independently. There is no `business_id` column and no composite foreign key or CHECK constraint guaranteeing that the store and channel belong to the same business. | **High** |
| **CM-03** | [`00-project-context.md` (line 18)](docs/00-project-context.md#L18)<br>[`01-prd.md` (line 59)](docs/01-prd.md#L59)<br>[`02-system-architecture-final.md` (line 464)](docs/02-system-architecture-final.md#L464)<br>[`04-api-final.md` (lines 132-146)](docs/04-api-final.md#L132-L146) | [`ADR-004-application-model.md` (line 3)](docs/10-adr/ADR-004-application-model.md#L3)<br>[`10-adr-validation-report.md` (line 14)](docs/10-adr/10-adr-validation-report.md#L14)<br>[`V3__create_business_application.sql`](phase1_sql_final_canonical/migration/tenant/V3__create_business_application.sql) | **Cross-Document Contradiction on ADR-004 Approval Status:**<br>`ADR-004` is formally recorded as `Status: Accepted for Phase 1`. In contrast, `00-project-context.md`, `01-prd.md`, `02-system-architecture-final.md`, and `04-api-final.md` explicitly describe ADR-004 as `Open` / unresolved, marking API routes (`/api/v1/applications`, `/api/v1/businesses/{businessId}/applications`) as `Open/Blocked`. | **High** |
| **CM-04** | [`09-security-final.md` (line 90)](docs/09-security-final.md#L90)<br>[`04-api-final.md` (line 236)](docs/04-api-final.md#L236) | [`V1__create_phase1_schema.sql` (line 518)](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L518) | **Password Persistence Policy Contradiction:**<br>`09-security-final.md` mandates *"do not persist identity-provider passwords in application tables"* and `04-api-final.md` notes that credentials are not managed via an application password API. However, table `user_account` in the DDL retains `password_hash varchar(255) NULL`. | **Medium** |
| **CM-05** | [`04-api-final.md` (lines 330-338)](docs/04-api-final.md#L330-L338) | [`V1__create_phase1_schema.sql` (lines 672-687)](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L672-L687) | **Missing Audit Log Outcome Column:**<br>`04-api-final.md` specifies that audit event records must capture `outcome`. Table `audit_log` in the DDL lacks an `outcome` or `status` column. | **Medium** |

---

## 3. TABLE RECONCILIATION

The canonical SQL package establishes exactly **42 domain tables**: 2 public control-plane tables and 40 tenant-schema tables (39 in V1 + 1 in V3).

| # | Table Name | Schema Plane | Documented in Spec | Exists in DDL | Consistency Status |
|---|---|---|---|---|---|
| 1 | `tenant_registry` | Control (`public`) | Yes ([03-database-final.md](docs/03-database-final.md#L47)) | Yes ([V1__create_tenant_registry.sql](phase1_sql_final_canonical/migration/control/V1__create_tenant_registry.sql#L6)) | **E. Consistent** |
| 2 | `application` | Control (`public`) | Yes ([03-database-final.md](docs/03-database-final.md#L48)) | Yes ([V2__create_application_catalog.sql](phase1_sql_final_canonical/migration/control/V2__create_application_catalog.sql#L5)) | **E. Consistent** |
| 3 | `tenant` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L198)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L5)) | **E. Consistent** |
| 4 | `organization` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L199)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L19)) | **E. Consistent** |
| 5 | `organization_legal_detail` | Tenant (`tenant_*`) | Omitted from 03 body; in Deletion Matrix | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L34)) | **B. Documentation omission** |
| 6 | `tax_registration` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L238)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L49)) | **E. Consistent** |
| 7 | `business_tax_registration` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L239)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L74)) | **E. Consistent** |
| 8 | `organization_address` | Tenant (`tenant_*`) | Omitted from 03 body; in Deletion Matrix | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L95)) | **B. Documentation omission** |
| 9 | `organization_contact` | Tenant (`tenant_*`) | Omitted from 03 body; in Deletion Matrix | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L119)) | **B. Documentation omission** |
| 10 | `company` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L200)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L138)) | **E. Consistent** |
| 11 | `company_legal_detail` | Tenant (`tenant_*`) | Omitted from 03 body; in Deletion Matrix | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L154)) | **B. Documentation omission** |
| 12 | `company_address` | Tenant (`tenant_*`) | Omitted from 03 body; in Deletion Matrix | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L172)) | **B. Documentation omission** |
| 13 | `company_contact` | Tenant (`tenant_*`) | Omitted from 03 body; in Deletion Matrix | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L195)) | **B. Documentation omission** |
| 14 | `business` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L201)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L215)) | **E. Consistent** |
| 15 | `brand` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L208)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L232)) | **E. Consistent** |
| 16 | `store` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L209)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L248)) | **E. Consistent** |
| 17 | `store_location` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L210)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L265)) | **E. Consistent** |
| 18 | `warehouse` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L211)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L292)) | **A. Confirmed mismatch** (Parentage mismatch) |
| 19 | `warehouse_location` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L212)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L311)) | **E. Consistent** |
| 20 | `pos` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L213)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L334)) | **A. Confirmed mismatch** (Parentage mismatch) |
| 21 | `website` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L214)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L352)) | **A. Confirmed mismatch** (Parentage mismatch) |
| 22 | `website_domain` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L215)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L368)) | **E. Consistent** |
| 23 | `channel` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L216)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L389)) | **E. Consistent** |
| 24 | `store_channel` | Tenant (`tenant_*`) | Omitted from 03 body; in Deletion Matrix | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L405)) | **A. Confirmed mismatch** (Constraint overstatement) |
| 25 | `store_branding` | Tenant (`tenant_*`) | Omitted from 03 body; in Deletion Matrix | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L418)) | **B. Documentation omission** |
| 26 | `store_theme` | Tenant (`tenant_*`) | Omitted from 03 body; in Deletion Matrix | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L440)) | **B. Documentation omission** |
| 27 | `country` | Tenant (`tenant_*`) | In validation array; omitted from 03 body | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L461)) | **B. Documentation omission** |
| 28 | `state` | Tenant (`tenant_*`) | In validation array; omitted from 03 body | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L476)) | **B. Documentation omission** |
| 29 | `district` | Tenant (`tenant_*`) | In validation array; omitted from 03 body | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L494)) | **B. Documentation omission** |
| 30 | `user_account` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L397)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L509)) | **D. Ambiguous** (`password_hash` column) |
| 31 | `role` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L398)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L542)) | **E. Consistent** |
| 32 | `permission` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L399)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L556)) | **E. Consistent** |
| 33 | `role_permission` | Tenant (`tenant_*`) | Omitted from 03 body; in Deletion Matrix | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L571)) | **B. Documentation omission** |
| 34 | `user_access_scope` | Tenant (`tenant_*`) | Yes ([00-project-context.md](docs/00-project-context.md#L84)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L582)) | **E. Consistent** |
| 35 | `hierarchy_change_history` | Tenant (`tenant_*`) | Conceptually noted; omitted from 03 body | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L601)) | **B. Documentation omission** (Schema omitted) |
| 36 | `user_access_history` | Tenant (`tenant_*`) | Conceptually noted; omitted from 03 body | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L620)) | **B. Documentation omission** (Schema omitted) |
| 37 | `organization_change_history` | Tenant (`tenant_*`) | Conceptually noted; omitted from 03 body | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L641)) | **B. Documentation omission** (Schema omitted) |
| 38 | `user_account_history` | Tenant (`tenant_*`) | Conceptually noted; omitted from 03 body | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L657)) | **B. Documentation omission** (Schema omitted) |
| 39 | `audit_log` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L416)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L672)) | **B. Documentation omission** (`outcome` missing) |
| 40 | `reference_type` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L352)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L689)) | **E. Consistent** |
| 41 | `reference_value` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L352)) | Yes ([V1__create_phase1_schema.sql](phase1_sql_final_canonical/migration/tenant/V1__create_phase1_schema.sql#L703)) | **E. Consistent** |
| 42 | `business_application` | Tenant (`tenant_*`) | Yes ([03-database-final.md](docs/03-database-final.md#L77)) | Yes ([V3__create_business_application.sql](phase1_sql_final_canonical/migration/tenant/V3__create_business_application.sql#L4)) | **E. Consistent** |

---

## 4. COLUMN & CONSTRAINT VERIFICATION

### 4.1 Primary Keys
- **Total Tenant PKs:** Exactly 40 domain table primary keys (verified by assertion in `02_constraint_validation.sql`).
- **Control Plane PKs:** 2 primary keys (`pk_tenant_registry`, `pk_application`).
- **UUID & Application Generation:** All primary keys use `uuid` without database default generators (no `gen_random_uuid()` or `uuid_generate_v4()`), aligning with [ADR-005](docs/10-adr/ADR-005-database-strategy.md#L28).
- **Syntax Nullability Observation:** Single-column PKs in `V1__create_phase1_schema.sql` omit explicit `NOT NULL` in the DDL column specification (e.g. `tenant_id uuid`), whereas composite PKs (`business_tax_registration`, `store_channel`, `role_permission`) and subsequent migrations explicitly specify `NOT NULL`. In PostgreSQL, a primary key constraint implicitly enforces `NOT NULL` on underlying columns, so physical consistency is maintained.

### 4.2 Foreign Keys
- **Baseline Tenant FKs (V1):** Exactly 66 foreign keys (defined via `ALTER TABLE ... ADD CONSTRAINT`).
- **V3 Additions:** Exactly 2 foreign keys in `business_application`:
  - `fk_business_application_business`: `business_id` $\rightarrow$ `business(business_id)`
  - `fk_business_application_application`: `application_id` $\rightarrow$ `public.application(application_id)`
- **Total Tenant FKs:** **68** (verified by assertion in `02_constraint_validation.sql`).
- **Cascade Policy:** **Zero** `ON DELETE CASCADE` or `ON UPDATE CASCADE` constraints exist across the schema. All foreign keys default to `NO ACTION` / `RESTRICT`, matching the hybrid deletion policy in [ADR-007](docs/10-adr/ADR-007-deletion-policy.md).

### 4.3 Check Constraints
There are exactly **14 CHECK constraints** in the tenant schema (all 14 verified in `02_constraint_validation.sql`):
1. `ck_tax_registration_valid_until_range`: `valid_until IS NULL OR valid_until >= valid_from`
2. `ck_business_tax_registration_valid_until_range`: `valid_until IS NULL OR valid_until >= valid_from`
3. `ck_user_access_scope_scope_type`: `scope_type IN ('ORGANIZATION','COMPANY','BUSINESS','BRAND','STORE','CHANNEL')`
4. `ck_organization_address_latitude`: `latitude BETWEEN -90 AND 90`
5. `ck_organization_address_longitude`: `longitude BETWEEN -180 AND 180`
6. `ck_company_address_latitude`: `latitude BETWEEN -90 AND 90`
7. `ck_company_address_longitude`: `longitude BETWEEN -180 AND 180`
8. `ck_store_location_latitude`: `latitude BETWEEN -90 AND 90`
9. `ck_store_location_longitude`: `longitude BETWEEN -180 AND 180`
10. `ck_warehouse_location_latitude`: `latitude BETWEEN -90 AND 90`
11. `ck_warehouse_location_longitude`: `longitude BETWEEN -180 AND 180`
12. `ck_user_access_scope_valid_until_range`: `valid_until IS NULL OR valid_until >= valid_from`
13. `ck_hierarchy_change_history_effective_until_range`: `effective_until IS NULL OR effective_until >= effective_from`
14. `ck_user_access_history_effective_until_range`: `effective_until IS NULL OR effective_until >= effective_from`

### 4.4 Explicit Indexes
- Tenant V1 creates **28 explicit indexes** (including unique indexes and filtered partial indexes).
- Tenant V3 creates **1 explicit index**: `ix_business_application_application_id ON business_application (application_id)`.
- Total Tenant Explicit Indexes after V3: **29**.
  *(Discrepancy: `03-database-final.md` and `SQL_RECONCILIATION_REPORT.md` state 28 explicit indexes, failing to count the V3 index).*
- Control Plane creates **2 explicit indexes**: `ix_tenant_registry_status` and `ix_tenant_registry_provisioning_status`.

---

## 5. TENANT ISOLATION AUDIT

- **Namespace Separation:** 1 PostgreSQL schema per Tenant (`tenant_<generated>`). Control plane resides in `public`.
- **Intra-Tenant vs Cross-Tenant Integrity:** Validated by [05_tenant_isolation_validation.sql](phase1_sql_final_canonical/validation/05_tenant_isolation_validation.sql); zero cross-tenant foreign keys exist.
- **Cross-Schema Allowance:** The sole cross-schema foreign key is `<tenant_schema>.business_application.application_id` pointing to `public.application.application_id`.
- **Security Posture:** Documented in [02-system-architecture-final.md](docs/02-system-architecture-final.md) and [09-security-final.md](docs/09-security-final.md) that client-supplied schema names are never trusted; schema switching is performed transaction-locally using server-resolved registry state.

---

## 6. MIGRATION ORDER & REPLAY STATUS

### 6.1 Canonical Topology
```text
Control Plane (Target: public, History: public.flyway_schema_history)
  V1__create_tenant_registry.sql
       ↓
  V2__create_application_catalog.sql
       ↓
  V3__insert_application_catalog.sql
       ↓
Tenant Plane (Target: <tenant_schema>, History: <tenant_schema>.flyway_schema_history)
  V1__create_phase1_schema.sql
       ↓
  V2__insert_reference_data.sql
       ↓
  V3__create_business_application.sql
```

### 6.2 Execution Evidence vs. Claims
- **Direct-SQL Execution:** Successfully executed and validated against the validation database (`tenant_t001` and `tenant_t002`).
- **Flyway Replay Execution:** Accurately and consistently recorded as **DEFERRED** across all documents ([`03-database-final.md`](docs/03-database-final.md#L503-L514), [`06-testing-qa-final.md`](docs/06-testing-qa-final.md#L11), [`07-devops.md`](docs/07-devops.md#L173), [`08-implementation-plan-final.md`](docs/08-implementation-plan-final.md#L40)). No document falsely claims Flyway runtime verification.

---

## 7. REFERENCE DATA & APPLICATION CATALOG

### 7.1 Reference Data
Seeded by `V2__insert_reference_data.sql` and verified by `03_reference_data_validation.sql`:
- **6 Reference Types:** `STATUS`, `CHANNEL_TYPE`, `BUSINESS_TYPE`, `TAX_REGISTRATION_TYPE`, `ORGANIZATION_TYPE`, `COMPANY_TYPE`.
- **42 Reference Values:**
  - `STATUS`: 4 values (`ACTIVE`, `INACTIVE`, `SUSPENDED`, `ARCHIVED`)
  - `CHANNEL_TYPE`: 7 values (`ONLINE`, `OFFLINE`, `MARKETPLACE`, `SOCIAL_COMMERCE`, `MOBILE_APP`, `POS`, `B2B`)
  - `BUSINESS_TYPE`: 9 values (`RETAIL`, `WHOLESALE`, `DISTRIBUTION`, `MANUFACTURING`, `SERVICE`, `E_COMMERCE`, `B2B`, `B2C`, `D2C`)
  - `TAX_REGISTRATION_TYPE`: 5 values (`GST`, `VAT`, `SALES_TAX`, `TAX_ID`, `OTHER`)
  - `ORGANIZATION_TYPE`: 9 values (`GROUP`, `HOLDING_COMPANY`, `CORPORATION`, `PARTNERSHIP`, `PROPRIETORSHIP`, `TRUST`, `SOCIETY`, `NON_PROFIT`, `OTHER`)
  - `COMPANY_TYPE`: 8 values (`PRIVATE_LIMITED`, `PUBLIC_LIMITED`, `LLP`, `PARTNERSHIP`, `PROPRIETORSHIP`, `ONE_PERSON_COMPANY`, `GOVERNMENT`, `OTHER`)

### 7.2 Application Catalog Seed
Seeded by `V3__insert_application_catalog.sql` into `public.application`:
1. `E_COMMERCE` (`10000000-0000-0000-0000-000000000001`)
2. `DISTRIBUTION` (`10000000-0000-0000-0000-000000000002`)
3. `WAREHOUSE` (`10000000-0000-0000-0000-000000000003`)
- **Shared Domain Rule:** `Inventory` is consistently classified across all documents as a shared core capability and excluded from selectable applications.

---

## 8. DETAILED CHECKLIST OF 30 VERIFICATION ITEMS

| # | Check Item | Result | Findings & Details |
|---|---|---|---|
| 1 | Every database table documented actually exists in the DDL | **PASS** | All documented tables exist in DDL or Flyway migration scripts. |
| 2 | Every DDL table is correctly represented in the documentation | **FAIL (B)** | 37 out of 40 tenant tables lack explicit schema/column definitions in `03-database-final.md`. |
| 3 | Table names match exactly | **PASS** | Exact case-insensitive / snake_case matching confirmed across all 42 domain tables. |
| 4 | Column names match exactly | **FAIL (C)** | `audit_log.outcome` documented in `04-api-final.md` is missing in DDL. |
| 5 | Data types match | **PASS** | Documented column types (`uuid`, `varchar`, `timestamptz`, `boolean`) match DDL. |
| 6 | Nullable / NOT NULL rules match | **PASS** | Required fields match DDL rules; PostgreSQL PK non-nullability preserved. |
| 7 | Primary keys match | **PASS** | All 40 tenant PKs and 2 control PKs match specifications. |
| 8 | Foreign keys match | **FAIL (A/C)** | Warehouse/POS/Website FKs point to `store`, not `business`. Store/Channel lacks composite FK. |
| 9 | FK direction and referenced tables/columns match | **FAIL (A)** | `warehouse.store_id`, `pos.store_id`, `website.store_id` contradict Business-parentage in docs 03 & 04. |
| 10 | Unique constraints match | **PASS** | Natural unique keys (tenant codes, store codes, domain uniqueness) match DDL. |
| 11 | CHECK constraints match | **PASS** | Exactly 14 tenant CHECK constraints in DDL match validation baseline. |
| 12 | Indexes documented as required actually exist | **PASS** | All 8 required validation indexes exist in DDL (total count has minor documentation discrepancy). |
| 13 | Tenant isolation rules match actual FK structure | **PASS** | Zero cross-tenant foreign keys; single controlled public application catalog reference. |
| 14 | public/control-plane tables correctly separated | **PASS** | Clean physical separation (`public` vs `tenant_<generated>`). |
| 15 | tenant_registry is correctly represented | **PASS** | Table structure, unique constraints, and mapping role match documentation. |
| 16 | application catalog is correctly represented | **PASS** | Control-plane catalog `public.application` matches specification. |
| 17 | Application seed data contains exact 3 codes | **PASS** | Seed contains exactly `E_COMMERCE`, `DISTRIBUTION`, `WAREHOUSE`. |
| 18 | business_application is correctly represented | **PASS** | Tenant enablement table with composite uniqueness `(business_id, application_id)` matches DDL. |
| 19 | Reference types and reference values match DDL | **PASS** | Exactly 6 reference types and 42 reference values match DDL seed. |
| 20 | Tenant table count matches canonical implementation | **PASS** | Exactly 39 in V1 + 1 in V3 = 40 domain tables (41 physical with Flyway history). |
| 21 | Migration ordering is correct | **PASS** | Control V1–V3 precedes Tenant V1–V3; dependency on `public.application` satisfied. |
| 22 | Cross-schema dependencies are correct | **PASS** | Only `business_application` $\rightarrow$ `public.application` cross-schema FK exists. |
| 23 | Provisioning flow is consistent with DDL | **PASS** | Registry creation $\rightarrow$ schema generation $\rightarrow$ V1–V3 migrations $\rightarrow$ bootstrap $\rightarrow$ PROVISIONED. |
| 24 | Audit/history tables are consistent | **FAIL (C)** | 5 history/audit tables exist; `audit_log` is missing documented `outcome` column. |
| 25 | Tax registration tables are consistent | **PASS** | `tax_registration` and `business_tax_registration` match structure, PKs, FKs, and validity checks. |
| 26 | Store/Brand/Channel relationships consistent | **FAIL (A/C)** | Document claims database enforces same-business Store/Channel integrity; DDL lacks constraint. |
| 27 | Warehouse/POS/Website relationships consistent | **FAIL (A)** | DDL attaches them to `store_id`; Docs 03 and 04 model them under `business_id` (Doc 06 models under Store). |
| 28 | No documentation claims something DDL does not implement | **FAIL (A)** | Docs claim same-business Store/Channel DB integrity and Business-level Warehouse/POS/Website endpoints. |
| 29 | No DDL functionality is undocumented where authoritative | **FAIL (D)** | `user_account.password_hash` is present in DDL but forbidden by security documentation. |
| 30 | Identify any contradiction between documents | **FAIL (A)** | ADR-004 status (`Accepted` in ADR vs `Open` in Context/PRD/Arch/API); Warehouse parentage (03/04 vs 06). |

---

## 9. DOCUMENTATION OMISSIONS

1. **Absence of Canonical Data Dictionary in `03-database-final.md`:** While declaring itself the authoritative database contract, `03-database-final.md` only provides explicit column definitions for `public.tenant_registry`, `public.application`, `tax_registration`, `business_tax_registration`, and `business_application`. The columns, types, nullability rules, and specific constraints for the remaining 37 tenant tables are omitted from document text.
2. **Missing Table Catalog Entries:** Tables `organization_legal_detail`, `organization_address`, `organization_contact`, `company_legal_detail`, `company_address`, `company_contact`, `store_branding`, `store_theme`, `country`, `state`, and `district` are omitted from the narrative body of `03-database-final.md`.
3. **Explicit Index Count Stale:** `03-database-final.md` and `SQL_RECONCILIATION_REPORT.md` state `Explicit tenant indexes: 28`. The actual count after Tenant V3 is **29** (including `ix_business_application_application_id`).

---

## 10. DDL OMISSIONS

1. **Store/Channel Same-Business Integrity Check:** The DDL lacks a composite foreign key or CHECK constraint on `store_channel` to enforce that a store and a channel belong to the same business.
2. **Audit Log Outcome Column:** The DDL table `audit_log` lacks an `outcome` column as required by `04-api-final.md`.
3. **Business FK on Warehouse, POS, Website:** If the business-level hierarchy in `03-database-final.md` and `04-api-final.md` is intended, the DDL lacks `business_id` columns and foreign keys on `warehouse`, `pos`, and `website`.

---

## 11. AMBIGUITIES REQUIRING HUMAN DECISION

1. **Warehouse, POS, and Website Hierarchy & API Routing:**
   - *Option 1 (Align with DDL and QA doc):* Confirm that Warehouse, POS, and Website are children of `Store` (`store_id uuid NOT NULL`). Correct `03-database-final.md` and `04-api-final.md` hierarchy trees and change endpoints from `/businesses/{businessId}/...` to `/stores/{storeId}/...`.
   - *Option 2 (Align with DB/API docs):* Alter the DDL and QA doc to add `business_id uuid NOT NULL` to `warehouse`, `pos`, and `website`, making them direct children of `Business`.
2. **Enforcement Mechanism for Store/Channel Same-Business Integrity:**
   - *Option 1 (Database Enforcement):* Add `business_id` to `store_channel` with composite foreign keys referencing `store(business_id, store_id)` and `channel(business_id, channel_id)`.
   - *Option 2 (Application Enforcement):* Reconcile documentation to state that Store/Channel integrity is an application-tier service responsibility, removing claims of database-enforced integrity.
3. **Reconciliation of ADR Approval Status:**
   - Formalize whether ADR-004 is **Accepted** (unblocking `/api/v1/applications` and business enablement endpoints in `04-api-final.md` and updating `00-project-context.md`, `01-prd.md`, and `02-system-architecture-final.md`) or **Open** (reverting `ADR-004-application-model.md`).
   - Align ADR-002 and ADR-005 statuses in `02-system-architecture-final.md` with their accepted ADR documents.
4. **Resolution of `user_account.password_hash`:**
   - Remove `password_hash` from DDL if Keycloak/external OIDC is exclusively authoritative as stated in `09-security-final.md`, or update security documentation if a local fallback/break-glass hash is intended.

---

## 12. FINAL VERDICT

The project documentation and the PostgreSQL DDL / Flyway migrations are **not internally consistent**. Direct contradictions exist regarding entity hierarchy parentage (Warehouse, POS, and Website), claimed versus implemented Store/Channel integrity constraints, audit log schema fields, password persistence policy, and ADR approval states across documents.
