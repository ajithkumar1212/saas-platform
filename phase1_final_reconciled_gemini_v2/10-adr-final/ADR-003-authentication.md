# ADR-003: Authentication and Identity Boundary

- **Status:** Accepted for Phase 1
- **Type:** Architecture decision
- **Scope:** Identity and authentication

## Context

The platform has platform-level and tenant/business-scoped users. Identity credentials and tenant authorization have different ownership and lifecycle concerns.

The business requirement requires authentication before access to `app.xyz.com` and requires tenant context to participate in authorization.

## Decision

Use an external standards-based OIDC identity provider as the authentication boundary, with **Keycloak** designated as the official reference implementation for development, testing, and self-hosted environments (per ADR-008), for:

- credential authentication;
- MFA, if approved;
- identity-provider session management;
- authentication token issuance (JWT Bearer tokens).

The application remains responsible for:

- validating issuer, audience, signature, expiry, and approved claims via Spring Security OAuth2 Resource Server;
- mapping the stable identity-provider subject (`sub`) to application user/account records;
- resolving tenant membership;
- resolving roles and permissions;
- enforcing hierarchy scopes;
- enforcing Business access;
- enforcing Business Application access;
- auditing security-sensitive changes.

## Identity Provider Compatibility & Scope

- While **Keycloak** is the reference implementation for local dev and self-hosted deployments, the backend is implemented as a standard OAuth2 Resource Server (validating against configurable JWKS endpoints) so that managed providers (Auth0, AWS Cognito, Okta) can be utilized by configuring issuer and JWKS URI properties without code modification.
- Hosted vs. self-managed deployment remains an environment-level configuration choice.
- Exact MFA policy and password reset flows are managed within the IdP realm.

## Consequences

### Positive

- Credential handling is delegated to a specialized identity boundary.
- Identity lifecycle can be centralized.
- MFA can be provided without embedding credential infrastructure in the application.
- Application authorization remains aligned with the tenant hierarchy.

### Negative

- The platform depends on identity-provider availability.
- Token validation and subject mapping must be implemented correctly.
- Provider changes require careful identity mapping.
- Invitations, account recovery, revocation, and tenant association require explicit workflows.

## Security requirements

The application must never treat UI route guards as authorization.

Authentication establishes identity; authorization establishes what the identity may access.

No provider-specific implementation may be frozen until the identity provider decision is approved.

## Verification

Before production:

- token validation tests pass;
- invalid issuer/audience/signature/expiry cases are rejected;
- subject-to-user mapping is tested;
- tenant association is tested;
- role/permission enforcement is tested;
- revocation/logout behavior is defined;
- MFA decision is approved.
