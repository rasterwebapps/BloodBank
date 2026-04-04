# M11: Regional Rollout

**Duration:** 4 weeks
**Dependencies:** M10 (Pilot)
**Exit Gate:** All branches in the region live and stable

---

## Objective

Roll out to all branches in batches, with data migration and staff training for each batch.

## Issues

### Rollout Planning
- [ ] **M11-001**: Define rollout batches (group branches by region/risk)
- [ ] **M11-002**: Create rollout schedule (2-4 branches per week)
- [ ] **M11-003**: Create per-branch migration checklist
- [ ] **M11-004**: Create automated data migration scripts (historical donor data, inventory)
- [ ] **M11-005**: Create staff training schedule per batch

### Batch 1: Week 1 (2-4 branches)
- [ ] **M11-006**: Migrate Batch 1 branch data
- [ ] **M11-007**: Create Batch 1 branch Keycloak groups and users
- [ ] **M11-008**: Train Batch 1 staff
- [ ] **M11-009**: Go-live Batch 1 branches
- [ ] **M11-010**: Batch 1 hypercare (3 days intensive)
- [ ] **M11-011**: Batch 1 sign-off

### Batch 2: Week 2 (2-4 branches)
- [ ] **M11-012**: Migrate Batch 2 branch data
- [ ] **M11-013**: Create Batch 2 Keycloak configuration
- [ ] **M11-014**: Train Batch 2 staff
- [ ] **M11-015**: Go-live Batch 2 branches
- [ ] **M11-016**: Batch 2 hypercare
- [ ] **M11-017**: Batch 2 sign-off

### Batch 3: Week 3 (2-4 branches)
- [ ] **M11-018**: Migrate Batch 3 branch data
- [ ] **M11-019**: Train Batch 3 staff
- [ ] **M11-020**: Go-live Batch 3 branches
- [ ] **M11-021**: Batch 3 hypercare and sign-off

### Batch 4: Week 4 (remaining branches)
- [ ] **M11-022**: Migrate Batch 4 branch data
- [ ] **M11-023**: Train Batch 4 staff
- [ ] **M11-024**: Go-live Batch 4 branches
- [ ] **M11-025**: Batch 4 hypercare and sign-off

### Cross-Branch Validation
- [ ] **M11-026**: Verify inter-branch transfers work between live branches
- [ ] **M11-027**: Verify regional dashboard aggregates data from all branches
- [ ] **M11-028**: Verify emergency request broadcasts reach all branches
- [ ] **M11-029**: Verify REGIONAL_ADMIN can see all branches in region
- [ ] **M11-030**: Regional sign-off from management

### Scaling Validation
- [ ] **M11-031**: Monitor HPA scaling with increased branch load
- [ ] **M11-032**: Verify database performance with multi-branch data volume
- [ ] **M11-033**: Verify Redis cache hit rates across branches
- [ ] **M11-034**: Tune alerting thresholds based on real usage patterns

## Deliverables

1. All regional branches live on BloodBank
2. Per-batch migration and training reports
3. Cross-branch feature validation report
4. Regional management sign-off
