# Cross-Branch Validation Procedures

**Last Updated**: 2026-04-22
**Milestone**: M11 — Regional Rollout
**Issues**: M11-026, M11-027, M11-028, M11-029, M11-030
**Status**: 🔴 NOT STARTED

---

## Overview

Cross-branch validation is executed once **all regional batches are live** (end of Week 4, after Batch 4 sign-off). Its purpose is to confirm that the inter-branch features — transfers, dashboards, emergency broadcasts, and multi-branch admin views — function correctly across the full set of live branches, not just within any single batch.

Validation is led by the **QA Lead** and must be completed before the Regional Management Sign-off meeting (M11-030).

> **Prerequisites**
> - All four batch sign-off documents received (M11-011, M11-017, M11-021, M11-025)
> - No open P1 or P2 incidents across any branch
> - REGIONAL_ADMIN account provisioned for the region under test
> - At least two BRANCH_ADMIN accounts from different branches available for testing

---

## 1. Inter-Branch Transfer Test Procedure (M11-026)

### 1.1 Purpose

Verify that blood units can be transferred between any two live branches, that the chain of custody is recorded in the audit log, and that the source branch inventory is decremented while the destination branch inventory is incremented atomically.

### 1.2 Test Accounts Required

| Account | Role | Branch |
|---|---|---|
| `transfer-test-source@bloodbank` | INVENTORY_MANAGER | Source Branch |
| `transfer-test-dest@bloodbank` | INVENTORY_MANAGER | Destination Branch |
| `transfer-test-admin@bloodbank` | BRANCH_ADMIN | Source Branch |

### 1.3 Pre-Conditions

- At least 3 blood units of type **O+** are available in the source branch inventory.
- The destination branch has storage capacity in the same component type.
- Both branches are `ACTIVE` status in Keycloak.

### 1.4 Test Steps

| Step | Action | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Log in as `INVENTORY_MANAGER` for Source Branch | Dashboard loads, shows **Source Branch** inventory only | — |
| 2 | Navigate to **Inventory → Blood Units → Transfer** | Transfer form displays | — |
| 3 | Select 1 unit of O+ whole blood for transfer | Unit selected; source branch unit count decrements in preview | — |
| 4 | Select Destination Branch from the region dropdown | Only branches within the same region are listed | — |
| 5 | Submit transfer request | Confirmation dialog appears with transfer ID | — |
| 6 | Confirm transfer | `201 Created` response; transfer status shows **IN_TRANSIT** | — |
| 7 | Log in as `INVENTORY_MANAGER` for Destination Branch | Destination branch inventory page loads | — |
| 8 | Check **Incoming Transfers** | Pending transfer appears with correct unit details | — |
| 9 | Accept the transfer at the destination branch | Unit status changes to **AVAILABLE**; destination count increments | — |
| 10 | Log in as `BRANCH_ADMIN` for Source Branch | Source branch audit log visible | — |
| 11 | Open audit log for the transferred unit | Chain of custody shows: Source → IN_TRANSIT → Destination | — |
| 12 | Verify source branch inventory | Unit is **no longer listed** in source branch available stock | — |

### 1.5 Negative Tests

| Test | Action | Expected Result | Pass/Fail |
|---|---|---|---|
| N1 | Attempt to transfer a unit that is RESERVED | Transfer blocked; error: *Unit is reserved and cannot be transferred* | — |
| N2 | Attempt to transfer to a branch in a different region | Branch not available in dropdown (region filter enforced) | — |
| N3 | `INVENTORY_MANAGER` from Branch A attempts to accept transfer at Branch B | `403 Forbidden` response | — |

### 1.6 Pass Criteria

- All 12 positive steps pass without errors.
- All 3 negative tests return the expected error/restriction.
- Audit log chain of custody is complete and immutable (no gaps).
- No cross-branch inventory contamination (Source Branch shows zero transferred units; Destination Branch shows exactly 1 gained unit).

---

## 2. Regional Dashboard Verification (M11-027)

### 2.1 Purpose

Confirm that the regional dashboard (visible to REGIONAL_ADMIN) correctly aggregates live data from **all** branches in the region — blood stock levels, daily donation counts, pending test orders, and open blood requests.

### 2.2 Test Account Required

| Account | Role | Scope |
|---|---|---|
| `regional-admin-test@bloodbank` | REGIONAL_ADMIN | All branches in region |

### 2.3 Baseline Collection (Before Testing)

Before starting, record the current figures from each branch's individual BRANCH_ADMIN dashboard:

| Branch | Available O+ Units | Available O− Units | Today's Donations | Pending Test Orders |
|---|---|---|---|---|
| Branch 1 | — | — | — | — |
| Branch 2 | — | — | — | — |
| Branch 3 | — | — | — | — |
| Branch 4 | — | — | — | — |
| **Expected total** | — | — | — | — |

