# Worldwide Launch Checklist

**Last Updated**: 2026-04-22
**Milestone**: M12 — Worldwide Launch
**Issues**: M12-001, M12-006
**Status**: 🔴 NOT STARTED

---

## Overview

This checklist must be completed by the Operations Lead, Technical Lead, and Security Lead before the worldwide go-live switch is thrown. Every item must be signed off. No item may be skipped without written approval from the Executive Sponsor.

**Sign-off owners:**

| Role | Name | Sign-off Date |
|---|---|---|
| Technical Lead | _TBD_ | — |
| Security Lead | _TBD_ | — |
| Operations Lead | _TBD_ | — |
| Project Manager | _TBD_ | — |
| Executive Sponsor | _TBD_ | — |

---

## Part 1 — Branch Verification (All Regions Live)

### 1.1 Branch Health Checks

Verify that every branch in every region is live and healthy before the worldwide announcement is sent.

```bash
# Check health for all pods in production namespace
kubectl get pods -n bloodbank-prod --field-selector=status.phase!=Running

# Verify all 14 services are healthy across all regions
for REGION in us-east eu-west ap-southeast; do
  echo "=== Region: $REGION ==="
  kubectl get pods -n bloodbank-prod --context=k8s-${REGION} | grep -v Running
done
```

| Region | Branches | Health Check | Sign-off |
|---|---|---|---|
| North America | All branches | `GET /actuator/health` → `UP` | ☐ |
| Europe | All branches | `GET /actuator/health` → `UP` | ☐ |
| Asia-Pacific | All branches | `GET /actuator/health` → `UP` | ☐ |
| Middle East & Africa | All branches | `GET /actuator/health` → `UP` | ☐ |
| Latin America | All branches | `GET /actuator/health` → `UP` | ☐ |

### 1.2 Service-Level Health Matrix

All 14 microservices must be `UP` in every region:

| Service | Endpoint | Expected Status | Verified |
|---|---|---|---|
| api-gateway | `/actuator/health` | `UP` | ☐ |
| donor-service | `/actuator/health` | `UP` | ☐ |
| inventory-service | `/actuator/health` | `UP` | ☐ |
| lab-service | `/actuator/health` | `UP` | ☐ |
| branch-service | `/actuator/health` | `UP` | ☐ |
| transfusion-service | `/actuator/health` | `UP` | ☐ |
| hospital-service | `/actuator/health` | `UP` | ☐ |
| billing-service | `/actuator/health` | `UP` | ☐ |
| request-matching-service | `/actuator/health` | `UP` | ☐ |
| notification-service | `/actuator/health` | `UP` | ☐ |
| reporting-service | `/actuator/health` | `UP` | ☐ |
| document-service | `/actuator/health` | `UP` | ☐ |
| compliance-service | `/actuator/health` | `UP` | ☐ |
| config-server | `/actuator/health` | `UP` | ☐ |

### 1.3 Dependent Infrastructure

| Component | Check | Expected | Verified |
|---|---|---|---|
| PostgreSQL 17 | Connection pool usage | < 70% | ☐ |
| Redis 7 | Cache hit rate | ≥ 80% | ☐ |
| RabbitMQ 3.13 | Queue depths | < 100 messages | ☐ |
| Keycloak 26 | Realm status | `ACTIVE` | ☐ |
| MinIO | Storage accessible | HTTP 200 | ☐ |
| CDN | Assets served | HTTP 200, < 200 ms | ☐ |

---

## Part 2 — Security Scan (Clean)

### 2.1 Vulnerability Scanning

Run all security tools and confirm zero critical/high findings before go-live.

```bash
# Trivy — container image scan
trivy image --severity CRITICAL,HIGH bloodbank/api-gateway:latest
trivy image --severity CRITICAL,HIGH bloodbank/donor-service:latest
# ... repeat for all 14 services

# OWASP Dependency-Check
./gradlew dependencyCheckAnalyze

# Snyk
snyk test --severity-threshold=high

# OWASP ZAP — production URL baseline scan
docker run -t owasp/zap2docker-stable zap-baseline.py \
  -t https://api.bloodbank.example.com \
  -r zap-report.html
```

