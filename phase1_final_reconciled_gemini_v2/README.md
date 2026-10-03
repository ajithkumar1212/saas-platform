# Phase 1 Final Reconciled Documentation Package

This package is the corrected Phase 1 documentation baseline after reconciliation against the Gemini/Antigravity specification-vs-DDL audit.

Read first:

1. `FINAL_RECONCILIATION_REPORT.md`
2. `FINDINGS_DECISION_MATRIX.md`
3. `DDL_CHANGE_DECISION.md`
4. `FINAL_BASELINE_STATUS.md`

Key decisions:

- Warehouse, POS and Website are Store-owned.
- Store/Channel same-business integrity is application-service enforced.
- ADR-004 is Accepted for Phase 1.
- `user_account.password_hash` remains nullable but is not used for external IdP credentials.
- `audit_log` has no generic `outcome` column.
- Final tenant explicit index count is 29.
- A complete 40-table catalog/data dictionary is included.
- No PostgreSQL DDL change is required by the audit.
- Flyway replay remains Deferred.
