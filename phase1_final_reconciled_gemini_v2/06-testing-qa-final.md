# Testing and Quality Assurance

## 1. Document status

**Status:** FINALIZED for the Phase 1 testing and QA baseline.

This document defines the Phase 1 quality strategy, test scope, minimum scenarios, evidence requirements, and release gates.

It distinguishes **planned/required tests** from **tests already evidenced as executed**. A test is not described as passed unless the project contains evidence of its execution and result.

The current project database evidence includes a successful direct-SQL Phase 1 validation. Flyway replay validation remains deferred and must not be represented as passed.

---

## 2. Quality goals

Phase 1 quality assurance must protect the core platform foundations:

- Tenant isolation.
- Tenant → Organization → Company → Business hierarchy.
- Store / Brand / Channel / Warehouse / POS / Website relationships.
- Role, Permission, and Access Scope enforcement.
- Authentication boundary.
- Legal and tax-data handling.
- Audit/history behavior.
- PostgreSQL constraints and migration contract.
- Automated tenant provisioning.
- API/UI consistency.
- Safe failure behavior.
- Regression protection for the finalized Phase 1 database and API contracts.

Application Catalog / Business Application behavior requires separate treatment because its business semantics remain subject to the applicable ADR/product decision.

---

## 3. Testing principles

### 3.1 Evidence over assumption

The QA document distinguishes:

```text
Required test
     ≠
Executed test
     ≠
Passed test
```

A test becomes a project result only when execution evidence exists.

### 3.2 Database contract is authoritative for persistence integrity

Application validation is not a substitute for database constraints.

Tests must verify both:

```text
Application/API validation
        +
PostgreSQL constraints
```

### 3.3 Security is tested as an end-to-end property

Tenant isolation and access control must be tested through the real request/context path where possible.

Tests must not rely only on UI hiding.

### 3.4 Negative cases are first-class tests

Phase 1 QA must deliberately test:

- wrong tenant;
- wrong hierarchy parent;
- insufficient role;
- insufficient scope;
- invalid lifecycle transition;
- duplicate resource;
- invalid reference data;
- invalid dates;
- malformed identifiers;
- unauthorized mutation;
- retry/duplicate submission;
- protected-resource discovery.

---

## 4. Test layers

| Layer | Phase 1 coverage |
|---|---|
| Unit | Domain rules, validation, hierarchy rules, lifecycle transitions, authorization decisions, scope evaluation |
| Repository / database integration | PostgreSQL constraints, foreign keys, unique constraints, CHECK constraints, transaction behavior, timestamps, deletion restrictions |
| Migration | Tenant schema creation and migration scripts; clean-schema replay and supported upgrade paths when Flyway execution is available |
| API integration | Request validation, authorization, tenant resolution, business scope, error contract, pagination/filter behavior, idempotency and concurrency where implemented |
| Security | Cross-tenant IDOR, forged tenant context, scope escalation, SQL/schema injection, sensitive-data leakage, authentication boundary |
| Provisioning | Tenant registration, registry state, schema creation, migration execution, bootstrap, retry/failure handling |
| End-to-end | Sign-up/login through tenant setup, organization/company/business setup, Business Management, and authorized Business workspace access |
| UI/accessibility | Keyboard navigation, validation errors, loading/empty/error/no-access states, responsive core flows, accessible hierarchy navigation |
| Resilience / performance | Provisioning retry behavior, connection-pool isolation, agreed workload/SLO tests once those targets are approved |
| Regression | Re-run protected Phase 1 suites after changes to API, database, authorization, provisioning, or shared UI components |

### 4.1 Approved Testing & Quality Tooling Baseline (ADR-008)

The implementation verification baseline is locked to the following tooling:

