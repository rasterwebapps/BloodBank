# 🩸 BloodBank — Development Milestones

This document defines the complete development roadmap for the BloodBank project, organized into 14 milestones (M0–M13) spanning approximately 28 weeks.

**Last Updated:** 2026-04-07 | **Status Report:** [STATUS-REPORT.md](STATUS-REPORT.md)

---

## Milestone Overview

| Milestone | Name | Duration | Dependencies | Status | Completion |
|---|---|---|---|---|---|
| **M0** | Project Setup & Architecture | 2 weeks | None | ✅ COMPLETE | 100% (24/24) |
| **M1** | Foundation (Build + DB + Shared Libs) | 2 weeks | M0 | ✅ COMPLETE | 100% (33/33) |
| **M2** | Core Services | 2 weeks | M1 | ✅ COMPLETE | 100% (54/54) |
| **M3** | Clinical Services | 2 weeks | M2 | 🟡 IN PROGRESS | ~35% (15/43) |
| **M4** | Support Services | 2 weeks | M2 | 🟡 PARTIAL | ~79% (52/66) |
| **M5** | API Gateway + Frontend | 3 weeks | M2, M3, M4 | 🔴 NOT STARTED | 0% (0/52) |
| **M6** | Integration + Security | 2 weeks | M5 | 🔴 NOT STARTED | 0% (0/30) |
| **M7** | Infrastructure (Docker/K8s/CI) | 2 weeks | M2 | 🔴 NOT STARTED | 0% (0/46) |
| **M8** | Performance Testing | 2 weeks | M6, M7 | 🔴 NOT STARTED | 0% (0/28) |
| **M9** | UAT + Compliance | 2 weeks | M8 | 🔴 NOT STARTED | 0% (0/40) |
| **M10** | Pilot (1 Branch) | 2 weeks | M9 | 🔴 NOT STARTED | 0% (0/27) |
| **M11** | Regional Rollout | 4 weeks | M10 | 🔴 NOT STARTED | 0% (0/34) |
| **M12** | Worldwide Launch | 1 week | M11 | 🔴 NOT STARTED | 0% (0/20) |
| **M13** | Post-Launch & Continuous Improvement | Ongoing | M12 | 🔴 NOT STARTED | 0% (0/33) |

**Overall: 178/530 issues completed (~34%)**

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
