# 4-Layer Branch Data Isolation Architecture

This document details the BloodBank branch data isolation strategy that ensures multi-branch blood bank operations are fully isolated at every layer of the application stack.

## Overview

BloodBank operates across multiple countries, regions, and branches. Each branch processes sensitive donor and patient data subject to regulatory requirements (HIPAA, GDPR). The system enforces data isolation through **4 independent, layered security mechanisms** — a defense-in-depth approach where any single layer's failure is caught by the remaining layers.

```
                                     REQUEST FLOW
                                          │
                                          ▼
┌──────────────────────────────────────────────────────────────────────┐
│  LAYER 1: API GATEWAY                                                │
│  BranchIdExtractionFilter                                            │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │ 1. Validate JWT token (via Keycloak)                            │ │
│  │ 2. Extract branch_id claim from JWT payload                     │ │
│  │ 3. Set X-Branch-Id header on downstream request                 │ │
│  │ 4. Reject requests without valid branch_id (non-admin roles)    │ │
│  └─────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
                                          │
                                          ▼
┌──────────────────────────────────────────────────────────────────────┐
│  LAYER 2: SPRING SECURITY                                            │
│  @PreAuthorize on every controller method                            │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │ 1. Verify role has permission for this endpoint                  │ │
│  │ 2. For branch-scoped roles: verify user's branch matches        │ │
│  │    the resource's branch                                         │ │
│  │ 3. For admin roles: allow cross-branch access per scope          │ │
│  └─────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
                                          │
                                          ▼
┌──────────────────────────────────────────────────────────────────────┐
│  LAYER 3: JPA DATA FILTERING                                         │
│  BranchDataFilterAspect (AOP) + Hibernate @Filter                    │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │ 1. AOP aspect intercepts all repository method calls             │ │
│  │ 2. Reads branch_id from security context                         │ │
│  │ 3. Enables Hibernate @Filter("branchFilter") with branch_id     │ │
│  │ 4. All SQL queries automatically include:                        │ │
│  │    WHERE branch_id = :branchId                                   │ │
│  └─────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
                                          │
                                          ▼
┌──────────────────────────────────────────────────────────────────────┐
│  LAYER 4: DATABASE                                                   │
│  branch_id column + composite indexes                                │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │ 1. All branch-scoped tables have: branch_id UUID NOT NULL        │ │
│  │ 2. Foreign key constraint: branch_id → branches(id)              │ │
│  │ 3. Composite indexes: (branch_id, <primary_query_columns>)       │ │
│  │ 4. Every row is permanently associated with a branch             │ │
│  └─────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Layer 1: API Gateway — Branch ID Extraction

### Component

`BranchIdExtractionFilter` — a Spring Cloud Gateway `GlobalFilter`.

### Mechanism

1. Every incoming request passes through the API Gateway.
2. The filter validates the JWT token against Keycloak's public key.
3. It extracts the `branch_id` claim from the JWT payload.
4. The `branch_id` is set as the `X-Branch-Id` HTTP header on the proxied request to downstream services.
5. Requests from non-admin roles **without a valid `branch_id`** are rejected with HTTP 403.

### JWT Token Structure

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "realm_access": {
    "roles": ["REGIONAL_ADMIN"]
  },
  "resource_access": {
    "bloodbank-api": {
      "roles": ["BRANCH_MANAGER"]
    }
  },
  "branch_id": "660e8400-e29b-41d4-a716-446655440001",
  "region_id": "770e8400-e29b-41d4-a716-446655440002",
  "groups": ["/regions/north-america/new-york-central"]
}
```

### Admin Role Bypass

| Role | Gateway Behavior |
|---|---|
| SUPER_ADMIN | No branch_id required — global access |
| SYSTEM_ADMIN | No branch_id required — global access |
| AUDITOR | No branch_id required — global read-only access |
| REGIONAL_ADMIN | `region_id` extracted — access to all branches in region |
| All other roles | `branch_id` required — single branch access |

---

## Layer 2: Spring Security — Role-Based Access Control

### Component

`@PreAuthorize` annotation on **every** controller method (enforced by `validate-code-patterns.sh` hook).

### Mechanism

Every REST endpoint declares which roles can access it:

```java
@PostMapping
@PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER','RECEPTIONIST','PHLEBOTOMIST')")
public ResponseEntity<ApiResponse<DonorResponse>> createDonor(
        @Valid @RequestBody DonorCreateRequest request) {
    // ...
}
```

### Branch Ownership Validation

For branch-scoped roles, the security layer also validates that the authenticated user's `branch_id` matches the target resource's `branch_id`. This prevents a user from one branch accessing another branch's data even if they have the correct role.

```java
// Pseudocode — SecurityContext validation
if (isBranchScopedRole(currentUser)) {
    UUID userBranch = currentUser.getBranchId();
    UUID resourceBranch = getResourceBranchId(request);
    if (!userBranch.equals(resourceBranch)) {
        throw new AccessDeniedException("Cross-branch access denied");
    }
}
```

