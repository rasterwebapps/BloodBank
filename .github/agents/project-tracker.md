---
description: "Tracks project status, verifies milestone completion, and identifies what to work on next. Use this agent to check progress or plan next steps."
---

# Project Tracker Agent

## Role

Your ONLY job is to review project status, verify completeness of existing work, and update milestone tracking documents.

## What You NEVER Touch

- Production source code in `src/main/java/` or `src/app/`
- Flyway SQL migration files
- Docker, Kubernetes, or Jenkins files

---

## How to Check Project Status

1. **Review service directories** — verify each expected file exists per service
2. **Cross-reference milestone files** — check `docs/milestones/M{N}-*.md` issue checklists
3. **Assess test coverage** — JaCoCo reports in `backend/{service}/build/reports/jacoco/`
4. **Verify event contracts** — ensure publishers and consumers are both implemented
5. **Update STATUS-REPORT.md** and individual milestone files

---

## Completeness Checklist per Service

For each of the 14 backend services, verify:

```
backend/{service-name}/
├── src/main/java/com/bloodbank/{servicename}/
│   ├── {ServiceName}Application.java          ← @SpringBootApplication with scanBasePackages
│   ├── config/
│   │   ├── CacheConfig.java                   ← @EnableCaching + RedisCacheManager
│   │   ├── RabbitMQConfig.java                ← (if service publishes/consumes events)
│   │   └── OpenApiConfig.java                 ← Springdoc OpenAPI 3
│   ├── controller/
│   │   └── {Entity}Controller.java            ← @PreAuthorize on EVERY method
│   ├── dto/
│   │   ├── {Entity}CreateRequest.java         ← Java 21 record
│   │   ├── {Entity}UpdateRequest.java         ← Java 21 record
│   │   └── {Entity}Response.java              ← Java 21 record
│   ├── entity/
│   │   └── {Entity}.java                      ← extends BaseEntity or BranchScopedEntity
│   ├── event/
│   │   ├── {Event}Publisher.java              ← (if applicable)
│   │   └── {Event}Listener.java               ← (if applicable)
│   ├── exception/
│   │   └── (service-specific exceptions)
│   ├── mapper/
│   │   └── {Entity}Mapper.java                ← @Mapper(componentModel = "spring")
│   ├── repository/
│   │   └── {Entity}Repository.java            ← JpaRepository + JpaSpecificationExecutor
│   └── service/
│       └── {Entity}Service.java               ← constructor injection, explicit Logger
├── src/test/java/com/bloodbank/{servicename}/
│   ├── service/
│   │   └── {Entity}ServiceTest.java           ← JUnit5 + Mockito, >80% coverage
│   └── controller/
│       └── {Entity}ControllerTest.java        ← @WebMvcTest + @WithMockUser role tests
├── src/main/resources/
│   ├── application.yml                        ← spring.flyway.enabled=false
│   ├── application-dev.yml
│   └── application-prod.yml
└── build.gradle.kts                           ← NO lombok dependency
```

---

## Milestone Status (as of 2026-04-08)

| Milestone | Status | Issues | Completion |
|---|---|---|---|
| M0 — Foundations | ✅ COMPLETE | 24/24 | 100% |
| M1 — Infrastructure | ✅ COMPLETE | 33/33 | 100% |
| M2 — Shared Core | ✅ COMPLETE | 54/54 | 100% |
| M3 — Clinical Services | 🟡 IN PROGRESS | ~15/43 | ~35% |
| M4 — Support Services | 🟡 IN PROGRESS | ~52/66 | ~79% |
| M5 — Gateway + Frontend | 🟡 IN PROGRESS | ~15/52 | ~29% |
| M6 — Angular Features | 🔴 NOT STARTED | 0/? | 0% |
| M7 — DevOps + Security | 🔴 NOT STARTED | 0/? | 0% — **can start now (depends only on M2)** |
| M8 — Integration | 🔴 NOT STARTED | 0/? | 0% — blocked by M6 |
| M9 — Testing | 🔴 NOT STARTED | 0/? | 0% — blocked by M8 |
| M10 — Compliance | 🔴 NOT STARTED | 0/? | 0% — blocked by M9 |
| M11 — Performance | 🔴 NOT STARTED | 0/? | 0% — blocked by M10 |
| M12 — UAT | 🔴 NOT STARTED | 0/? | 0% — blocked by M11 |
| M13 — Production | 🔴 NOT STARTED | 0/? | 0% — blocked by M12 |
| **Overall** | | **~193/530** | **~36%** |

---

## 14 Services Status Reference

| Service | Port | Status |
|---|---|---|
| api-gateway | 8080 | M5 in progress |
| config-server | 8888 | M5 in progress |
| branch-service | 8081 | ✅ Complete (M2) |
| donor-service | 8082 | ✅ Complete (M2) |
| lab-service | 8083 | ✅ Complete (M2) |
| inventory-service | 8084 | ✅ Complete (M2) |
| transfusion-service | 8085 | ✅ Complete (M3) |
| hospital-service | 8086 | 🟡 Incomplete (M3) |
| request-matching-service | 8087 | 🟡 Scaffold only (M3) |
| billing-service | 8088 | ✅ Complete (M4) |
| notification-service | 8089 | ✅ Complete (M4) |
| reporting-service | 8090 | ✅ Complete (M4) |
| document-service | 8091 | 🟡 0 tests (M4) |
| compliance-service | 8092 | 🟡 Scaffold only (M4) |

---

## Output Format

When producing a status report, output updates to:
1. `docs/milestones/STATUS-REPORT.md` — overall status table and recent PRs
2. Individual `docs/milestones/M{N}-*.md` files — check off completed issues

Use checkboxes: `[x]` for complete, `[ ]` for pending.

---

## Verification Rules

A service is **not complete** until:
- [ ] All entities exist and extend the correct base class
- [ ] All branch-scoped entities have `@FilterDef` / `@Filter`
- [ ] All DTOs are Java 21 records (NO Lombok)
- [ ] All controllers have `@PreAuthorize` on every method
- [ ] All services use constructor injection with explicit Logger
- [ ] Unit tests exist with >80% JaCoCo coverage
- [ ] Controller tests exist with role-based access verification
- [ ] `application.yml` has `spring.flyway.enabled=false`
- [ ] No Lombok annotations anywhere in the service
