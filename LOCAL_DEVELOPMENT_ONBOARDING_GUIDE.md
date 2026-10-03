# Multi-Tenant SaaS Platform — Local Development & Teammate Onboarding Guide

This document is the authoritative, step-by-step developer onboarding runbook for teammates cloning this repository. It covers both **First-Time Clean Setup (Day 1)** and **Daily Continuation (Day 2+)** with existing database data preserved.

---

## 1. System Prerequisites

Before starting, ensure your workstation has:

| Tool | Minimum Version | Verification Command |
|---|---|---|
| **Java JDK** | 21 LTS (Eclipse Temurin / OpenJDK) | `java -version` |
| **Apache Maven** | 3.9+ | `mvn -version` |
| **PostgreSQL** | 16+ (or Docker Desktop) | `psql -V` or `docker -v` |
| **Git** | 2.40+ | `git --version` |

---

## 2. SCENARIO A: First-Time Clean Setup (Day 1)

Follow these steps when you have freshly cloned the repository onto a new machine.

### Step 2.1: Clone the Repository
```bash
git clone <repository_url> saas
cd saas
```

---

### Step 2.2: Setup Local PostgreSQL Database

You can run PostgreSQL either **natively** on your machine or via **Docker**. Choose one:

#### Option 1: Native PostgreSQL (Installed on Host)
Open **PowerShell** or Command Prompt and run (enter your `postgres` master password when prompted):
```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -c "CREATE USER saas_user WITH PASSWORD 'saas_password' SUPERUSER;"
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -c "CREATE DATABASE saas_platform OWNER saas_user;"
```
*(If you want to use custom database credentials, see Step 2.3).*

#### Option 2: Docker Compose (Zero PostgreSQL Installation)
If you have Docker Desktop installed, run from the repository root:
```bash
cd deploy
docker compose up -d postgres
cd ..
```
*This starts PostgreSQL 16 on port `5432` with database `saas_platform`, user `saas_user`, and password `saas_password` automatically.*

---

### Step 2.3: Configure Database Credentials (Without Modifying Git)

The backend defaults to:
- **Host/Port:** `localhost:5432`
- **Database:** `saas_platform`
- **Username:** `saas_user` (or `saas_ajith`)
- **Password:** `saas_password`

If your local PostgreSQL uses a different user or password, **do not edit `application.yml` directly**. Instead, set environment variables in your terminal:

**Windows PowerShell:**
```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/saas_platform"
$env:SPRING_DATASOURCE_USERNAME="YOUR_POSTGRES_USER"
$env:SPRING_DATASOURCE_PASSWORD="YOUR_POSTGRES_PASSWORD"
```

**macOS / Linux:**
```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/saas_platform"
export SPRING_DATASOURCE_USERNAME="YOUR_POSTGRES_USER"
export SPRING_DATASOURCE_PASSWORD="YOUR_POSTGRES_PASSWORD"
```

---

### Step 2.4: Build and Verify the Test Suite

Navigate to the `backend` directory and run all automated unit and architecture tests:
```powershell
cd backend
mvn clean test
```

**Expected output:**
```text
[INFO] Results:
[INFO] Tests run: 24, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

### Step 2.5: Start the Backend Application

Run:
```powershell
mvn spring-boot:run
```

#### What happens automatically on first boot:
1. **Control-Plane Migration**: Flyway automatically executes `db/migration/control/` against schema `public`.
   - Creates `public.tenant_registry`
   - Creates `public.application`
   - Seeds default applications (`E_COMMERCE`, `DISTRIBUTION`, `WAREHOUSE`)
2. **Server Starts**: Tomcat binds to port `8081`.
   ```text
   Tomcat started on port 8081 (http) with context path '/'
   Started SaaSApplication in X.XXX seconds
   ```

---

### Step 2.6: Open Swagger UI & Verify Setup

Open your browser and navigate to:
👉 **`http://localhost:8081/swagger-ui.html`**

#### 1. Check Health:
- Execute `GET /actuator/health` → Response: `{"status": "UP"}`.

#### 2. Provision Your First Tenant:
- Expand **Control Plane - Tenant Provisioning** → `POST /api/v1/provisioning/tenants`
- Body:
  ```json
  {
    "tenantCode": "acme",
    "tenantName": "Acme Retail Enterprise"
  }
  ```
- Click **Execute** → Response: `201 Created`.
- *Under the hood:* PostgreSQL dynamically creates schema `tenant_acme`, executes 40 Flyway tenant migrations, seeds reference data, and creates the tenant record.

