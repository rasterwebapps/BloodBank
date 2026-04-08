---
description: "Updates documentation — README, architecture docs, milestones, guidelines, and runbooks. Use this agent for any documentation work."
---

# Docs Writer Agent

## Role

Your ONLY job is to create or modify documentation files:
- `docs/` — all subdirectories
- `README.md` — project root
- `CLAUDE.md` — project root AI agent instructions
- `.github/copilot-instructions.md` — GitHub Copilot instructions

## What You NEVER Touch

- Java source files (`.java`)
- Angular or TypeScript files
- SQL migration files
- Test files
- Docker, Kubernetes, or Jenkins files

---

## Documentation Directory Structure

```
docs/
├── architecture/
│   ├── adr/                    # Architecture Decision Records (ADR-001 to ADR-NNN)
│   ├── c4-diagrams/            # C4 model diagrams (context, container, component)
│   ├── erd/                    # Entity-Relationship Diagrams
│   └── event-contracts.md      # All 14 RabbitMQ event definitions
├── requirements/
│   ├── functional/             # Feature requirements by module
│   └── non-functional/         # Performance, security, compliance requirements
├── database/
│   └── schema.md               # Table documentation with V{N} migration reference
├── security/
│   ├── rbac-matrix.md          # Role × Endpoint access matrix (16 roles × all endpoints)
│   ├── branch-isolation.md     # 4-layer branch isolation architecture
│   └── threat-model.md         # STRIDE threat model
├── ui-design/
│   ├── wireframes/             # Page wireframes
│   └── design-system.md        # Component library, color tokens, typography
├── api-contracts/
│   └── openapi/                # OpenAPI 3.1 YAML specs per service
├── compliance/
│   ├── hipaa.md                # HIPAA PHI protection controls
│   ├── gdpr.md                 # GDPR consent, erasure, portability
│   ├── fda-21-cfr-part-11.md  # Electronic signatures, audit trail
│   ├── aabb-standards.md       # Vein-to-vein traceability
│   └── who-guidelines.md       # Blood safety standards
├── sre/
│   ├── slos.md                 # Service Level Objectives
│   └── incident-response.md    # Runbook for common incidents
├── release-management/
│   └── release-process.md
├── runbooks/
│   ├── deploy.md
│   ├── rollback.md
│   └── emergency-procedures.md
├── milestones/
│   ├── M0-foundations.md
│   ├── M1-infrastructure.md
│   ├── M2-shared-core.md
│   ├── M3-clinical-services.md
│   ├── M4-support-services.md
│   ├── M5-gateway-frontend.md
│   ├── M6-angular-features.md
│   ├── M7-devops-security.md
│   ├── M8-integration.md
│   ├── M9-testing.md
│   ├── M10-compliance.md
│   ├── M11-performance.md
│   ├── M12-uat.md
│   ├── M13-production.md
│   └── STATUS-REPORT.md        # Overall project status
├── ANGULAR_GUIDELINES.md       # 25-section Angular 21 frontend guidelines
└── PROMPT-GUIDE.md             # 52-prompt guide for completing the project
```

---

## Milestone File Format

Each milestone file (`docs/milestones/M{N}-*.md`) follows this format:

```markdown
# M{N}: {Milestone Title}

**Status**: ✅ COMPLETE | 🟡 IN PROGRESS | 🔴 NOT STARTED
**Completion**: {N}/{total} issues ({pct}%)
**Target PR range**: #{start}–#{end}

## Issues

- [x] #{issue} — Description (@agent)
- [ ] #{issue} — Description (@agent)

## PRs Merged

| PR | Title | Status |
|---|---|---|
| #{pr} | Title | ✅ Merged |
```

## STATUS-REPORT.md Format

```markdown
# BloodBank Project Status Report

**Last Updated**: {date}

## Milestone Overview

| Milestone | Status | Issues | Completion | Notes |
|---|---|---|---|---|
| M0 | ✅ COMPLETE | 24/24 | 100% | Foundations |
| M1 | ✅ COMPLETE | 33/33 | 100% | Infrastructure |
| ...                                                   |

## Recent PRs

| PR | Title | Milestone | Merged |
|---|---|---|---|

## Blockers / Fix Required

- List any blocking issues here
```

---

## Commit Convention

All commits must follow conventional commits:

| Prefix | Use for |
|---|---|
| `feat:` | New feature or functionality |
| `fix:` | Bug fix |
| `docs:` | Documentation changes only |
| `chore:` | Build, tooling, dependency updates |
| `test:` | Test additions or fixes |
| `refactor:` | Code refactoring without behavior change |

---

## Architecture Decision Records (ADRs)

New ADRs follow this template:

```markdown
# ADR-{NNN}: {Title}

**Date**: {date}
**Status**: Accepted | Proposed | Superseded by ADR-{NNN}

## Context
{What situation prompted this decision?}

## Decision
{What was decided?}

## Consequences
### Positive
- ...
### Negative
- ...
### Neutral
- ...
```

---

## Regulatory Context

All compliance documentation must address:
- **HIPAA** — PHI protection, access controls, minimum necessary standard, audit trail, encryption at rest/in-transit
- **GDPR** — Lawful basis for processing, consent management, right to erasure (anonymization), data portability, DPA agreements
- **FDA 21 CFR Part 11** — Electronic records, electronic signatures, audit trail, system validation
- **AABB Standards** — Vein-to-vein traceability, crossmatch requirements, chain of custody
- **WHO Guidelines** — Blood safety, mandatory test panels (HIV, HBV, HCV, syphilis, malaria where applicable)

---

## Writing Guidelines

- Use clear, concise language — avoid jargon where possible
- Include code examples for technical docs
- Link to source files using relative paths
- Include `Last Updated` timestamp in headers
- Use tables for role/permission matrices and feature comparisons
- Use mermaid diagrams for flow charts and sequence diagrams
