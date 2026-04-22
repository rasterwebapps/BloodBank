# M11: Regional Rollout

**Duration:** 4 weeks
**Dependencies:** M10 (Pilot)
**Exit Gate:** All branches in the region live and stable

## 📊 Development Status: 🟡 READY — Planning Complete (5/34 complete, 29/34 operational READY)

**Issues Completed (planning/scripts/docs in place):** 5/34
**Issues READY (operational — executed during actual regional rollout):** 29/34
**Blocked by:** M10 (Pilot Deployment sign-off) for execution

### What's In Place
- ✅ `k8s/scripts/branch-onboard.sh` — end-to-end branch provisioning (Keycloak group, BRANCH_ADMIN user, data migration, isolation check)
- ✅ `k8s/scripts/batch-rollout.sh` — batch orchestrator: onboard → verify → scaling check → HTML/text report
- ✅ `k8s/scripts/verify-branch.sh` — post-onboarding verification (auth, branch isolation, service health, workflow smoke tests)
- ✅ `k8s/scripts/scaling-check.sh` — infrastructure health: HPA replicas, DB pool, Redis hit rate, RabbitMQ queue depths
- ✅ `docs/operations/rollout-schedule.md` — batch grouping strategy (risk tiers, composition rules), 4-week timeline, per-branch migration checklist, staff training schedule template (Day 1–3)
- ✅ `docs/operations/data-migration-guide.md` — step-by-step migration import/rollback procedures
- ✅ `docs/operations/cross-branch-validation.md` — inter-branch transfer, regional dashboard, emergency broadcast, REGIONAL_ADMIN test procedures (M11-026–M11-029)
- ✅ `docs/operations/scaling-validation.md` — HPA, DB pool, Redis, RabbitMQ formal validation procedures (M11-031–M11-034)
- ✅ `docs/operations/regional-signoff-template.md` — formal regional sign-off record (M11-030)
- ✅ `docs/operations/hypercare-plan.md` — 14-day hypercare plan, on-call rota, escalation matrix (referenced per batch)

---

## Objective

Roll out to all branches in batches, with data migration and staff training for each batch.

## Issues

### Rollout Planning
- [x] **M11-001**: Define rollout batches (group branches by region/risk) — batch grouping strategy, risk tiers, composition rules documented in `docs/operations/rollout-schedule.md` Section 1; `k8s/scripts/branch-onboard.sh` implements per-branch provisioning ✅
- [x] **M11-002**: Create rollout schedule (2-4 branches per week) — 4-week week-by-week timeline in `docs/operations/rollout-schedule.md` Section 2; `k8s/scripts/batch-rollout.sh` orchestrates execution ✅
- [x] **M11-003**: Create per-branch migration checklist — full T−2w / T−1w / T−2d / T−0 / T+3d checklist in `docs/operations/rollout-schedule.md` Section 3 ✅
- [x] **M11-004**: Create automated data migration scripts (historical donor data, inventory) — `k8s/scripts/branch-onboard.sh` (Keycloak + data import + isolation check) and `k8s/scripts/verify-branch.sh` (post-migration verification); procedures in `docs/operations/data-migration-guide.md` ✅
- [x] **M11-005**: Create staff training schedule per batch — Day 1–3 schedule, role-group sessions, competency assessment, quick-ref guides in `docs/operations/rollout-schedule.md` Section 4; `k8s/scripts/scaling-check.sh` validates infrastructure post-training go-live ✅

> ⚙️ **M11-006 to M11-025 are operational batch execution tasks** — scripts (`batch-rollout.sh`, `branch-onboard.sh`, `verify-branch.sh`, `scaling-check.sh`) and procedures (`rollout-schedule.md`, `data-migration-guide.md`, `hypercare-plan.md`) are in place. Execution happens during the actual regional rollout after M10 pilot sign-off.

### Batch 1: Week 1 (2-4 branches)
- [ ] **M11-006**: Migrate Batch 1 branch data ⚙️ READY — run `batch-rollout.sh --config batch-1.json`
- [ ] **M11-007**: Create Batch 1 branch Keycloak groups and users ⚙️ READY — automated by `branch-onboard.sh` Step 1–3
- [ ] **M11-008**: Train Batch 1 staff ⚙️ READY — Day 1–3 schedule in `docs/operations/rollout-schedule.md` Section 4
- [ ] **M11-009**: Go-live Batch 1 branches ⚙️ READY — go/no-go criteria documented in `docs/operations/rollout-schedule.md` Section 3 (T+0 checklist)
- [ ] **M11-010**: Batch 1 hypercare (3 days intensive) ⚙️ READY — hypercare plan in `docs/operations/hypercare-plan.md`
- [ ] **M11-011**: Batch 1 sign-off ⚙️ READY — T+3 days sign-off checklist in `docs/operations/rollout-schedule.md` Section 3