| Tool | Scope | Critical | High | Status | Sign-off |
|---|---|---|---|---|---|
| Trivy | All 14 container images | 0 | 0 | ☐ PASS / ☐ FAIL | ☐ |
| OWASP Dependency-Check | All Gradle dependencies | 0 | 0 | ☐ PASS / ☐ FAIL | ☐ |
| Snyk | Application code + deps | 0 | 0 | ☐ PASS / ☐ FAIL | ☐ |
| OWASP ZAP | Production API baseline | 0 | 0 | ☐ PASS / ☐ FAIL | ☐ |

### 2.2 SSL/TLS & Security Headers

```bash
# SSL Labs check (command-line tool)
sslyze --regular api.bloodbank.example.com
sslyze --regular app.bloodbank.example.com

# Security headers check
curl -s -D - https://api.bloodbank.example.com/actuator/health | grep -i "strict-transport\|x-content-type\|x-frame\|content-security"
```

| Check | Expected | Status |
|---|---|---|
| SSL/TLS rating | A or A+ | ☐ |
| Strict-Transport-Security | max-age ≥ 31536000; includeSubDomains | ☐ |
| X-Content-Type-Options | `nosniff` | ☐ |
| X-Frame-Options | `DENY` or `SAMEORIGIN` | ☐ |
| Content-Security-Policy | Configured and restrictive | ☐ |
| Certificate expiry | > 60 days remaining | ☐ |

### 2.3 Access Control Verification

- [ ] All admin endpoints require SUPER_ADMIN or SYSTEM_ADMIN role
- [ ] Branch-scoped data returns HTTP 403 when accessed by a different branch token
- [ ] Donor Portal cannot access staff endpoints
- [ ] Hospital Portal cannot access internal clinical data
- [ ] Keycloak MFA enforced for all staff roles
- [ ] API rate limiting active on all public endpoints

---

## Part 3 — Disaster Recovery Tested

### 3.1 Database Failover Test

Document the results of the most recent DR failover test. This test must have been completed within the last 7 days.

```bash
# Simulate primary DB failure
# RTO target: < 5 minutes
# RPO target: < 1 minute (with streaming replication)

# Step 1: Trigger manual failover on PostgreSQL
pg_ctl promote -D /var/lib/postgresql/data

# Step 2: Verify application reconnects
kubectl rollout restart deployment -n bloodbank-prod

# Step 3: Measure time to first successful API call
```

| DR Test | Date | RTO Achieved | RPO Achieved | Result | Sign-off |
|---|---|---|---|---|---|
| PostgreSQL primary failover | _TBD_ | < 5 min | < 1 min | ☐ PASS / ☐ FAIL | ☐ |
| Redis failover (Sentinel) | _TBD_ | < 30 s | — | ☐ PASS / ☐ FAIL | ☐ |
| RabbitMQ cluster failover | _TBD_ | < 60 s | 0 messages lost | ☐ PASS / ☐ FAIL | ☐ |
| Kubernetes node failure | _TBD_ | < 5 min | — | ☐ PASS / ☐ FAIL | ☐ |
| Full region failover | _TBD_ | < 15 min | < 5 min | ☐ PASS / ☐ FAIL | ☐ |

### 3.2 Backup Verification

```bash
# Verify latest backup exists and is recent
aws s3 ls s3://bloodbank-backups/postgres/ --recursive | sort | tail -5

# Test restore from backup to staging environment
pg_restore -d bloodbank_staging -F c bloodbank_$(date +%Y%m%d).dump

# Verify restored data integrity
psql -d bloodbank_staging -c "SELECT COUNT(*) FROM donors;"
psql -d bloodbank_staging -c "SELECT COUNT(*) FROM blood_units;"
```

