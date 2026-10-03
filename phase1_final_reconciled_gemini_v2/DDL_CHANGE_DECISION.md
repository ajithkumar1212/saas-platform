# DDL Change Decision

## Result

**No PostgreSQL DDL change is required by the Gemini audit findings.**

Intentionally retained:

```text
warehouse.store_id       → store.store_id
pos.store_id             → store.store_id
website.store_id         → store.store_id
store_channel.store_id   → store.store_id
store_channel.channel_id → channel.channel_id
user_account.password_hash nullable
audit_log without outcome
```

Reason:

- Warehouse/POS/Website are Store-owned.
- Store/Channel same-business integrity is an application-service rule.
- `password_hash` is dormant pending ADR-003.
- Domain audit does not require a generic outcome field.
