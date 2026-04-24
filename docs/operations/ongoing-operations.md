# Ongoing Operations

**Last Updated**: 2026-04-22
**Milestone Issues**: M13-006, M13-007, M13-008, M13-009, M13-010, M13-011
**Effective From**: End of Week 2 stabilization period (see `docs/operations/stabilization-plan.md`)
**Status**: 🔴 NOT STARTED

---

## Overview

This document defines the steady-state operational cadence for BloodBank after the 2-week stabilization period. It covers weekly operations reviews, monthly SLO reviews, quarterly DR drills, quarterly security assessments, quarterly dependency updates, and annual penetration testing.

| Cadence | Process | Owner | Issues |
|---|---|---|---|
| Weekly | Operations review | DevOps Lead | M13-006 |
| Monthly | SLO review | SRE Lead | M13-007 |
| Quarterly | Disaster recovery drill | DevOps Lead | M13-008 |
| Quarterly | Security vulnerability assessment | Security Lead | M13-009 |
| Quarterly | Dependency updates | Backend/Frontend Lead | M13-010 |
| Annual | Penetration testing | Security Lead + External vendor | M13-011 |

---

## M13-006: Weekly Operations Review

### Schedule

Every **Monday at 10:00 UTC**. Duration: 60 minutes.
Attendees: DevOps Lead, SRE Lead, Backend Lead, Frontend Lead, Project Manager.

### Weekly Operations Review Template

```
BloodBank Weekly Operations Review — Week [N], [DATE]
Prepared by: [NAME]
Period: [MON] to [SUN]
```

#### 1. Uptime and Availability

| Service | Target | Actual | Status |
|---|---|---|---|
| API Gateway | ≥ 99.5% | | 🟢/🟡/🔴 |
| donor-service | ≥ 99.5% | | 🟢/🟡/🔴 |
| inventory-service | ≥ 99.5% | | 🟢/🟡/🔴 |
| lab-service | ≥ 99.5% | | 🟢/🟡/🔴 |
| transfusion-service | ≥ 99.5% | | 🟢/🟡/🔴 |
| All other services | ≥ 99.5% | | 🟢/🟡/🔴 |
| External uptime (Uptime Robot) | ≥ 99.5% | | 🟢/🟡/🔴 |

#### 2. Error Rate Summary

| Metric | Target | Actual (Week) | Trend | Status |
|---|---|---|---|---|
| API 5xx error rate | < 0.5% | | ↑/↓/→ | 🟢/🟡/🔴 |
| API 4xx rate | < 2% | | ↑/↓/→ | 🟢/🟡/🔴 |
| RabbitMQ DLQ messages | 0 | | ↑/↓/→ | 🟢/🟡/🔴 |
| Failed Keycloak logins | < 5% | | ↑/↓/→ | 🟢/🟡/🔴 |

#### 3. Incident Summary

| Ticket | Severity | Date | Duration | Status | Root Cause |
|---|---|---|---|---|---|
| — | — | — | — | — | — |

**Total incidents this week**: P1: __ P2: __ P3: __ P4: __
**Error budget consumed**: __%  (monthly budget remaining: __%)

#### 4. Performance Highlights

| Metric | Baseline | This Week | Delta |
|---|---|---|---|
| API p95 response time | | | |
| DB connection pool (peak) | | | |
| Redis cache hit rate | | | |
| RabbitMQ throughput (msg/min peak) | | | |
| Collections processed | | | |
| Blood units issued | | | |
| Crossmatch TAT (median) | | | |

#### 5. Infrastructure Spend

| Resource | Budget (monthly) | Spend this week | Projected monthly | Status |
|---|---|---|---|---|
| Kubernetes (compute) | | | | 🟢/🟡/🔴 |
| PostgreSQL storage | | | | 🟢/🟡/🔴 |
| MinIO / object storage | | | | 🟢/🟡/🔴 |
| Network egress | | | | 🟢/🟡/🔴 |
| **Total** | | | | 🟢/🟡/🔴 |

#### 6. Open Action Items from Last Week

| # | Action | Owner | Due | Status |
|---|---|---|---|---|
| 1 | | | | ☐ Done / 🔄 In Progress / ❌ Blocked |

