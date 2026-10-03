# Phase 1 Product Requirements

**Status:** Finalized Phase 1 PRD baseline for the current documentation set.

**Authority:** This document defines the current product requirements baseline. It does not by itself approve architecture decisions or database changes. Where a statement is based on the canonical database rather than an authoritative business requirement, it is explicitly identified as a database-backed constraint. Individual ADR status remains authoritative for unresolved technical or business decisions.

## Purpose

Establish the onboarding, tenant boundary, hierarchy, access model, and business/store foundation for a multi-tenant, multi-business SaaS platform. The product journey begins on the public site and continues in the authenticated application.

The intended hierarchy is:

```text
Tenant
└── Organization
    └── Company
        └── Business
```

A Tenant is the technical isolation boundary. A Business is the operational scope at which enabled business applications may vary. The platform also provides shared control capabilities for identity, access, administration, configuration, and audit.

The current documentation snapshot does not contain the original authoritative business-requirement file or a `sources/` directory. Therefore, this PRD does not claim complete business-requirement traceability. Requirements whose source is the prior project discussion or the current database contract are identified accordingly rather than being presented as independently verified business requirements.

## Product model and flow

```text
www.xyz.com
     │
     ├── Sign Up
     └── Login
          │
          ▼
     app.xyz.com
          │
          ▼
       Tenant
          │
          ▼
     Organization
          │
          ▼
       Company
          │
          ▼
       Business
          │
          ├── application configuration
          └── manage Business / permitted workspace
```

The platform supports multiple Businesses under a Company. Different Businesses may use different combinations of business applications. The current canonical database package contains an Application Catalog with exactly three seeded application codes:

- `E_COMMERCE`
- `DISTRIBUTION`
- `WAREHOUSE`

`Inventory` is a shared domain capability, not a selectable Phase 1 application. Administration, identity/access, roles, permissions, subscriptions, billing, and audit are platform/control capabilities rather than selectable Business Applications in the current Phase 1 baseline.

The database representation of application enablement is present through `public.application` and tenant-level `business_application`. ADR-004 is **Accepted for Phase 1**, approving the platform-owned catalog and Business-level enablement model. Authorization, lifecycle and entitlement controls must be enforced by application services and must not implicitly grant privileges.

## Phase 1 scope

Phase 1 establishes the foundation required to onboard and administer tenants and businesses and to support the business/store foundation represented by the canonical Phase 1 schema.

### In scope

- Public entry points for sign-up and login leading to the authenticated application.
- Tenant establishment and tenant-scoped data isolation.
- Tenant → Organization → Company → Business hierarchy.
- Multiple Businesses under a Company.
- Business-level application configuration boundary.
- Store, Brand, and Channel as distinct business concepts and their approved relationships.
- Warehouse, Warehouse Location, POS, Website, Website Domain, and related store-foundation structures represented by the canonical schema.
- User accounts, roles, permissions, and scoped access represented by the canonical schema.
- Hierarchy, access, account, and audit history represented by the canonical schema.
- Tax/legal registration information represented by the canonical schema, subject to the documented ownership/cardinality rules.
- Reference data required by the Phase 1 database contract.
- Server-side authorization and tenant isolation controls.

### Out of scope for Phase 1

The following are not implied to be implemented merely because the broader product may eventually support them:

- Detailed product catalog operations.
- Cart and checkout workflows.
- Payment processing and payment-method orchestration.
- Installments and wallet management.
- Order management.
- Fulfillment and delivery operations.
- Returns and exchanges.
- Subscription billing implementation.
- Other future business applications or domain modules not included in the approved Phase 1 scope.

Future functionality must be introduced through an explicit product/implementation decision rather than inferred from the presence of a broader platform concept.

## Functional requirements

