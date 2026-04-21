# Keycloak UAT Test Users

**Last Updated**: 2026-04-21
**Environment**: UAT — Keycloak Realm `bloodbank`
**Keycloak URL**: `https://auth.uat.bloodbank.internal`

---

## Overview

This document lists the 16 UAT test accounts — one for each role in the BloodBank system. All accounts are provisioned in the `bloodbank` Keycloak realm for the UAT environment.

> **Security Notice**: These credentials are for the UAT environment only. They must **never** be used in production. Rotate all UAT passwords after each UAT cycle.

---

## Test Account Provisioning

All test accounts must be created in Keycloak by a SYSTEM_ADMIN before UAT begins. Use the Keycloak Admin Console at `https://auth.uat.bloodbank.internal/admin`.

### Setup Checklist

- [ ] All 16 accounts created in Keycloak realm `bloodbank`
- [ ] Correct realm role assigned to each account (global roles: SUPER_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN, AUDITOR)
- [ ] Correct client role assigned to each account (client: `bloodbank-app`)
- [ ] `branch_id` claim set in JWT for branch-scoped roles (via Keycloak mapper)
- [ ] `region_id` claim set for REGIONAL_ADMIN
- [ ] MFA configured for admin-tier accounts (SUPER_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN, AUDITOR)
- [ ] All accounts set to require password change on first login = FALSE (UAT convenience)
- [ ] All accounts verified as Active in Keycloak

---

## 16 UAT Test Accounts

### 1. SUPER_ADMIN

| Field | Value |
|---|---|
| **Username** | `uat-super-admin` |
| **Email** | `uat-super-admin@bloodbank.test` |
| **Password** | `UatSuperAdmin!2026` |
| **Keycloak Role Type** | Realm Role |
| **Realm Role** | `SUPER_ADMIN` |
| **Branch** | N/A (Global) |
| **Region** | N/A (Global) |
| **MFA** | Required (TOTP) |
| **TOTP Secret** | `JBSWY3DPEHPK3PXP` (seed for test authenticator) |
| **Scope** | All branches, all regions |

---

### 2. REGIONAL_ADMIN

| Field | Value |
|---|---|
| **Username** | `uat-regional-admin` |
| **Email** | `uat-regional-admin@bloodbank.test` |
| **Password** | `UatRegionalAdmin!2026` |
| **Keycloak Role Type** | Realm Role |
| **Realm Role** | `REGIONAL_ADMIN` |
| **Branch** | N/A |
| **Region** | `North Region` (region_id = `REG-UAT-NORTH`) |
| **MFA** | Required (TOTP) |
| **TOTP Secret** | `JBSWY3DPEHPK3PXQ` |
| **Scope** | All branches in North Region |

---

### 3. SYSTEM_ADMIN

| Field | Value |
|---|---|
| **Username** | `uat-system-admin` |
| **Email** | `uat-system-admin@bloodbank.test` |
| **Password** | `UatSysAdmin!2026` |
| **Keycloak Role Type** | Realm Role |
| **Realm Role** | `SYSTEM_ADMIN` |
| **Branch** | N/A (Global) |
| **Region** | N/A (Global) |
| **MFA** | Required (TOTP) |
| **TOTP Secret** | `JBSWY3DPEHPK3PXR` |
| **Scope** | System configuration, monitoring, feature flags |

---

### 4. AUDITOR

| Field | Value |
|---|---|
| **Username** | `uat-auditor` |
| **Email** | `uat-auditor@bloodbank.test` |
| **Password** | `UatAuditor!2026` |
| **Keycloak Role Type** | Realm Role |
| **Realm Role** | `AUDITOR` |
| **Branch** | N/A (Global read-only) |
| **Region** | N/A (Global read-only) |
| **MFA** | Required (TOTP) |
| **TOTP Secret** | `JBSWY3DPEHPK3PXS` |
| **Scope** | Read-only access to all data globally |

---

### 5. BRANCH_ADMIN

