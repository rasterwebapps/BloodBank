# ADR-004: 4-Layer Branch Data Isolation

**Status:** Accepted
**Date:** 2026-04-04
**Decision Makers:** Architecture Team

## Context

BloodBank operates across multiple countries, regions, and branches. Each branch handles sensitive patient and donor data. Regulatory requirements (HIPAA, GDPR) demand that:

- Branch staff can only access data belonging to their own branch.
- Regional admins can see data across branches in their assigned region.
- Super admins and auditors can access data globally.
- No single point of failure in the isolation mechanism — defense in depth is required.

A single-layer approach (e.g., only application-level filtering) is insufficient for healthcare compliance. A breach at any single layer would expose cross-branch data.

## Decision

Branch data isolation is enforced through **4 independent layers**, each providing defense in depth:

### Layer 1: API Gateway — Branch ID Extraction

The Spring Cloud Gateway runs a `BranchIdExtractionFilter` that:

1. Validates the JWT token (via Keycloak).
2. Extracts the `branch_id` claim from the token payload.
3. Sets the `X-Branch-Id` HTTP header on the downstream request.
4. Rejects requests without a valid `branch_id` (except for super/system admins).

### Layer 2: Spring Security — Role-Based Access Control

Every controller method has a `@PreAuthorize` annotation specifying which roles can invoke it:

```java
@PreAuthorize("hasAnyRole('BRANCH_ADMIN','BRANCH_MANAGER','PHLEBOTOMIST')")
public ResponseEntity<ApiResponse<DonorResponse>> createDonor(...) { }
```

The security context also validates that the authenticated user's branch matches the requested resource's branch (for branch-scoped roles).

### Layer 3: JPA Data Filtering — Hibernate @Filter

A `BranchDataFilterAspect` (AOP aspect) automatically enables the Hibernate `@Filter` on all repository queries:

- All branch-scoped entities declare `@FilterDef(name = "branchFilter")` and `@Filter(name = "branchFilter", condition = "branch_id = :branchId")`.
- The aspect reads the `branch_id` from the security context and enables the filter before any query executes.
- This ensures that even if application code forgets a `WHERE branch_id = ?` clause, the Hibernate filter adds it automatically.

### Layer 4: Database — Column + Composite Indexes

All branch-scoped tables include:

- A `branch_id UUID NOT NULL` column with a foreign key to `branches(id)`.
- Composite indexes on `(branch_id, <primary_query_columns>)` for query performance.
- The `branch_id` column is **not nullable** — every row must belong to a branch.

### Scope by Role

| Role | Branch Scope |
|---|---|
| SUPER_ADMIN | All branches (filter disabled) |
| REGIONAL_ADMIN | All branches in assigned region |
| SYSTEM_ADMIN | All branches (filter disabled) |
| AUDITOR | All branches (read-only, filter disabled) |
| Branch-level roles | Own branch only |
| HOSPITAL_USER | Linked hospital's requests only |
| DONOR | Own data only |

## Consequences

### Positive

- **Defense in depth** — A bug or misconfiguration at any single layer cannot expose cross-branch data because the other 3 layers still enforce isolation.
- **Transparent to developers** — The AOP aspect and Hibernate filter handle branch filtering automatically. Developers do not need to add `WHERE branch_id = ?` to every query.
- **Audit-friendly** — Each layer is independently testable and auditable for compliance (HIPAA, GDPR).
- **Performance** — Composite indexes on `branch_id` ensure filtered queries are fast. The filter adds negligible overhead.

### Negative

- **Complexity** — Four layers require consistent configuration. Mitigated by: automated validation hooks (`validate-code-patterns.sh` checks for `@Filter` on entities), integration tests per layer.
- **Global queries for admin roles** — Super admins and auditors bypass the branch filter, which requires careful implementation to avoid accidentally showing filtered data. Mitigated by explicit role checks in the AOP aspect.

### Alternatives Considered

| Alternative | Reason Rejected |
|---|---|
| Row-Level Security (PostgreSQL RLS) | Requires database-level session variables per request; harder to test; less visible to application-level auditing |
| Separate schema per branch | Massive schema duplication; Flyway must run per schema; joins across branches become complex |
| Separate database per branch | Same issues as database-per-service (ADR-001); operational burden at scale |
| Application-only filtering | Single layer — a missed WHERE clause exposes cross-branch data; unacceptable for healthcare |

## References

- CLAUDE.md — Architecture Rules → Branch Data Isolation (4 Layers)
- README.md — 4-Layer Branch Data Isolation table
- HIPAA Security Rule — Access Controls (§ 164.312(a))
- GDPR Article 25 — Data Protection by Design and by Default