| ID | Requirement | Basis / status |
|---|---|---|
| FR-01 | A user can enter through the public site and reach the authenticated application through the supported sign-up or login flow. | Product flow established in project documentation; detailed signup fields and lifecycle require authoritative requirement confirmation. |
| FR-02 | The platform supports the Tenant → Organization → Company → Business hierarchy. | Current project/product baseline; represented by the canonical database model. |
| FR-03 | A Company can contain multiple Businesses. | Current product baseline and canonical hierarchy model. |
| FR-04 | A Business is the operational scope at which application usage may vary from another Business. | Accepted Phase 1 application model under ADR-004. |
| FR-05 | The platform provides shared control capabilities for tenant administration, identity/access, organization/company/business administration, application access, configuration, and audit. | Product/architecture intent; exact operational workflows remain subject to the detailed documents and ADR status. |
| FR-06 | Store, Brand, and Channel are modeled as distinct concepts and their approved relationships must be preserved. | Finalized database decision. |
| FR-07 | User access can be scoped to approved hierarchy/business entities and must be authorized server-side on every protected request. | Finalized database/security baseline. |
| FR-08 | Tax/legal registration information is captured at the scope represented by the approved database model and is protected as sensitive business/legal data. | Finalized database/security baseline; detailed business/legal requirements require authoritative requirement confirmation. |
| FR-09 | Material hierarchy, access, account, and security-sensitive changes are retained through the approved history/audit model. | Finalized database/security baseline. |
| FR-10 | Tenant isolation must prevent a user or request from accessing another tenant's data by manipulating identifiers, request context, or schema-selection inputs. | Finalized security/tenancy baseline. |
| FR-11 | The Phase 1 application catalog contains the three seeded application codes `E_COMMERCE`, `DISTRIBUTION`, and `WAREHOUSE` in the canonical database package. | Accepted Phase 1 application catalog under ADR-004. |
| FR-12 | Tenant-level Business Application enablement is represented by the canonical `business_application` relationship to the application catalog. | Accepted Phase 1 Business Application model; authorization/lifecycle behavior follows ADR-004. |

## Business and access boundaries

### Tenant

Tenant is the technical isolation boundary. Tenant data is stored in a tenant-specific PostgreSQL schema. A request must not be permitted to select an arbitrary tenant schema or bypass the authenticated tenant context.

### Organization, Company, and Business

The hierarchy provides progressively narrower business scope:

```text
Tenant
  └── Organization
       └── Company
            └── Business
```

A user's permissions and access scopes must be evaluated against this hierarchy. A valid identifier alone is not evidence of authorization.

### Store, Brand, and Channel

Store, Brand, and Channel are separate concepts. Their relationships must preserve the same-business integrity and other constraints defined by the canonical database contract.

### Applications and shared capabilities

Application availability is represented at Business scope. ADR-004 accepts the platform-owned catalog and tenant-level enablement model for Phase 1.

Inventory is treated as a shared capability rather than a selectable application in the current Phase 1 product model.

## Acceptance outcomes

A Phase 1 implementation is consistent with this PRD when all of the following are demonstrable:

1. The onboarding/application flow does not confuse Tenant, Organization, Company, or Business identity.
2. A Company can manage multiple Businesses within the tenant hierarchy.
3. Business scope is available as the boundary for application usage/configuration.
4. Store, Brand, Channel, tax/legal registration, user access scope, history, and audit are represented consistently with the canonical database contract.
5. Unauthorized cross-tenant and out-of-scope access is rejected server-side.
6. The Phase 1 application catalog contains exactly the three canonical application codes and Business Application relationships can be represented without violating tenant isolation or database constraints.
7. No later-phase commerce workflow is treated as implemented solely because the broader product roadmap contains that capability.
8. Requirements that depend on the missing authoritative business requirement remain explicitly traceable as requiring confirmation rather than being silently invented.

## Assumptions and constraints

### Assumptions

- The product remains a multi-tenant, multi-business SaaS platform.
- Tenant is the technical isolation boundary.
- The hierarchy is Tenant → Organization → Company → Business.
- A Company may contain multiple Businesses.
- Business applications share the tenant's data isolation boundary; there is no schema-per-application model.
- Inventory is a shared capability rather than a selectable Phase 1 application.
- The canonical Phase 1 SQL package is the current database reference for this documentation reconciliation.

