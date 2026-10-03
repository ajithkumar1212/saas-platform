# Security Architecture

## 1. Document status

**Status: FINALIZED — Phase 1 security baseline**

This document defines the Phase 1 security architecture and control baseline. It is derived from the original `09-security.md` and reconciled with the finalized Phase 1 architecture, database, API, UI, QA, and DevOps documents.

Security controls described as requirements below are implementation expectations for Phase 1. Provider selection, compliance obligations, retention periods, recovery objectives, and other explicitly open decisions remain subject to owner approval.

---

## 2. Security objectives

The Phase 1 security objectives are:

1. Protect tenant and business data isolation.
2. Enforce least-privilege access at every applicable hierarchy scope.
3. Protect identity, authentication material, tax identifiers, contact data, and audit data.
4. Prevent unauthorized cross-tenant and cross-business access.
5. Ensure tenant context cannot be selected or forged by client input.
6. Protect the tenant-provisioning workflow.
7. Maintain trustworthy audit evidence for security-sensitive operations.
8. Protect secrets and credentials throughout development, deployment, and runtime.
9. Apply secure input, output, session, API, database, browser, and dependency controls.
10. Make security verification part of the Phase 1 release gate.

---

## 3. Security architecture boundary

The platform has two major security areas:

```text
www.xyz.com
    |
    | Public functions only
    v
app.xyz.com
    |
    +-------------------------------+
    |                               |
    v                               v
Control Plane                  Application Plane
Identity / Access              Business workspaces
Tenant / hierarchy             Enabled applications
Application access             Store / business operations
Audit / platform controls
    |
    v
PostgreSQL
public + one schema per tenant
```

The tenant boundary is a generated PostgreSQL schema.

Applications enabled for a Business remain inside the tenant's isolation boundary. A separate database schema must not be created per application.

---

## 4. Identity and authentication

### 4.1 Identity-provider model

Use a standards-based OIDC identity provider as the Phase 1 architecture recommendation.

The exact provider and hosted-versus-self-managed deployment model remain open decisions and must be approved separately.

The application must not implement an independent authentication protocol merely to avoid using the identity provider.

### 4.2 Authentication validation

For authenticated requests, validate at minimum:

- issuer;
- audience;
- signature;
- token expiry;
- issued-at validity where applicable;
- required/approved claims;
- authenticated subject;
- token type and intended usage where applicable.

Authentication establishes the principal. It does not by itself establish business authorization.

### 4.3 Credential handling

Where credentials are managed by the identity provider:

- do not persist identity-provider passwords in application tables;
- do not expose credentials through application logs;
- do not place secrets in source control;
- do not return credentials in API responses;
- keep MFA secrets under the approved identity/security boundary.

The current database model contains a nullable `user_account.password_hash`. Because ADR-003 is still Proposed and the identity provider is not selected, the field remains in the Phase 1 schema but is not an active identity-provider password store. It must not be populated with or used for an external identity-provider credential.

---

### 4.4 Application password storage rule

`user_account.password_hash` is a reserved nullable field in the canonical Phase 1 schema. It must never contain an identity-provider password and must not be populated or used while an external OIDC provider is authoritative. Any future local-credential use requires an explicit approved authentication decision under ADR-003 and a corresponding implementation/migration plan.

## 5. Session and browser security

The authenticated web application must protect sessions using the selected identity-provider/session model.

Required controls include:

- secure cookie configuration where cookies are used;
- appropriate `HttpOnly` and `Secure` attributes;
- appropriate `SameSite` policy;
- protection against session fixation;
- safe logout/token invalidation behavior according to the identity architecture;
- CSRF protection for browser state-changing requests where applicable;
- strict origin/CORS policy;
- security headers;
- protection against clickjacking;
- safe handling of redirects;
- no sensitive tokens in URLs.

UI route guards are convenience controls only. Backend authorization remains mandatory.

---

## 6. Tenant isolation

Tenant isolation is a primary security control.

### 6.1 Trusted tenant resolution

The application shall resolve tenant context from trusted authenticated identity and server-side tenant registry state.

The application must never accept:

- PostgreSQL schema names;
- tenant schema identifiers;
- arbitrary tenant context;
- privileged tenant IDs

from URL, query parameter, request body, or untrusted headers as authoritative security context.

### 6.2 Schema selection

The schema-selection process must:

1. establish the authenticated principal;
2. resolve the tenant identity;
3. load trusted tenant registry state;
4. verify tenant status/provisioning state;
5. resolve the generated schema;
6. establish tenant context transaction-locally;
7. execute tenant data access;
8. safely reset connection/session state before connection reuse.

Connection-pool reset behavior must be tested because schema context leakage can become a cross-tenant data exposure.

### 6.3 Cross-tenant authorization

Every protected operation must be evaluated against the authenticated tenant.

The system must deny:

- access to another tenant's UUIDs;
- access through manipulated business IDs;
- access through manipulated hierarchy IDs;
- access through stale authorization;
- access through forged tenant headers;
- access through schema-name manipulation.

Non-disclosing 404 behavior should be used where appropriate so that unauthorized resources are not revealed through error responses.

---

## 7. Hierarchy authorization

The approved hierarchy is:

```text
Tenant
  |
  v
Organization
  |
  v
Company
  |
  v
Business
```

The authorization model additionally supports business-scoped operational resources such as Store, Brand, Channel, Warehouse, POS, Website, and related configuration.

For each protected request, the server must verify:

1. authenticated user;
2. tenant membership;
3. applicable role;
4. required permission;
5. applicable organization/hierarchy scope;
6. target-resource ownership;
7. Business access where required;
8. application/capability access where required.

Do not rely on a single role check when the resource also requires hierarchy or business ownership validation.

---

## 8. Role and permission model

The security model is:

```text
User
  |
  v
Tenant
  |
  v
Roles
  |
  v
Permissions
  |
  v
Business / hierarchy access scope
```

Authorization shall be deny-by-default.

Permissions should represent explicit actions rather than broad implicit trust.

Access changes must be auditable.

The `user_access_scope`/access model must be evaluated server-side. Scope IDs are polymorphic references and therefore require application-level existence and ownership checks.

---

## 9. Business application security

The Phase 1 application catalog contains:

- `E_COMMERCE`
- `DISTRIBUTION`
- `WAREHOUSE`

Inventory is a shared capability rather than a selectable application.

Business Application enablement must verify:

- the Business belongs to the authenticated tenant;
- the requested application exists in the approved catalog;
- the caller has permission to enable/disable it;
- duplicate enablement is rejected or handled according to the approved contract;
- application lifecycle/status is respected;
- application access is limited to the Business for which it is enabled.

Application enablement must never grant broader tenant privileges automatically.

---

## 10. Tenant provisioning security

Tenant provisioning is security-sensitive because it creates a new isolation boundary.

The workflow is:

```text
Registration
    |
    v
tenant_registry
    |
    v
Generate schema name
    |
    v
CREATE SCHEMA
    |
    v
Tenant migrations
    |
    v
Tenant row
    |
    v
Tenant administrator
    |
    v
Verify
    |
    v
PROVISIONED
```

Required controls:

- server-generated tenant IDs/schema names;
- strict validation of tenant codes;
- no arbitrary SQL/schema identifiers from user input;
- least-privilege database credentials;
- provisioning idempotency;
- transaction/error handling appropriate to DDL boundaries;
- migration verification before readiness;
- failure-state recording;
- retry protection;
- audit of administrative provisioning actions;
- no exposure of database/schema internals to the customer.

A failed or partially provisioned tenant must not be represented as successfully provisioned.

Provisioning should be threat-modelled separately because it combines identity, database DDL, migrations, tenant creation, and administrator bootstrap.

---

## 11. Administrator and bootstrap security

The first successfully registered user becomes the initial Tenant Administrator according to the business requirement.

The bootstrap flow must ensure:

- administrator creation occurs only for the intended tenant;
- administrator privileges cannot be assigned across tenants;
- duplicate bootstrap cannot create an unintended second privileged bootstrap path;
- bootstrap state is auditable;
- provisioning failure cannot leave an untracked privileged account;
- subsequent privilege changes require normal authorization.

The exact bootstrap and recovery procedure remains an open security decision and must be approved before production.

---

## 12. API security

All non-public operations require authentication and authorization.

API controls include:

- server-side input validation;
- request size limits;
- strict content-type handling;
- parameterized SQL;
- safe ORM/JPA query construction;
- output encoding where rendered;
- safe error responses;
- consistent RFC 9457 problem responses;
- correlation IDs;
- rate limiting where appropriate;
- idempotency for retryable creation/provisioning operations;
- optimistic concurrency where supported;
- authorization before resource disclosure;
- no internal schema names in API responses.

