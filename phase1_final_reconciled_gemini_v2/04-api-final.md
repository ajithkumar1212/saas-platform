# Phase 1 API Contract

## Status and authority

**Contract status:** Frozen for the supported Phase 1 HTTP boundary (v1.0).

This document defines the API boundary and contract conventions. It does not itself approve database changes or close unresolved ADRs. Items explicitly marked **Open** or **Deferred** are not frozen implementation commitments.

The API contract is reconciled against the current project documentation and the canonical Phase 1 SQL package. The canonical SQL package establishes the current database structures, including `public.tenant_registry`, `public.application`, the 39-table tenant V1 baseline, `business_application`, reference data, constraints, and tenant-isolation rules.

Field-level request/response schemas, validation limits, and lifecycle details are frozen only where supported by the available project sources. Where the sources do not define them, this document keeps the item **Open** rather than inventing a contract.

## Contract-wide conventions

- JSON REST resources are rooted at `/api/v1`.
- Resource identifiers use UUIDs where the canonical data model defines UUID keys.
- Instants use UTC ISO-8601 representations.
- Protected operations use bearer access tokens through the approved identity boundary. The current API contract adopts **OIDC/JWT with Keycloak as the requested implementation basis**, while ADR-003 remains **Proposed; identity provider not selected**. This API document does not silently change that ADR status.
- Validate token signature, issuer, audience, expiry, and required claims once the provider-specific contract is approved.
- JWT subject identity establishes the authenticated principal. Application-side records and authorization policy determine tenant, role, permission, hierarchy, and business access.
- Never trust request-supplied tenant IDs, business scope, or PostgreSQL schema names as authorization evidence.
- The server resolves tenant context from trusted principal membership and `public.tenant_registry`, then enforces Tenant → Organization → Company → Business ownership and `user_access_scope`.
- A client-provided resource identifier selects the requested resource; it does not grant authorization.
- Tenant schemas are selected only from trusted server-side registry state. No endpoint accepts a PostgreSQL schema name.
- Errors use RFC 9457 `application/problem+json`, with a stable application error code and correlation identifier. Validation errors may include field-level details.
- Do not expose schema names, cross-tenant existence, SQL details, credentials, tokens, or unnecessary sensitive tax data.
- A resource that exists but is outside the caller's authorized scope should normally return a non-disclosing `404`.
- All input is validated server-side and database constraints remain authoritative for persistence integrity.
- Collection endpoints use endpoint-specific filter/sort allowlists and bounded page sizes. Exact parameter names, defaults, maxima, and resource-specific filters remain **Open** until payloads and API-owner decisions are frozen.
- Retryable create/provisioning operations use an `Idempotency-Key`. Exact retention, key format, and replay/conflict behavior remain **Open**.
- Mutable resources use optimistic concurrency where a canonical version/updated-at basis exists. Exact `ETag`/`If-Match` or equivalent representation remains **Open**.
- Sensitive hierarchy, membership/access, provisioning, and application-access changes are audited. Audit records contain actor, action, target, time, tenant, correlation, and an appropriately redacted change summary according to the approved domain-audit model. Security and operational outcomes are recorded in the appropriate security/operational telemetry.

## Resource model and hierarchy

```text
Tenant
└── Organization
    └── Company
        └── Business
            ├── Brand
            ├── Channel
            └── Store
                ├── Store Location
                ├── Warehouse
                │   └── Warehouse Location
                ├── POS
                └── Website
                    └── Website Domain
```

The API keeps Tenant, Organization, Company, and Business as distinct parent/ownership concepts.

Store, Brand, and Channel are distinct concepts.

Warehouse, POS, and Website are distinct operational resources represented in the Phase 1 database model.

The API also treats User, Role, Permission, and User Access Scope as distinct authorization concepts.

The verified Phase 1 access-scope types are:

```text
ORGANIZATION
COMPANY
BUSINESS
BRAND
STORE
CHANNEL
```

A scope target is never trusted solely because its ID was supplied. The server verifies existence, tenant ownership, hierarchy relationship, and allowed scope type.

## Tenant provisioning boundary

Tenant provisioning is backend-controlled.

A client may request onboarding through the approved onboarding operation, but cannot:

- choose a PostgreSQL schema name;
- create a schema;
- execute migrations;
- select a provisioning state;
- mark provisioning complete;
- bypass tenant registry checks.

The server owns:

