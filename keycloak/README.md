# Keycloak Configuration — BloodBank

This directory contains all Keycloak configuration for the BloodBank realm, covering authentication, authorisation, LDAP federation, MFA policies, session policies, and dev test users.

---

## Files

| File | Purpose |
|---|---|
| `realm-export.json` | Full realm export — import this into Keycloak to bootstrap the `bloodbank` realm |
| `README.md` | This file — setup guide and policy reference |

---

## Quick Start (Local Dev)

The `docker-compose.yml` at the project root mounts `realm-export.json` and imports it automatically on first start:

```bash
docker-compose up -d keycloak
```

Keycloak is available at **http://localhost:8180**.  
Admin console: **http://localhost:8180/admin** (admin / admin).  
Realm: **http://localhost:8180/realms/bloodbank**.

---

## Clients

### `bloodbank-api` — Backend API (Confidential)

| Setting | Value |
|---|---|
| Client type | Confidential (server-to-server) |
| Service accounts | Enabled |
| Client secret | `bloodbank-api-secret-change-in-production` |
| Redirect URIs | `http://localhost:8080/*` |
| PKCE | Not required (machine-to-machine) |

**Change the client secret before deploying to any non-dev environment.**

### `bloodbank-ui` — Angular SPA (Public + PKCE)

| Setting | Value |
|---|---|
| Client type | Public (no secret) |
| PKCE method | S256 |
| Redirect URIs | `http://localhost:4200/*`, `https://bloodbank.example.com/*` |
| Web origins | `http://localhost:4200`, `https://bloodbank.example.com` |
| Post-logout redirect | `http://localhost:4200/*` |

PKCE is enforced via `pkce.code.challenge.method=S256` in the client attributes.

---

## Roles

### Realm Roles (platform-wide)

| Role | Description | MFA | Session Policy |
|---|---|---|---|
| `SUPER_ADMIN` | Full system access | **Required** | Idle 15 min / Max 8 h |
| `REGIONAL_ADMIN` | Multi-branch regional management | **Required** | Idle 15 min / Max 8 h |
| `SYSTEM_ADMIN` | Infrastructure and configuration | **Required** | Idle 15 min / Max 8 h |
| `AUDITOR` | Read-only compliance auditing | **Required** | Idle 15 min / Max 8 h |

### Client Roles on `bloodbank-api` (branch-scoped operations)

| Role | Description | MFA | Session Policy |
|---|---|---|---|
| `BRANCH_ADMIN` | Day-to-day branch administration | **Required** | Idle 15 min / Max 8 h |
| `BRANCH_MANAGER` | Clinical and operational oversight | Optional | Idle 30 min / Max 12 h |
| `DOCTOR` | Clinical assessments and approvals | Optional | Idle 30 min / Max 12 h |
| `LAB_TECHNICIAN` | Blood testing and QC | Optional | Idle 30 min / Max 12 h |
| `PHLEBOTOMIST` | Blood collection procedures | None | Idle 30 min / Max 12 h |
| `NURSE` | Clinical procedures and donor care | Optional | Idle 30 min / Max 12 h |
| `INVENTORY_MANAGER` | Blood unit stock management | None | Idle 30 min / Max 12 h |
| `BILLING_CLERK` | Invoices and payment processing | None | Idle 30 min / Max 12 h |
| `CAMP_COORDINATOR` | Donation camp organisation | None | Idle 30 min / Max 12 h |
| `RECEPTIONIST` | Donor registration and appointments | None | Idle 30 min / Max 12 h |
| `HOSPITAL_USER` | Hospital portal — blood requests | None | Idle 60 min / Max 24 h |
| `DONOR` | Donor self-service portal | None | Idle 60 min / Max 24 h |

---

## Group Hierarchy

```
/global                                  ← super-admin / global operations
/regions
  /regions/north-india
    /regions/north-india/delhi-branch    ← branch_id: bb000000-...-0001
    /regions/north-india/punjab-branch   ← branch_id: bb000000-...-0002
  /regions/south-india
    /regions/south-india/chennai-branch  ← branch_id: bb000000-...-0003
    /regions/south-india/bangalore-branch ← branch_id: bb000000-...-0004
  /regions/west-india
    /regions/west-india/mumbai-branch    ← branch_id: bb000000-...-0005
/hospitals
  /hospitals/city-general-hospital
  /hospitals/apollo-hospital
  /hospitals/fortis-hospital
/mfa
  /mfa/mfa-required                      ← users in this group must complete OTP
  /mfa/mfa-optional                      ← OTP prompted only if already configured
```

Each branch group carries a `branch_id` attribute that is propagated to the JWT via the `branch-id-mapper` protocol mapper.

---

## Custom JWT Mapper — `branch_id` Claim

The `branch-id-mapper` protocol mapper (configured on both clients) reads the user attribute `branch_id` and injects it into the access token, ID token, and userinfo endpoint.

**Token payload example:**

