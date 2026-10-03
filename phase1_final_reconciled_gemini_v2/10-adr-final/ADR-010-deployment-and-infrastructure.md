# ADR-010: Deployment Topology, Containerization, and Infrastructure Baseline

- **Status:** Accepted for Phase 1
- **Type:** Architecture decision
- **Scope:** Container packaging, orchestration, runtime topology, database hosting, and CI/CD pipelines

## Context

The platform requires an immutable, automated, repeatable deployment and operational baseline supporting:
- The Spring Boot 3.3 modular monolith backend ([ADR-001](file:///D:/saas/phase1_final_reconciled_gemini_v2/10-adr-final/ADR-001-modular-monolith.md), [ADR-008](file:///D:/saas/phase1_final_reconciled_gemini_v2/10-adr-final/ADR-008-tech-stack-and-runtime.md));
- The Next.js 14+ frontend application ([ADR-009](file:///D:/saas/phase1_final_reconciled_gemini_v2/10-adr-final/ADR-009-frontend-ui-stack.md));
- PostgreSQL schema-per-tenant isolation ([ADR-002](file:///D:/saas/phase1_final_reconciled_gemini_v2/10-adr-final/ADR-002-schema-per-tenant.md));
- Automated tenant schema provisioning via programmatic Flyway migrations ([ADR-005](file:///D:/saas/phase1_final_reconciled_gemini_v2/10-adr-final/ADR-005-database-strategy.md), [ADR-006](file:///D:/saas/phase1_final_reconciled_gemini_v2/10-adr-final/ADR-006-tenant-provisioning-and-migration-runner.md));
- The OIDC authentication boundary via Keycloak ([ADR-003](file:///D:/saas/phase1_final_reconciled_gemini_v2/10-adr-final/ADR-003-authentication.md));
- The DevOps quality gates and operational principles defined in [`07-devops.md`](file:///D:/saas/phase1_final_reconciled_gemini_v2/07-devops.md).

## Decision

The Phase 1 deployment, containerization, and infrastructure baseline is finalized as follows:

### 1. Containerization & OCI Images
All runtime components are packaged into minimal, secure, immutable OCI/Docker container images:
- **Backend Service:** Multi-stage Dockerfile using `maven:3.9-eclipse-temurin-21` for building and `eclipse-temurin:21-jre-alpine` (or distroless) for runtime execution.
- **Frontend UI:** Multi-stage Dockerfile using `node:20-alpine` with Next.js standalone output mode (`output: "standalone"`).
- **Security:** Containers run as unprivileged non-root users with read-only root filesystems where applicable.

### 2. Local & Development Environment
- **Tooling:** Docker Compose (`docker-compose.yml`)
- **Services Orchestrated:**
  1. `db`: PostgreSQL 16 (official image, port 5432) initialized with canonical schema.
  2. `idp`: Keycloak (official Quay image, port 8080) pre-configured with realm and client definitions.
  3. `backend`: Spring Boot application service (port 8081).
  4. `frontend`: Next.js UI service (port 3000).
- Enables 1-command local development reproduction (`docker compose up`).

### 3. Production Deployment & Orchestration
- **Runtime Orchestration:** Kubernetes (or managed container runtimes: AWS ECS Fargate / GCP Cloud Run / Azure Container Apps).
- **Ingress & Reverse Proxy:**
  - **NGINX Ingress Controller** (or Cloud Application Load Balancer) handling TLS termination, HTTP/2, and domain routing:
    - `www.xyz.com` $\rightarrow$ Next.js Frontend (public pages).
    - `app.xyz.com` $\rightarrow$ Next.js Frontend (authenticated SaaS workspace).
    - `app.xyz.com/api/v1/*` $\rightarrow$ Spring Boot Backend.
    - `auth.xyz.com/*` $\rightarrow$ Keycloak / OIDC IdP.
- **Scaling:** Horizontal Pod Autoscaler (HPA) scaling backend pods based on CPU/memory and active connection metrics.

### 4. Database Infrastructure
- **Hosting:** Managed PostgreSQL 16+ (AWS RDS PostgreSQL, GCP Cloud SQL, or Azure Database for PostgreSQL).
- **High Availability & Durability:**
  - Multi-AZ deployment with synchronous stand-by replication.
  - Automated continuous WAL archiving enabling Point-In-Time Recovery (PITR) to meet the RPO/RTO objectives defined in `07-devops.md`.
  - Automated daily snapshots retained per the data retention policy ([ADR-007](file:///D:/saas/phase1_final_reconciled_gemini_v2/10-adr-final/ADR-007-deletion-policy.md)).

### 5. CI/CD Automation Pipeline
- **Automation Engine:** GitHub Actions (or GitLab CI)
- **Pipeline Stages:**
  1. **Lint & Static Analysis:** SpotBugs / Checkstyle (backend) + ESLint / Prettier (frontend).
  2. **Test Execution:** `mvn test` (unit tests), `npm run test` (UI tests).
  3. **Integration & Migration Verification:** Direct-SQL and programmatic Flyway validation against ephemeral test PostgreSQL container.
  4. **Security Scans:** Trivy / Grype (vulnerability scan), GitLeaks (secret scan).
  5. **Build & Image Publishing:** Docker build multi-stage images, tag with git commit SHA and semver, publish to private container registry (ECR / Artifact Registry / GHCR).
  6. **Deployment:** GitOps / Helm deployment to staging and production environments.

## Consequences

### Positive
- **Complete Environment Parity:** Docker Compose provides identical local development topology matching production Kubernetes architecture.
- **Operational Simplicity:** Standardized container contracts allow deployment across any cloud provider (AWS, GCP, Azure, or on-premise) without vendor lock-in.
- **Enterprise High Availability:** Managed PostgreSQL handles failover, backups, and physical durability transparently.
- **Strict Quality Control:** Every commit is gated through compile, test, security scan, and container packaging.

### Negative / Trade-Offs
- Requires managing container definitions, registry credentials, and ingress TLS certificates.
- Production Kubernetes clusters require ongoing cluster maintenance and observability monitoring.