| Backup Component | Frequency | Retention | Last Successful | Restore Tested | Sign-off |
|---|---|---|---|---|---|
| PostgreSQL full dump | Daily | 30 days | _TBD_ | ☐ | ☐ |
| PostgreSQL WAL (PITR) | Continuous | 7 days | _TBD_ | ☐ | ☐ |
| MinIO documents | Daily | 90 days | _TBD_ | ☐ | ☐ |
| Keycloak realm config | Weekly | 4 weeks | _TBD_ | ☐ | ☐ |
| Kubernetes config/secrets | On change | 10 versions | _TBD_ | ☐ | ☐ |

---

## Part 4 — Performance Validation

### 4.1 Load Test Results (Worldwide Scale)

Load tests must have been completed within the last 14 days under worldwide traffic simulation.

| Endpoint / Workflow | Target RPS | p50 | p95 | p99 | Error Rate | Pass? |
|---|---|---|---|---|---|---|
| `GET /api/v1/donors` | 500 | < 100 ms | < 500 ms | < 1 s | < 0.1% | ☐ |
| `POST /api/v1/collections` | 200 | < 200 ms | < 1 s | < 2 s | < 0.1% | ☐ |
| `GET /api/v1/inventory/units` | 1000 | < 100 ms | < 500 ms | < 1 s | < 0.1% | ☐ |
| `POST /api/v1/crossmatch` | 100 | < 500 ms | < 2 s | < 5 s | < 0.1% | ☐ |
| End-to-end collection workflow | 50 concurrent | < 5 min total | — | — | < 0.5% | ☐ |

### 4.2 Auto-Scaling Verification

- [ ] HPA triggers at ≥ 70% CPU and scales up within 2 minutes
- [ ] HPA scales down after 5 minutes of low utilisation
- [ ] Pod disruption budgets prevent simultaneous scale-down below minimum replicas
- [ ] Database connection pool handles peak concurrent connections without exhaustion

---

## Part 5 — SLA Documentation Summary

### 5.1 Production SLAs

| SLA | Target | Measurement | Reporting Period |
|---|---|---|---|
| **API Availability** | ≥ 99.9% uptime | Uptime Robot + internal health checks | Monthly |
| **API Response Time (p95)** | < 500 ms | Prometheus / Grafana | Real-time + weekly |
| **API Response Time (p99)** | < 2 s | Prometheus / Grafana | Real-time + weekly |
| **API Error Rate (5xx)** | < 0.1% | Prometheus / Grafana | Real-time |
| **Data Durability** | 99.9999% | PostgreSQL WAL + backups | Continuous |
| **RTO** (Recovery Time Objective) | < 5 minutes | DR test results | Tested quarterly |
| **RPO** (Recovery Point Objective) | < 1 minute | WAL streaming replication | Tested quarterly |
| **Planned Maintenance Window** | Max 4 h/month, off-peak | Change advisory board | Monthly |

### 5.2 Incident Response SLAs

| Severity | Definition | Response Time | Resolution Target |
|---|---|---|---|
| **P1 — Critical** | Full system outage or data corruption | 15 minutes | 4 hours |
| **P2 — High** | Core clinical workflow broken (collections, issuing, crossmatch) | 30 minutes | 8 hours |
| **P3 — Medium** | Non-critical feature unavailable; workaround exists | 2 hours | 24 hours |
| **P4 — Low** | Cosmetic defect or minor UX issue | Next business day | 72 hours |

### 5.3 Support Channel SLAs (Business Hours)

| Channel | Hours | Response Time |
|---|---|---|
| PagerDuty (on-call) | 24/7 | 15 minutes (P1/P2 only) |
| Slack `#bloodbank-ops-alerts` | 24/7 (automated) | Immediate (automated) |
| Email `support@bloodbank.example.com` | Business hours | 2 hours |
| Help Centre / Ticketing | Business hours | 4 hours |

