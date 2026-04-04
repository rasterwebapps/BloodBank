# ADR-006: Keycloak Single Realm with Group Hierarchy for Branches

**Status:** Accepted
**Date:** 2026-04-04
**Decision Makers:** Architecture Team

## Context

BloodBank operates across multiple countries, regions, and branches. It requires:

- 16 distinct user roles with different access levels (4 realm roles, 12 client roles).
- Branch-level data isolation — users should only access data from their assigned branch.
- LDAP/Active Directory federation for enterprise identity management.
- Multi-factor authentication (MFA) for administrative and clinical roles.
- SSO across all portals (Staff, Hospital, Donor).

The key question is whether to use **one Keycloak realm** (with group-based branch separation) or **multiple realms** (one per branch or region).

## Decision

Use a **single Keycloak realm** (`bloodbank`) with a **group hierarchy** to represent the organizational structure.

### Realm Configuration

| Setting | Value |
|---|---|
| Realm | `bloodbank` |
| Confidential Client | `bloodbank-api` (server-to-server) |
| Public Client | `bloodbank-ui` (browser, PKCE flow) |
| Token Format | JWT (RS256) |
| Access Token Lifetime | 5 minutes |
| Refresh Token Lifetime | 30 minutes |

### Group Hierarchy

```
/global
    /super-admins
    /system-admins
    /auditors
/regions
    /{region-name}
        /{branch-name}
            /branch-admins
            /branch-managers
            /doctors
            /lab-technicians
            /phlebotomists
            /nurses
            /inventory-managers
            /billing-clerks
            /camp-coordinators
            /receptionists
/hospitals
    /{hospital-name}
        /hospital-users
/donors
```

### Role Mapping

| # | Role | Type | Scope | MFA |
|---|---|---|---|---|
| 1 | SUPER_ADMIN | Realm | Global | Required |
| 2 | REGIONAL_ADMIN | Realm | Region | Required |
| 3 | SYSTEM_ADMIN | Realm | Global | Required |
| 4 | AUDITOR | Realm | Global | Required |
| 5 | BRANCH_ADMIN | Client | Branch | Required |
| 6 | BRANCH_MANAGER | Client | Branch | Required |
| 7 | DOCTOR | Client | Branch | Optional |
| 8 | LAB_TECHNICIAN | Client | Branch | Optional |
| 9 | PHLEBOTOMIST | Client | Branch | Optional |
| 10 | NURSE | Client | Branch | Optional |
| 11 | INVENTORY_MANAGER | Client | Branch | Optional |
| 12 | BILLING_CLERK | Client | Branch | Optional |
| 13 | CAMP_COORDINATOR | Client | Branch | Optional |
| 14 | RECEPTIONIST | Client | Branch | Optional |
| 15 | HOSPITAL_USER | Client | Hospital | Not required |
| 16 | DONOR | Client | Self | Not required |

### JWT Claims

The JWT token includes:

```json
{
  "sub": "user-uuid",
  "realm_access": { "roles": ["REGIONAL_ADMIN"] },
  "resource_access": { "bloodbank-api": { "roles": ["BRANCH_MANAGER"] } },
  "branch_id": "branch-uuid",
  "region_id": "region-uuid",
  "groups": ["/regions/north-america/new-york-central"]
}
```

### LDAP Federation

| Setting | Value |
|---|---|
| Mode | READ_ONLY |
| Protocol | LDAPS (port 636) |
| Full Sync | Daily |
| Changed User Sync | Every 5 minutes |

### Session Policies

| Role Category | Idle Timeout | Max Session |
|---|---|---|
| Admin roles | 30 minutes | 8 hours |
| Clinical roles | 60 minutes | 12 hours |
| Donor / Hospital | — | 24 hours |

### Password Policy

- Minimum 12 characters
- Must include uppercase, lowercase, number, and special character
- Cannot reuse last 12 passwords
- Maximum age: 90 days

## Consequences

### Positive

- **Single SSO domain** — All users (staff, hospitals, donors) authenticate against one realm. One login page, one token issuer.
- **Simpler LDAP** — One federation connection per LDAP/AD server, not one per branch.
- **Centralized role management** — All 16 roles defined once. Group membership determines branch/region scope.
- **JWT branch_id** — The branch is embedded in the token, enabling Layer 1 of the 4-layer branch isolation (ADR-004).
- **Simpler upgrades** — One realm to migrate during Keycloak version upgrades.

### Negative

- **Group hierarchy complexity** — Adding a new branch requires creating groups and assigning roles. Mitigated by: Keycloak admin API automation scripts.
- **Token size** — Group paths and roles increase JWT size. Mitigated by: limiting included claims to essential fields only.
- **Blast radius** — A misconfiguration in the single realm affects all users. Mitigated by: realm export/import for backup, staging environment for testing changes.

### Alternatives Considered

| Alternative | Reason Rejected |
|---|---|
| Realm per branch | Extreme administrative overhead; no SSO between branches; LDAP federation must be configured per realm |
| Realm per region | Moderate overhead; cross-region operations (transfers, emergency requests) require cross-realm token exchange |
| External IdP (Auth0, Okta) | Vendor lock-in; Keycloak is open-source and self-hosted, matching the healthcare data residency requirements |
| Spring Security (no Keycloak) | No centralized identity management, LDAP federation, MFA, or SSO capabilities out of the box |

## References

- CLAUDE.md — 16 User Roles
- README.md — Keycloak Configuration
- ADR-004 — 4-Layer Branch Data Isolation (Layer 1 depends on JWT `branch_id`)