#### 3. Create Sample Organization Data:
- Expand **Organization Management** → `POST /api/v1/organizations`
- Header: `X-Tenant-Code: acme`
- Body:
  ```json
  {
    "organizationCode": "ORG_HQ",
    "organizationName": "Acme Global HQ",
    "description": "Headquarters Organization"
  }
  ```
- Click **Execute** → Response: `201 Created` with your new `organizationId` UUID!

---

## 3. SCENARIO B: Day 2+ Daily Work (Continuing with Existing Data)

When returning to work the next day or continuing development:

> [!IMPORTANT]
> **Your database data is 100% preserved.** Hibernate is configured with `ddl-auto: none`, and Flyway checks existing schema histories without re-running applied migrations or wiping data.

### Step 3.1: Ensure PostgreSQL is Running
- If using native PostgreSQL: Ensure the Windows Service is running.
- If using Docker Compose: Run `cd deploy && docker compose up -d postgres`.

### Step 3.2: Start the Backend Application
```powershell
cd D:\saas\backend
mvn spring-boot:run
```

### Step 3.3: Verify Console Output
On startup, Flyway will detect existing schema versions and skip re-applying:
```text
Current version of schema "public": 3
Schema "public" is up to date. No migration necessary.
Started SaaSApplication in X.XXX seconds
```

### Step 3.4: Resume API Testing or Client Development
All previously created tenants, schemas (`tenant_acme`), organizations, companies, and businesses are immediately available for queries:
```bash
curl -X GET http://localhost:8081/api/v1/organizations -H "X-Tenant-Code: acme"
```

---

## 4. Resetting the Database to Clean State (If Needed)

If a developer ever wants to wipe all local test data and start fresh from zero:

```powershell
# In PowerShell:
$env:PGPASSWORD="saas_password"
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U saas_user -d postgres -c "DROP DATABASE IF EXISTS saas_platform WITH (FORCE);"
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U saas_user -d postgres -c "CREATE DATABASE saas_platform;"
```
Then restart the app with `mvn spring-boot:run` to re-apply clean migrations.

---

## 5. Common Troubleshooting & FAQs

### Q1: `FATAL: password authentication failed for user "..."`
**Cause:** The database username/password in `application.yml` does not match your local PostgreSQL install.  
**Fix:** Set environment variables in your terminal without editing tracked code:
```powershell
$env:SPRING_DATASOURCE_USERNAME="your_username"
$env:SPRING_DATASOURCE_PASSWORD="your_password"
mvn spring-boot:run
```

### Q2: `Web server failed to start. Port 8081 was already in use.`
**Cause:** A previous instance of the backend is still running.  
**Fix (PowerShell):**
```powershell
Get-Process -Id (Get-NetTCPConnection -LocalPort 8081).OwningProcess | Stop-Process -Force
```

### Q3: `403 Forbidden` / `Tenant not recognized`
**Cause:** Calling tenant-specific endpoints without passing the `X-Tenant-Code` header or supplying an unregistered tenant code.  
**Fix:** Pass header `X-Tenant-Code: <tenantCode>` (e.g. `X-Tenant-Code: acme`).

### Q4: `400 Bad Request` / `CROSS_BUSINESS_ASSOCIATION_FORBIDDEN`
**Cause:** Attempting to associate a Store with a Sales Channel belonging to a different Business.  
**Fix:** Architectural rule: `store.business_id` and `channel.business_id` must match.

---

## 6. Enterprise API Response & Pagination Standard

All REST endpoints in this platform adhere to the **Symmetrical Enterprise Envelope (`ApiResponse<T>`)** and **Canonical Pagination (`PagedResponse<T>`)** standard.