| Field | Value |
|---|---|
| **Username** | `uat-branch-admin` |
| **Email** | `uat-branch-admin@bloodbank.test` |
| **Password** | `UatBranchAdmin!2026` |
| **Keycloak Role Type** | Client Role (`bloodbank-app`) |
| **Client Role** | `BRANCH_ADMIN` |
| **Branch** | `UAT Central Branch` (branch_id = `BR-UAT-CENTRAL`) |
| **Region** | `North Region` |
| **MFA** | Optional |
| **Scope** | Full access within assigned branch |

---

### 6. BRANCH_MANAGER

| Field | Value |
|---|---|
| **Username** | `uat-branch-manager` |
| **Email** | `uat-branch-manager@bloodbank.test` |
| **Password** | `UatBranchManager!2026` |
| **Keycloak Role Type** | Client Role (`bloodbank-app`) |
| **Client Role** | `BRANCH_MANAGER` |
| **Branch** | `UAT Central Branch` (branch_id = `BR-UAT-CENTRAL`) |
| **Region** | `North Region` |
| **MFA** | Optional |
| **Scope** | Operational oversight within assigned branch |

---

### 7. RECEPTIONIST

| Field | Value |
|---|---|
| **Username** | `uat-receptionist` |
| **Email** | `uat-receptionist@bloodbank.test` |
| **Password** | `UatReceptionist!2026` |
| **Keycloak Role Type** | Client Role (`bloodbank-app`) |
| **Client Role** | `RECEPTIONIST` |
| **Branch** | `UAT Central Branch` (branch_id = `BR-UAT-CENTRAL`) |
| **Region** | `North Region` |
| **MFA** | Not required |
| **Scope** | Donor registration, appointments, check-in |

---

### 8. PHLEBOTOMIST

| Field | Value |
|---|---|
| **Username** | `uat-phlebotomist` |
| **Email** | `uat-phlebotomist@bloodbank.test` |
| **Password** | `UatPhlebotomist!2026` |
| **Keycloak Role Type** | Client Role (`bloodbank-app`) |
| **Client Role** | `PHLEBOTOMIST` |
| **Branch** | `UAT Central Branch` (branch_id = `BR-UAT-CENTRAL`) |
| **Region** | `North Region` |
| **MFA** | Not required |
| **Scope** | Blood collection, vitals, adverse reactions |

---

### 9. LAB_TECHNICIAN

| Field | Value |
|---|---|
| **Username** | `uat-lab-technician` |
| **Email** | `uat-lab-technician@bloodbank.test` |
| **Password** | `UatLabTech!2026` |
| **Keycloak Role Type** | Client Role (`bloodbank-app`) |
| **Client Role** | `LAB_TECHNICIAN` |
| **Branch** | `UAT Central Branch` (branch_id = `BR-UAT-CENTRAL`) |
| **Region** | `North Region` |
| **MFA** | Not required |
| **Scope** | Test orders, results, QC, component processing, cross-match |

---

### 10. INVENTORY_MANAGER

| Field | Value |
|---|---|
| **Username** | `uat-inventory-manager` |
| **Email** | `uat-inventory-manager@bloodbank.test` |
| **Password** | `UatInventory!2026` |
| **Keycloak Role Type** | Client Role (`bloodbank-app`) |
| **Client Role** | `INVENTORY_MANAGER` |
| **Branch** | `UAT Central Branch` (branch_id = `BR-UAT-CENTRAL`) |
| **Region** | `North Region` |
| **MFA** | Not required |
| **Scope** | Blood units, storage, transfers, disposals, transport |

---

### 11. DOCTOR

| Field | Value |
|---|---|
| **Username** | `uat-doctor` |
| **Email** | `uat-doctor@bloodbank.test` |
| **Password** | `UatDoctor!2026` |
| **Keycloak Role Type** | Client Role (`bloodbank-app`) |
| **Client Role** | `DOCTOR` |
| **Branch** | `UAT Central Branch` (branch_id = `BR-UAT-CENTRAL`) |
| **Region** | `North Region` |
| **MFA** | Not required |
| **Break-Glass** | Enabled (max 60 min override duration) |
| **Scope** | Cross-match, blood issues, transfusion prescriptions, hemovigilance |

