# ADR-007: Data Deletion and Retention Policy

- **Status:** Accepted baseline; retention periods remain open
- **Type:** Data lifecycle decision
- **Scope:** Phase 1 deletion behavior

## Context

The platform contains tenant hierarchy data, user/access history, audit records, legal/tax information, and business configuration.

Uncontrolled cascading deletion could remove related business or security history unexpectedly.

The database baseline therefore uses explicit non-cascading relationships.

## Decision

Use a hybrid deletion policy.

### Default relational behavior

Do not use blanket `ON DELETE CASCADE`.

Foreign-key relationships use restrictive/default behavior unless an explicitly approved relationship requires another action.

### Business and configuration data

Use archive/soft-delete semantics where business history or referential continuity requires preservation.

### Physical deletion

Physical deletion is permitted only for data categories explicitly approved for deletion.

Deletion operations must respect:

- tenant ownership;
- authorization;
- dependency constraints;
- audit requirements;
- legal holds;
- retention obligations.

### Audit records

Audit evidence must not be deleted through ordinary application CRUD paths.

Any exceptional audit-retention/deletion operation requires explicit governance.

### Legal/tax data

Tax and legal records may have retention requirements beyond ordinary application data. Exact retention periods are not defined by this ADR and require owner/legal approval.

## Consequences

### Positive

- Reduces accidental loss of business history.
- Protects auditability.
- Makes deletion behavior explicit.
- Avoids destructive cascade chains.

### Negative

- More explicit lifecycle management is required.
- Historical records can increase storage requirements.
- Purging requires governed jobs/processes rather than unrestricted CRUD deletes.

## Required controls

Deletion must be:

- authorized;
- tenant-scoped;
- auditable;
- dependency-aware;
- consistent with retention and legal-hold rules.

## Open items

This ADR does not define:

- exact retention periods;
- legal-hold implementation;
- regulatory deletion exceptions;
- archive storage technology;
- purge schedules.

Those require separate approved operational/legal decisions.

## Verification

The canonical Phase 1 database model must not contain blanket cascading deletes.

Deletion behavior must be covered by repository/integration tests and security authorization tests.