API errors must not disclose:

- tenant schema names;
- SQL statements;
- database credentials;
- stack traces;
- tokens;
- internal infrastructure identifiers;
- unrelated tenant information.

---

## 13. Input validation and injection prevention

Validate all externally controlled input on the server.

Controls include:

- allowlists for enumerated/reference values;
- length and format validation;
- UUID validation;
- date/time validation;
- numeric bounds;
- request-body size limits;
- pagination limits;
- filter/sort allowlists;
- safe path parameter handling;
- parameterized SQL.

Never concatenate user-controlled values into:

- SQL;
- schema names;
- table names;
- shell commands;
- filesystem paths;
- dynamic authorization expressions.

The application must not use request-supplied schema identifiers to construct database queries.

---

## 14. Browser and frontend security

The React application is not a security boundary.

The frontend must:

- avoid storing sensitive credentials unnecessarily;
- avoid putting access tokens in URLs;
- avoid rendering untrusted HTML without appropriate sanitization;
- protect state-changing operations against CSRF where applicable;
- use safe redirect handling;
- avoid exposing tenant/schema internals;
- enforce secure logout behavior;
- present only authorized navigation where practical.

Backend enforcement remains authoritative even when the UI hides unauthorized options.

---

## 15. Data protection

### 15.1 Data in transit

Use TLS for application and administrative traffic.

### 15.2 Data at rest

Use encryption at rest through approved infrastructure.

The exact infrastructure/key-management design remains an open decision.

### 15.3 Sensitive data

Particular care is required for:

- authentication data;
- passwords/password hashes where applicable;
- MFA material;
- tax identifiers;
- contact information;
- audit payloads;
- verification tokens;
- secrets;
- database credentials.

Only authorized application paths should access sensitive fields.

### 15.4 Data minimization

Do not return or log full sensitive values when a masked/partial value is sufficient.

Examples include:

```text
Tax identifier: ********1234
Mobile number: ******1234
```

Exact masking rules should be defined by the relevant UI/API contract.

---

## 16. Secrets management

Secrets must be stored in an approved secret-management mechanism.

Do not store secrets in:

- source code;
- Git history;
- committed `.env` files;
- container images;
- frontend bundles;
- ordinary configuration files;
- database records unless explicitly required and protected.

Required operational controls:

- least-privilege secret access;
- rotation capability;
- environment separation;
- access auditing;
- no secret values in logs;
- secret scanning in CI.

The exact secret-manager technology remains an infrastructure decision.

---

## 17. Database security

Database access shall follow least privilege.

Application credentials must not receive unnecessary administrative privileges.

Security controls include:

- separate credentials by environment;
- restricted database network access;
- no public database exposure unless explicitly approved;
- schema-level isolation;
- controlled migration credentials;
- restricted DDL permissions;
- parameterized queries;
- connection-pool hygiene;
- audit of privileged database operations where applicable.

Migration execution must be separated from ordinary application permissions where the deployment architecture permits.

---

## 18. Audit security

The audit model records security-sensitive actions with sufficient context to establish:

- actor;
- tenant;
- action;
- target;
- timestamp;
- correlation/request identifier;
- relevant hierarchy context;
- redacted change summary.

Audit records must not become a secondary source of sensitive-data leakage.

Do not automatically store unrestricted request/response bodies in audit records.

Ordinary application paths must not be able to modify or delete audit evidence.

Audit tamper protection remains an open decision and requires explicit production approval.

---

## 19. History and audit separation

Business/history tables, domain audit logs, and security/operational telemetry have different purposes. `audit_log` records domain changes; authentication failures, authorization denials, token rejection, tenant-resolution failures, rate limiting and provisioning failures belong in security/operational telemetry.

History records explain business or administrative state changes.

Audit records provide security/accountability evidence.

Examples of history include:

- hierarchy changes;
- user access changes;
- organization changes;
- user-account state changes.

Security-sensitive actions should be represented in the audit model even when a related history record also exists.

---

## 20. Logging and telemetry security

Logs must be structured and must exclude secrets and unnecessary sensitive data.

Never log:

- passwords;
- access tokens;
- refresh tokens;
- MFA secrets;
- database credentials;
- private keys;
- raw tax identifiers;
- full payment credentials;
- arbitrary request bodies containing sensitive information.

Telemetry should support detection of:

- repeated authentication failures;
- authorization denials;
- unusual provisioning failures;
- migration failures;
- excessive rate-limit events;
- audit-write failures;
- unexpected cross-tenant access attempts.

