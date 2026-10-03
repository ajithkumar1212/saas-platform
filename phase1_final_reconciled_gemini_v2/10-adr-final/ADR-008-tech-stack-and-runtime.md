# ADR-008: Technology Stack, Framework, and Runtime Baseline

- **Status:** Accepted for Phase 1
- **Type:** Architecture decision
- **Scope:** Backend implementation platform, runtime, data access, package layout, migration runner, and testing governance

## Context

The platform requires a robust, type-safe, enterprise-grade backend for the modular monolith architecture ([ADR-001](file:///D:/saas/phase1_final_reconciled_gemini_v2/10-adr-final/ADR-001-modular-monolith.md)) that natively supports:
- PostgreSQL schema-per-tenant isolation ([ADR-002](file:///D:/saas/phase1_final_reconciled_gemini_v2/10-adr-final/ADR-002-schema-per-tenant.md));
- Automated tenant provisioning via programmatic Flyway migrations ([ADR-005](file:///D:/saas/phase1_final_reconciled_gemini_v2/10-adr-final/ADR-005-database-strategy.md), [ADR-006](file:///D:/saas/phase1_final_reconciled_gemini_v2/10-adr-final/ADR-006-tenant-provisioning-and-migration-runner.md));
- Standards-based OIDC/OAuth2 authentication ([ADR-003](file:///D:/saas/phase1_final_reconciled_gemini_v2/10-adr-final/ADR-003-authentication.md));
- Clean domain separation preventing cross-module database pollution and facilitating future service extraction;
- High-concurrency database connection pooling and resilient transactional boundaries.

## Decision

The Phase 1 backend technology stack and runtime baseline is finalized as follows:

### 1. Language & Runtime
- **Language:** Java 21 LTS
- **Runtime Distribution:** Eclipse Temurin OpenJDK 21 (LTS)
- **Language Features:** Records, Pattern Matching, Sealed Classes, Virtual Threads (Project Loom) for high-throughput multi-tenant I/O.

### 2. Build & Packaging
- **Build Tool:** Apache Maven 3.9+
- **Packaging:** Executable Spring Boot JAR (`.jar`), packaged into a lightweight, secure container image (`eclipse-temurin:21-jre-alpine` or distroless).

### 3. Core Framework & Compatible Version Management
- **Framework:** **Spring Boot 3.5.x** (Spring Framework 6.x)
- Spring Boot 3.5.x centrally controls and manages curated compatible versions of:
  - Spring Framework
  - Spring Security
  - Hibernate ORM
  - Jackson (JSON serialization)
  - Micrometer (Observability & Metrics)
  - Embedded Tomcat (Servlet Container)
  - Flyway Database Migration Integration

### 4. Modular Monolith Architecture & Package Layout
The backend strictly adheres to domain boundaries rather than flat technical layers (`controller/`, `service/`, `repository/`, `entity/`). Domains must never be mixed together into a generic bucket.

**Approved Package Structure:**
```text
com.xyz.platform
│
├── controlplane
│   ├── tenant              # Tenant onboarding, status lifecycle
│   ├── application         # Application catalog management
│   └── provisioning        # Schema generation, admin seeding saga
│
├── identity                # User accounts, OIDC subject mapping, credentials
├── organization            # Organization, legal details, addresses, contacts
├── company                 # Company, legal details, addresses, contacts
├── business                # Business entity, business applications, tax registrations
├── store                   # Store entity, store locations, branding, themes
├── brand                   # Brand management & store associations
├── channel                 # Channel management & store associations
├── website                 # Website & website domain operational resources
├── warehouse               # Warehouse & warehouse location resources
├── pos                     # POS operational resources
├── tenancy                 # Multi-tenancy routing, connection provider, tenant context
├── security                # Security filters, access scopes, role permissions
├── audit                   # Domain audit log and entity change history
└── shared                  # Base entities, exception models, pagination DTOs, utilities
```

Each package encapsulates its internal controllers, services, repositories, DTOs, and entities. Cross-domain interactions must occur solely via formal application services or domain events.

### 5. Persistence Responsibilities: JPA vs. JdbcTemplate Separation
A strict separation of persistence concerns is established:

```text
JPA / Hibernate (Spring Data JPA)
    ↓
Tenant Business Data (schema: tenant_<generated>)

JdbcTemplate (Spring JDBC)
    ↓
Control-Plane & Provisioning Infrastructure (schema: public / DDL)
```

**Explicit Responsibility Split:**
- **Spring Data JPA / Hibernate:**
  - Manages tenant domain entities: `Organization`, `Company`, `Business`, `Store`, `Brand`, `Channel`, `Website`, `Warehouse`, `POS`, `store_location`, `store_branding`, `store_theme`, `tax_registration`, `user_account`, `role`, `audit_log`, etc.
- **JdbcTemplate:**
  - Manages control-plane and infrastructure DDL: `public.tenant_registry`, `CREATE SCHEMA`, schema existence checks, schema metadata introspection, provisioning status transitions, and dynamic migration runner invocation.
- **Security Rule:** Arbitrary business services are strictly forbidden from executing `CREATE SCHEMA` or issuing `SET search_path`. Schema-level operations are exclusively restricted to the `tenancy` and `controlplane.provisioning` infrastructure.

### 6. Multi-Tenancy Execution Pipeline
Casually issuing `SET search_path` from arbitrary application code is prohibited. Multi-tenancy is centrally encapsulated behind:
1. `TenantContext` (ThreadLocal request store)
2. `TenantIdentifierResolver` (`CurrentTenantIdentifierResolver`)
3. `MultiTenantConnectionProvider` (schema-switching connection wrapper)

**Request Lifecycle:**
```text
HTTP Request
    ↓
JWT Authentication Filter
    ↓
Resolve trusted tenant schema from tenant_registry (Cached server-side lookup)
    ↓
TenantContext.setTenantId(tenantId)
    ↓
TenantIdentifierResolver.resolveCurrentTenantIdentifier()
    ↓
Hibernate Session / EntityManager
    ↓
MultiTenantConnectionProvider.getConnection(tenantId)
    ↓
Connection.createStatement().execute("SET search_path TO " + trustedSchema + ", public")
    ↓
Execute Domain Repository Queries
    ↓
MultiTenantConnectionProvider.releaseConnection(tenantId, connection)
    ↓
Reset search_path / release to HikariCP
    ↓
TenantContext.clear()
```

**Non-Negotiable Isolation Rule:** The physical schema name MUST be resolved server-side from the trusted `tenant_registry` or verified JWT claim. Client requests must **never** be allowed to supply arbitrary schema names.

### 7. Flyway Migration Strategy Separation
Dynamic tenant provisioning requires separating migrations into two distinct lifecycles:

```text
CONTROL PLANE (Public Schema)
Application Startup
        ↓
Flyway Migration Runner
        ↓
Apply control/ V1 → V2 → V3 to public schema
```

```text
TENANT PROVISIONING (Tenant Schemas)
Tenant Signup Request
        ↓
Insert tenant_registry (status: PROVISIONING)
        ↓
JdbcTemplate: CREATE SCHEMA tenant_<generated>
        ↓
Programmatic Flyway Instance:
  - DataSource: HikariCP
  - Target Schema: tenant_<generated>
  - Migration Location: classpath:db/migration/tenant
        ↓
Apply tenant/ V1 → V2 → V3
        ↓
Seed Initial Tenant Administrator
        ↓
Verify Schema Completeness (40 domain tables)
        ↓
Update tenant_registry (status: PROVISIONED)
```

**Guardrail:** Tenant migrations are **never** blindly executed across all tenant schemas on application startup. Tenant schema migrations occur solely during on-demand provisioning or via dedicated, controlled tenant migration maintenance runners.

### 8. Testing & Build Quality Tooling Baseline
The implementation baseline establishes explicit quality and testing tooling:

| Category | Tool | Scope & Enforcement |
|---|---|---|
| **Unit Testing** | **JUnit 5 (Jupiter)** + **Mockito** + **AssertJ** | Isolated domain service and business rule logic testing with fluent assertions. |
| **Integration Testing** | **Spring Boot Test** + **MockMvc** | Web slice and end-to-end controller contract testing. |
| **Real Database Testing** | **Testcontainers PostgreSQL** | **Mandatory.** All multi-tenant schema routing, dynamic `SET search_path`, and Flyway migrations are tested against a real PostgreSQL 16 container. In-memory H2 substitutes are strictly prohibited. |
| **API Contract Validation** | **REST Assured** / **MockMvc** + **SpringDoc** | Asserts compliance with OpenAPI 3.0 specs and RFC 7807 `ProblemDetail` responses. |
| **Architecture Governance** | **ArchUnit** | Programmatically asserts package boundaries (e.g. `com.xyz.platform.store` cannot directly access `com.xyz.platform.pos` repositories; only `tenancy` can touch connection routing). |
| **Code Coverage** | **JaCoCo** | Minimum 85% branch coverage required on core domain logic, security checks, and tenant isolation layers. |
| **Code Formatting & Style** | **Spotless** / **Checkstyle** | Enforces consistent formatting and import order during `mvn clean verify`. |

## Consequences

### Positive
- **Architectural Longevity:** Strict domain packaging ensures that if high-scale demands require extracting `warehouse` or `store` into standalone services, boundaries are already cleanly decoupled.
- **Fail-Safe Tenant Isolation:** Encapsulating schema switching inside the Hibernate connection provider completely eliminates cross-tenant data leaks and casual SQL injection via `search_path`.
- **Zero In-Memory DB Drift:** Using Testcontainers ensures tests execute against the exact PostgreSQL engine, catching schema issues, FK constraints, and DDL quirks prior to deployment.
- **Deterministic Provisioning:** Two-phase Flyway execution keeps application startup fast and makes tenant provisioning idempotent and retry-safe.

### Negative / Trade-Offs
- Higher test suite execution time due to spinning up PostgreSQL testcontainers (mitigated via reusable testcontainers).
- Strict package encapsulation requires discipline when designing cross-domain service interfaces.