- **Unit Testing:** **JUnit 5 (Jupiter)** + **Mockito** + **AssertJ** for isolated domain logic and rule validation.
- **Real PostgreSQL Testing (Mandatory):** **Testcontainers PostgreSQL**. Tenant-schema isolation, dynamic `search_path` switching, and Flyway migration execution must be validated against real PostgreSQL instances in test containers. **In-memory H2 substitutes are strictly prohibited** to prevent database dialect and constraint drift.
- **Web & API Contract Testing:** **Spring Boot Test** + **MockMvc** / **REST Assured** verifying OpenAPI 3.0 contracts and RFC 7807 `ProblemDetail` structures.
- **Architecture & Modularity Governance:** **ArchUnit** programmatically asserts modular monolith boundaries:
  - Prevents circular dependencies between domain packages;
  - Ensures business packages (`com.xyz.platform.store`, etc.) do not execute schema DDL;
  - Restricts multi-tenant connection management exclusively to `com.xyz.platform.tenancy`.
- **Code Coverage & Quality Gates:** **JaCoCo** with an 85%+ branch coverage threshold on core business rules, security scope filters, and tenant-routing components.
- **Static Analysis & Formatting:** **Spotless** / **Checkstyle** enforced on `mvn clean verify`.

---

## 5. Phase 1 test scope

### 5.1 Tenant and provisioning

Test:

```text
Tenant registration
    ↓
Tenant registry
    ↓
Schema creation
    ↓
Tenant migration
    ↓
Tenant bootstrap
    ↓
Provisioned state
```

Required cases:

1. Valid tenant registration creates the expected provisioning workflow.
2. Tenant code uniqueness is enforced.
3. Tenant schema name is generated server-side.
4. Client input cannot choose an arbitrary PostgreSQL schema.
5. Provisioning failure leaves an observable failed/retryable state according to the approved provisioning workflow.
6. Retry does not create duplicate tenant state.
7. Tenant registry and tenant schema remain consistent.
8. Tenant bootstrap creates the expected initial administrator state when that workflow is implemented.
9. A provisioned tenant can access only its own tenant context.

The exact synchronous/asynchronous provisioning model remains dependent on the finalized implementation.

---

## 6. Tenant isolation tests

Tenant isolation is a release-critical test area.

### Minimum scenarios

For Tenant A and Tenant B:

1. Tenant A cannot read Tenant B entities by guessing UUIDs.
2. Tenant A cannot mutate Tenant B entities by guessing UUIDs.
3. Tenant A cannot delete Tenant B entities.
4. Tenant A cannot submit a request that changes the tenant context to Tenant B.
5. Tenant A cannot select Tenant B's PostgreSQL schema.
6. Tenant A cannot use query/filter parameters to retrieve Tenant B data.
7. Tenant A cannot access Tenant B through a nested child resource.
8. Tenant A cannot access Tenant B through alternate resource relationships.
9. No database foreign key crosses Tenant A and Tenant B schemas.
10. Connection reuse cannot retain the previous tenant's schema context.

Cross-tenant tests must be executed through the actual authorization/context path, not only by querying the database directly.

---

## 7. Hierarchy tests

The finalized hierarchy is:

```text
Tenant
└── Organization
    └── Company
        └── Business
            ├── Brand
            ├── Store
            │   ├── Store Location
            │   ├── Warehouse
            │   │   └── Warehouse Location
            │   ├── POS
            │   └── Website
            │       └── Website Domain
            └── Channel
```

Required tests:

- Organization belongs to the correct Tenant.
- Company belongs to the correct Organization.
- Business belongs to the correct Company.
- Brand belongs to the correct Business.
- Store belongs to the correct Business.
- Channel belongs to the correct Business.
- Store-channel relationships are rejected by the application service when Store and Channel belong to different Businesses.
- Store-brand relationships do not cross Businesses.
- Warehouse belongs to the expected Store/business hierarchy.
- Warehouse Location belongs to its Warehouse.
- Website belongs to the expected Store/business hierarchy.
- Website Domain belongs to its Website.
- POS belongs to the expected Store/business hierarchy.
- Invalid parent-child relationships are rejected.
- Cross-tenant parent-child relationships are rejected.
- Invalid hierarchy changes are audited where applicable.

Where the approved hierarchy rules prohibit cycles or invalid re-parenting, those rules must have explicit positive and negative tests.

---

## 8. Authorization and access-scope tests

The authorization model is:

```text
Authenticated User
        ↓
Tenant Membership
        ↓
Role
        ↓
Permission
        ↓
Access Scope
```