### Constraints

- PostgreSQL schema-per-tenant isolation is the current database baseline.
- Tenant schema resolution must be controlled by trusted server-side tenant registry state.
- Cross-tenant foreign-key relationships are not permitted.
- No blanket cascading-delete behavior is permitted; the approved deletion policy governs lifecycle/archive versus physical deletion.
- Sensitive tax/legal and access/audit data must be protected according to the security baseline.
- Database changes require synchronized documentation and explicit approval; this PRD does not authorize schema changes.

## Risks

| Risk | Impact | Mitigation / treatment |
|---|---|---|
| Missing authoritative business requirement in the documentation snapshot | Product requirements may be incomplete or incorrectly attributed. | Obtain/reconcile the authoritative requirement before final implementation sign-off; do not invent missing fields or lifecycle rules. |
| Application Catalog / Business Application semantics remain open | API/UI/entitlement behavior could diverge from the database representation. | Finalize ADR-004 before implementation of application-selection behavior. |
| Identity-provider decision remains unresolved in ADR-003 | Authentication implementation could diverge from the intended contract. | Keep provider-specific implementation behind the approved security/API boundary until ADR-003 is finalized. |
| Flyway replay has not been executed | Runtime migration behavior remains unverified. | Treat Flyway replay as Deferred and execute it against a fresh validation database before production migration approval. |
| Detailed tax/legal requirements are not fully traceable to the missing requirement source | Incorrect business/legal behavior could be implemented. | Confirm legal/tax requirements and ownership/cardinality before implementation of related workflows. |

## Success criteria

Phase 1 is successful when:

1. The Tenant → Organization → Company → Business foundation can be established without ambiguity of scope.
2. Tenant isolation and hierarchy-scoped authorization are enforced server-side.
3. The store foundation and access/audit structures conform to the canonical Phase 1 database contract.
4. The three Phase 1 application catalog records and Business Application relationship are represented consistently with the canonical SQL package, while unresolved ADR-004 business semantics remain controlled.
5. The Phase 1 API/UI/security/test documentation can trace each implemented requirement to an agreed product or database baseline.
6. Direct-SQL database validation remains passing and Flyway replay is completed before production migration approval.

## Open product questions

The following remain explicitly open because the current documentation snapshot does not contain the authoritative business-requirement file or because the responsible ADR has not been finalized:

1. What are the exact signup fields, verification steps, required/optional fields, and lifecycle gates?
2. Is signup self-service, invitation-based, operator-created, or a combination?
3. Does one Tenant contain exactly one Organization or multiple Organizations? The current data model permits multiple Organizations; the product requirement should confirm the intended business rule.
4. What are the exact lifecycle states and transition rules for Tenant, Organization, Company, and Business?
5. What are the exact Application Catalog ownership and maintenance rules?
6. Can an application be enabled more than once per Business, or only once? The current database uniqueness constraint permits only one Business-to-Application relationship per application.
7. What are application dependencies, plan entitlements, feature flags, enable/disable rules, and deprovisioning behavior?
8. What exact business/legal rules govern tax-registration ownership, cardinality, effective dates, and changes?
9. What administrator/bootstrap and identity-linking rules apply to the first user and subsequent users?
10. Which detailed operational workflows belong in Phase 1 versus later implementation phases where the broader product roadmap is concerned?

## Traceability and implementation rule

This PRD must be read together with the finalized project context and the detailed architecture, database, API, UI, testing, DevOps, security, and ADR documents.

- A product requirement must not be inferred solely from an implementation detail.
- A database constraint may establish an implementation/data invariant without proving that the corresponding business policy has been approved.
- An ADR marked Proposed/Open remains unresolved even if a corresponding database structure exists in the canonical SQL package.
- A validation activity marked Deferred must not be described as passed.
- Any future change to Phase 1 scope must update this PRD and the affected detailed documents/ADRs together.