#### 7. Action Items for Next Week

| # | Action | Owner | Due |
|---|---|---|---|
| 1 | | | |

#### 8. Decisions Required

| Decision | Options | Decision Maker | Due |
|---|---|---|---|
| | | | |

#### 9. Overall Status

**This week**: 🟢 HEALTHY / 🟡 ATTENTION NEEDED / 🔴 INTERVENTION REQUIRED

**Notes**:

---

**Distribution**: DevOps Lead, SRE Lead, Backend Lead, Frontend Lead, Project Manager, Executive Sponsor

---

## M13-007: Monthly SLO Review

### Schedule

First **Tuesday of each month at 14:00 UTC**. Duration: 90 minutes.
Attendees: SRE Lead, DevOps Lead, Backend Lead, Product Owner, Project Manager.

### SLO Definitions

| SLO | SLI | Target | Error Budget (monthly) |
|---|---|---|---|
| API Availability | % of successful requests (non-5xx) | ≥ 99.5% | 3.65 hours downtime |
| API Latency | % of requests completing in < 2 s | ≥ 95% | — |
| Collection Workflow | End-to-end time < 30 min | ≥ 98% | — |
| Crossmatch TAT (routine) | TAT < 2 h | ≥ 98% | — |
| Notification Delivery | % of notifications delivered | ≥ 99% | — |
| Report Generation | Report ready in < 30 s | ≥ 95% | — |
| Data Durability | Zero data loss events | 100% | — |

### Monthly SLO Review Template

```
BloodBank Monthly SLO Review — [MONTH YEAR]
Prepared by: [NAME]
Period: [FIRST DAY] to [LAST DAY]
Review Date: [DATE]
```

#### 1. SLO Scorecard

| SLO | Target | Actual | Error Budget Used | Budget Remaining | Status |
|---|---|---|---|---|---|
| API Availability | ≥ 99.5% | | | | 🟢/🟡/🔴 |
| API Latency (p95 < 2s) | ≥ 95% | | | | 🟢/🟡/🔴 |
| Collection Workflow | ≥ 98% | | | | 🟢/🟡/🔴 |
| Crossmatch TAT | ≥ 98% | | | | 🟢/🟡/🔴 |
| Notification Delivery | ≥ 99% | | | | 🟢/🟡/🔴 |
| Report Generation | ≥ 95% | | | | 🟢/🟡/🔴 |
| Data Durability | 100% | | | | 🟢/🟡/🔴 |

**Overall SLO compliance**: 🟢 ALL MET / 🟡 PARTIAL / 🔴 BREACH

#### 2. Error Budget Analysis

| SLO | Monthly Budget | Budget Used | Budget Remaining | Burn Rate (trend) |
|---|---|---|---|---|
| API Availability | 3.65 h | | | ↑ Fast / ↓ Normal |

**Budget policy**:
- Budget remaining > 50% → feature development may proceed at full pace
- Budget remaining 20–50% → slow down risky deployments, increase testing
- Budget remaining < 20% → freeze feature deployments, focus on reliability

#### 3. Incident Deep-Dives

For each P1 or P2 incident this month, summarize:

| Incident | Date | Duration | SLO Impact | Root Cause Category | Recurrence Risk |
|---|---|---|---|---|---|
| — | — | — | — | — | — |

**Root cause categories**: Infrastructure failure / Deployment error / Dependency failure / Code bug / Configuration error / Unknown

#### 4. Trend Analysis (3-Month View)

| Metric | Month -2 | Month -1 | This Month | Trend |
|---|---|---|---|---|
| API availability | | | | ↑/↓/→ |
| Incident count (P1+P2) | | | | ↑/↓/→ |
| Error budget consumed | | | | ↑/↓/→ |
| Mean time to recovery (P1/P2) | | | | ↑/↓/→ |
| Deployment frequency | | | | ↑/↓/→ |
| Change failure rate | | | | ↑/↓/→ |

#### 5. Capacity Review

| Resource | Current Usage | 3-Month Trend | Projected Need (3 months) | Action Required |
|---|---|---|---|---|
| PostgreSQL storage | | | | — |
| Kubernetes CPU | | | | — |
| Kubernetes Memory | | | | — |
| Redis memory | | | | — |
| RabbitMQ throughput | | | | — |

