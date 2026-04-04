# RBAC Matrix — 16 Roles × API Endpoints

This document defines the Role-Based Access Control (RBAC) matrix for all BloodBank API endpoints across 14 services and 16 user roles.

## Role Legend

| # | Role | Abbreviation | Scope |
|---|---|---|---|
| 1 | SUPER_ADMIN | SA | Global |
| 2 | REGIONAL_ADMIN | RA | Region |
| 3 | SYSTEM_ADMIN | SYA | Global |
| 4 | AUDITOR | AUD | Global (read-only) |
| 5 | BRANCH_ADMIN | BA | Branch |
| 6 | BRANCH_MANAGER | BM | Branch |
| 7 | DOCTOR | DOC | Branch |
| 8 | LAB_TECHNICIAN | LAB | Branch |
| 9 | PHLEBOTOMIST | PHL | Branch |
| 10 | NURSE | NUR | Branch |
| 11 | INVENTORY_MANAGER | INV | Branch |
| 12 | BILLING_CLERK | BIL | Branch |
| 13 | CAMP_COORDINATOR | CAM | Branch |
| 14 | RECEPTIONIST | REC | Branch |
| 15 | HOSPITAL_USER | HOS | Hospital |
| 16 | DONOR | DON | Self |

**Access Key:** ✅ = Full access | 📖 = Read-only | ❌ = No access

---

## Donor Management (`/api/v1/donors`)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `POST /donors` | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ✅¹ |
| `GET /donors` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | 📖 | 📖 | ❌ | ❌ | 📖 | 📖 | ❌ | ❌ |
| `GET /donors/{id}` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | 📖 | 📖 | ❌ | ❌ | 📖 | 📖 | ❌ | ✅² |
| `PUT /donors/{id}` | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ✅² |
| `GET /donors/{id}/health-records` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | 📖 | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ | ✅² |
| `POST /donors/{id}/health-records` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /donors/{id}/deferrals` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | 📖 | ❌ | ❌ | ❌ | ❌ | 📖 | ❌ | ✅² |
| `POST /donors/{id}/deferrals` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

> ¹ Self-registration only. ² Own data only.

---

## Blood Collection (`/api/v1/collections`)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `POST /collections` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| `GET /collections` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | 📖 | ❌ | 📖 | ❌ | 📖 | ❌ | ❌ | ❌ |
| `GET /collections/{id}` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | 📖 | ❌ | 📖 | ❌ | 📖 | ❌ | ❌ | ❌ |
| `PUT /collections/{id}` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `POST /collections/{id}/adverse-reactions` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## Blood Camp Management (`/api/v1/blood-camps`)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `POST /blood-camps` | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| `GET /blood-camps` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | ❌ | ❌ | 📖 | ❌ | ❌ | ❌ | 📖 | ❌ | ❌ | 📖 |
| `GET /blood-camps/{id}` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | ❌ | ❌ | 📖 | ❌ | ❌ | ❌ | 📖 | ❌ | ❌ | 📖 |
| `PUT /blood-camps/{id}` | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| `POST /blood-camps/{id}/resources` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |

---

## Lab Testing (`/api/v1/test-orders`, `/api/v1/test-results`)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `POST /test-orders` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /test-orders` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /test-orders/{id}` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `POST /test-results` | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `PUT /test-results/{id}/verify` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /test-results` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## Inventory Management (`/api/v1/blood-units`, `/api/v1/blood-components`, `/api/v1/storage-locations`)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `GET /blood-units` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | ❌ | ❌ | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /blood-components` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | ❌ | ❌ | 📖 | 📖 | ❌ | ❌ | ❌ | ❌ |
| `POST /blood-components` | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `PUT /blood-components/{id}` | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /storage-locations` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ |
| `POST /storage-locations` | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `POST /stock-transfers` | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `POST /unit-disposals` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## Cross-Match & Issuing (`/api/v1/crossmatch-requests`, `/api/v1/blood-issues`)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `POST /crossmatch-requests` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| `GET /crossmatch-requests` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | ❌ | ❌ | 📖 | ❌ | ❌ | ❌ | 📖 | ❌ |
| `POST /crossmatch-results` | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `POST /blood-issues` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /blood-issues` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | ❌ | ❌ | 📖 | 📖 | ❌ | ❌ | 📖 | ❌ |
| `POST /emergency-issues` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## Transfusion Management (`/api/v1/transfusions`)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `POST /transfusions` | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /transfusions` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | ❌ | ❌ | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `PUT /transfusions/{id}` | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `POST /transfusion-reactions` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /transfusion-reactions` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | ❌ | ❌ | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `POST /hemovigilance-reports` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /hemovigilance-reports` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## Branch Management (`/api/v1/branches`)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `POST /branches` | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /branches` | ✅ | ✅ | ✅ | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 |
| `GET /branches/{id}` | ✅ | ✅ | ✅ | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 |
| `PUT /branches/{id}` | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /branches/{id}/equipment` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## Master Data (`/api/v1/blood-groups`, `/api/v1/component-types`, etc.)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `GET /blood-groups` | ✅ | ✅ | ✅ | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 |
| `GET /component-types` | ✅ | ✅ | ✅ | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 |
| `GET /deferral-reasons` | ✅ | ✅ | ✅ | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | ❌ | ❌ | ❌ | 📖 | ❌ | ❌ |
| `GET /reaction-types` | ✅ | ✅ | ✅ | 📖 | 📖 | 📖 | 📖 | 📖 | ❌ | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `POST /blood-groups` | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `POST /component-types` | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## Hospital Management (`/api/v1/hospitals`, `/api/v1/hospital-requests`)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `POST /hospitals` | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /hospitals` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | ❌ | ❌ | ❌ | 📖 | 📖 | ❌ | ❌ | 📖 | ❌ |
| `PUT /hospitals/{id}` | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `POST /hospital-requests` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| `GET /hospital-requests` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | ❌ | ❌ | ❌ | 📖 | 📖 | ❌ | ❌ | 📖 | ❌ |
| `POST /hospital-contracts` | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## Billing & Invoicing (`/api/v1/invoices`, `/api/v1/payments`)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `GET /rate-master` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | 📖 | ❌ | ❌ | ❌ | ❌ |
| `POST /rate-master` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| `POST /invoices` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| `GET /invoices` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | 📖 | ❌ | ❌ | 📖 | ❌ |
| `GET /invoices/{id}` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | 📖 | ❌ | ❌ | 📖 | ❌ |
| `POST /payments` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| `POST /credit-notes` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |

---

## Notification Management (`/api/v1/notifications`)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `GET /notifications` | ✅ | ✅ | ✅ | 📖 | ✅ | ✅ | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 |
| `POST /notification-templates` | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `PUT /notification-preferences` | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `POST /campaigns` | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |

---

## Compliance (`/api/v1/compliance`)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `GET /regulatory-frameworks` | ✅ | ✅ | ✅ | 📖 | 📖 | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `POST /regulatory-frameworks` | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /sop-documents` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | 📖 | 📖 | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ |
| `POST /deviations` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /deviations` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | ❌ | ❌ | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ |
| `POST /recall-records` | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /recall-records` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | ❌ | ❌ | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## Reporting & Audit (`/api/v1/audit-logs`, `/api/v1/reports`)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `GET /audit-logs` | ✅ | ✅ | ✅ | 📖 | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /reports` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `POST /reports/generate` | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /dashboard-widgets` | ✅ | ✅ | ✅ | 📖 | ✅ | ✅ | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 | 📖 |

---

## Document Management (`/api/v1/documents`)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `POST /documents` | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅¹ |
| `GET /documents` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | 📖² |
| `GET /documents/{id}` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | 📖² |
| `DELETE /documents/{id}` | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

> ¹ Consent forms only. ² Own documents only.

---

## Emergency & Disaster (`/api/v1/emergency-requests`, `/api/v1/disaster-events`)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `POST /emergency-requests` | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ |
| `GET /emergency-requests` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | ❌ | ❌ | ❌ | 📖 | ❌ | ❌ | ❌ | 📖 | ❌ |
| `POST /disaster-events` | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /disaster-events` | ✅ | ✅ | ❌ | 📖 | ✅ | ✅ | 📖 | ❌ | ❌ | ❌ | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ |
| `POST /donor-mobilizations` | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |

---

## System Administration (`/api/v1/system`)

| Endpoint | SA | RA | SYA | AUD | BA | BM | DOC | LAB | PHL | NUR | INV | BIL | CAM | REC | HOS | DON |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `GET /system-settings` | ✅ | ❌ | ✅ | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `PUT /system-settings` | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /feature-flags` | ✅ | ❌ | ✅ | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `PUT /feature-flags` | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `GET /scheduled-jobs` | ✅ | ❌ | ✅ | 📖 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## Special Access Policies

### Dual Authorization Required

The following operations require approval from a second authorized user:

| Operation | Initiator Roles | Approver Roles |
|---|---|---|
| Test result release | LAB_TECHNICIAN | BRANCH_ADMIN, BRANCH_MANAGER, LAB_TECHNICIAN (different user) |
| Blood unit issuing | LAB_TECHNICIAN, INVENTORY_MANAGER | BRANCH_ADMIN, BRANCH_MANAGER |
| Unit disposal | INVENTORY_MANAGER | BRANCH_ADMIN, BRANCH_MANAGER |
| Emergency O-neg issue | DOCTOR | BRANCH_ADMIN, BRANCH_MANAGER |

### Break-Glass Access

DOCTOR role has emergency override capability for time-critical operations (e.g., emergency blood issue without cross-match). All break-glass access is:

- Fully audited in `audit_logs` with `action = 'BREAK_GLASS'`
- Time-limited (auto-revoked after 60 minutes)
- Requires post-incident review

### Data Masking

Donor PII (name, email, phone, national ID) is masked via `DataMaskingAspect` for roles that do not have direct patient care responsibilities. Full PII is visible only to: SA, RA, BA, BM, DOC, PHL, NUR, REC.