Correlation IDs should be propagated without becoming a substitute for authorization.

---

## 21. Security headers and transport controls

The web/API deployment should apply appropriate:

- Content-Security-Policy;
- HSTS;
- X-Content-Type-Options;
- frame-ancestors/clickjacking protection;
- Referrer-Policy;
- secure cookie settings where applicable;
- CORS restrictions.

Exact header values should be defined during implementation/testing against the actual hosting and frontend architecture rather than invented here.

---

## 22. Rate limiting and abuse controls

Rate limiting should protect security-sensitive and resource-intensive operations, including where applicable:

- signup;
- authentication;
- verification;
- password/recovery flows;
- provisioning;
- administrative mutations;
- high-cost list/search operations.

Rate-limit policies must account for legitimate tenant activity and avoid using a shared global limit that unintentionally creates cross-tenant interference.

Exact thresholds remain an operational/security decision.

---

## 23. Dependency and supply-chain security

CI should include:

- dependency vulnerability scanning;
- secret scanning;
- static analysis;
- container/image scanning where containers are used;
- license review where required by project policy;
- dependency update management.

Build artifacts should be immutable and promoted through environments rather than rebuilt differently for production.

The application must not silently introduce unapproved third-party authentication or security libraries.

---

## 24. Security testing

Security testing is a release requirement.

### Authentication tests

Verify:

- invalid token rejection;
- expired token rejection;
- invalid issuer rejection;
- invalid audience rejection;
- invalid signature rejection;
- missing required claims;
- logout/session behavior according to the chosen identity model.

### Authorization tests

Verify:

- denied-by-default behavior;
- role restrictions;
- permission restrictions;
- hierarchy-scope restrictions;
- Business restrictions;
- application enablement restrictions.

### Tenant-isolation tests

Verify that Tenant A cannot access Tenant B through:

- guessed UUIDs;
- manipulated headers;
- manipulated path/query/body identifiers;
- forged tenant context;
- stale sessions;
- schema-name manipulation;
- authorization-scope manipulation.

### Provisioning tests

Verify:

- unauthorized provisioning;
- duplicate provisioning attempts;
- malformed tenant identifiers;
- migration failure;
- partial provisioning;
- retry behavior;
- false `PROVISIONED` prevention.

### Application security tests

Verify:

- SQL injection resistance;
- XSS resistance;
- CSRF behavior where applicable;
- unsafe redirect resistance;
- request-size limits;
- rate limiting;
- sensitive error disclosure;
- secret leakage;
- security-header behavior.

---

## 25. Threat-model priorities

The following areas require explicit threat modelling before production:

| Area | Primary security concern |
|---|---|
| Tenant resolution | Cross-tenant data exposure |
| Schema selection | Tenant-context/schema leakage |
| Tenant provisioning | Unauthorized tenant/DDL creation |
| Administrator bootstrap | Privilege escalation |
| Identity linking | Account/tenant association errors |
| Access-scope changes | Privilege escalation |
| Business application enablement | Unauthorized capability access |
| Support access | Unapproved privileged access |
| Audit | Tampering or sensitive-data leakage |
| Secrets | Credential compromise |
| Database migrations | Excessive database privileges |
| Browser session | Token/session compromise |

---

## 26. Security incident handling

A production security incident process must define:

1. detection;
2. triage;
3. containment;
4. evidence preservation;
5. tenant-impact assessment;
6. remediation;
7. recovery;
8. communication;
9. post-incident review.

The exact incident-response process, notification obligations, severity model, contacts, and legal requirements remain open and require owner/legal approval.

---

## 27. Data retention, deletion and legal holds

Security-related retention must align with the approved deletion policy.

The current architecture calls for a hybrid deletion model:

- archive/soft-delete where business history requires it;
- physical deletion only where explicitly permitted;
- protect audit evidence from ordinary deletion paths.

Retention periods and legal holds must not be invented by implementation teams. They require owner/legal approval based on applicable obligations and business requirements.

---

## 28. Security responsibilities by layer