### 2.4 Test Steps

| Step | Action | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Log in as `REGIONAL_ADMIN` | Regional dashboard loads; region name displayed in header | — |
| 2 | View **Regional Blood Stock** summary widget | Total O+ and O− counts match the sum from per-branch baselines (±1 unit tolerance for concurrent transactions) | — |
| 3 | View **Daily Donations** chart | Aggregated donation count matches sum of all branch counts for today | — |
| 4 | View **Pending Test Orders** | Total count matches sum of per-branch pending orders | — |
| 5 | Click a branch name in the branch list | Branch-specific sub-view opens; data consistent with that branch's BRANCH_ADMIN view | — |
| 6 | Use the **Blood Group Filter** (e.g., A+) | Dashboard re-aggregates showing A+ stock from all branches | — |
| 7 | View the **Critical Stock Alert** panel | Branches with stock below minimum threshold are highlighted | — |
| 8 | Export the **Regional Summary Report** | PDF/CSV generated with data from all branches; no blank sections | — |

### 2.5 Data Consistency Spot Check

Perform a targeted injection test: request a BRANCH_ADMIN at one branch to register 5 new donors in the system, then within 60 seconds check the regional dashboard donation count. It should increment by 5.

### 2.6 Pass Criteria

- Regional totals match the sum of per-branch figures (±2% for concurrent-transaction tolerance).
- Per-branch drill-down matches BRANCH_ADMIN view exactly.
- Regional report exports without errors and contains data from all branches.
- No data from branches **outside** the region appears on the dashboard.

---

## 3. Emergency Broadcast Verification (M11-028)

### 3.1 Purpose

Verify that an emergency blood request broadcast, triggered by any branch in the region, is received by **all other branches** in the region within the expected delivery SLA, and that branches outside the region do **not** receive the broadcast.

### 3.2 Test Accounts Required

| Account | Role | Branch |
|---|---|---|
| `emergency-doctor-src@bloodbank` | DOCTOR | Originating Branch |
| `emergency-inv-b2@bloodbank` | INVENTORY_MANAGER | Receiving Branch 2 |
| `emergency-inv-b3@bloodbank` | INVENTORY_MANAGER | Receiving Branch 3 |
| `emergency-inv-b4@bloodbank` | INVENTORY_MANAGER | Receiving Branch 4 |
| `emergency-inv-other@bloodbank` | INVENTORY_MANAGER | Branch in DIFFERENT region |

### 3.3 Test Steps

| Step | Action | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Log in as `DOCTOR` at the Originating Branch | Dashboard loads correctly | — |
| 2 | Navigate to **Blood Requests → Emergency Request** | Emergency request form loads | — |
| 3 | Submit emergency request: Blood type AB−, 2 units, Reason: *Trauma surgery* | Request submitted; event ID returned; status **BROADCAST** | — |
| 4 | Check RabbitMQ management console | `EmergencyRequestEvent` message published to `bloodbank.events` exchange with correct `branchId` and `region` | — |
| 5 | Log in as `INVENTORY_MANAGER` at Branch 2 (same region) | Check **Alerts** or **Notification bell** | Emergency notification appears within 30 seconds | — |
| 6 | Repeat step 5 for Branch 3 | Emergency notification appears within 30 seconds | — |
| 7 | Repeat step 5 for Branch 4 | Emergency notification appears within 30 seconds | — |
| 8 | Log in as `INVENTORY_MANAGER` at the **other-region branch** | Check Alerts/Notifications | **No** notification for this emergency request | — |
| 9 | From Branch 2, respond to the emergency: "Can supply 1 unit AB−" | Response recorded; originating branch receives notification of response | — |
| 10 | At Originating Branch, review emergency request details | Shows response from Branch 2; status updated to **PARTIALLY_FULFILLED** | — |
| 11 | Check audit log | All broadcast events logged with timestamp, sender branch, and recipient branches | — |

### 3.4 Timing Measurements

Record actual delivery times:

| Recipient Branch | Time of Broadcast | Time Notification Received | Latency (seconds) |
|---|---|---|---|
| Branch 2 | — | — | — |
| Branch 3 | — | — | — |
| Branch 4 | — | — | — |

**SLA**: Notification must be received by all branches within **60 seconds** of broadcast.

### 3.5 Negative Tests

| Test | Action | Expected Result | Pass/Fail |
|---|---|---|---|
| N1 | `RECEPTIONIST` tries to submit emergency request | `403 Forbidden` — insufficient role | — |
| N2 | Emergency request with no blood type specified | Validation error returned; request not broadcast | — |