#### 6. SLO Target Review

Review whether current SLO targets remain appropriate:

| SLO | Current Target | Proposed Change | Rationale |
|---|---|---|---|
| | | No change | |

#### 7. Reliability Improvements

| # | Improvement | Priority | Owner | Target Quarter |
|---|---|---|---|---|
| 1 | | | | |

#### 8. Decisions

| Decision | Outcome |
|---|---|
| SLO targets: maintain or update? | |
| Error budget: freeze deployments? | |
| Capacity scaling required? | |

**Sign-off**: SRE Lead: _____________ Date: _____________

---

## M13-008: Quarterly Disaster Recovery Drill

### Schedule

One DR drill per quarter, approximately:
- **Q1**: March (last week)
- **Q2**: June (last week)
- **Q3**: September (last week)
- **Q4**: December (last week)

Duration: Half-day exercise (4 hours). Requires maintenance window notification.

### DR Objectives

Each quarterly drill validates:
1. Recovery Time Objective (RTO): System recoverable within **4 hours** of total failure
2. Recovery Point Objective (RPO): Data loss no more than **15 minutes** of transactions
3. Team capability: On-call team can execute recovery procedures without external help
4. Runbook accuracy: Runbooks reflect current infrastructure

### DR Drill Scenario Rotation

| Quarter | Scenario | Focus Area |
|---|---|---|
| Q1 | Complete database failure (PostgreSQL crash) | Database recovery, Flyway re-run |
| Q2 | Kubernetes cluster failure (control plane loss) | Cluster restoration, service re-deployment |
| Q3 | Data corruption incident (bad migration) | Rollback, point-in-time recovery |
| Q4 | Multi-region / multi-branch network partition | Split-brain resolution, branch isolation |

### DR Drill Execution Checklist

#### Pre-Drill (T-1 week)

- [ ] Schedule maintenance window and notify branch managers
- [ ] Confirm backup restoration has been tested in staging environment
- [ ] Brief on-call team on the drill scenario
- [ ] Prepare staging environment to mirror production
- [ ] Verify backup job succeeded within last 24 h before drill

#### Drill Execution (Day of)

- [ ] **T-0**: Announce start of DR drill in `#bloodbank-ops`
- [ ] **T+0**: Execute failure injection (per scenario)
- [ ] **T+15 min**: Confirm failure detected by monitoring (alert fired)
- [ ] **T+30 min**: Incident declared and commander assigned
- [ ] **T+60 min**: Recovery actions underway, ETA communicated
- [ ] **T+RTO**: System restored — validate all 14 services healthy
- [ ] **T+RTO+30 min**: Data integrity check — confirm RPO met
- [ ] **T+RTO+60 min**: Drill declared complete, debrief begins

#### Data Integrity Validation

```sql
-- Verify most recent successful transaction timestamp
SELECT MAX(created_at) AS last_record
FROM audit_logs
ORDER BY created_at DESC
LIMIT 1;

-- Verify blood unit count integrity
SELECT status, COUNT(*) FROM blood_units GROUP BY status;

-- Verify no orphaned records
SELECT COUNT(*) FROM collections WHERE status = 'IN_PROGRESS'
  AND created_at < NOW() - INTERVAL '1 hour';
```

#### Post-Drill (within 5 business days)

- [ ] DR drill report published (use template below)
- [ ] RTO and RPO results documented
- [ ] Gaps and failures documented with action items
- [ ] Runbooks updated to reflect lessons learned
- [ ] Next drill scenario planned

### DR Drill Report Template

```markdown
## Quarterly DR Drill Report — Q[N] [YEAR]

**Date**: [DATE]
**Scenario**: [SCENARIO NAME]
**Commander**: [NAME]
**Duration**: [START] to [END] UTC

### Results

| Objective | Target | Actual | Met? |
|---|---|---|---|
| RTO | < 4 hours | | ✅ / ❌ |
| RPO | < 15 minutes data loss | | ✅ / ❌ |
| Alert detection | < 5 minutes | | ✅ / ❌ |
| Runbook accuracy | 100% steps valid | | ✅ / ❌ |

### Timeline
| Time (UTC) | Action | Outcome |
|---|---|---|
| | | |

### What Went Well
- ...

### Gaps Found
| # | Gap | Severity | Action | Owner | Due |
|---|---|---|---|---|---|
| 1 | | | | | |

### Runbooks Updated
- [ ] `docs/operations/runbooks/[runbook].md`

**Sign-off**:
DevOps Lead: _____________ Date: _____________
Project Manager: _____________ Date: _____________
```