```json
{
  "sub": "uu000009-0000-0000-0000-000000000005",
  "preferred_username": "branch_admin",
  "email": "branch.admin@bloodbank.internal",
  "branch_id": "bb000000-0000-0000-0000-000000000001",
  "roles": ["BRANCH_ADMIN"],
  "realm_roles": ["default-roles-bloodbank"]
}
```

The `branch_id` is used by the API Gateway's `BranchIdExtractionFilter` to set the tenant context on every downstream request.

---

## Token Lifetimes

| Token | Lifetime | Notes |
|---|---|---|
| Access token | **5 minutes** | Short-lived; refresh frequently |
| SSO session idle | **30 minutes** | Refresh token idle expiry |
| SSO session max | **12 hours** | Clinical staff default |
| Offline session idle | 30 days | For mobile / offline use |

### Session Policy by Role Category

Because Keycloak applies session timeouts at realm or client scope (not per-role natively), the following conventions are adopted:

| Category | Roles | Idle | Max |
|---|---|---|---|
| **Admin** | SUPER_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN, AUDITOR, BRANCH_ADMIN | 15 min | 8 h |
| **Clinical** | BRANCH_MANAGER, DOCTOR, LAB_TECHNICIAN, PHLEBOTOMIST, NURSE, INVENTORY_MANAGER, BILLING_CLERK, CAMP_COORDINATOR, RECEPTIONIST | 30 min | 12 h |
| **Portal** | HOSPITAL_USER, DONOR | 60 min | 24 h |

**Implementation approach:**
- Realm defaults are set to clinical values (idle 30 min, max 12 h).
- For admin users, set a shorter `SSO Session Idle Override` on the `bloodbank-ui` client, or enforce short access token (5 min) combined with refresh rotation.
- For portal users (HOSPITAL_USER, DONOR), extend the SSO session max on the `bloodbank-ui` client to 24 h via `ssoSessionMaxLifespanOverride`.
- Fine-grained per-role session control can be implemented via Keycloak's **Client Policies** (available in Keycloak 19+).

---

## Password Policy

Configured on the realm:

```
length(12) AND upperCase(1) AND lowerCase(1) AND digits(1) AND specialChars(1) AND passwordHistory(5) AND forceExpiredPasswordChange(90)
```

| Constraint | Value |
|---|---|
| Minimum length | 12 characters |
| Uppercase letters | ≥ 1 |
| Lowercase letters | ≥ 1 |
| Digits | ≥ 1 |
| Special characters | ≥ 1 |
| Password history | Last 5 passwords blocked |
| Expiry | 90 days |

---

## MFA Policies

MFA is implemented via a custom browser authentication flow (`bloodbank-browser`) that contains two conditional sub-flows driven by user attributes:

### Flow: `bloodbank-browser`
```
bloodbank-browser (top-level)
├─ auth-cookie                        [ALTERNATIVE]
├─ identity-provider-redirector       [ALTERNATIVE]
└─ bloodbank-browser-forms            [ALTERNATIVE]
   ├─ auth-username-password-form     [REQUIRED]
   ├─ bloodbank-mfa-required          [CONDITIONAL]
   │   ├─ condition-user-attribute (mfa_required=true)  [REQUIRED]
   │   └─ auth-otp-form              [REQUIRED]
   └─ bloodbank-mfa-optional          [CONDITIONAL]
       ├─ condition-user-attribute (mfa_optional=true)  [REQUIRED]
       ├─ conditional-user-configured                   [REQUIRED]
       └─ auth-otp-form              [REQUIRED]
```

### User Attribute Assignment

| User Attribute | Value | Applied To |
|---|---|---|
| `mfa_required` | `true` | SUPER_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN, AUDITOR, BRANCH_ADMIN |
| `mfa_optional` | `true` | DOCTOR, LAB_TECHNICIAN, NURSE |
| _(neither)_ | — | BRANCH_MANAGER, PHLEBOTOMIST, INVENTORY_MANAGER, BILLING_CLERK, CAMP_COORDINATOR, RECEPTIONIST, HOSPITAL_USER, DONOR |

Set these attributes on users via the admin console or LDAP mapper.

### OTP Policy

| Setting | Value |
|---|---|
| Algorithm | TOTP (Time-based) / HmacSHA1 |
| Digits | 6 |
| Period | 30 seconds |
| Look-ahead window | 1 |
| Supported apps | Google Authenticator, Microsoft Authenticator, FreeOTP |

---

## LDAP Federation

The LDAP federation provider is pre-configured in `realm-export.json` under `components.org.keycloak.storage.UserStorageProvider` with the following settings.

> **Note:** LDAP federation is a production integration — it is not used in local development. When running locally via `docker-compose`, Keycloak will import the realm but the LDAP provider will be disabled until a real LDAP/AD server is reachable at `ldaps://ldap.bloodbank.internal:636`. Add an entry to `/etc/hosts` pointing that hostname at your directory server, or disable the LDAP component in the admin console for local work.