### 3.6 Pass Criteria

- All same-region branches receive the notification within 60 seconds.
- Branches in other regions do **not** receive the notification.
- Audit log captures the full broadcast chain.
- RabbitMQ DLQ shows zero messages related to this broadcast.
- Response workflow (Branch 2 → Originating Branch) completes without errors.

---

## 4. REGIONAL_ADMIN Multi-Branch View Verification (M11-029)

### 4.1 Purpose

Verify that a REGIONAL_ADMIN can view and manage data across all branches in their region, and that their access is strictly bounded to their region (not global).

### 4.2 Test Accounts Required

| Account | Role | Region Scope |
|---|---|---|
| `regional-admin-test@bloodbank` | REGIONAL_ADMIN | Region A (all batches deployed) |
| `super-admin-test@bloodbank` | SUPER_ADMIN | Global (for comparison/validation) |

### 4.3 Test Steps

| Step | Action | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Log in as `REGIONAL_ADMIN` | Dashboard shows region name; no global admin controls visible | — |
| 2 | Navigate to **Branch Management** | List shows **all** branches in Region A; no branches from other regions | — |
| 3 | Click each branch in the list | Branch detail page loads; donor count, staff count, and inventory summary correct | — |
| 4 | Navigate to **Staff Management** | All staff accounts across all Region A branches are visible | — |
| 5 | Search for a donor by name across the region | Donor found; branch affiliation shown; donor details accessible | — |
| 6 | Attempt to navigate directly to a branch in Region B (via URL manipulation) | `403 Forbidden` or redirect to Region A dashboard | — |
| 7 | View **Regional Audit Log** | Events from all Region A branches shown; no events from Region B | — |
| 8 | View **Regional SLO Dashboard** | API response times, uptime, and error rates shown per branch | — |
| 9 | Generate **Regional Management Report** | Report includes all Region A branches; Region B data absent | — |
| 10 | As SUPER_ADMIN, verify Region A data matches what REGIONAL_ADMIN sees | Data consistent (same figures) | — |

### 4.4 Branch Count Verification

| Check | Expected | Actual | Pass/Fail |
|---|---|---|---|
| Number of branches visible to REGIONAL_ADMIN | (total deployed in region) | — | — |
| Number of staff accounts visible | (sum of all branch staff) | — | — |
| Number of branches visible to SUPER_ADMIN for this region | (same as REGIONAL_ADMIN) | — | — |

### 4.5 Pass Criteria

- REGIONAL_ADMIN sees all branches in their region and zero branches outside it.
- All data (donors, staff, inventory) from every branch is accessible.
- URL manipulation to access out-of-region data returns `403 Forbidden`.
- Regional report contains exactly the expected number of branches.

---

## 5. Validation Execution Checklist

Use this checklist to track completion of all four validation areas. The QA Lead must sign off on each before the Regional Management sign-off meeting.

| # | Validation Area | Issue | Pass/Fail | Signed Off By | Date |
|---|---|---|---|---|---|
| 1 | Inter-Branch Transfer (Section 1) | M11-026 | — | — | — |
| 2 | Regional Dashboard (Section 2) | M11-027 | — | — | — |
| 3 | Emergency Broadcast (Section 3) | M11-028 | — | — | — |
| 4 | REGIONAL_ADMIN Multi-Branch View (Section 4) | M11-029 | — | — | — |

**Overall Cross-Branch Validation Result**: ☐ PASS ☐ FAIL

**QA Lead signature**: _________________________ **Date**: _____________

---

## 6. Issue Escalation

If any validation test **fails**, follow this escalation path:

```
QA Lead raises defect in issue tracker (severity: P1 or P2)
  ↓
DevOps Lead + Backend Lead assigned (response within 1 h during validation window)
  ↓
Fix deployed to production (via hotfix process)
  ↓
Failed test re-executed
  ↓
If still failing after 2 attempts → Escalate to Project Manager
  ↓
Regional Management sign-off delayed until all tests pass
```

All defects must be resolved and all tests must achieve PASS status before the Regional sign-off document (M11-030) is completed.

---

## References

- [`docs/operations/rollout-schedule.md`](./rollout-schedule.md) — Week-by-week rollout timeline
- [`docs/operations/regional-signoff-template.md`](./regional-signoff-template.md) — Sign-off template (M11-030)
- [`docs/security/branch-isolation.md`](../security/branch-isolation.md) — 4-layer isolation architecture
- [`docs/architecture/event-contracts.md`](../architecture/event-contracts.md) — RabbitMQ event definitions
- [`docs/milestones/M11-regional-rollout.md`](../milestones/M11-regional-rollout.md) — Issue tracker