---

## M13-009: Quarterly Security Vulnerability Assessment

### Schedule

One assessment per quarter, aligned with DR drill weeks:
- **Q1**: March
- **Q2**: June
- **Q3**: September
- **Q4**: December

Duration: 2–3 days (automated scans Day 1, manual review + remediation planning Days 2–3).

### Assessment Scope

| Area | Tool | Performed By |
|---|---|---|
| Container image vulnerabilities | Trivy | SRE / automated CI |
| Dependency vulnerabilities (Java) | OWASP Dependency-Check | CI pipeline |
| Dependency vulnerabilities (npm/Angular) | npm audit / Snyk | CI pipeline |
| Infrastructure misconfigurations | Kubernetes kube-bench | SRE |
| OWASP Top 10 (web) | OWASP ZAP | Security Lead |
| Secrets in codebase | Gitleaks / truffleHog | CI pipeline |
| Keycloak configuration review | Manual review | Security Lead |
| Database access control review | Manual SQL audit | DBA |
| Network policy review | kubectl / manual | SRE Lead |

### Quarterly Assessment Checklist

#### Container and Image Security

- [ ] Run Trivy scan on all 14 service images: `trivy image bloodbank/<service>:latest`
- [ ] All CRITICAL CVEs: zero tolerance — must be remediated before next release
- [ ] HIGH CVEs: remediated within 30 days
- [ ] MEDIUM CVEs: tracked in backlog, remediated within 90 days
- [ ] Base images updated to latest patched versions (Temurin JRE 21 Alpine)

```bash
# Scan all service images
for svc in api-gateway donor-service inventory-service lab-service \
           branch-service transfusion-service hospital-service \
           billing-service request-matching-service notification-service \
           reporting-service document-service compliance-service config-server; do
  echo "=== Scanning $svc ==="
  trivy image --severity CRITICAL,HIGH bloodbank/$svc:latest
done
```

#### Dependency Vulnerabilities

- [ ] Run OWASP Dependency-Check: `./gradlew dependencyCheckAnalyze`
- [ ] Review HTML report in `build/reports/dependency-check-report.html`
- [ ] Run npm audit: `cd frontend/bloodbank-ui && npm audit`
- [ ] All CRITICAL vulnerabilities in used code paths: 0 tolerance
- [ ] Update vulnerable dependencies (follow M13-010 process)

#### Keycloak Configuration Review

- [ ] MFA enforced for all clinical roles (DOCTOR, LAB_TECHNICIAN, PHLEBOTOMIST)
- [ ] MFA enforced for all admin roles (SUPER_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN)
- [ ] Session max age ≤ 8 hours, idle timeout ≤ 30 minutes
- [ ] Password policy: min 12 chars, complexity enabled, max age 90 days
- [ ] Unused Keycloak clients reviewed and disabled
- [ ] Keycloak admin console access restricted to internal network only
- [ ] Keycloak version current (check for security advisories)

#### Infrastructure Security

```bash
# Run kube-bench CIS Kubernetes benchmark
kubectl run kube-bench --image=aquasec/kube-bench:latest \
  --restart=Never --command -- kube-bench

# Review network policies
kubectl get networkpolicies -n bloodbank-prod

# Check for privileged pods
kubectl get pods -n bloodbank-prod -o json | \
  jq '.items[].spec.containers[].securityContext.privileged // false'

# Verify no default service account tokens mounted unnecessarily
kubectl get pods -n bloodbank-prod -o json | \
  jq '.items[] | select(.spec.automountServiceAccountToken != false) | .metadata.name'
```

#### Database Access Review