```text
Tenant identity allocation
        ↓
public.tenant_registry
        ↓
trusted schema-name generation
        ↓
schema creation
        ↓
tenant migrations
        ↓
tenant bootstrap
        ↓
PROVISIONED state
```

The canonical database package establishes `public.tenant_registry` as the control-plane registry.

The exact public provisioning state machine, synchronous/asynchronous job model, bootstrap administrator workflow, failure/retry semantics, and response payload remain **Open**.

The API never returns the physical PostgreSQL schema name.

## Application Catalog and Business Application

The canonical Phase 1 SQL package now contains:

```text
public.application
```

with exactly these application codes:

```text
E_COMMERCE
DISTRIBUTION
WAREHOUSE
```

and each tenant schema contains:

```text
business_application
```

with an application reference to `public.application`.

ADR-004 is **Accepted for Phase 1**. The Application Catalog and Business Application resource model is therefore unblocked. Authorization and lifecycle behavior must follow the accepted ADR.

```text
GET  /api/v1/applications
GET  /api/v1/businesses/{businessId}/applications
POST /api/v1/businesses/{businessId}/applications
```

The unresolved questions include:

- catalog ownership;
- application metadata contract;
- enablement cardinality;
- enablement lifecycle;
- dependencies;
- entitlement/subscription rules;
- duplicate enablement behavior;
- disable behavior;
- authorization required to enable/disable;
- whether an application can be globally unavailable while existing Business enablements remain;
- API/UI behavior when an application is disabled.

Inventory remains a **shared capability**, not a selectable Phase 1 application in the current product model.

## Store/Channel same-business validation

`store_channel` intentionally contains `store_id` and `channel_id` without a shared `business_id`. Before creating or updating the association, the application service must resolve both resources in the authenticated tenant and verify that `store.business_id = channel.business_id`. A cross-business association must be rejected as a business-rule validation error.

## Operation catalog

The routes below freeze the supported Phase 1 resource boundary and intent. Exact request/response properties, required fields, validation limits, and lifecycle transitions are frozen only where the project sources define them.

| Method | Route | Contract status / intent |
|---|---|---|
| POST | `/api/v1/tenants` | Request tenant onboarding; provisioning is server-controlled. Exact authentication/bootstrap eligibility and sync vs async response remain **Open**. |
| GET | `/api/v1/tenants/{tenantId}` | Read an authorized tenant summary. |
| POST | `/api/v1/organizations` | Create an Organization within trusted tenant context. |
| GET | `/api/v1/organizations` | List authorized Organizations in the effective tenant scope. |
| GET | `/api/v1/organizations/{organizationId}` | Read an authorized Organization. |
| POST | `/api/v1/companies` | Create a Company under an authorized Organization; parent ownership is checked server-side. |
| GET | `/api/v1/companies` | List authorized Companies. |
| GET | `/api/v1/companies/{companyId}` | Read an authorized Company. |
| POST | `/api/v1/businesses` | Create a Business under an authorized Company; parent ownership is checked server-side. |
| GET | `/api/v1/businesses` | List authorized Businesses. |
| GET | `/api/v1/businesses/{businessId}` | Read an authorized Business and permitted summary. |
| GET | `/api/v1/businesses/{businessId}/stores` | List authorized Stores for a Business. |
| GET | `/api/v1/businesses/{businessId}/brands` | List authorized Brands for a Business. |
| GET | `/api/v1/businesses/{businessId}/channels` | List authorized Channels for a Business. |
| GET | `/api/v1/stores/{storeId}/warehouses` | List authorized Warehouses owned by a Store. |
| GET | `/api/v1/stores/{storeId}/pos` | List authorized POS resources owned by a Store. |
| GET | `/api/v1/stores/{storeId}/websites` | List authorized Websites owned by a Store. |
| GET | `/api/v1/reference-data/{typeCode}` | Read approved reference values. Ordinary business clients do not receive reference-data write operations. |
| GET | `/api/v1/businesses/{businessId}/tax-registrations` | Read authorized tax registrations according to the canonical tax-registration model. Detailed response fields and write workflow remain **Open**. |
| GET | `/api/v1/applications` | Read the platform-owned Phase 1 application catalog. |
| GET | `/api/v1/businesses/{businessId}/applications` | List application enablements for an authorized Business. |
| POST | `/api/v1/businesses/{businessId}/applications` | Enable an approved Phase 1 application for an authorized Business. |

### Routes intentionally not frozen

The following write boundaries are not frozen by this document because the current project sources do not define sufficient lifecycle/payload detail:

- Store create/update/delete
- Brand create/update/delete
- Channel create/update/delete
- Warehouse create/update/delete
- POS create/update/delete
- Website create/update/delete
- User administration
- Role administration
- Permission administration
- User access-scope administration
- Tax-registration create/update/deactivate
- Audit query/export APIs
- Reference-data administration

These routes must be added through reviewed OpenAPI changes rather than inferred from table existence alone.

## Identity and authorization

### Authentication

The API contract adopts the project's requested Keycloak OIDC/JWT basis.

ADR-003 itself remains:

```text
Proposed; identity provider not selected
```

Therefore the API freezes the **security boundary and JWT/OIDC contract shape**, not a vendor-specific final ADR.

When the provider decision is approved, the implementation must validate:

- token signature;
- issuer;
- audience;
- expiry;
- required claims;
- stable subject identity.

Signup/login credentials are not implemented as a competing password API in this contract.

### Authorization

The application maps the authenticated subject to platform User/account records and evaluates:

```text
Tenant membership
      ↓
Role
      ↓
Permission
      ↓
Hierarchy
      ↓
User Access Scope
```

A valid JWT does not authorize arbitrary tenant/business access.

Cross-tenant and out-of-scope access must be rejected without revealing another tenant's resource existence.

UI route guards are not security controls.

The exact provider claim names, token scopes, invitation policy, bootstrap-admin mapping, MFA policy, and revocation timing remain **Open**.

## Reference data

The canonical tenant V2 migration seeds six reference types:

```text
STATUS
CHANNEL_TYPE
BUSINESS_TYPE
TAX_REGISTRATION_TYPE
ORGANIZATION_TYPE
COMPANY_TYPE
```

The canonical validation baseline contains:

```text
6 reference types
42 reference values
```

The read API is:

```text
GET /api/v1/reference-data/{typeCode}
```

Reference data is read-only for ordinary business clients.

Exact reference-value payloads, localization behavior, filtering, ordering, and administration privileges are implementation/API-owner details that must be frozen in OpenAPI before implementation.

## Tax registration

The canonical database package contains:

```text
tax_registration
business_tax_registration
```

and these are part of the final 40-table tenant model.

The API must treat tax registration as legal/business data and enforce its approved owning scope and access permissions.

The currently frozen read boundary is:

```text
GET /api/v1/businesses/{businessId}/tax-registrations
```

The following remain **Open** until the product/API contract is explicitly finalized:

- complete response schema;
- registration-type semantics;
- multiple-registration behavior;
- primary registration behavior;
- create/update/deactivate operations;
- exposure/redaction of registration identifiers;
- effective/valid date workflow.

The API must not infer a one-registration-per-business or one-registration-per-company rule merely from the table structure.

## Audit

Sensitive API operations must generate audit information consistent with the canonical audit model.

At minimum, an audit event should capture, where supported:

```text
actor
action
target
time
tenant
correlation identifier
redacted change summary
```

Audit records are not exposed through general resource reads.

Audit query/export endpoints remain **Open** until permission, filtering, retention, and export requirements are approved.

## Error contract

All API errors use:

```http
Content-Type: application/problem+json
```

The problem response must support:

- HTTP status;
- stable application error code;
- human-readable detail suitable for the caller;
- correlation identifier;
- optional field-level validation details.

Do not expose:

- PostgreSQL schema names;
- SQL statements;
- stack traces;
- database constraint internals;
- access-control evidence;
- tokens or credentials;
- unnecessary sensitive tax data.

For an unauthorized resource discovery attempt, prefer a non-disclosing `404` where appropriate.

The complete error-code catalog remains **Open** and must be defined in OpenAPI.

## Pagination and filtering

Collection APIs are paginated.

The server must:

- enforce a bounded page size;
- provide deterministic ordering;
- allow only resource-approved sort fields;
- allow only resource-approved filter fields;
- reject unsupported filter/sort parameters;
- never pass raw client values into SQL or schema selection.

The following remain **Open**:

- parameter names;
- default page size;
- maximum page size;
- offset vs cursor pagination;
- total-count behavior;
- resource-specific filter set;
- resource-specific sort set.

These values must be frozen before OpenAPI sign-off.

## Idempotency

Tenant provisioning and other retryable create operations must support:

```http
Idempotency-Key: <client-generated-key>
```

The server must prevent duplicate resource creation when a request is retried after an uncertain network outcome.

