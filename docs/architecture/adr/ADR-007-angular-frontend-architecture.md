# ADR-007: Angular 21 Frontend Architecture

**Status:** Accepted
**Date:** 2026-04-07
**Decision Makers:** Architecture Team

## Context

The BloodBank system requires a comprehensive frontend application to serve 3 portals (Staff, Hospital, Donor) with 17 feature modules, 16 user roles, and healthcare regulatory compliance (HIPAA, GDPR, AABB). The frontend must integrate with:
- API Gateway (port 8080) for all backend communication
- Keycloak 26+ for authentication/authorization (OIDC)
- 14 backend microservices via REST APIs

## Decision

We adopt **Angular 21** as the frontend framework with the following architectural decisions:

### 1. Angular 21 with Zoneless Change Detection

- Use **Angular 21** (latest LTS-aligned release)
- Enable **zoneless change detection** (no Zone.js)
- Use `ChangeDetectionStrategy.OnPush` on every component
- Use Angular **Signals** (`signal()`, `computed()`, `effect()`) for all reactive state

**Rationale:** Zoneless + signals provide better performance and simpler mental model than Zone.js + RxJS. Signals are Angular's recommended state primitive from v17+.

### 2. Standalone Components Only (No NgModules)

- Every component, directive, and pipe MUST be `standalone: true`
- No `NgModule` classes anywhere in the project
- Use `loadComponent` and `loadChildren` for lazy loading

**Rationale:** Standalone components are Angular's default since v19. They simplify the dependency graph and improve tree-shaking.

### 3. Angular Material (M3) + Tailwind CSS

- **Angular Material** for all interactive UI components (buttons, dialogs, forms, tables, menus, snackbars)
- **Tailwind CSS** for layout, spacing, typography, and responsive design
- **Tailwind Preflight disabled** to prevent conflicts with Material
- NEVER override Material component styles with Tailwind utility classes

**Rationale:** Material provides accessible, well-tested healthcare-appropriate components. Tailwind provides efficient layout utilities without the overhead of a full CSS framework.

### 4. Signal-Based Inputs/Outputs

- Use `input()` / `input.required()` instead of `@Input()` decorator
- Use `output()` instead of `@Output()` decorator
- Use `inject()` function instead of constructor injection in components

**Rationale:** Signal-based APIs are type-safe, enable fine-grained change detection, and are the Angular team's recommended approach.

### 5. Single SPA with Role-Based Portals

- One Angular application serves all 3 portals
- Routes are role-filtered: Staff (`/staff/*`), Hospital (`/hospital/*`), Donor (`/donor/*`)
- `roleGuard` on every route verifies user has required role(s)
- Navigation menu items filtered by authenticated user's roles

**Rationale:** A single SPA simplifies deployment, sharing of common code, and consistency. Role-based routing provides portal separation without multiple builds.

### 6. Keycloak Integration via keycloak-angular

- Use `keycloak-angular` library with `APP_INITIALIZER`
- Tokens managed in-memory by Keycloak JS adapter (never localStorage)
- Bearer token automatically injected via HTTP interceptor
- Branch context (`branch_id`) extracted from JWT claims

**Rationale:** Keycloak is already the backend's identity provider. In-memory token storage is more secure than localStorage for healthcare applications.

### 7. Template Control Flow (@if, @for, @switch)

- Use Angular 21 built-in control flow syntax
- NEVER use structural directives (`*ngIf`, `*ngFor`, `[ngSwitch]`)
- Every `@for` MUST have a `track` expression

**Rationale:** Built-in control flow is more performant, more readable, and the Angular team's recommended approach since v17.

## Consequences

### Positive
- Optimal performance via zoneless + OnPush + signals
- Consistent design via Material Design 3
- Healthcare-grade accessibility via Material's ARIA support
- Simple state management without NgRx overhead
- Type-safe component APIs via signal inputs

### Negative
- Developers must learn Angular 21 signal APIs
- Tailwind Preflight must be disabled (some default resets unavailable)
- Material + Tailwind boundary requires discipline (see ANGULAR_GUIDELINES.md Section 6)

### Risks
- Angular 21 is the latest version — community resources may be limited initially
- Tailwind + Material coexistence requires documented rules to prevent conflicts

## Related Documents

- `docs/ANGULAR_GUIDELINES.md` — comprehensive frontend development guidelines
- `.claude/skills/create-angular-feature.md` — feature module scaffolding skill
- `docs/milestones/M5-gateway-frontend.md` — frontend implementation milestone