```sql
-- List all database users and their privileges
SELECT usename, usesuper, usecreatedb, usecreaterole
FROM pg_user
ORDER BY usename;

-- Verify application user has no superuser privileges
SELECT usesuper FROM pg_user WHERE usename = 'bloodbank_user';
-- Expected: false

-- Check for any new database users created since last review
SELECT usename, usecreatedb, pg_postmaster_start_time() AS since
FROM pg_user
WHERE usesysid > (SELECT MAX(usesysid) FROM pg_user WHERE usename = 'bloodbank_user');
```

### Vulnerability Severity SLA

| Severity | Remediation Deadline |
|---|---|
| CRITICAL | Within 24 hours (emergency patch) |
| HIGH | Within 30 days |
| MEDIUM | Within 90 days (next quarterly update) |
| LOW | Tracked in backlog, no deadline |

### Quarterly Security Assessment Report Template

```markdown
## Quarterly Security Assessment — Q[N] [YEAR]

**Date**: [DATE]
**Performed by**: [NAME]

### Summary

| Category | Critical | High | Medium | Low | Action |
|---|---|---|---|---|---|
| Container images | | | | | |
| Java dependencies | | | | | |
| npm dependencies | | | | | |
| Infrastructure | | | | | |
| Keycloak config | | | | | |

### Critical and High Findings

| # | CVE / Finding | Severity | Component | Action | Owner | Due |
|---|---|---|---|---|---|---|
| 1 | | | | | | |

### Remediated Since Last Quarter

| # | CVE / Finding | Severity | Remediation | Date |
|---|---|---|---|---|
| 1 | | | | |

### Sign-off

Security Lead: _____________ Date: _____________
DevOps Lead: _____________ Date: _____________
```

---

## M13-010: Quarterly Dependency Update Process

### Schedule

Dependency updates are performed once per quarter, aligned with the security assessment cycle. This ensures vulnerabilities found in the security assessment are addressed in the same cycle.

**Target weeks**: Last 2 weeks of each quarter.

### Dependency Update Scope

| Category | Components | Approach |
|---|---|---|
| Spring Boot | `3.4.x` → latest `3.4.y` patch | `./gradlew dependencyUpdates` |
| Spring Cloud | Aligned with Spring Boot BOM | BOM update |
| Java runtime | Temurin JRE 21 Alpine base image | Dockerfile base image tag |
| MapStruct | `1.6.x` → latest | `gradle.properties` |
| Testcontainers | All versions | `gradle.properties` |
| PostgreSQL driver | `42.x.x` | `gradle.properties` |
| Angular | `21.x.x` → latest `21.x.y` | `npm update` |
| Angular Material | Aligned with Angular | `npm update` |
| Keycloak-Angular | Latest compatible | `npm update` |
| Other npm deps | Patch versions | `npm update` / Snyk |
| Docker base images | Temurin JRE 21 Alpine | Dockerfile update |
| Keycloak | `26.x` → latest `26.x.y` | Helm chart / Docker image |

### Update Process

#### 1. Dependency Audit (Day 1)

```bash
# Check available Java dependency updates
./gradlew dependencyUpdates -Drevision=release

# Check npm vulnerabilities and updates
cd frontend/bloodbank-ui
npm audit
npm outdated
```

Review the output and create a dependency update ticket for each component to be updated. Separate tickets per service/area to limit blast radius.

#### 2. Update Execution (Days 2–5)

For each dependency update:

1. **Create a feature branch** from `main`: `chore/q[N]-deps-[component]`
2. **Update the version** in `gradle.properties` or `package.json`
3. **Run full build**: `./gradlew build`
4. **Run full test suite**: `./gradlew test`
5. **Run integration tests**: `./gradlew integrationTest`
6. **Review changelog** of the updated dependency for breaking changes
7. **Open PR** with label `chore:dependency-update`

#### 3. Spring Boot / Spring Cloud Update

```bash
# Check current version
grep "springBootVersion" gradle.properties

# Edit gradle.properties — update springBootVersion
# Then build and test
./gradlew build test

# Verify actuator endpoints still functional
curl http://localhost:8080/actuator/health
```

**IMPORTANT**: When updating Spring Boot:
- Check Spring Boot release notes for deprecations
- Verify Keycloak Spring adapter compatibility
- Check MapStruct compatibility matrix
- Update Spring Cloud version to match BOM