The exact:

- key format;
- retention duration;
- scope;
- replay response;
- incompatible-key reuse response

remain **Open**.

## Concurrency

Mutable resources should use optimistic concurrency when the canonical model provides a reliable version/updated-at basis.

The API contract permits either:

```http
ETag
If-Match
```

or an equivalent version representation.

The exact wire representation and conflict status mapping remain **Open**.

Stale updates must not silently overwrite a newer approved state.

## Security requirements

The API implementation must:

- use TLS for protected traffic;
- validate the approved OIDC/JWT contract;
- derive tenant/schema context only from trusted server-side state;
- prevent tenant-context leakage through pooled database connections;
- validate resource ownership and access scope on every request;
- parameterize database access;
- never accept SQL or PostgreSQL schema identifiers from callers;
- enforce request-size and input-validation controls;
- protect cookie-based browser sessions against CSRF if such sessions are introduced;
- encode/sanitize outputs according to the frontend/browser context;
- redact credentials, tokens, secrets, and unnecessary sensitive tax identifiers from logs;
- apply approved rate limiting and security headers;
- audit security-sensitive mutations and denials where supported by the approved audit model.

Exact thresholds, rate limits, retention, encryption/key controls, and assurance standards remain **Open** in the security/operations documentation.

## OpenAPI mapping

`04-api.md` is the normative human-readable API contract.

The OpenAPI specification is its machine-readable projection and must:

1. use `/api/v1`;
2. define bearer JWT security for protected operations;
3. define every implemented path and method;
4. define path/query parameters;
5. define request/response schemas;
6. define status codes and RFC 9457 problem responses;
7. define stable application error codes;
8. define pagination/filter/sort rules;
9. define idempotency/concurrency headers where applicable;
10. document authorization requirements and tenant/business scope;
11. include the accepted Phase 1 Application Catalog / Business Application operations defined by ADR-004;
12. be reconciled against the canonical database contract before implementation sign-off.

The previously referenced `docs/openapi.yaml` is not present in the uploaded project ZIP and is therefore not treated as an authoritative API contract.

OpenAPI version, exact file location, generation/validation tooling, and backward-compatibility policy remain **Open**.

## Phase 1 acceptance criteria

The Phase 1 API contract is ready for implementation when:

1. Protected operations use the approved OIDC/JWT identity boundary and validate token integrity and required claims.
2. Tenant → Organization → Company → Business ownership is enforced server-side.
3. User Role, Permission, and Access Scope are evaluated for every protected operation.
4. Cross-tenant and out-of-scope access cannot disclose resource existence.
5. Tenant onboarding and schema provisioning remain server-controlled.
6. No endpoint accepts or returns a physical PostgreSQL schema name.
7. Organization, Company, Business, Store, Brand, Channel, Warehouse, POS, Website, User, Role, Permission, and Access Scope remain distinct concepts.
8. Reference-data reads follow the canonical six-type/42-value baseline.
9. Tax-registration access follows the canonical database model and approved permissions.
10. Errors use RFC 9457 `application/problem+json` with stable codes and correlation identifiers.
11. Collection APIs enforce bounded pagination and allowlisted filtering/sorting.
12. Retryable provisioning/creates support idempotency.
13. Supported mutable resources use the approved optimistic-concurrency mechanism.
14. Sensitive hierarchy/access/provisioning/application-access changes are auditable.
15. OpenAPI matches the implemented contract.
16. Application Catalog / Business Application enablement follows accepted ADR-004 and server-side authorization.
17. Flyway replay validation remains **Deferred** until a clean-database replay is actually executed.

## Explicitly open or deferred

- Exact request/response schemas and field-level validation not defined by the source documents.
- Exact signup/invitation/bootstrap workflow.
- Provider-specific token claims and final identity-provider approval under ADR-003.
- Application Catalog / Business Application business semantics under ADR-004.
- Full write workflows for Store, Brand, Channel, Warehouse, POS, Website, User, Role, Permission, Access Scope, and Tax Registration.
- Provisioning job/polling model and exact provisioning state machine.
- Pagination/filter/sort parameter names and limits.
- Idempotency retention and replay semantics.
- Optimistic-concurrency wire representation.
- Audit query/export contract.
- OpenAPI tooling and compatibility policy.
- Production rate limits, SLOs, and security assurance thresholds.
- Flyway replay validation.

ADR-001 Modular Monolith remains **Proposed**. This API contract does not approve or reject that architecture decision.
