# 🩸 BloodBank — Development Milestones

This document defines the complete development roadmap for the BloodBank project, organized into 14 milestones (M0–M13) spanning approximately 28 weeks.

---

## Milestone Overview

| Milestone | Name | Duration | Dependencies |
|---|---|---|---|
| **M0** | Project Setup & Architecture | 2 weeks | None |
| **M1** | Foundation (Build + DB + Shared Libs) | 2 weeks | M0 |
| **M2** | Core Services | 2 weeks | M1 |
| **M3** | Clinical Services | 2 weeks | M2 |
| **M4** | Support Services | 2 weeks | M2 |
| **M5** | API Gateway + Frontend | 3 weeks | M2, M3, M4 |
| **M6** | Integration + Security | 2 weeks | M5 |
| **M7** | Infrastructure (Docker/K8s/CI) | 2 weeks | M2 |
| **M8** | Performance Testing | 2 weeks | M6, M7 |
| **M9** | UAT + Compliance | 2 weeks | M8 |
| **M10** | Pilot (1 Branch) | 2 weeks | M9 |
| **M11** | Regional Rollout | 4 weeks | M10 |
| **M12** | Worldwide Launch | 1 week | M11 |
| **M13** | Post-Launch & Continuous Improvement | Ongoing | M12 |

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