| Setting | Value |
|---|---|
| Provider | LDAP |
| Edit mode | READ_ONLY |
| Connection URL | `ldaps://ldap.bloodbank.internal:636` |
| Users DN | `ou=users,dc=bloodbank,dc=internal` |
| Bind DN | `cn=keycloak,ou=service-accounts,dc=bloodbank,dc=internal` |
| Bind credential | `**CHANGE_IN_PRODUCTION**` |
| Changed sync period | **900 seconds (15 minutes)** |
| Full sync period | 86400 seconds (24 hours) |
| Batch size | 1000 |
| TLS | LDAPS on port 636 |
| Trust store | `ldapsOnly` (use Keycloak's trust store for LDAP only) |

### LDAP Attribute Mappers

| LDAP Attribute | Keycloak Attribute |
|---|---|
| `uid` | username |
| `mail` | email |
| `givenName` | firstName |
| `sn` | lastName |
| `bloodbankBranchId` | branch_id (custom) |
| `member` / groups | Keycloak groups (READ_ONLY) |

### Pre-requisites for LDAP in production

1. Add the LDAP server's CA certificate to the Keycloak trust store:
   ```bash
   keytool -import -alias ldap-ca -file ldap-ca.crt \
     -keystore /opt/keycloak/conf/truststore.jks \
     -storepass changeit
   ```
2. Set `KC_SPI_TRUSTSTORE_FILE_FILE` and `KC_SPI_TRUSTSTORE_FILE_PASSWORD` env vars.
3. Replace `**CHANGE_IN_PRODUCTION**` with the actual service account password (use a Kubernetes Secret).
4. Extend the LDAP schema to include the `bloodbankBranchId` attribute.

---

## Test Users (Dev Only)

All test users share the password format below. **Never use these in staging or production.**

| Username | Password | Role(s) | branch_id |
|---|---|---|---|
| `super_admin` | `Admin@1234!` | SUPER_ADMIN | — |
| `regional_admin` | `Admin@1234!` | REGIONAL_ADMIN | — |
| `system_admin` | `Admin@1234!` | SYSTEM_ADMIN | — |
| `auditor` | `Admin@1234!` | AUDITOR | — |
| `branch_admin` | `Admin@1234!` | BRANCH_ADMIN | delhi-branch |
| `branch_manager` | `Admin@1234!` | BRANCH_MANAGER | delhi-branch |
| `doctor` | `Admin@1234!` | DOCTOR | delhi-branch |
| `lab_tech` | `Admin@1234!` | LAB_TECHNICIAN | delhi-branch |
| `phlebotomist` | `Admin@1234!` | PHLEBOTOMIST | delhi-branch |
| `nurse` | `Admin@1234!` | NURSE | delhi-branch |
| `inventory_mgr` | `Admin@1234!` | INVENTORY_MANAGER | delhi-branch |
| `billing_clerk` | `Admin@1234!` | BILLING_CLERK | delhi-branch |
| `camp_coord` | `Admin@1234!` | CAMP_COORDINATOR | delhi-branch |
| `receptionist` | `Admin@1234!` | RECEPTIONIST | delhi-branch |
| `hospital_user` | `Portal@1234!` | HOSPITAL_USER | — |
| `donor_user` | `Donor@1234!` | DONOR | delhi-branch |

---

## Brute-Force Protection

| Setting | Value |
|---|---|
| Enabled | Yes |
| Max login failures | 5 |
| Wait increment | 60 seconds |
| Max wait | 15 minutes (900 s) |
| Quick login check | 1 second |
| Max delta time | 12 hours |
| Permanent lockout | No |

---

## Production Checklist

Before going to production:

- [ ] Replace `bloodbank-api-secret-change-in-production` with a strong random secret
- [ ] Replace `**CHANGE_IN_PRODUCTION**` LDAP bind credential with a Kubernetes Secret reference
- [ ] Set `sslRequired` to `"all"` (currently `"external"`)
- [ ] Configure a real SMTP server in `smtpServer`
- [ ] Enable SSL on PostgreSQL (`KC_DB_URL` with `sslmode=require`)
- [ ] Remove all test users from the realm export (or use a separate dev-only export)
- [ ] Set `KC_HOSTNAME` and `KC_HTTPS_CERTIFICATE_FILE` for production HTTPS
- [ ] Review and tighten `redirectUris` and `webOrigins` for each client
- [ ] Enable Keycloak's external database pool sizing (`KC_DB_POOL_*`)
- [ ] Configure Keycloak clustering for HA (`KC_CACHE_STACK=kubernetes`)
- [ ] Rotate realm keys after initial setup

---

## Importing the Realm Manually

If automatic import is not available, import via the admin REST API:

```bash
# Obtain admin token
TOKEN=$(curl -s -X POST http://localhost:8180/realms/master/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=admin-cli&username=admin&password=admin&grant_type=password" \
  | jq -r '.access_token')

# Import realm
curl -s -X POST http://localhost:8180/admin/realms \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d @keycloak/realm-export.json
```

Or use the Keycloak Admin UI: **Realm Settings → Action → Partial Import**.