---

### 12. NURSE

| Field | Value |
|---|---|
| **Username** | `uat-nurse` |
| **Email** | `uat-nurse@bloodbank.test` |
| **Password** | `UatNurse!2026` |
| **Keycloak Role Type** | Client Role (`bloodbank-app`) |
| **Client Role** | `NURSE` |
| **Branch** | `UAT Central Branch` (branch_id = `BR-UAT-CENTRAL`) |
| **Region** | `North Region` |
| **MFA** | Not required |
| **Scope** | Transfusion administration, reaction reporting |

---

### 13. BILLING_CLERK

| Field | Value |
|---|---|
| **Username** | `uat-billing-clerk` |
| **Email** | `uat-billing-clerk@bloodbank.test` |
| **Password** | `UatBillingClerk!2026` |
| **Keycloak Role Type** | Client Role (`bloodbank-app`) |
| **Client Role** | `BILLING_CLERK` |
| **Branch** | `UAT Central Branch` (branch_id = `BR-UAT-CENTRAL`) |
| **Region** | `North Region` |
| **MFA** | Not required |
| **Scope** | Rate master, invoices, payments, credit notes |

---

### 14. CAMP_COORDINATOR

| Field | Value |
|---|---|
| **Username** | `uat-camp-coordinator` |
| **Email** | `uat-camp-coordinator@bloodbank.test` |
| **Password** | `UatCampCoord!2026` |
| **Keycloak Role Type** | Client Role (`bloodbank-app`) |
| **Client Role** | `CAMP_COORDINATOR` |
| **Branch** | `UAT Central Branch` (branch_id = `BR-UAT-CENTRAL`) |
| **Region** | `North Region` |
| **MFA** | Not required |
| **Scope** | Blood camp planning, resources, collections |

---

### 15. HOSPITAL_USER

| Field | Value |
|---|---|
| **Username** | `uat-hospital-user` |
| **Email** | `uat-hospital-user@bloodbank.test` |
| **Password** | `UatHospitalUser!2026` |
| **Keycloak Role Type** | Client Role (`bloodbank-app`) |
| **Client Role** | `HOSPITAL_USER` |
| **Hospital** | `UAT General Hospital` (hospital_id = `HOSP-UAT-001`) |
| **Branch** | N/A (Hospital scope) |
| **MFA** | Not required |
| **Portal** | Hospital Portal (`/hospital`) |
| **Scope** | Blood requests, order tracking, feedback |

---

### 16. DONOR

| Field | Value |
|---|---|
| **Username** | `uat-donor` |
| **Email** | `uat-donor@bloodbank.test` |
| **Password** | `UatDonor!2026` |
| **Keycloak Role Type** | Client Role (`bloodbank-app`) |
| **Client Role** | `DONOR` |
| **Donor ID** | `DON-UAT-20260421-001` |
| **Blood Group** | `O+` |
| **Branch** | `UAT Central Branch` (preferred branch) |
| **MFA** | Not required |
| **Portal** | Donor Portal (`/donor`) |
| **Scope** | Own donation history, appointments, donor card, GDPR export |

---

## Keycloak Configuration Reference

### Realm Settings

```yaml
realm: bloodbank
enabled: true
ssoSessionMaxLifespan: 86400        # 24 hours
accessTokenLifespan: 1800           # 30 minutes
refreshTokenMaxReuse: 0
bruteForceProtected: true
failureFactor: 5
waitIncrementSeconds: 60
```

### Client Settings (`bloodbank-app`)