Phase 1 scope types are:

```text
ORGANIZATION
COMPANY
BUSINESS
BRAND
STORE
CHANNEL
```

Required tests include:

1. Authorized user can access resources within the granted scope.
2. User cannot access resources outside the granted scope.
3. Parent scope behavior follows the approved authorization rules.
4. Child-resource access does not bypass parent authorization.
5. Role without the required permission cannot execute the operation.
6. Permission without the required resource scope does not grant access.
7. Disabled/inactive access is rejected according to the approved lifecycle.
8. Changing a user's scope takes effect according to the approved authorization cache/session behavior.
9. Scope changes are auditable where required.
10. UI-hidden features remain protected by backend authorization.

---

## 9. Authentication tests

The project API contract uses an OIDC/JWT security boundary, while the authentication ADR remains subject to its documented decision status.

When the authentication implementation is tested, verify:

- valid token accepted;
- expired token rejected;
- malformed token rejected;
- invalid signature rejected;
- incorrect issuer rejected;
- incorrect audience rejected;
- missing required claims rejected;
- unauthenticated access rejected;
- authenticated identity maps to the correct application user/account;
- tenant context is not accepted from an untrusted client field;
- logout/session behavior follows the approved identity architecture.

Provider-specific claims and final identity-provider decisions must be tested against the approved authentication configuration once finalized.

---

## 10. API tests

The Phase 1 API test suite must follow the finalized API contract.

### Request validation

Test:

- missing required fields;
- invalid formats;
- invalid UUIDs;
- invalid enum/reference values;
- invalid parent IDs;
- duplicate identifiers;
- malformed query parameters;
- unsupported filter/sort parameters;
- invalid pagination parameters.

### Authorization

Every protected endpoint requires authorization tests for:

```text
Allowed
Denied by permission
Denied by scope
Denied by tenant
Denied by resource ownership
```

### Error contract

Verify that API failures use the approved `application/problem+json` structure and do not expose:

- SQL;
- stack traces;
- PostgreSQL schema names;
- credentials;
- tokens;
- unnecessary sensitive tax information;
- cross-tenant existence evidence.

### Pagination and filtering

Where implemented, test:

- default page size;
- maximum page size;
- deterministic ordering;
- valid filters;
- invalid filters;
- valid sorts;
- invalid sorts;
- empty results;
- page boundary behavior.

Exact parameter names and limits remain subject to the finalized API contract.

---

## 11. Idempotency and duplicate-submission tests

Retryable create/provisioning operations must be tested for duplicate prevention.

Minimum cases:

1. Same idempotency key + same request returns the original operation/result according to the approved contract.
2. Same idempotency key + incompatible request is rejected according to the approved contract.
3. Network retry does not create duplicate tenant/business/resource state.
4. Concurrent duplicate submissions do not create duplicate records where uniqueness is required.
5. Idempotency behavior is scoped correctly and does not leak data between tenants.

Exact key-retention and replay semantics remain open until finalized.

---

## 12. Concurrency tests

Where optimistic concurrency is implemented, test:

```text
Read version N
      ↓
User A updates
      ↓
Version N+1
      ↓
User B attempts stale update
      ↓
Conflict
```

Required behavior:

- stale writes are not silently accepted;
- concurrent updates do not corrupt relationships;
- conflict responses follow the API contract;
- retry/reload behavior is safe.

The exact `ETag`/`If-Match` or equivalent wire representation remains an API decision until finalized.

---

## 13. Database and repository tests

### 13.1 Schema integrity

The canonical Phase 1 tenant baseline is:

```text
39 tenant domain tables from V1
+ business_application
= 40 tenant domain tables
```

The control plane contains:

```text
public.tenant_registry
public.application
```

Database tests must verify:

- primary keys;
- foreign keys;
- unique constraints;
- CHECK constraints;
- required indexes;
- restrictive deletion behavior;
- reference-data integrity;
- cross-schema application reference;
- absence of cross-tenant foreign keys.

### 13.2 Tax registration

Test:

- valid tax registration creation;
- invalid date ranges;
- valid business-to-tax-registration association;
- invalid association;
- duplicate association;
- authorization to read/write legal tax information;
- sensitive-data logging/redaction behavior.

