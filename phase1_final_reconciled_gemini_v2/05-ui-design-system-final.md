# UI Design System and Phase 1 UX

## 1. Document status

**Status:** FINALIZED for Phase 1 UX/design-system baseline.

This document defines the Phase 1 UI/UX baseline using the project's uploaded requirements and existing project documentation. It separates requirements explicitly supported by the source material from design proposals and unresolved decisions.

The UI is a presentation and interaction layer. Backend authentication, authorization, tenant isolation, and database constraints remain authoritative.

---

## 2. Product experience

The platform has two primary web experiences:

```text
www.xyz.com
    │
    ├── Product information
    ├── Platform features
    ├── Pricing
    ├── Sign Up
    ├── Login
    └── Contact / Support
            │
            ↓
       app.xyz.com
            │
            ├── Tenant context
            ├── Organization
            ├── Company
            ├── Business Management
            └── Business Workspace
```

The public website is not tenant-specific operational UI.

The authenticated application is the operational SaaS UI.

The authenticated user journey is based on:

```text
User
  ↓
Tenant
  ↓
Roles
  ↓
Permissions
  ↓
Businesses
  ↓
Business Applications
```

The authenticated tenant context must remain available to the application so that the UI can present the correct scope and the backend can enforce authorization.

---

## 3. Phase 1 information hierarchy

The core business hierarchy is:

```text
Tenant
└── Organization
    └── Company
        ├── Business
        │   ├── Applications
        │   ├── Brand
        │   ├── Store
        │   └── Channel
        │
        └── Business
```

The business workspace is the primary operational context after a user selects a Business.

The UI must not collapse Tenant, Organization, Company, and Business into one generic concept.

Store, Brand, and Channel remain separate concepts.

Administration is a cross-platform capability and is not presented as an installable Business Application.

---

## 4. Public website

### 4.1 Public navigation

The source requirement defines the public website as the platform's public-facing entry point.

The baseline navigation is:

```text
Home
Product
Pricing
Sign Up
Login
Contact / Support
```

The public website must not display tenant-specific operational data.

### 4.2 Sign-up

The sign-up experience is the entry point for a new tenant.

The source requirement identifies these registration inputs:

```text
Tenant / Account Name
Administrator Name
Email Address
Mobile Number
Country
Password
Required verification information
Terms and Conditions acceptance
```

The first registered user becomes the initial **Tenant Administrator** after successful registration and verification.

The exact verification interaction, password policy presentation, error copy, and provider-specific authentication screens remain implementation details to be defined consistently with the authentication decision.

### 4.3 Login

The public site provides a Login entry point to `app.xyz.com`.

The UI must not attempt to determine authorization by itself. After authentication, the application receives the authenticated context and displays only resources available to the user.

---

## 5. Initial tenant setup

After first successful login, the Tenant Administrator is guided through:

```text
Organization Setup
        ↓
Company Setup
        ↓
Business Setup
        ↓
Application Configuration
```

### 5.1 Organization setup

The source requirement identifies:

```text
Organization Name
Organization Code
Description
Contact Information
Address
Domain
```

The UI should group these into understandable sections rather than presenting one undifferentiated form.

### 5.2 Company setup

The source requirement identifies:

```text
Company Name
Company Code
Legal Name
Registration Details
Tax Information
Address
Contact Information
```

Legal and tax information must be shown only to users authorized to access it.

### 5.3 Setup progression

The setup experience should make the current step and remaining setup work visible.

A user should be able to understand:

```text
Current step
Completed steps
Next step
Validation errors
```

The exact ability to move backward/forward between setup steps is not specified by the source and remains an implementation decision.

---

## 6. Business Management

Business Management is a first-class control-plane experience.

Baseline presentation:

```text
My Businesses

+ Create Business

Business A
[ Manage Business ]

Business B
[ Manage Business ]
```

A tenant can create multiple businesses.

A Business represents an operational business/workspace within the tenant.

### 6.1 Create Business

Selecting **+ Create Business** opens a popup or wizard.

The source requirement identifies:

```text
Business Name
Business Code
Business Description
Business Type
Company
Country
Currency
Time Zone
```

The form must validate required fields before submission and preserve entered values when recoverable validation errors occur.

The UI must not allow a caller to select or edit the physical PostgreSQL schema name.

### 6.2 Business list

The Business Management screen should make the following clear:

- Business name
- Business identity/code where applicable
- Business status where applicable
- Parent Company
- Available applications/capabilities where the approved API provides them
- Manage Business action

The exact table/card layout is a UI implementation choice.

### 6.3 Manage Business

Each Business provides a **Manage Business** action.

The recommended workspace URL is:

```text
app.xyz.com/business/{businessId}
```

The Business workspace displays only the applications and capabilities available to that Business.

---

## 7. Business applications

A Business is not tied to a single application.

