# Phase 1 Deletion Matrix

## Policy

Phase 1 uses a hybrid deletion strategy. Business and governance records are normally retained and transitioned through lifecycle/status/history. Physical deletion is limited to explicitly approved transient/configuration associations.

| Area | Default action | Physical delete? | Notes |
|---|---|---:|---|
| Tenant | Archive/deactivate | No | Tenant is an isolation boundary; retain identity/provisioning history. |
| Organization | Deactivate/archive | No | Preserve hierarchy history. |
| Organization legal/address/contact | Retain/archive | No | Legal/contact history may be required. |
| Company | Deactivate/archive | No | Preserve company history. |
| Company legal/address/contact | Retain/archive | No | Preserve legal context. |
| Business | Deactivate/archive | No | Do not destroy operational history by default. |
| Brand | Deactivate/archive | No | Preserve references from historical records. |
| Store | Deactivate/archive | No | Preserve storefront history. |
| Store location | Deactivate/archive | No | Primary-location changes must be historical. |
| Warehouse | Deactivate/archive | No | Preserve stock/operational history. |
| Warehouse location | Deactivate/archive | No | Preserve warehouse-location references. |
| POS | Deactivate | No | Preserve transaction references. |
| Website | Deactivate/archive | No | Preserve domain/configuration history. |
| Website domain | Deactivate/unassign | Yes, only if no retained reference requires it | Prefer retaining domain history. |
| Channel | Deactivate | No | Preserve business-channel history. |
| Store-channel association | Remove association | Yes | Association is configuration, not historical business evidence by itself. |
| Store-brand association | Remove/reassign | Yes, subject to historical-reference checks | Enforced same-business integrity remains. |
| Store branding/theme | Replace/deactivate | Yes only for unused configuration | Preserve active historical configuration when required. |
| User account | Disable | No | Never physically delete as the normal deprovisioning path. |
| Role | Deactivate | No | Preserve authorization history. |
| Permission | Deactivate | No | Preserve authorization history. |
| Role-permission association | Remove | Yes | Association can be recreated. Audit the change. |
| User access scope | Revoke | Yes only when history is preserved elsewhere | Revoke access; preserve `user_access_history`. |
| Application catalog | Deactivate | No | Platform catalog history is retained. |
| Business application | Disable | No | Preserve enable/disable history. |
| Reference type/value | Deactivate | No | System reference data should not be destroyed. |
| Tax registration | Deactivate/end validity | No | Preserve legal/tax history. |
| Business-tax association | End validity/disable | No | Preserve historical tax applicability. |
| History tables | Retain | No | Immutable historical evidence. |
| Audit log | Retain | No | Immutable audit evidence. |

## Rules

1. No blanket `ON DELETE CASCADE`.
2. No blanket `ON DELETE SET NULL`.
3. Foreign-key relationships remain restrictive unless a specific approved exception exists.
4. Physical deletion must be blocked when retained business/history records still reference the target.
5. Deactivation/archival APIs must be preferred over `DELETE` for core entities.
6. Any legal-retention or privacy-driven erasure requirement is a separate approved policy and must not be implemented as an ad-hoc hard delete.