The database contract contains:

```text
tax_registration
business_tax_registration
```

and the API/UI must remain aligned with that model.

---

## 14. Reference-data tests

The canonical tenant V2 reference-data baseline contains:

```text
6 reference types
42 reference values
```

Reference types:

```text
STATUS
CHANNEL_TYPE
BUSINESS_TYPE
TAX_REGISTRATION_TYPE
ORGANIZATION_TYPE
COMPANY_TYPE
```

Required tests:

- all six canonical types exist;
- expected value counts exist;
- no duplicate `(reference_type_id, value_code)`;
- no orphan reference values;
- unsupported reference values are rejected;
- reference values are read-only to ordinary business clients;
- reference data cannot cross tenant boundaries where tenant-local data is involved.

---

## 15. Application Catalog / Business Application tests

The canonical database package contains:

```text
public.application
business_application
```

with the application catalog currently containing:

```text
E_COMMERCE
DISTRIBUTION
WAREHOUSE
```

ADR-004 is accepted for Phase 1; application catalog and Business Application tests must follow its approved model.

Therefore:

### Currently testable

- database existence;
- application seed integrity;
- foreign key from `business_application` to `public.application`;
- uniqueness of `(business_id, application_id)`;
- absence of orphan application references;
- cross-tenant isolation.

### Blocked/open

Application enable/disable behavior, entitlement rules, UI behavior, dependencies, lifecycle semantics, and detailed API workflows must not be treated as finalized tests until the application-model decision is approved.

---

## Audit semantic boundary

`audit_log` is a domain-change audit store. Security/operational events such as authentication failures, token rejection, authorization denial, tenant-resolution failures, rate limiting and provisioning failures belong in security/operational telemetry. The domain audit schema therefore does not require a generic `outcome` column.

## 16. Audit and history tests

Security-sensitive changes must produce the expected audit/history information.

Test:

- actor identity;
- target;
- action;
- timestamp;
- tenant context;
- correlation identifier where applicable;
- appropriate redaction;
- hierarchy-change history;
- access-change history;
- organization-change history;
- user-account history;
- audit log behavior.

Negative cases must verify that unauthorized operations do not generate misleading successful-change events.

---

## 17. UI and accessibility tests

The finalized UI baseline includes:

- Sign-up/login;
- onboarding;
- Organization setup;
- Company setup;
- Business Management;
- Create Business;
- Manage Business;
- Business workspace.

Test:

### Functional UI

- required fields;
- inline validation;
- preserved input after recoverable errors;
- duplicate-submit protection;
- correct Business context;
- correct navigation based on authorization;
- safe API error presentation;
- loading state;
- empty state;
- error state;
- no-access state.

### Accessibility

The UI source proposes WCAG 2.2 AA as a target.

Therefore the QA plan should test:

- keyboard-only navigation;
- visible focus;
- semantic labels;
- form error association/announcement;
- accessible dialogs;
- accessible hierarchy/breadcrumb navigation;
- sufficient contrast;
- responsive usability.

**WCAG 2.2 AA remains a proposed project target unless separately approved.**

---

## 18. End-to-end onboarding test

The core new-customer journey is:

```text
www.xyz.com
   ↓
Sign Up
   ↓
Tenant Registration
   ↓
Verification
   ↓
Tenant Creation
   ↓
Tenant Provisioning
   ↓
Tenant Administrator
   ↓
app.xyz.com
   ↓
Organization Setup
   ↓
Company Setup
   ↓
Business Management
   ↓
Create Business
   ↓
Application Configuration
   ↓
Manage Business
```

The E2E test must verify that the user reaches the correct Business workspace without losing tenant context.

The test must also verify failure recovery at each provisioning/setup boundary that is implemented.

---

## 19. Existing-user E2E test

```text
www.xyz.com
   ↓
Login
   ↓
Authentication
   ↓
Resolve Tenant
   ↓
app.xyz.com
   ↓
Tenant Dashboard
   ↓
Select Business
   ↓
Manage Business
```

Verify:

- correct tenant;
- correct available Businesses;
- correct role/scope restrictions;
- correct Business workspace;
- no cross-tenant resource visibility.

---

## 20. Security testing

Minimum security scenarios:

1. Cross-tenant IDOR.
2. Forged tenant context.
3. Scope escalation.
4. Permission escalation.
5. Cross-business access.
6. Cross-business Store/Brand/Channel association attempts.
7. SQL injection.
8. PostgreSQL schema-name injection.
9. Sensitive-data leakage in API errors.
10. Sensitive-data leakage in application logs.
11. Token/credential leakage.
12. Unauthorized audit/history access.
13. Manipulation of resource IDs in nested routes.
14. Replay/duplicate submission of provisioning operations.
15. Attempted access after authorization is revoked.

Security tests must be performed against the actual application boundary wherever possible.

---

## 21. Resilience and performance

The source QA baseline requires resilience/performance coverage but does not freeze numerical thresholds.

Test areas:

- provisioning retry;
- failed provisioning recovery;
- database connection-pool isolation;
- concurrent tenant activity;
- concurrent Business operations;
- API response behavior under agreed workload;
- resource exhaustion behavior;
- timeout behavior;
- dependency failure handling.

Performance targets, SLOs, availability targets, recovery objectives, and workload thresholds must come from approved project decisions rather than being invented in this document.

---

## 22. Database validation evidence

A direct-SQL Phase 1 validation was executed against the project validation database.

The recorded result was:

```text
PASS: ALL DIRECT-SQL PHASE 1 VALIDATIONS PASSED

public.application            3
public.tenant_registry        2
tenant_t001.domain_tables    40
tenant_t002.domain_tables    40
tenant_t001.reference_values 42
tenant_t002.reference_values 42
```

The validation also confirmed:

- required cross-schema `business_application → public.application` relationships;
- no cross-tenant foreign keys between `tenant_t001` and `tenant_t002`.

This is an **executed direct-SQL validation result**, not merely a planned test.

### Important limitation

The direct-SQL validator intentionally does not validate Flyway history.

The project currently has **no completed Flyway replay validation evidence**.

Therefore:

```text
Direct SQL validation       PASS
Flyway replay validation    DEFERRED
```

No QA report should claim that Flyway migration replay has passed until it is actually executed against a clean supported environment.

---

## 23. Test data and fixtures

Test fixtures must support at least:

```text
Tenant A
Tenant B

Organization A
Organization B

Company A
Company B

Business A
Business B

Users with:
- Tenant-level access
- Organization scope
- Company scope
- Business scope
- Brand scope
- Store scope
- Channel scope

Roles
Permissions
Reference data
Tax registrations
```

Cross-tenant fixtures must be deliberately related so that authorization and relationship tests can detect accidental leakage.

The existing direct-SQL validation used deterministic tenant fixtures including `tenant_t001` and `tenant_t002`.

Test fixtures must not contain production credentials or real sensitive personal/tax information.

---

## 24. Test environment strategy

The QA environment should support:

```text
Application
API
PostgreSQL
Identity provider
Tenant provisioning
Database migrations
UI
```

Database tests must use an isolated database/schema environment.

Tenant-isolation tests should contain at least two independent tenant schemas.

Production data must not be copied into test environments without an approved sanitization process.

---

## 25. CI/CD quality gates

Changes affecting Phase 1 should pass the applicable automated gates before merge/release.

Minimum gate categories:

```text
Compile/build
Unit tests
Repository/database integration tests
API tests
Security tests
Tenant-isolation tests
Static analysis
Dependency/security scanning
UI tests for affected flows
Database validation for DDL/migration changes
```

The exact CI tooling and thresholds belong to the DevOps/implementation documentation.

A change to:

```text
Database
API
Authorization
Tenant provisioning
Shared UI components
```

must trigger the relevant regression suite.

---

## 26. Defect classification

The project should classify defects by impact.

### Critical

Examples:

- cross-tenant data exposure;
- unauthorized privileged access;
- credential/secret exposure;
- tenant schema corruption;
- destructive data corruption.

### High

Examples:

- incorrect authorization scope;
- broken provisioning;
- incorrect hierarchy ownership;
- inability to create/access a Business;
- severe tax/legal-data integrity issue.

### Medium

Examples:

- non-critical API validation defect;
- incorrect UI state;
- incorrect filtering/sorting;
- recoverable workflow failure.

### Low

Examples:

- cosmetic issue;
- non-blocking copy/layout issue;
- minor usability inconsistency.

Final SLA/response times for these classifications are not defined by the supplied project sources and remain open.

---

## 27. Traceability

Phase 1 QA must maintain traceability across:

```text
Requirement
   ↓
PRD
   ↓
Architecture
   ↓
Database
   ↓
API
   ↓
UI
   ↓
Test Case
   ↓
Execution Evidence
   ↓
Defect / Result
```

At minimum, critical requirements must map to one or more tests.

Critical traceability areas:

- tenant isolation;
- provisioning;
- hierarchy;
- authorization;
- access scopes;
- tax/legal data;
- audit;
- database integrity;
- API security;
- onboarding;
- Business Management.

---

## 28. Release gates

A Phase 1 release requires review of:

1. Schema/migration compatibility.
2. Tenant-isolation tests.
3. Authorization/access-scope tests.
4. Authentication tests.
5. Provisioning tests.
6. Audit behavior.
7. API contract tests.
8. UI onboarding acceptance.
9. Security/dependency scans.
10. Database validation.
11. Backup/restore readiness where required by the deployment plan.
12. Regression results for affected components.

Performance, availability, recovery, browser-support, and accessibility thresholds must be based on approved project decisions.

A release must not be declared fully migration-validated when Flyway replay remains unexecuted.

---

## 29. Current QA status

| Area | Status |
|---|---|
| QA strategy | Finalized |
| Test-layer definition | Finalized |
| Tenant isolation scenarios | Finalized |
| Authorization/access-scope scenarios | Finalized |
| Hierarchy scenarios | Finalized |
| API test scope | Finalized |
| UI/accessibility scope | Finalized |
| Provisioning test scope | Finalized |
| Reference-data test scope | Finalized |
| Application Catalog test boundary | Open/Blocked where semantics are unresolved |
| Direct-SQL database validation | **Executed — PASS** |
| Cross-tenant FK validation | **Executed — PASS** |
| Flyway replay validation | **Deferred** |
| Performance thresholds | Open |
| SLO/availability targets | Open |
| Recovery objectives | Open |
| Final browser matrix | Open |
| Final accessibility compliance process | Open |

---

## 30. Explicit open and deferred items

The following are intentionally not invented or treated as finalized:

- Final Application Catalog / Business Application business semantics.
- Provider-specific authentication test contract pending final authentication decision.
- Exact provisioning job/state-machine test contract where implementation is not yet frozen.
- Exact API pagination/filter/sort thresholds.
- Exact idempotency retention/replay rules.
- Exact concurrency wire contract.
- Performance workload and SLO thresholds.
- Availability targets.
- RTO/RPO targets.
- Final browser/device support matrix.
- Final accessibility compliance/acceptance process.
- Final CI tooling/threshold configuration.
- Flyway replay execution and validation.

---

## 31. Final QA acceptance criteria

The Phase 1 QA baseline is complete when:

1. Critical tenant-isolation scenarios exist.
2. Authorization and access-scope scenarios exist.
3. Hierarchy integrity scenarios exist.
4. Provisioning scenarios exist.
5. API security and validation scenarios exist.
6. UI onboarding and accessibility scenarios exist.
7. Database integrity scenarios map to the canonical DDL.
8. Reference-data integrity is tested against the canonical baseline.
9. Tax/legal-data protection is tested.
10. Audit behavior is tested.
11. Application Catalog / Business Application tests cover the accepted ADR-004 model; provider-specific authentication tests remain gated by ADR-003.
12. Direct-SQL database validation remains recorded as **PASS** based on actual execution evidence.
13. Flyway replay remains explicitly **DEFERRED** until actual execution evidence exists.
14. Release gates distinguish planned tests from executed results.
15. No test result is represented as passed without execution evidence.

**Document status: FINALIZED.**