The conceptual model is:

```text
Business
├── E-Commerce
├── Distribution
└── Warehouse
```

Inventory is treated as a shared capability in the current project direction rather than as a selectable application.

The source requirements also describe examples involving Inventory alongside applications. The UI must follow the finalized product/application model rather than presenting Inventory as a selectable application unless that product decision is explicitly changed.

### 7.1 Application visibility

The UI may hide unavailable applications or capabilities.

However:

> UI visibility is not authorization.

The API/backend remains authoritative for tenant, business, role, permission, and access-scope enforcement.

### 7.2 Application configuration

The high-level journey includes:

```text
Select / Enable Applications
        ↓
Manage Business
        ↓
Configure Business Applications
```

Detailed application-configuration screens are not frozen here because the source requirements do not define their complete Phase 1 field-level behavior.

---

## 8. Business workspace

The Business workspace is scoped to one Business.

Recommended URL:

```text
app.xyz.com/business/{businessId}
```

The workspace should always make the active Business context visible.

Where relevant, the UI should also provide the parent context:

```text
Tenant
  / Organization
    / Company
      / Business
```

### 8.1 Workspace navigation

Baseline conceptual navigation:

```text
Business Workspace
├── Overview
├── Applications
├── Store
├── Brand
├── Channel
├── Warehouse
├── POS
├── Website
└── Administration / Account
```

The exact module availability is permission- and application-dependent.

The product requirements identify broader future application capabilities such as Product Catalog, Inventory, Customers, Cart, Checkout, Payment, Orders, Fulfillment, Delivery, Returns/Exchange, Distribution, and Warehouse Management. These are not all Phase 1 database/UI implementation commitments and should not be presented as implemented Phase 1 screens unless their phase is explicitly approved.

---

## 9. Store, Brand, Channel, Warehouse, POS, Website

The finalized Phase 1 data model contains distinct resources for:

```text
Brand
Store
Store Location
Warehouse
Warehouse Location
POS
Website
Website Domain
Channel
Store Channel
Store Branding
Store Theme
```

The UI should preserve these distinctions.

### 9.1 Store

A Store belongs to a Business.

The Store experience may expose its configured location, warehouse, POS, website, branding, theme, and channel relationships according to permissions and available data. Warehouse, POS, and Website are Store-owned resources in the Phase 1 hierarchy.

### 9.2 Brand

Brand is a Business-level concept.

The UI must not treat Brand as a synonym for Store.

### 9.3 Channel

Channel is a Business-level concept.

Store-channel association is separate from the Channel definition itself.

### 9.4 Warehouse

Warehouse is represented in the Phase 1 data model under Store.

Warehouse Location is a separate concept.

### 9.5 POS

POS is represented as a Store-level resource.

### 9.6 Website

Website is a Store-level resource.

Website Domain is represented separately.

The UI must not expose the physical tenant schema or database implementation details.

---

## 10. Administration

Administration is a cross-platform capability.

Conceptually it includes:

```text
User Management
Role Management
Permission Management
Organization Management
Company Management
Business Management
Application Access
Subscription Management
Billing
Audit Logs
Security Configuration
```

Not every item above is a Phase 1 implementation commitment.

The UI should expose administrative capabilities according to the user's permissions and the approved Phase 1 scope.

Administration must not be represented as an installable Business Application.

---

## 11. User, role, permission, and access scope

The UI authorization model follows:

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
        ↓
Visible / actionable resources
```

The Phase 1 data model supports access scopes for:

```text
ORGANIZATION
COMPANY
BUSINESS
BRAND
STORE
CHANNEL
```

The UI should use access scope to determine which navigation items and resource actions are available.

The UI must not assume that visibility alone grants access. Every protected operation is revalidated by the backend.

---

## 12. Navigation model

### 12.1 Public navigation

```text
Home
Product
Pricing
Sign Up
Login
Contact / Support
```

### 12.2 Authenticated application navigation

Conceptual baseline:

```text
Tenant Context
├── Organizations
├── Companies
├── Businesses
│
└── Selected Business
    ├── Overview
    ├── Applications
    ├── Store
    ├── Brand
    ├── Channel
    ├── Warehouse
    ├── POS
    └── Website

Account / Administration
```

The exact navigation hierarchy may be adapted to the final application information architecture, provided the Tenant → Organization → Company → Business relationship remains clear.

---

## 13. Hierarchy presentation

The UI should provide clear hierarchy context.

Recommended patterns:

```text
Breadcrumb
Tenant / Organization / Company / Business
```

and, where appropriate:

```text
Hierarchy Tree
Tenant
└── Organization
    └── Company
        ├── Business A
        └── Business B