```yaml
clientId: bloodbank-app
clientAuthenticatorType: client-secret
standardFlowEnabled: true
implicitFlowEnabled: false
directAccessGrantsEnabled: false    # No password grant in production
bearerOnly: false
publicClient: false                 # Confidential client
redirectUris:
  - https://uat.bloodbank.internal/*
webOrigins:
  - https://uat.bloodbank.internal
```

### JWT Claim Mappers

The following Keycloak protocol mappers must be configured to add custom claims to the JWT:

| Mapper Name | Claim Name | Value Source | Applies To |
|---|---|---|---|
| Branch ID Mapper | `branch_id` | User attribute `branch_id` | Branch-scoped roles |
| Region ID Mapper | `region_id` | User attribute `region_id` | REGIONAL_ADMIN |
| Hospital ID Mapper | `hospital_id` | User attribute `hospital_id` | HOSPITAL_USER |
| Donor ID Mapper | `donor_id` | User attribute `donor_id` | DONOR |

### Keycloak User Attributes Setup

For each branch-scoped user, set the following user attribute in Keycloak Admin Console:

```
Attribute Key: branch_id
Attribute Value: BR-UAT-CENTRAL
```

For REGIONAL_ADMIN:
```
Attribute Key: region_id
Attribute Value: REG-UAT-NORTH
```

For HOSPITAL_USER:
```
Attribute Key: hospital_id
Attribute Value: HOSP-UAT-001
```

For DONOR:
```
Attribute Key: donor_id
Attribute Value: DON-UAT-20260421-001
```

---

## TOTP Setup for MFA Accounts

For the 4 admin-tier accounts requiring MFA (SUPER_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN, AUDITOR), use a TOTP authenticator (e.g., Google Authenticator, Authy).

Each account has a pre-seeded TOTP secret. To add it to an authenticator app:

1. Open your TOTP app
2. Choose "Add account manually"
3. Enter the account name (e.g., `uat-super-admin`)
4. Enter the TOTP Secret from the table above
5. Confirm the 6-digit code matches what Keycloak expects on login

Alternatively, ask the UAT System Admin to generate a QR code from Keycloak Admin Console for each admin account.

---

## Pre-UAT Data Setup Requirements

Before UAT sessions begin, the following seed data must exist in the UAT database:

| Data Entity | Required Records |
|---|---|
| Branches | `UAT Central Branch`, `UAT Branch B`, `UAT Branch C` |
| Regions | `North Region` with all 3 UAT branches |
| Hospitals | `UAT General Hospital` with active contract |
| Blood Units | At least 50 units across all blood groups, various statuses |
| Donors | At least 20 pre-existing donors including `John Smith` (B+), one deferred donor |
| Collections | At least 10 collections in last 30 days |
| Test Orders | At least 5 pending test orders |
| Pending Approvals | At least 2 pending lab result approvals |
| Disposal Requests | At least 1 pending disposal request |
| Hospital Requests | At least 2 pending hospital blood requests |
| Active Camp | 1 upcoming camp for camp coordinator test |
| Invoices | At least 3 invoices (1 paid, 1 unpaid, 1 partially paid) |
| Cross-Match Requests | At least 1 compatible cross-match result ready |
| SOP Documents | At least 5 SOP documents for various roles |
| Regulatory Frameworks | HIPAA, GDPR, FDA, AABB, WHO frameworks seeded |

---

## Password Policy

UAT passwords follow the BloodBank production password policy:

- Minimum 12 characters
- At least 1 uppercase, 1 lowercase, 1 digit, 1 special character
- Cannot be the same as the username

All UAT passwords comply with this policy.

---

## Account Reset Procedure

If a UAT account is locked out or needs to be reset during a session:

1. Log into Keycloak Admin Console as `keycloak-admin`
2. Navigate to `bloodbank` realm -> `Users`
3. Search for the affected user
4. Click `Credentials` tab
5. Click `Reset Password`, enter the password from the table above, uncheck `Temporary`
6. Click `Manage Consent` if needed and clear sessions
7. Notify the tester that the account is reset

---

*This document is for UAT use only. Do not share credentials outside the UAT team.*
