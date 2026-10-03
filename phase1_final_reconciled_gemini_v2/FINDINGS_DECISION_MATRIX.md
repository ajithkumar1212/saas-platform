# Gemini Findings Decision Matrix

| ID | Severity | Final result | Action |
|---|---|---|---|
| CM-01 | Critical | Documentation/API mismatch | Store ownership retained; docs/routes fixed |
| CM-02 | High | Documentation overstatement | Application-service validation fixed in contract/QA |
| CM-03 | High | Documentation mismatch | ADR-004 accepted status propagated |
| CM-04 | Medium | Authentication ambiguity | Nullable `password_hash` retained; external-IdP use prohibited |
| CM-05 | Medium | Audit semantic mismatch | `outcome` removed from domain audit contract |
| Table omissions | B | Documentation gap | 40-table catalog + data dictionary added |
| Index count | B | Documentation defect | 28 corrected to 29 |

**Database migration required: No.**