### 5.4 SLA Reporting

- **Weekly**: SLO scorecard emailed to Operations Lead and Regional Leads
- **Monthly**: Full SLA report to Executive Sponsor and regional management
- **Quarterly**: SLA review board meeting — review targets and adjust if needed
- **Annually**: Contract SLA renewal review with all stakeholders

---

## Part 6 — Operations Readiness

### 6.1 On-Call Rotation

- [ ] On-call rotation schedule published for next 4 weeks (see `docs/operations/on-call-guide.md`)
- [ ] All on-call engineers have been briefed on runbooks
- [ ] PagerDuty schedules and escalation policies configured
- [ ] On-call engineers have production Kubernetes and database read access
- [ ] Emergency contact list distributed to all L2/L3/L4 personnel

### 6.2 Runbooks

| Runbook | Location | Reviewed By | Sign-off |
|---|---|---|---|
| Service Down | `docs/operations/runbooks/runbook-service-down.md` | _TBD_ | ☐ |
| Database Issues | `docs/operations/runbooks/runbook-database-issues.md` | _TBD_ | ☐ |
| High Error Rate | `docs/operations/runbooks/runbook-high-error-rate.md` | _TBD_ | ☐ |
| Security Incident | `docs/operations/runbooks/runbook-security-incident.md` | _TBD_ | ☐ |
| Data Corruption | `docs/operations/runbooks/runbook-data-corruption.md` | _TBD_ | ☐ |
| Emergency Rollback | `docs/operations/runbooks/runbook-rollback.md` | _TBD_ | ☐ |

### 6.3 Monitoring

- [ ] Grafana dashboards live and accessible to operations team
- [ ] All 14 service dashboards configured
- [ ] Alert rules tested and routing to correct Slack channels and PagerDuty
- [ ] Synthetic monitoring (Uptime Robot or equivalent) pinging all public endpoints every 1 minute
- [ ] Log retention configured: 90 days hot, 1 year cold (regulatory requirement)
- [ ] Distributed tracing (Jaeger/OpenTelemetry) sampling at 1% in production

---

## Part 7 — Final Go-Live Approval

### 7.1 Executive Approval Gate

All items in Parts 1–6 must be checked off before proceeding. This section is completed in the go-live meeting.

| Approver | Role | Signature | Date |
|---|---|---|---|
| _TBD_ | Executive Sponsor | | |
| _TBD_ | Technical Lead | | |
| _TBD_ | Operations Lead | | |
| _TBD_ | Security Lead | | |
| _TBD_ | Project Manager | | |

### 7.2 Go-Live Sequence

Once approval is received, execute in this exact order:

1. ☐ DNS switch: point production domains to worldwide load balancer
2. ☐ Enable public Donor Portal (`donor.bloodbank.example.com`)
3. ☐ Enable public Hospital Portal (`hospital.bloodbank.example.com`)
4. ☐ Verify all public endpoints reachable and returning HTTP 200
5. ☐ Start intensive 24-hour monitoring watch (see `docs/operations/on-call-guide.md`)
6. ☐ Send go-live announcement to all users (see `docs/operations/go-live-announcement.md`)
7. ☐ Notify regional leads that their branches are live
8. ☐ Post internal announcement in `#bloodbank-ops` Slack channel

### 7.3 Go-Live Abort Criteria

Immediately abort go-live and revert DNS if any of the following occur within the first 30 minutes:

- Any P1 incident detected
- API error rate > 1%
- Any service returning HTTP 500 or 503 for > 2 consecutive minutes
- Any security alert triggered by WAF or SIEM
- Database connection pool exhaustion in any region

Rollback procedure: `docs/operations/runbooks/runbook-rollback.md`

---

*This document must be archived (PDF signed copy) after go-live and retained for 7 years per FDA 21 CFR Part 11 requirements.*