### 6.1 Success Envelope Format
Every successful operation returns:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-10-03T06:16:27.807080Z"
}
```

### 6.2 Paginated Collection Responses
Collection endpoints (e.g. `/api/v1/organizations`, `/api/v1/companies`, `/api/v1/businesses`, `/api/v1/stores`, `/api/v1/channels`, `/api/v1/brands`, `/api/v1/warehouses`, `/api/v1/pos`) accept standard query parameters:
* `page`: 0-indexed page number (default: `0`)
* `size`: Page size (default: `20`)
* `sort`: Sorting field and direction (default: `createdAt,desc`)
* `search`: Case-insensitive text search filtering across `name` and `code`

Example response:
```json
{
  "success": true,
  "message": "Organizations retrieved successfully",
  "data": {
    "content": [
      {
        "organizationId": "75040db2-be69-47f7-b842-a895620a1980",
        "tenantId": "3574d9ad-8071-4012-bee6-5f32370f8088",
        "organizationCode": "ORG_GLOBAL",
        "organizationName": "Acme Global Corp",
        "status": "ACTIVE",
        "description": "Primary holding organization",
        "createdAt": "2026-10-03T05:15:00.426396Z",
        "updatedAt": "2026-10-03T05:15:00.426396Z"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false,
    "first": true,
    "last": true
  },
  "timestamp": "2026-10-03T06:16:27.807080Z"
}
```

### 6.3 Validation Error Response (`422 Unprocessable Entity`)
When request body validation fails, field-level errors are cleanly enumerated:
```json
{
  "success": false,
  "message": "Validation failed for request parameters",
  "errorCode": "VALIDATION_FAILED",
  "validationErrors": [
    {
      "field": "organizationCode",
      "message": "Organization code is required",
      "rejectedValue": ""
    },
    {
      "field": "organizationName",
      "message": "Organization name is required",
      "rejectedValue": ""
    }
  ],
  "timestamp": "2026-10-03T06:16:28.112722600Z",
  "path": "/api/v1/organizations"
}
```

### 6.4 Business Rule Error Response (`400 Bad Request`)
```json
{
  "success": false,
  "message": "Organization with code 'ORG_GLOBAL' already exists",
  "errorCode": "ORGANIZATION_CODE_EXISTS",
  "timestamp": "2026-10-03T06:16:28.170539700Z",
  "path": "/api/v1/organizations"
}
```

### 6.5 Swagger / OpenAPI Documentation
Interactive Swagger UI with complete models and envelope schemas is available at:
👉 **`http://localhost:8081/swagger-ui/index.html`**

---

## 7. Frontend Application Setup & Development (`frontend`)

The SaaS platform presentation layer is built with **Next.js 16 (App Router)**, **React 19**, **TypeScript**, **Tailwind CSS v4**, and **Radix UI** accessible primitives, matching ADR-009 and the UI Design System.

### 7.1 Architecture & Stack
- **Framework:** Next.js 16 (App Router) + Turbopack
- **Styling:** Tailwind CSS v4 (`@import "tailwindcss";`)
- **State & Caching:** TanStack Query (React Query) v5 + Zustand (`useTenantStore`, `useBusinessStore`)
- **Forms & Validation:** React Hook Form + Zod (symmetrically mapping backend OpenAPI validation)
- **Icons:** Lucide React
- **Port:** `3000` (Backend: `8081`)

### 7.2 Directory Structure
```text
frontend/
├── src/
│   ├── app/
│   │   ├── layout.tsx                     # Root layout with QueryClientProvider
│   │   ├── page.tsx                       # Main control plane & dashboard
│   │   └── business/[businessId]/page.tsx # Canonical business workspace deep link
│   ├── components/
│   │   ├── layout/                        # Header (Tenant switcher, schema badge) & Sidebar
│   │   ├── ui/                            # Accessible Radix primitives (Button, Table, Dialog, etc.)
│   │   └── views/                         # Domain views (Organizations, Companies, Businesses, Workspace)
│   ├── lib/
│   │   ├── api-client.ts                  # Axios client injecting dynamic X-Tenant-Code
│   │   ├── services.ts                    # Fully typed API services
│   │   └── utils.ts                       # Tailwind merge cn() helper
│   ├── store/
│   │   ├── useTenantStore.ts              # Active tenant state synced to localStorage
│   │   └── useBusinessStore.ts            # Active business workspace state
│   └── types/
│       └── api.ts                         # Complete TypeScript models
```

### 7.3 Running the Frontend Locally

```powershell
# 1. Navigate to the frontend directory
cd D:\saas\frontend

# 2. Install dependencies (if not already installed)
npm.cmd install

# 3. Start the development server
npm.cmd run dev
```

The application will be live at:
👉 **`http://localhost:3000`**

### 7.4 Multi-Tenant Dynamic Context & Switching
- When navigating the UI, the top **Header** displays the active tenant and schema (e.g. `Schema: tenant_acme`).
- Clicking **"Switch Tenant"** allows selecting any provisioned tenant or typing a new tenant code.
- All subsequent Axios requests automatically inject `X-Tenant-Code: <selectedTenant>` in request headers.
- Selecting a Business navigates to the dedicated **Business Workspace Hub** (`/business/{businessId}`) for managing Applications (`E_COMMERCE`, `DISTRIBUTION`, `WAREHOUSE`), Stores, Brands, Channels, Warehouses, and POS terminals.