### Batch 2: Week 2 (2-4 branches)
- [ ] **M11-012**: Migrate Batch 2 branch data ⚙️ READY — run `batch-rollout.sh --config batch-2.json`
- [ ] **M11-013**: Create Batch 2 Keycloak configuration ⚙️ READY — automated by `branch-onboard.sh`
- [ ] **M11-014**: Train Batch 2 staff ⚙️ READY — training schedule template in `docs/operations/rollout-schedule.md` Section 4
- [ ] **M11-015**: Go-live Batch 2 branches ⚙️ READY — go/no-go criteria per `rollout-schedule.md` Section 3
- [ ] **M11-016**: Batch 2 hypercare ⚙️ READY — `docs/operations/hypercare-plan.md`
- [ ] **M11-017**: Batch 2 sign-off ⚙️ READY — sign-off checklist in `rollout-schedule.md` Section 3

### Batch 3: Week 3 (2-4 branches)
- [ ] **M11-018**: Migrate Batch 3 branch data ⚙️ READY — run `batch-rollout.sh --config batch-3.json`
- [ ] **M11-019**: Train Batch 3 staff ⚙️ READY — training schedule template in `docs/operations/rollout-schedule.md` Section 4
- [ ] **M11-020**: Go-live Batch 3 branches ⚙️ READY — go/no-go criteria per `rollout-schedule.md` Section 3
- [ ] **M11-021**: Batch 3 hypercare and sign-off ⚙️ READY — `docs/operations/hypercare-plan.md` + sign-off checklist

### Batch 4: Week 4 (remaining branches)
- [ ] **M11-022**: Migrate Batch 4 branch data ⚙️ READY — run `batch-rollout.sh --config batch-4.json` (extra staging dry-run for high-risk branches)
- [ ] **M11-023**: Train Batch 4 staff ⚙️ READY — training schedule template in `docs/operations/rollout-schedule.md` Section 4
- [ ] **M11-024**: Go-live Batch 4 branches ⚙️ READY — go/no-go criteria per `rollout-schedule.md` Section 3
- [ ] **M11-025**: Batch 4 hypercare and sign-off ⚙️ READY — `docs/operations/hypercare-plan.md` + sign-off checklist

> ⚙️ **M11-026 to M11-030 are cross-branch validation tasks** — test procedures documented in `docs/operations/cross-branch-validation.md`; sign-off template in `docs/operations/regional-signoff-template.md`. Executed at end of Week 4 after all batches are live.

### Cross-Branch Validation
- [ ] **M11-026**: Verify inter-branch transfers work between live branches ⚙️ READY — procedure in `docs/operations/cross-branch-validation.md` Section 1
- [ ] **M11-027**: Verify regional dashboard aggregates data from all branches ⚙️ READY — procedure in `docs/operations/cross-branch-validation.md` Section 2
- [ ] **M11-028**: Verify emergency request broadcasts reach all branches ⚙️ READY — procedure in `docs/operations/cross-branch-validation.md` Section 3
- [ ] **M11-029**: Verify REGIONAL_ADMIN can see all branches in region ⚙️ READY — procedure in `docs/operations/cross-branch-validation.md` Section 4
- [ ] **M11-030**: Regional sign-off from management ⚙️ READY — template in `docs/operations/regional-signoff-template.md`

> ⚙️ **M11-031 to M11-034 are scaling validation tasks** — formal validation procedures documented in `docs/operations/scaling-validation.md`; `k8s/scripts/scaling-check.sh` provides automated checks. Monitoring begins from Batch 3 go-live; formal validation at end of Week 4.

### Scaling Validation
- [ ] **M11-031**: Monitor HPA scaling with increased branch load ⚙️ READY — HPA validation procedure in `docs/operations/scaling-validation.md` Section 1; automated by `k8s/scripts/scaling-check.sh`
- [ ] **M11-032**: Verify database performance with multi-branch data volume ⚙️ READY — DB pool validation in `docs/operations/scaling-validation.md` Section 2
- [ ] **M11-033**: Verify Redis cache hit rates across branches ⚙️ READY — Redis hit rate validation in `docs/operations/scaling-validation.md` Section 3; automated by `scaling-check.sh`
- [ ] **M11-034**: Tune alerting thresholds based on real usage patterns ⚙️ READY — threshold tuning procedure in `docs/operations/scaling-validation.md` Section 4

## Deliverables

1. All regional branches live on BloodBank
2. Per-batch migration and training reports
3. Cross-branch feature validation report — [`docs/operations/cross-branch-validation.md`](../operations/cross-branch-validation.md) (M11-026–029)
4. Scaling and performance validation report — [`docs/operations/scaling-validation.md`](../operations/scaling-validation.md) (M11-031–034)
5. Regional management sign-off — [`docs/operations/regional-signoff-template.md`](../operations/regional-signoff-template.md) (M11-030)
