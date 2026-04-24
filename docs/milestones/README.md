# 🩸 BloodBank — Development Milestones

This document defines the complete development roadmap for the BloodBank project, organized into 14 milestones (M0–M13) spanning approximately 28 weeks.

**Last Updated:** 2026-04-24 | **Status Report:** [STATUS-REPORT.md](STATUS-REPORT.md)

---

## Milestone Overview

| Milestone | Name | Duration | Dependencies | Status | Completion |
|---|---|---|---|---|---|
| **M0** | Project Setup & Architecture | 2 weeks | None | ✅ COMPLETE | 100% (24/24) |
| **M1** | Foundation (Build + DB + Shared Libs) | 2 weeks | M0 | ✅ COMPLETE | 100% (33/33) |
| **M2** | Core Services | 2 weeks | M1 | ✅ COMPLETE | 100% (54/54) |
| **M3** | Clinical Services | 2 weeks | M2 | ✅ COMPLETE | 100% (43/43) |
| **M4** | Support Services | 2 weeks | M2 | ✅ COMPLETE | 100% (66/66) |
| **M5** | API Gateway + Frontend | 3 weeks | M2, M3, M4 | 🟡 NEARLY COMPLETE | 98% (51/52) — M5-023 i18n missing |
| **M6** | Integration + Security | 2 weeks | M5 | ✅ COMPLETE | 100% (30/30) |
| **M7** | Infrastructure (Docker/K8s/CI) | 2 weeks | M2 | ✅ COMPLETE | 100% (46/46) |
| **M8** | Performance Testing | 2 weeks | M6, M7 | ✅ COMPLETE | 100% (28/28) |
| **M9** | UAT + Compliance | 2 weeks | M8 | 🟡 IN PROGRESS | 40% auto + 60% manual READY (16/40 auto) |
| **M10** | Pilot (1 Branch) | 2 weeks | M9 | 🟡 READY | 41% infra (11/27) + 59% ops READY |
| **M11** | Regional Rollout | 4 weeks | M10 | 🟡 READY | 15% planning (5/34) + 85% ops READY |
| **M12** | Worldwide Launch | 1 week | M11 | 🟡 READY | 65% (13/20) + 7 ops READY |
| **M13** | Post-Launch & Continuous Improvement | Ongoing | M12 | 🟡 DOCS PREPARED | 0% operational (docs/runbooks/SRE guide ready) |

**Overall: 420/530 issues completed (~79%)**

---

## Dependency Graph

```
M0 (Setup)
  └── M1 (Foundation)
        ├── M2 (Core Services)
        │     ├── M3 (Clinical)
        │     ├── M4 (Support)
        │     └── M7 (Infrastructure) ←── can start in parallel
        │           │
        │     M5 (Gateway + Frontend) ←── needs M2+M3+M4
        │           │
        │     M6 (Integration + Security)
        │           │
        │     M8 (Performance)
        │           │
        │     M9 (UAT + Compliance)
        │           │
        │     M10 (Pilot)
        │           │
        │     M11 (Regional)
        │           │
        │     M12 (Worldwide)
        │           │
        └── M13 (Post-Launch)
```

---

## Performance Targets (M8 Exit Criteria)

| Metric | Target |
|---|---|
| API Response Time (P95) | < 200ms |
| API Response Time (P99) | < 500ms |
| Throughput | 500 req/sec sustained |
| Database Query (P95) | < 100ms |
| Uptime | 99.9% |
| Recovery Time (RTO) | < 15 minutes |
| Recovery Point (RPO) | < 5 minutes |
| Deployment Downtime | Zero (rolling/blue-green) |

---

See individual milestone files (M0–M13) for detailed issue breakdowns.