```

Hierarchy changes and access changes are security-sensitive and must follow backend authorization and audit requirements.

---

## 14. Forms

Forms are the primary interaction pattern for:

- Sign-up
- Organization setup
- Company setup
- Business creation/editing
- Application configuration
- Resource configuration

Baseline form principles:

- Clearly identify required and optional fields.
- Use field-level validation.
- Display understandable validation messages.
- Preserve recoverable user input after validation failures.
- Prevent accidental duplicate submission.
- Clearly distinguish destructive or lifecycle-changing actions.
- Require confirmation where a lifecycle operation needs explicit user acknowledgement.
- Never expose internal database/schema identifiers as editable business fields.

Exact component library, visual tokens, and field-level validation messages are implementation decisions unless already defined by the source project.

---

## 15. Tables, lists, and cards

The UI may represent collections using tables, lists, or cards according to the resource.

Collection views should support, where the API exposes them:

```text
Search / filter
Sorting
Pagination
Status
Primary identifying information
Available actions
Empty state
Loading state
Error state
No-access state
```

The UI must not assume that every API collection supports every filter or sort. It should use the actual API contract.

---

## 16. Dialogs and wizards

The source requirement explicitly allows a popup or wizard for Create Business.

Use a dialog for short, focused operations.

Use a wizard when the workflow has meaningful sequential steps.

The Create Business interaction must support the source-defined business fields and a clear completion result.

The exact dialog dimensions, animation, button labels, and wizard mechanics are design implementation details.

---

## 17. Loading, empty, error, and no-access states

Every operational screen should define these states:

### Loading

Indicate that data is being retrieved without presenting incomplete information as final.

### Empty

Explain what is empty and, where permitted, provide the next useful action.

Example:

```text
No businesses found.
[ Create Business ]
```

### Error

Provide a user-safe explanation and a retry/recovery action where appropriate.

Do not display:

- stack traces;
- SQL;
- PostgreSQL schema names;
- internal exception details;
- security-sensitive authorization evidence.

### No access

Use a clear access-denied experience without revealing protected resource existence.

---

## 18. Legal and sensitive data presentation

Organization/company legal and tax information is sensitive business information.

The UI should:

- show it only to authorized users;
- avoid unnecessary exposure;
- mask sensitive identifiers when the complete value is not required;
- avoid placing sensitive values into URLs;
- avoid logging sensitive values in browser-side diagnostics.

The UI is not responsible for enforcing authorization independently; backend authorization remains authoritative.

---

## 19. Responsive behavior

The UI should support responsive layouts.

At minimum, the design must remain usable across desktop and smaller viewport sizes for the core Phase 1 flows:

```text
Sign Up
Login
Tenant setup
Organization setup
Company setup
Business Management
Create Business
Manage Business
Business workspace
```

The exact breakpoint system is not specified by the source and remains a design implementation decision.

---

## 20. Accessibility

The current source UI document proposes accessibility requirements including:

- keyboard access;
- visible focus;
- semantic labels;
- error announcements;
- sufficient contrast;
- accessible hierarchy/breadcrumb presentation.

The source document proposes **WCAG 2.2 AA** as the target, pending approval.

Therefore:

```text
Accessibility target: WCAG 2.2 AA — Proposed
```

It must not be represented as an already-approved compliance certification.

---

## 21. Localization and formatting

The source UI document identifies localization as a design requirement.

UI copy and formats should be localizable.

Where approved, the UI should use tenant/business context for:

```text
Locale
Time zone
Currency
```

The exact localization framework, supported locales, date/time formats, number formats, and currency presentation are not defined by the source material and remain open.

---

## 22. Visual design-system baseline

The source UI document establishes interaction principles but does not provide a frozen numerical visual-token system.

Therefore the following are **not silently invented here**:

```text
Primary color palette
Secondary color palette
Typography scale
Exact font family
Spacing token values
Border-radius values
Shadow tokens
Breakpoints
Icon library
Animation durations
Component package
```

These must be defined in the actual design system/Figma/component-library work before being treated as implementation constants.

The UI implementation should nevertheless use centralized design tokens rather than page-specific hard-coded styling.

---

## 23. Core component inventory

The Phase 1 UI should have reusable components for the documented interaction patterns:

```text
Application Shell
Navigation
Tenant / Business Context
Breadcrumb
Hierarchy Tree
Page Header
Form
Form Field
Select
Autocomplete
Date / Time controls where required
Dialog
Wizard / Stepper
Button
Table / List
Card
Badge / Status
Alert
Toast / Notification
Loading State
Empty State
Error State
No-access State
Confirmation Dialog
```

The exact component library and implementation technology are not fixed by this document.

---

## 24. Security-sensitive UI rules

The UI must:

- never expose PostgreSQL schema names;
- never allow a user to edit tenant schema context;
- never use hidden UI elements as the sole authorization mechanism;
- avoid exposing cross-tenant resource existence;
- protect sensitive legal/tax information;
- avoid storing access tokens in unsafe browser-visible locations contrary to the final authentication design;
- prevent accidental duplicate submissions on provisioning/create operations;
- display safe API error messages;
- respect backend permission results.

---

## 25. Phase 1 user journeys

### 25.1 New customer

```text
www.xyz.com
      │
      └── Sign Up
            │
            ↓
      Tenant Registration
            │
            ↓
      Verify User
            │
            ↓
      Create Tenant
            │
            ↓
      Provision Tenant Schema
            │
            ↓
      Create Tenant Administrator
            │
            ↓
      app.xyz.com
            │
            ↓
      Organization Setup
            │
            ↓
      Company Setup
            │
            ↓
      Business Management
            │
            ↓
      + Create Business
            │
            ↓
      Select / Enable Applications
            │
            ↓
      Manage Business
            │
            ↓
      Configure Business Applications