---

## Layer 3: JPA Data Filtering — Hibernate @Filter

### Component

`BranchDataFilterAspect` — a Spring AOP `@Aspect` that intercepts repository calls.

### Entity Configuration

All branch-scoped entities extend `BranchScopedEntity` and declare the Hibernate filter:

```java
@Entity
@Table(name = "donors")
@FilterDef(name = "branchFilter",
    parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class Donor extends BranchScopedEntity {
    // ...
}
```

### AOP Aspect

```java
@Aspect
@Component
public class BranchDataFilterAspect {

    @Before("execution(* com.bloodbank..repository..*.*(..))")
    public void enableBranchFilter(JoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isBranchScopedRole(auth)) {
            UUID branchId = extractBranchId(auth);
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("branchFilter")
                   .setParameter("branchId", branchId);
        }
        // Admin roles: filter NOT enabled → full access
    }
}
```

### Effect

Every SQL query generated by Hibernate automatically includes a `WHERE branch_id = :branchId` clause. This is **transparent to developers** — they write normal repository queries, and the filter ensures branch isolation.

```sql
-- Developer writes:
SELECT d FROM Donor d WHERE d.bloodGroup = :bloodGroup

-- Hibernate generates (with filter enabled):
SELECT d.* FROM donors d
WHERE d.blood_group = ?
  AND d.branch_id = ?    -- ← Added automatically by @Filter
```

---

## Layer 4: Database — Column and Indexes

### Schema Design

All branch-scoped tables include:

```sql
CREATE TABLE donors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id UUID NOT NULL REFERENCES branches(id),
    first_name VARCHAR(100) NOT NULL,
    -- ... other columns ...
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

-- Composite indexes for performance
CREATE INDEX idx_donors_branch_blood_group
    ON donors(branch_id, blood_group);

CREATE INDEX idx_donors_branch_status
    ON donors(branch_id, status);

CREATE INDEX idx_donors_branch_created
    ON donors(branch_id, created_at);
```

### Properties

- `branch_id` is `NOT NULL` — every row must belong to a branch.
- Foreign key to `branches(id)` — prevents orphan records.
- Composite indexes on `(branch_id, ...)` — filtered queries perform full index scans, not table scans.

---

## Branch-Scoped vs Global Tables

| Scope | Description | Examples |
|---|---|---|
| **Branch-scoped** | Include `branch_id` column; filtered by branch | donors, collections, blood_units, blood_components, crossmatch_requests, transfusions, invoices, hospital_requests |
| **Global** | No `branch_id`; accessible system-wide | blood_groups, component_types, countries, regions, cities, notification_templates, regulatory_frameworks, system_settings |

---

## Failure Mode Analysis

| Layer Failed | Remaining Protection |
|---|---|
| Layer 1 (Gateway) | Security (@PreAuthorize) blocks unauthorized roles; JPA filter blocks wrong branch; DB indexes constrain queries |
| Layer 2 (Security) | Gateway already extracted branch_id; JPA filter adds WHERE clause; DB has branch_id constraint |
| Layer 3 (JPA Filter) | Gateway and Security already validated branch; DB queries still filter by branch_id if properly parameterized |
| Layer 4 (DB indexes) | Only performance impact; data integrity maintained by NOT NULL + FK; upper layers still enforce isolation |
| Layers 1 + 2 | JPA filter + DB constraint prevent cross-branch data access |
| Layers 1 + 3 | Security blocks unauthorized access; DB has branch_id constraint |
| Layers 2 + 3 | Gateway extracted branch_id; DB enforces NOT NULL constraint |

**No single layer failure exposes cross-branch data.** Even a two-layer failure is mitigated by the remaining two layers.

---

## Testing Strategy

### Layer 1 Tests

- Integration tests for `BranchIdExtractionFilter` with valid/invalid/missing JWT tokens.
- Verify `X-Branch-Id` header is set correctly.
- Verify 403 response for non-admin roles without `branch_id`.

### Layer 2 Tests

- `@WebMvcTest` with `@WithMockUser` for each role.
- Verify 403 for unauthorized roles.
- Verify cross-branch access is denied for branch-scoped roles.

### Layer 3 Tests

- Integration tests with Testcontainers (PostgreSQL).
- Verify queries return only data for the authenticated branch.
- Verify admin roles see all branches.

### Layer 4 Tests

- Database migration tests verify `NOT NULL` constraint on `branch_id`.
- Verify FK constraint prevents invalid `branch_id` values.
- Verify composite indexes exist via SQL inspection.

---

## References

- [ADR-004: 4-Layer Branch Data Isolation](../architecture/adr/ADR-004-branch-data-isolation.md)
- [RBAC Matrix](rbac-matrix.md)
- CLAUDE.md — Architecture Rules → Branch Data Isolation (4 Layers)
- HIPAA Security Rule — Access Controls (§ 164.312(a))
- GDPR Article 25 — Data Protection by Design and by Default