| Layer | Primary responsibility |
|---|---|
| Public website | Public-content security, transport security, abuse protection |
| Identity provider | Authentication, credential/MFA handling according to selected model |
| React application | Safe presentation, secure session integration, UX-level access filtering |
| Backend API | Authoritative authentication validation, authorization, validation, tenant isolation |
| Tenant context | Trusted tenant resolution and schema selection |
| PostgreSQL | Isolation boundary, constraints, least-privilege database access |
| Migration runner | Controlled DDL/migration execution |
| Audit | Security/accountability evidence |
| CI/CD | Security scanning and artifact integrity |
| Infrastructure | Network, encryption, secrets, runtime hardening |
| Operations | Monitoring, alerting, incident response, backup/restore |

---

## 29. Security release gates

Phase 1 must not be considered production-ready until applicable gates pass:

### Gate S1 — Identity

- [ ] Authentication integration is verified.
- [ ] Token validation is verified.
- [ ] Session behavior is verified.
- [ ] MFA decision is approved where required.

### Gate S2 — Authorization

- [ ] Role/permission enforcement passes.
- [ ] Hierarchy-scope enforcement passes.
- [ ] Business access enforcement passes.
- [ ] Deny-by-default behavior passes.

### Gate S3 — Tenant isolation

- [ ] Cross-tenant access tests pass.
- [ ] Tenant context cannot be forged.
- [ ] Schema resolution is server-controlled.
- [ ] Connection-pool tenant context reset is verified.

### Gate S4 — Data protection

- [ ] TLS is configured.
- [ ] Encryption at rest is configured through approved infrastructure.
- [ ] Sensitive values are appropriately masked.
- [ ] Secrets are externally managed.

### Gate S5 — Application security

- [ ] Input validation is verified.
- [ ] Injection controls are verified.
- [ ] Browser protections are verified.
- [ ] Rate limits are defined where required.
- [ ] Security headers are verified.
- [ ] Dependency/secret scanning is active.

### Gate S6 — Audit and operations

- [ ] Security-sensitive actions are audited.
- [ ] Audit data is protected.
- [ ] Security telemetry is available.
- [ ] Incident-response procedure exists.
- [ ] Backup/restore controls are operational.

---

## 30. Current security status

| Control area | Current status |
|---|---|
| Security architecture baseline | Defined |
| Tenant/schema isolation model | Defined |
| Trusted tenant resolution | Defined |
| Role/permission model | Defined |
| Hierarchy authorization | Defined |
| Provisioning security | Defined |
| API security baseline | Defined |
| Data-protection baseline | Defined |
| Secrets-management requirement | Defined |
| Audit-security requirement | Defined |
| Security-test requirements | Defined |
| Direct-SQL tenant isolation validation | Passed |
| Flyway security/migration replay validation | Deferred |
| Full application security test execution | Not yet evidenced as passed |
| Production identity/MFA decision | Open |
| Support-access workflow | Open |
| Data residency | Open |
| Retention/legal holds | Open |
| Incident-response procedure | Open |
| Audit tamper protection | Open |
| Encryption/key ownership | Open |

The distinction between **security architecture defined**, **implemented**, **tested**, and **passed** must be maintained.

---

## 31. Open security decisions

The following remain explicitly open and must not be silently decided by implementation:

1. Identity provider.
2. Hosted versus self-managed identity deployment.
3. MFA policy.
4. Administrator/bootstrap recovery process.
5. Support-access model and approval workflow.
6. Data residency requirements.
7. Security assurance/compliance baseline.
8. Security incident-response ownership and notification process.
9. Audit tamper-protection mechanism.
10. Encryption/key ownership and key-management architecture.
11. Security retention periods and legal-hold process.
12. Exact rate-limit thresholds.
13. Production security alert thresholds.

These decisions should be recorded in the appropriate ADR/operations documentation before production release.

---

## 32. Security change control

Any change affecting the following requires security impact assessment:

- tenant isolation;
- schema resolution;
- authentication;
- authorization;
- roles/permissions;
- access scopes;
- tenant provisioning;
- administrator bootstrap;
- application enablement;
- tax/sensitive data;
- audit/history;
- API contracts;
- browser/session behavior;
- secrets;
- database privileges;
- deployment architecture.

A security-sensitive implementation change must not silently redefine the security contract.

---

## 33. Finalization

`09-security.md` is finalized as the Phase 1 security architecture baseline.

It preserves the original security document's core decisions and open items while expanding the implementation, verification, tenant-isolation, provisioning, API, browser, audit, secrets, and operational controls needed to make those requirements actionable.

`10-adr/` and all documents after `09-security.md` are intentionally not reviewed, modified, or finalized in this step.

**Final status: `09-security.md` — FINALIZED.**