```

### 25.2 Existing customer

```text
www.xyz.com
      │
      └── Login
            │
            ↓
      Authentication
            │
            ↓
      Resolve Tenant
            │
            ↓
      app.xyz.com
            │
            ↓
      Tenant Dashboard
            │
            ↓
      Select Business
            │
            ↓
      Manage Business
```

---

## 26. Design-system acceptance criteria

The Phase 1 UI baseline is considered aligned when:

1. `www.xyz.com` is clearly separated from authenticated operational UI.
2. `app.xyz.com` is the authenticated application portal.
3. Tenant, Organization, Company, and Business are visibly distinct concepts.
4. Business is represented as the operational workspace.
5. The recommended Business URL is `app.xyz.com/business/{businessId}`.
6. Tenant Administrator onboarding follows Organization → Company → Business → Application Configuration.
7. Create Business supports the source-defined business fields.
8. Store, Brand, Channel, Warehouse, POS, and Website remain distinct concepts.
9. Administration is treated as a cross-platform capability rather than an installable Business Application.
10. UI visibility never replaces backend authorization.
11. Legal/tax information is protected in the UI.
12. Loading, empty, error, and no-access states are defined.
13. Forms provide clear validation and recovery behavior.
14. Core Phase 1 flows are responsive.
15. Keyboard access, visible focus, semantic labels, error announcements, and sufficient contrast are addressed.
16. WCAG 2.2 AA remains explicitly marked **Proposed** unless separately approved.
17. Visual tokens are centralized rather than hard-coded per screen.
18. The UI follows the implemented API contract rather than assuming unsupported filters, fields, or operations.
19. Application visibility follows approved Business Application semantics.
20. No physical PostgreSQL schema information is exposed to users.

---

## 27. Finalized Frontend UI Stack (ADR-009)

The Phase 1 presentation and interaction layer is locked to the following frontend stack:

- **Framework:** React 18+ (Next.js App Router / Vite SPA) with TypeScript (Strict mode)
- **Styling & Tokens:** Tailwind CSS with centralized design tokens defined in CSS variables
- **Component Architecture:** **shadcn/ui** (owned source components with full styling control)
- **Accessibility Primitives:** **Radix UI** (headless, fully accessible ARIA/WCAG 2.2 AA primitives for dialogs, dropdowns, popovers, tabs)
- **Icon Suite:** **Lucide React**
- **Global Client State:** **Zustand / Redux Toolkit** (lightweight, predictable client-side state for active tenant context, session UI, drawers, and multi-step workflows)
- **Server Cache & Async State:** **TanStack Query (React Query) v5** for API server caching and optimistic mutations
- **Forms & Validation:** **React Hook Form** + **Zod** schema validation aligned with backend OpenAPI schemas
- **Client Auth:** OIDC Authorization Code Flow with PKCE against the platform Keycloak/OIDC boundary (ADR-003)

## 28. Open and deferred UI decisions

The following cosmetic and design details remain open for designer specification:

- final visual brand identity and marketing logo;
- exact brand color hex palette;
- typography/font family selection;
- final Figma design tokens;
- detailed screen-by-screen wireframes for post-Phase 1 modules;
- final localization framework and supported locales;
- detailed subscription/billing UI for later phases.

These are design/creative items and do not block engineering setup.

---

## 29. Source and consistency notes

This document is intentionally aligned with the supplied requirements and the finalized platform model.

Important consistency rules:

- The original requirement examples mention Inventory beside applications. The current finalized project direction treats Inventory as a **shared capability**, not a selectable Phase 1 application.
- The API/database model contains an Application Catalog and `business_application`; ADR-004 is accepted for Phase 1. The UI must follow the approved enablement/authorization behavior and must not invent entitlement rules.
- The UI must follow the finalized Phase 1 database hierarchy and the API authorization model.
- The canonical database schema is an implementation detail and must never become a user-facing navigation or configuration concept.

**Document status: FINALIZED.**