#### 4. Angular Update

```bash
cd frontend/bloodbank-ui

# Use Angular CLI for major/minor updates
ng update @angular/core @angular/cli

# For patch updates
npm update

# Verify build
ng build --configuration production

# Run tests
ng test --watch=false
ng e2e
```

**IMPORTANT**: When updating Angular:
- Run `ng update` to handle migrations automatically
- Check Angular Material compatibility
- Verify Keycloak-Angular compatibility
- Check Chart.js / ng2-charts compatibility

#### 5. Docker Base Image Update

```dockerfile
# In all Dockerfiles, update base image tag:
FROM eclipse-temurin:21-jre-alpine
# Verify exact digest for reproducibility
```

```bash
# Pull latest and verify
docker pull eclipse-temurin:21-jre-alpine
docker inspect eclipse-temurin:21-jre-alpine | jq '.[0].RepoDigests'
```

#### 6. Validation Checklist

Before merging dependency update PRs:

- [ ] `./gradlew build` passes with no errors
- [ ] `./gradlew test` passes (all unit tests green)
- [ ] `./gradlew integrationTest` passes (Testcontainers tests green)
- [ ] `ng build --configuration production` succeeds
- [ ] `ng test --watch=false` passes
- [ ] Docker images build successfully
- [ ] Staging deployment successful
- [ ] Smoke tests pass in staging (all 14 services healthy)
- [ ] `trivy image` scan shows no new CRITICAL/HIGH CVEs
- [ ] OWASP Dependency-Check report reviewed

#### 7. Dependency Update Report Template

```markdown
## Quarterly Dependency Update Report — Q[N] [YEAR]

**Period**: [DATE RANGE]
**Performed by**: [NAME]

### Updates Applied

| Component | Previous Version | New Version | Breaking Changes | PRs |
|---|---|---|---|---|
| Spring Boot | | | None / [describe] | #NNN |
| Angular | | | None / [describe] | #NNN |
| | | | | |

### CVEs Resolved

| CVE | Severity | Component | Fixed By |
|---|---|---|---|
| CVE-XXXX-XXXXX | HIGH | | Spring Boot X.X.X |

### Deferred Updates

| Component | Available Version | Reason Deferred | Target Quarter |
|---|---|---|---|
| | | Breaking change — needs migration | Q[N+1] |

### Sign-off

Backend Lead: _____________ Date: _____________
Frontend Lead: _____________ Date: _____________
SRE Lead: _____________ Date: _____________
```

---

## M13-011: Annual Penetration Testing

### Schedule

Once per year, typically in **Q1** (January–February), before the annual compliance audit cycle.

Duration: 2–3 weeks (vendor engagement + remediation).

### Penetration Test Scope

| Scope Area | In Scope | Notes |
|---|---|---|
| External web application | ✅ | `https://bloodbank.example.com` |
| API endpoints (all services) | ✅ | All `/api/v1/` routes |
| Keycloak authentication | ✅ | Login, token handling, MFA bypass attempts |
| Branch data isolation | ✅ | Attempt cross-branch data access |
| Role-based access control | ✅ | Privilege escalation attempts |
| Network perimeter | ✅ | External-facing ports and services |
| Kubernetes cluster | ✅ | Exposed APIs, RBAC, pod escape |
| Mobile interfaces | ☐ | If mobile app is released |
| Internal network | ❌ | Out of scope (insider threat — separate assessment) |
| Social engineering | ❌ | Out of scope |

### Test Methodology

The penetration test follows OWASP Testing Guide v4 methodology with additional healthcare-specific tests:

| Phase | Activity | Timeframe |
|---|---|---|
| Reconnaissance | Passive information gathering | Day 1 |
| Scanning | Port/service scan, OSINT | Day 1–2 |
| Vulnerability analysis | Automated + manual | Day 2–5 |
| Exploitation | Controlled exploitation of findings | Day 5–8 |
| Post-exploitation | Privilege escalation, lateral movement | Day 8–10 |
| Reporting | Draft report | Day 11–14 |
| Remediation | Fix critical/high findings | Day 14–21 |
| Retest | Verify remediations | Day 21–25 |

### Vendor Requirements

The selected penetration testing vendor must:

- [ ] Hold CREST, OSCP, or equivalent certification
- [ ] Have prior experience with healthcare applications (HIPAA / GDPR context)
- [ ] Sign a Non-Disclosure Agreement (NDA) before engagement
- [ ] Sign a Business Associate Agreement (BAA) if PHI access is granted
- [ ] Provide a Scope of Work (SOW) and Rules of Engagement (RoE)
- [ ] Operate within agreed test windows (to minimize disruption)
- [ ] Deliver a written report within 10 business days of test completion

### Remediation SLA (Post Pen-Test)

| Finding Severity | Remediation Deadline |
|---|---|
| CRITICAL | Within 48 hours of report |
| HIGH | Within 14 days |
| MEDIUM | Within 90 days (next quarter update) |
| LOW | Tracked in backlog |
| Informational | At team's discretion |

### Annual Pen-Test Report Requirements

The vendor's report must include:

1. Executive summary (non-technical, suitable for board/executives)
2. Technical findings with CVSS scores
3. Evidence (screenshots, payloads, proof-of-concept — in controlled conditions)
4. Remediation recommendations per finding
5. Retest results confirming fixes

### Post-Test Remediation Checklist

- [ ] All CRITICAL findings: patch deployed and verified
- [ ] All HIGH findings: patch deployed or risk-accepted with written sign-off
- [ ] MEDIUM findings: tickets created in backlog with Q target
- [ ] Retest performed by vendor (or internal team) for CRITICAL and HIGH
- [ ] Retest report received confirming successful remediation
- [ ] Report filed in `docs/compliance/` (redacted version for audit evidence)
- [ ] HIPAA Security Officer notified of findings and remediations
- [ ] Lessons learned incorporated into security assessment checklist (M13-009)
- [ ] Annual pen-test summary added to compliance evidence folder

### Annual Pen-Test Summary Template

```markdown
## Annual Penetration Test Summary — [YEAR]

**Vendor**: [Vendor Name]
**Test Period**: [DATE RANGE]
**Report Date**: [DATE]
**Retest Date**: [DATE]

### Finding Summary

| Severity | Found | Remediated | Accepted (with justification) | Outstanding |
|---|---|---|---|---|
| Critical | | | | 0 (required) |
| High | | | | |
| Medium | | | | |
| Low | | | | |

### Critical Findings (summary — no sensitive details)
[Describe category without exploitable detail]

### Remediation Status
All critical and high findings: ✅ Remediated / ❌ Outstanding

### Sign-off

Security Lead: _____________ Date: _____________
HIPAA Security Officer: _____________ Date: _____________
Project Manager: _____________ Date: _____________
Executive Sponsor: _____________ Date: _____________
```

---

## Operational Calendar

Use this calendar template to schedule recurring activities for the year:

| Month | Week 1 | Week 2 | Week 3 | Week 4 |
|---|---|---|---|---|
| January | Pen-test kick-off | Pen-test execution | Pen-test execution | Remediation |
| February | Remediation + retest | — | — | — |
| March | Ops review | Ops review | DR Drill (Q1) + Sec assessment | Dep updates |
| April | Ops review | Ops review | Ops review | Ops review |
| May | Ops review | Ops review | Ops review | Ops review |
| June | Ops review | Ops review | DR Drill (Q2) + Sec assessment | Dep updates |
| July | Ops review | Ops review | Ops review | Ops review |
| August | Ops review | Ops review | Ops review | Ops review |
| September | Ops review | Ops review | DR Drill (Q3) + Sec assessment | Dep updates |
| October | Ops review | Ops review | Ops review | Ops review |
| November | Ops review | Ops review | Ops review | Ops review |
| December | Ops review | Ops review | DR Drill (Q4) + Sec assessment | Dep updates |

Monthly SLO Review: First Tuesday of every month.

---

*Related documents:*
- *`docs/operations/stabilization-plan.md` — First 2 weeks post-launch*
- *`docs/operations/incident-response.md` — Incident response playbook*
- *`docs/operations/runbooks/` — Technical runbooks*
- *`docs/compliance/ongoing-compliance.md` — Quarterly and annual compliance*
- *`docs/milestones/M13-post-launch.md` — Milestone tracker*
