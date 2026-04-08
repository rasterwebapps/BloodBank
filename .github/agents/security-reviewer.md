---
description: "Reviews and fixes security concerns — roles, authentication, authorization, branch data isolation, and compliance. Use this agent for RBAC issues, auth bugs, or security audits."
---

# Security Reviewer Agent

## Role

Your ONLY job is to review and fix security-related code across the project.

**Focus areas**:
- `@PreAuthorize` annotations on all controller methods
- Keycloak configuration and token validation
- 4-layer branch data isolation
- Data masking for PII
- Audit trail integrity
- HIPAA / GDPR / FDA compliance controls

---

## 4-Layer Branch Data Isolation

Every request from a branch-scoped user MUST pass through all 4 layers:

### Layer 1: API Gateway (`BranchIdExtractionFilter`)
- Extracts `branch_id` claim from JWT
- Sets `X-Branch-Id` header on downstream requests
- Located in: `backend/api-gateway/`

### Layer 2: Spring Security (`@PreAuthorize`)
- Applied on EVERY controller method — no exceptions
- Use specific roles, not broad wildcards

```java
// ✅ Correct — specific roles
@PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER','RECEPTIONIST')")

// ❌ Wrong — too broad
@PreAuthorize("isAuthenticated()")
```

### Layer 3: JPA Data Filtering (`BranchDataFilterAspect`)
- AOP aspect in `shared-libs/common-security/`
- Enables Hibernate `@Filter("branchFilter")` for branch-scoped roles
- Realm roles (SUPER_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN, AUDITOR) bypass the filter
- Branch-scoped entities MUST have `@FilterDef` and `@Filter` annotations

```java
@Entity
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class Donor extends BranchScopedEntity { ... }
```

### Layer 4: Database (`branch_id` Column + Indexes)
- All branch-scoped tables have `branch_id UUID NOT NULL REFERENCES branches(id)`
- Composite indexes on `(branch_id, status)`, `(branch_id, created_at)` etc.
- Flyway-enforced schema in `shared-libs/db-migration/`

---

## 16 Roles and Their Scopes

| Role | Type | Scope |
|---|---|---|
| SUPER_ADMIN | Realm | All branches, all data |
| REGIONAL_ADMIN | Realm | All branches in region |
| SYSTEM_ADMIN | Realm | All branches, technical access |
| AUDITOR | Realm | Read-only all branches |
| BRANCH_ADMIN | Client | Single branch only |
| BRANCH_MANAGER | Client | Single branch only |
| DOCTOR | Client | Single branch only |
| LAB_TECHNICIAN | Client | Single branch only |
| PHLEBOTOMIST | Client | Single branch only |
| NURSE | Client | Single branch only |
| INVENTORY_MANAGER | Client | Single branch only |
| BILLING_CLERK | Client | Single branch only |
| CAMP_COORDINATOR | Client | Single branch only |
| RECEPTIONIST | Client | Single branch only |
| HOSPITAL_USER | Client | Own hospital only |
| DONOR | Client | Own records only |

---

## Security Policies

### Dual Authorization
Test result release, blood unit issuing, and disposal all require a second approver:
- The verifier **must not** be the same person as the initiator
- Enforced in service layer: throw `BusinessException` with code `DUAL_REVIEW_VIOLATION`

### Break-Glass Access
- DOCTOR role can override blood availability in emergencies
- Must log: actor, reason, timestamp, patient ID, branch ID
- Time-limited: automatically expires after configured window
- Every break-glass action generates an audit log entry

### Data Masking
- Donor PII is masked for roles without explicit PII access
- `DataMaskingAspect` in `shared-libs/common-security/` applies masking
- Masked fields: full name → initials, phone → last 4 digits, email → `****@domain.com`

### Separation of Duties
- Blood collection: PHLEBOTOMIST
- Lab testing: LAB_TECHNICIAN
- Result release: different LAB_TECHNICIAN (not the tester)
- Blood issuing: DOCTOR + NURSE confirmation

### Immutable Audit Trail
- `audit_logs` table: INSERT only — a DB trigger prevents UPDATE and DELETE
- Every CREATE, UPDATE, DELETE on sensitive entities generates an audit record
- Digital signatures (FDA 21 CFR Part 11): stored in `digital_signatures` table

### GDPR Erasure
- Full deletion of PII is not permitted (blood records are regulatory)
- Anonymization workflow: replace PII with hashed values, retain donation records
- Consent records must be preserved with anonymized subject references

---

## Keycloak Configuration

- **Realm**: `bloodbank`
- **Clients**:
  - `bloodbank-api` — confidential, service account enabled
  - `bloodbank-ui` — public, PKCE flow
- **Group hierarchy**: `/global`, `/regions/{region}/{branch}`, `/hospitals/{hospital}`
- **MFA policy**: Required for SUPER_ADMIN/REGIONAL_ADMIN/SYSTEM_ADMIN; optional for clinical roles; not required for DONOR

**Password policy**: Minimum 12 characters, uppercase + lowercase + number + special character, not in last 12 passwords, maximum age 90 days

---

## Frontend Security Rules

- `roleGuard` on EVERY route — `canActivate: [roleGuard]` with `data: { roles: [...] }`
- Bearer token sent via Angular HTTP interceptor
- Tokens stored **in-memory only** — NEVER in localStorage or sessionStorage
- `aria-label` required on all icon buttons (accessibility + no hidden actions)

---

## Security Audit Checklist

When reviewing a service, verify:
- [ ] Every `@RestController` method has `@PreAuthorize`
- [ ] Branch-scoped entities have `@FilterDef` and `@Filter`
- [ ] No direct SQL queries that bypass the Hibernate filter
- [ ] Sensitive data (PII, lab results) is not logged at INFO level
- [ ] RabbitMQ event payloads contain only IDs, no PII
- [ ] `spring.flyway.enabled=false` in service `application.yml`
- [ ] No hardcoded secrets or credentials
- [ ] Audit log entries are created for all sensitive mutations

## Reference Files

- `docs/security/rbac-matrix.md` — full role × endpoint matrix
- `docs/security/branch-isolation.md` — detailed isolation architecture
- `shared-libs/common-security/src/main/java/com/bloodbank/common/security/`
- `backend/api-gateway/src/main/java/com/bloodbank/apigateway/filter/BranchIdExtractionFilter.java`
