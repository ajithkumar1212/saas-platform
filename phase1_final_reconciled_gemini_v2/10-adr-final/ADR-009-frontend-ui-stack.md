# ADR-009: Frontend Framework, UI Design System, and Client Architecture

- **Status:** Accepted for Phase 1
- **Type:** Architecture decision
- **Scope:** Frontend user interface, client-side architecture, styling, and component library

## Context

The platform requires a modern, responsive, highly accessible (WCAG 2.2 AA) web presentation layer supporting two distinct user experiences as defined in [`05-ui-design-system-final.md`](file:///D:/saas/phase1_final_reconciled_gemini_v2/05-ui-design-system-final.md):
1. **Public Web Experience (`www.xyz.com`):** Marketing, product info, pricing, and self-service tenant signup. Requires SEO-friendly server-rendered pages and fast Core Web Vitals.
2. **Authenticated SaaS Workspace (`app.xyz.com`):** High-density operational interface supporting Tenant, Organization, Company, and Business management, Store/Brand/Channel setup, Warehouse/POS/Website resources, and Business Application enablement under the canonical route `app.xyz.com/business/{businessId}`.

## Decision

The Phase 1 frontend and client-side technology stack is finalized as follows:

### 1. Core Framework & Language
- **Framework:** Next.js (App Router) with React 18+
- **Language:** TypeScript 5.x (Strict mode enabled)
- **Rendering Strategy:**
  - Public marketing and signup pages (`www.xyz.com`): Static Site Generation (SSG) / Server-Side Rendering (SSR).
  - Authenticated SaaS app (`app.xyz.com`): Client-side interactive dashboards backed by React Server Components (RSC) and Next.js Route Handlers.

### 2. Styling & Design Tokens
- **CSS Framework:** Tailwind CSS 3.4+ / 4.x
- **Token System:** Centralized design tokens (colors, spacing, typography, radii, elevation) defined in CSS variables matching the token specifications in [`05-ui-design-system-final.md`](file:///D:/saas/phase1_final_reconciled_gemini_v2/05-ui-design-system-final.md).
- **Dark Mode Support:** Class-based theme toggling via `next-themes`.

### 3. Component Library & Accessibility Primitives
- **Component Architecture:** **shadcn/ui** (copy-in component architecture with complete code ownership, zero rigid vendor lock-in).
- **Accessibility Primitives:** **Radix UI Primitives** (headless, fully accessible components ensuring keyboard navigation, focus trap, screen-reader announcements, and ARIA conformance targeting WCAG 2.2 AA).
- **Icons:** **Lucide React** (consistent, lightweight SVG icon suite).

### 4. Client State Management & Data Fetching
- **Global Client State:** **Zustand / Redux Toolkit** (predictable, lightweight state management for active tenant/business context, session UI state, drawers/modals, and multi-step transaction workflows).
- **Server Cache & Async State:** **TanStack Query (React Query) v5**
  - Manages API queries, optimistic updates, background refetching, and cache invalidation.
- **Client Form State & Validation:** **React Hook Form** combined with **Zod** schema validation.
  - Zod schemas mirror backend OpenAPI models from [`04-api-final.md`](file:///D:/saas/phase1_final_reconciled_gemini_v2/04-api-final.md) for bidirectional type safety.

### 5. Authentication & Token Management
- **Client OIDC Integration:** Standard OpenID Connect client flow (`oidc-client-ts` or NextAuth.js / Auth.js) integrating with the platform's Keycloak/OIDC IdP ([ADR-003](file:///D:/saas/phase1_final_reconciled_gemini_v2/10-adr-final/ADR-003-authentication.md)).
- **Session & Token Storage:** Secure HTTP-only cookies with CSRF protection; access tokens attached as Bearer tokens to backend REST calls.
- **Tenant Context Preservation:** Tenant context is captured on login and preserved across client requests, matching the active Business workspace context (`/business/{businessId}`).

### 6. Client Project Structure
```text
ui/
├── app/
│   ├── (public)/              # www.xyz.com (Landing, Pricing, Signup)
│   ├── (auth)/                # Login, OAuth callback, Logout
│   └── (dashboard)/           # app.xyz.com
│       ├── business/
│       │   └── [businessId]/  # Operational Business Workspace
│       └── admin/             # Tenant Admin / Organization / Company
├── components/
│   ├── ui/                    # shadcn/ui & Radix primitives (Button, Table, Dialog, etc.)
│   └── shared/                # App-specific reusable components (TenantSelector, Header)
├── hooks/                     # Custom React hooks (useTenant, useAuth, useBusiness)
├── lib/                       # API client, query client, formatting utilities
└── schemas/                   # Zod validation schemas
```

## Consequences

### Positive
- **Unified Web Stack:** Next.js allows unified maintenance of public marketing routes and authenticated app routes within one modern TypeScript codebase.
- **Strict Accessibility:** Radix UI primitives guarantee accessible modals, dropdowns, and navigation out-of-the-box, meeting the UX requirements of `05-ui-design-system-final.md`.
- **Zero Component Lock-in:** shadcn/ui provides editable source components directly in the repository rather than compiled npm package constraints.
- **End-to-End Type Safety:** TypeScript + Zod schemas derived from backend OpenAPI contracts prevent runtime data mismatch bugs.

### Negative / Trade-Offs
- Requires a Node.js 20+ runtime for building and serving the Next.js application container.
- Initial frontend build setup requires configuring Tailwind CSS, Radix UI, and TypeScript build pipelines.
