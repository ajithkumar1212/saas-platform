# ADR-001: Modular Monolith for Initial Delivery

- **Status:** Accepted for Phase 1
- **Type:** Architecture decision
- **Scope:** Phase 1 deployment architecture

## Context

The platform has multiple business domains and shared tenant/control capabilities. Deploying each domain as an independent service immediately increases operational, deployment, observability, and data-consistency complexity before independent scaling needs are established.

The platform is intended to support E-Commerce, Distribution, Warehouse Management, and future business domains through a common tenant and control foundation.

## Decision

Start Phase 1 with one deployable modular monolith with explicit domain boundaries, internal application interfaces, ownership rules, and limited cross-module dependencies.

The backend package layout is organized strictly by domain rather than flat technical layers:

```text
com.xyz.platform
│
├── controlplane
│   ├── tenant
│   ├── application
│   └── provisioning
│
├── identity
├── organization
├── company
├── business
├── store
├── brand
├── channel
├── website
├── warehouse
├── pos
├── tenancy
├── security
├── audit
└── shared
```

Do not organize the backend primarily as `controller/`, `service/`, `repository/`, `entity/` with all domains mixed together. Domain boundaries are preserved for eventual extraction into microservices if future scale demands it.

ArchUnit rules programmatically enforce package isolation during automated builds:
- No module may directly access repositories or entities of another domain module.
- Cross-module operations must execute via published domain interfaces or application services.
- Only the `tenancy` and `controlplane.provisioning` modules are permitted to interact with schema DDL or multi-tenant connection routing.

## Consequences

### Positive

- Simpler initial deployment.
- Lower operational overhead.
- Easier transactional consistency within the initial application.
- Clear path for incremental modularization.
- Fewer distributed-system failure modes in Phase 1.

### Negative

- Independent scaling and deployment are unavailable until a module is extracted.
- Module boundaries must be actively enforced in code review and module tests.
- A modular monolith can become tightly coupled if ownership rules are ignored.

## Constraints

- No unrestricted cross-module database access.
- No circular module dependencies.
- Extraction must be based on an explicit decision rather than premature service decomposition.
- Security and tenant-isolation rules apply across all modules.

## Verification

Before implementation is considered complete:

- module ownership is documented;
- dependency direction is reviewed;
- cross-module access is tested;
- architecture tests prevent prohibited dependencies where practical;
- deployment remains one deployable unit unless a new ADR changes the decision.

## Related decisions

- ADR-002: Schema-per-Tenant Isolation
- ADR-003: Authentication and Identity Boundary
- ADR-004: Application Catalog and Business Application
- ADR-005: PostgreSQL, UUIDs, Auditing, and Migration Strategy
- ADR-006: Tenant Provisioning and Migration Runner
