# Final Phase 1 Baseline Status

**FINALIZED AFTER GEMINI AUDIT**

- Tenant isolation: schema-per-tenant
- Control plane: `public`
- Tenant domain tables: 40
- Application catalog: 3
- Reference types: 6
- Reference values: 42
- Warehouse/POS/Website parent: Store
- Store/Brand same-business: database-enforced
- Store/Channel same-business: application-service enforced
- ADR-001: Accepted for Phase 1 (Modular Monolith)
- ADR-003: Accepted for Phase 1 (Standards-based OIDC / Keycloak reference)
- ADR-004: Accepted for Phase 1 (Application Catalog & Business Application)
- ADR-008: Accepted for Phase 1 (Java 21 LTS, Spring Boot 3.5.x, Hibernate 6 schema multi-tenancy, programmatic Flyway 10.x, Testcontainers PG, ArchUnit)
- ADR-009: Accepted for Phase 1 (Next.js 14+ App Router, TypeScript, Tailwind CSS, shadcn/ui + Radix UI)
- ADR-010: Accepted for Phase 1 (Docker OCI, Docker Compose dev, Kubernetes/ECS, NGINX Ingress, Managed PostgreSQL 16)
- Build System: Apache Maven 3.9+ (Backend), Node.js / pnpm (Frontend)
- `password_hash`: nullable/dormant; never an external IdP password store
- `audit_log.outcome`: not part of the database contract
- Tenant explicit indexes after V3: 29
- Flyway replay: Deferred (to be executed programmatically via Spring Boot provisioning service)

The canonical SQL schema is unchanged by this reconciliation.
