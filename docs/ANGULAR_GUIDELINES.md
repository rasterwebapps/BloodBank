# Angular 21 Frontend Guidelines — BloodBank Project

> **Scope:** This document is the single source of truth for all Angular frontend development in the BloodBank project. It covers architecture, coding conventions, design system, security, accessibility, testing, i18n, and performance. Apply these rules when starting frontend development, re-engineering, or reviewing code. Update on demand.

---

## Table of Contents

1. [Tech Stack & Versions](#1-tech-stack--versions)
2. [Project Scaffolding](#2-project-scaffolding)
3. [Architecture & Directory Structure](#3-architecture--directory-structure)
4. [Angular 21 Coding Conventions](#4-angular-21-coding-conventions)
5. [Design System — Angular Material + Tailwind CSS](#5-design-system--angular-material--tailwind-css)
6. [Material + Tailwind Coexistence Rules](#6-material--tailwind-coexistence-rules)
7. [Theme & Color System](#7-theme--color-system)
8. [Typography](#8-typography)
9. [Layout Shell](#9-layout-shell)
10. [Component Patterns](#10-component-patterns)
11. [State Management](#11-state-management)
12. [Forms](#12-forms)
13. [Data Tables](#13-data-tables)
14. [Dashboard & Charts](#14-dashboard--charts)
15. [Security](#15-security)
16. [Accessibility (a11y)](#16-accessibility-a11y)
17. [Internationalization (i18n)](#17-internationalization-i18n)
18. [Error Handling UX](#18-error-handling-ux)
19. [Performance](#19-performance)
20. [Testing Standards](#20-testing-standards)
21. [File Naming & Import Conventions](#21-file-naming--import-conventions)
22. [Healthcare-Specific UI Rules](#22-healthcare-specific-ui-rules)
23. [Common Mistakes to Avoid](#23-common-mistakes-to-avoid)
24. [Build & Deployment](#24-build--deployment)
25. [17 Feature Modules](#25-17-feature-modules)

---

## 1. Tech Stack & Versions

| Technology | Version | Purpose |
|---|---|---|
| Angular | **21** | Frontend framework |
| Angular Material | **21** (M3 design tokens) | UI component library |
| Tailwind CSS | **4.x** | Utility-first CSS for layout & spacing |
| TypeScript | **5.8+** | Type safety |
| Keycloak JS | **26+** | OIDC/OAuth2 authentication |
| keycloak-angular | **latest** | Angular Keycloak integration |
| Chart.js + ng2-charts | latest | Dashboard charts |
| date-fns | latest | Date manipulation (lightweight) |
| Angular CDK | **21** | Virtual scrolling, drag-drop, a11y |
| Playwright | latest | E2E testing |
| Jest / Karma + Jasmine | latest | Unit testing |

### ⛔ Do NOT Use

- **RxJS BehaviorSubject** for state — use Angular Signals
- **NgModules** — use standalone components
- **Zone.js** — use zoneless change detection
- **Moment.js** — use date-fns
- **Bootstrap / jQuery** — use Material + Tailwind
- **NgRx / Akita** — use Signals + computed for state
- **`@Input()` / `@Output()` decorators** — use `input()` / `output()` signal APIs

---

## 2. Project Scaffolding

```bash
# Initial scaffold
ng new bloodbank-ui --standalone --style=scss --routing --ssr=false --zoneless
cd bloodbank-ui

# Add Angular Material (M3 theme)
ng add @angular/material --theme=custom --typography=true --animations=true

# Add Tailwind CSS 4
npm install tailwindcss @tailwindcss/postcss

# Add Keycloak
npm install keycloak-js keycloak-angular

# Add Charts
npm install chart.js ng2-charts

# Add date-fns
npm install date-fns
```

### Path Aliases (tsconfig.json)

```json
{
  "compilerOptions": {
    "paths": {
      "@core/*": ["src/app/core/*"],
      "@shared/*": ["src/app/shared/*"],
      "@features/*": ["src/app/features/*"],
      "@env/*": ["src/environments/*"],
      "@models/*": ["src/app/shared/models/*"]
    }
  }
}
```

---

## 3. Architecture & Directory Structure

```
frontend/bloodbank-ui/
├── src/
│   ├── app/
│   │   ├── app.component.ts              # Root (standalone)
│   │   ├── app.config.ts                 # provideRouter, provideHttpClient, etc.
│   │   ├── app.routes.ts                 # Top-level lazy routes
│   │   ├── core/                         # Singleton services & guards
│   │   │   ├── auth/
│   │   │   │   ├── auth.service.ts       # Keycloak wrapper
│   │   │   │   ├── auth.interceptor.ts   # JWT Bearer token
│   │   │   │   └── auth.init.ts          # APP_INITIALIZER for Keycloak
│   │   │   ├── guards/
│   │   │   │   ├── role.guard.ts         # Role-based route guard
│   │   │   │   └── branch.guard.ts       # Branch context guard
│   │   │   ├── interceptors/
│   │   │   │   ├── error.interceptor.ts  # Global error handler
│   │   │   │   └── branch.interceptor.ts # X-Branch-Id header injection
│   │   │   ├── services/
│   │   │   │   ├── notification.service.ts
│   │   │   │   ├── branch-context.service.ts
│   │   │   │   └── theme.service.ts
│   │   │   └── models/
│   │   │       ├── user.model.ts
│   │   │       └── role.enum.ts
│   │   ├── shared/                       # Reusable components & pipes
│   │   │   ├── components/
│   │   │   │   ├── data-table/
│   │   │   │   ├── search-bar/
│   │   │   │   ├── form-field/
│   │   │   │   ├── status-badge/
│   │   │   │   ├── confirm-dialog/
│   │   │   │   ├── loading-skeleton/
│   │   │   │   ├── empty-state/
│   │   │   │   ├── error-card/
│   │   │   │   └── blood-group-badge/
│   │   │   ├── layout/
│   │   │   │   ├── shell/               # Main layout wrapper
│   │   │   │   ├── sidenav/
│   │   │   │   ├── topbar/
│   │   │   │   ├── breadcrumb/
│   │   │   │   └── footer/
│   │   │   ├── pipes/
│   │   │   │   ├── blood-group.pipe.ts
│   │   │   │   ├── date-ago.pipe.ts
│   │   │   │   └── truncate.pipe.ts
│   │   │   ├── directives/
│   │   │   │   ├── has-role.directive.ts
│   │   │   │   └── auto-focus.directive.ts
│   │   │   └── models/
│   │   │       ├── api-response.model.ts
│   │   │       ├── paged-response.model.ts
│   │   │       └── branch.model.ts
│   │   └── features/                     # Lazy-loaded feature modules
│   │       ├── dashboard/
│   │       ├── donor/
│   │       ├── collection/
│   │       ├── camp/
│   │       ├── lab/
│   │       ├── inventory/
│   │       ├── transfusion/
│   │       ├── hospital/
│   │       ├── billing/
│   │       ├── compliance/
│   │       ├── notification/
│   │       ├── reporting/
│   │       ├── branch/
│   │       ├── user-management/
│   │       ├── document/
│   │       ├── emergency/
│   │       └── donor-portal/
│   ├── assets/
│   │   ├── i18n/                         # Translation JSON files
│   │   │   ├── en.json
│   │   │   ├── es.json
│   │   │   └── fr.json
│   │   └── images/
│   ├── environments/
│   │   ├── environment.ts
│   │   ├── environment.development.ts
│   │   └── environment.production.ts
│   ├── styles/
│   │   ├── _variables.scss               # Design tokens
│   │   ├── _material-theme.scss          # M3 custom theme
│   │   ├── _tailwind.scss                # Tailwind imports
│   │   ├── _typography.scss
│   │   ├── _healthcare-colors.scss       # Blood/status colors
│   │   └── styles.scss                   # Global entry point
│   └── index.html
├── angular.json
├── tsconfig.json
├── tailwind.config.ts
├── package.json
└── Dockerfile
```

### 3 Portals (Same Angular App, Role-Filtered)

| Portal | URL Prefix | Target Users |
|---|---|---|
| Staff Portal | `/staff/` | All staff roles (12 roles) |
| Hospital Portal | `/hospital/` | HOSPITAL_USER |
| Donor Portal | `/donor/` | DONOR (self-service) |

The same Angular app serves all 3 portals. Routes and navigation are filtered by the authenticated user's role(s).

---

## 4. Angular 21 Coding Conventions

### Standalone Components Only

```typescript
@Component({
  selector: 'app-donor-list',
  standalone: true,
  imports: [CommonModule, RouterModule, MatTableModule, MatPaginatorModule],
  templateUrl: './donor-list.component.html',
  styleUrl: './donor-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DonorListComponent { }
```

**Rules:**
- Every component MUST have `standalone: true`
- Every component MUST have `changeDetection: ChangeDetectionStrategy.OnPush`
- NEVER create `NgModule` classes
- NEVER use `declarations` arrays

### Signals for State (NOT RxJS BehaviorSubject)

```typescript
// ✅ Correct — Signals
readonly donors = signal<Donor[]>([]);
readonly loading = signal(false);
readonly selectedDonor = signal<Donor | null>(null);
readonly donorCount = computed(() => this.donors().length);

// ❌ Wrong — BehaviorSubject
private donors$ = new BehaviorSubject<Donor[]>([]);
```

### inject() Function (NOT Constructor Injection in Components)

```typescript
// ✅ Correct — inject()
export class DonorListComponent {
  private readonly donorService = inject(DonorService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
}

// ❌ Wrong — constructor injection in components
export class DonorListComponent {
  constructor(private donorService: DonorService) { }
}
```

### Signal-Based Inputs/Outputs (Angular 21)

```typescript
// ✅ Correct — signal input/output APIs
export class DonorCardComponent {
  readonly donor = input.required<Donor>();
  readonly showActions = input(true);
  readonly donorSelected = output<Donor>();
}

// ❌ Wrong — decorator-based
export class DonorCardComponent {
  @Input() donor!: Donor;
  @Output() donorSelected = new EventEmitter<Donor>();
}
```

### Template Control Flow (Angular 21)

```html
<!-- ✅ Correct — built-in control flow -->
@if (loading()) {
  <app-loading-skeleton />
} @else if (isEmpty()) {
  <app-empty-state message="No donors found" />
} @else {
  @for (donor of donors(); track donor.id) {
    <app-donor-card [donor]="donor" />
  } @empty {
    <p>No donors match your search.</p>
  }
}

@switch (donor().bloodGroup) {
  @case ('A_POSITIVE') { <span class="text-red-600">A+</span> }
  @case ('O_NEGATIVE') { <span class="text-blue-600">O−</span> }
  @default { <span>{{ donor().bloodGroup }}</span> }
}

<!-- ❌ Wrong — structural directives -->
<div *ngIf="loading">...</div>
<div *ngFor="let donor of donors">...</div>
<div [ngSwitch]="status">...</div>
```

### HTTP Services — firstValueFrom

```typescript
@Injectable({ providedIn: 'root' })
export class DonorService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/donors`;

  async list(page = 0, size = 20): Promise<PagedResponse<Donor>> {
    return firstValueFrom(
      this.http.get<PagedResponse<Donor>>(this.baseUrl, {
        params: { page: page.toString(), size: size.toString() }
      })
    );
  }

  async getById(id: string): Promise<ApiResponse<Donor>> {
    return firstValueFrom(this.http.get<ApiResponse<Donor>>(`${this.baseUrl}/${id}`));
  }
}
```

---

## 5. Design System — Angular Material + Tailwind CSS

### Division of Responsibilities

| Layer | Technology | Use For |
|---|---|---|
| **Interactive Components** | Angular Material (M3) | Buttons, dialogs, tables, forms, menus, tabs, snackbars, datepickers, autocomplete |
| **Layout & Spacing** | Tailwind CSS | Flexbox, grid, padding, margin, width, height, responsive breakpoints |
| **Typography** | Tailwind CSS | Font sizes, weights, line heights, text colors |
| **Custom Colors** | CSS custom properties | Healthcare-specific colors (blood types, statuses, severity) |
| **Icons** | Material Icons (filled) | All UI icons |
| **Theming** | Angular Material M3 tokens | Primary, secondary, tertiary, error palettes |

### What Tailwind Handles (Layout Only)

```html
<!-- ✅ Tailwind for layout -->
<div class="flex items-center gap-4 p-6">
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
    <div class="w-full max-w-sm rounded-lg shadow-sm">
```

### What Material Handles (Components Only)

```html
<!-- ✅ Material for interactive components -->
<mat-form-field appearance="outline">
  <mat-label>Blood Group</mat-label>
  <mat-select formControlName="bloodGroup">
    <mat-option value="A_POSITIVE">A+</mat-option>
  </mat-select>
</mat-form-field>

<button mat-flat-button color="primary">Register Donor</button>
<mat-paginator [pageSize]="20" [pageSizeOptions]="[10, 20, 50]" />
```

---

## 6. Material + Tailwind Coexistence Rules

### ⛔ NEVER Override Material Component Internals with Tailwind

```html
<!-- ❌ NEVER — breaks Material theming -->
<button mat-flat-button class="bg-blue-500 text-white rounded-full">Save</button>
<mat-form-field class="border-2 border-red-500">

<!-- ✅ CORRECT — use Material color system -->
<button mat-flat-button color="primary">Save</button>
<button mat-flat-button color="warn">Delete</button>
```

### ✅ DO Use Tailwind for Wrapper Layout

```html
<!-- ✅ Tailwind wraps Material components -->
<div class="flex items-center gap-4 p-4">
  <mat-form-field appearance="outline" class="flex-1">
    <mat-label>Search donors</mat-label>
    <input matInput />
  </mat-form-field>
  <button mat-flat-button color="primary">Search</button>
</div>
```

### ✅ Disable Tailwind Preflight

Tailwind Preflight resets conflicting with Material. Disable it:

```typescript
// tailwind.config.ts
export default {
  corePlugins: {
    preflight: false,  // MUST disable — conflicts with Material
  },
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        // Custom healthcare colors — see Section 7
      },
    },
  },
};
```

---

## 7. Theme & Color System

### M3 Material Custom Theme

```scss
// src/styles/_material-theme.scss
@use '@angular/material' as mat;

$bloodbank-primary: mat.m3-define-palette(mat.$red-palette);     // Blood red
$bloodbank-secondary: mat.m3-define-palette(mat.$blue-palette);  // Medical blue
$bloodbank-tertiary: mat.m3-define-palette(mat.$green-palette);  // Success green

$bloodbank-theme: mat.m3-define-theme((
  color: (
    primary: $bloodbank-primary,
    secondary: $bloodbank-secondary,
    tertiary: $bloodbank-tertiary,
  ),
  typography: mat.m3-define-typography((
    brand-family: 'Inter, Roboto, sans-serif',
    plain-family: 'Inter, Roboto, sans-serif',
  )),
  density: 0,
));

html {
  @include mat.all-component-themes($bloodbank-theme);
}
```

### Healthcare-Specific CSS Custom Properties

```scss
// src/styles/_healthcare-colors.scss
:root {
  /* Blood Group Colors */
  --bg-a-positive:  #DC2626;    /* Red-600 */
  --bg-a-negative:  #B91C1C;    /* Red-700 */
  --bg-b-positive:  #2563EB;    /* Blue-600 */
  --bg-b-negative:  #1D4ED8;    /* Blue-700 */
  --bg-ab-positive: #7C3AED;    /* Violet-600 */
  --bg-ab-negative: #6D28D9;    /* Violet-700 */
  --bg-o-positive:  #059669;    /* Emerald-600 */
  --bg-o-negative:  #047857;    /* Emerald-700 */

  /* Clinical Status Colors */
  --status-available:   #16A34A; /* Green-600 — unit available */
  --status-reserved:    #D97706; /* Amber-600 — unit reserved */
  --status-issued:      #2563EB; /* Blue-600 — unit issued */
  --status-quarantine:  #DC2626; /* Red-600 — unit quarantined */
  --status-expired:     #6B7280; /* Gray-500 — unit expired */
  --status-disposed:    #374151; /* Gray-700 — unit disposed */
  --status-tested:      #0891B2; /* Cyan-600 — testing complete */
  --status-pending:     #F59E0B; /* Amber-500 — pending action */

  /* Severity / Priority Colors */
  --severity-critical:  #DC2626; /* Red-600 */
  --severity-urgent:    #EA580C; /* Orange-600 */
  --severity-routine:   #2563EB; /* Blue-600 */
  --severity-low:       #16A34A; /* Green-600 */

  /* Donation / Collection Status */
  --donation-success:   #16A34A; /* Successful */
  --donation-deferred:  #D97706; /* Deferred */
  --donation-adverse:   #DC2626; /* Adverse reaction */
}
```

### Tailwind Custom Colors

```typescript
// tailwind.config.ts — extend colors
theme: {
  extend: {
    colors: {
      bloodbank: {
        primary: '#DC2626',     // Blood red
        secondary: '#2563EB',   // Medical blue
        success: '#16A34A',
        warning: '#D97706',
        danger: '#DC2626',
        info: '#0891B2',
      },
    },
  },
},
```

---

## 8. Typography

| Element | Tailwind Class | Size | Weight |
|---|---|---|---|
| Page title | `text-2xl font-bold` | 24px | 700 |
| Section header | `text-xl font-semibold` | 20px | 600 |
| Card title | `text-lg font-medium` | 18px | 500 |
| Body text | `text-base font-normal` | 16px | 400 |
| Table cell | `text-sm font-normal` | 14px | 400 |
| Caption / helper | `text-xs text-gray-500` | 12px | 400 |
| Badge text | `text-xs font-semibold uppercase` | 12px | 600 |

### Font Stack

```scss
// Primary: Inter (modern, healthcare-friendly)
// Fallback: Roboto (Material default), then system fonts
font-family: 'Inter', 'Roboto', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
```

---

## 9. Layout Shell

### Responsive Breakpoints

| Breakpoint | Tailwind | Width | Layout |
|---|---|---|---|
| Mobile | `sm:` | 640px | Collapsed sidenav (hamburger), stacked content |
| Tablet | `md:` | 768px | Rail sidenav (icons only), 2-column grid |
| Desktop | `lg:` | 1024px | Full sidenav (expanded), 3-4 column grid |
| Wide | `xl:` | 1280px | Full sidenav, 4-column grid with extra sidebar |

### Shell Component Structure

```html
<!-- app-shell.component.html -->
<div class="flex h-screen overflow-hidden bg-gray-50">
  <!-- Sidenav -->
  <app-sidenav [collapsed]="sidenavCollapsed()" />

  <!-- Main content area -->
  <div class="flex-1 flex flex-col overflow-hidden">
    <!-- Top bar -->
    <app-topbar
      (toggleSidenav)="toggleSidenav()"
      [currentBranch]="currentBranch()"
      [user]="currentUser()" />

    <!-- Breadcrumb -->
    <app-breadcrumb class="px-6 py-2 bg-white border-b" />

    <!-- Page content -->
    <main class="flex-1 overflow-y-auto p-6">
      <router-outlet />
    </main>
  </div>
</div>
```

### Sidenav Navigation (Role-Filtered)

```typescript
// Navigation items filtered by user roles
readonly navItems: NavItem[] = [
  { label: 'Dashboard',     icon: 'dashboard',       route: '/staff/dashboard',     roles: ['*'] },
  { label: 'Donors',        icon: 'people',          route: '/staff/donors',        roles: ['RECEPTIONIST', 'PHLEBOTOMIST', 'BRANCH_ADMIN'] },
  { label: 'Collections',   icon: 'bloodtype',       route: '/staff/collections',   roles: ['PHLEBOTOMIST'] },
  { label: 'Lab',           icon: 'science',         route: '/staff/lab',           roles: ['LAB_TECHNICIAN'] },
  { label: 'Inventory',     icon: 'inventory_2',     route: '/staff/inventory',     roles: ['INVENTORY_MANAGER'] },
  { label: 'Transfusions',  icon: 'medical_services',route: '/staff/transfusions',  roles: ['DOCTOR', 'NURSE'] },
  { label: 'Hospitals',     icon: 'local_hospital',  route: '/staff/hospitals',     roles: ['BRANCH_ADMIN', 'BRANCH_MANAGER'] },
  { label: 'Billing',       icon: 'receipt_long',    route: '/staff/billing',       roles: ['BILLING_CLERK'] },
  { label: 'Blood Camps',   icon: 'camping',         route: '/staff/camps',         roles: ['CAMP_COORDINATOR'] },
  { label: 'Reports',       icon: 'assessment',      route: '/staff/reports',       roles: ['REGIONAL_ADMIN', 'AUDITOR'] },
  { label: 'Compliance',    icon: 'verified',        route: '/staff/compliance',    roles: ['AUDITOR'] },
  { label: 'Notifications', icon: 'notifications',   route: '/staff/notifications', roles: ['SYSTEM_ADMIN'] },
  { label: 'Documents',     icon: 'description',     route: '/staff/documents',     roles: ['*'] },
  { label: 'Emergency',     icon: 'emergency',       route: '/staff/emergency',     roles: ['DOCTOR', 'BRANCH_MANAGER'] },
  { label: 'Settings',      icon: 'settings',        route: '/staff/settings',      roles: ['BRANCH_ADMIN', 'SUPER_ADMIN'] },
];
```

---

## 10. Component Patterns

### Status Badge Component

```typescript
@Component({
  selector: 'app-status-badge',
  standalone: true,
  template: `
    <span [class]="badgeClass()" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold">
      {{ label() }}
    </span>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatusBadgeComponent {
  readonly status = input.required<string>();
  readonly label = input.required<string>();

  readonly badgeClass = computed(() => {
    const map: Record<string, string> = {
      AVAILABLE: 'bg-green-100 text-green-800',
      RESERVED: 'bg-amber-100 text-amber-800',
      ISSUED: 'bg-blue-100 text-blue-800',
      QUARANTINE: 'bg-red-100 text-red-800',
      EXPIRED: 'bg-gray-100 text-gray-800',
      PENDING: 'bg-yellow-100 text-yellow-800',
      COMPLETED: 'bg-green-100 text-green-800',
      REJECTED: 'bg-red-100 text-red-800',
    };
    return map[this.status()] ?? 'bg-gray-100 text-gray-600';
  });
}
```

### Blood Group Badge Component

```typescript
@Component({
  selector: 'app-blood-group-badge',
  standalone: true,
  template: `
    <span [class]="badgeClass()" class="inline-flex items-center justify-center w-10 h-10 rounded-full text-white text-sm font-bold">
      {{ displayLabel() }}
    </span>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BloodGroupBadgeComponent {
  readonly bloodGroup = input.required<string>();

  readonly displayLabel = computed(() => {
    return this.bloodGroup().replace('_POSITIVE', '+').replace('_NEGATIVE', '−');
  });

  readonly badgeClass = computed(() => {
    const map: Record<string, string> = {
      A_POSITIVE: 'bg-red-600', A_NEGATIVE: 'bg-red-700',
      B_POSITIVE: 'bg-blue-600', B_NEGATIVE: 'bg-blue-700',
      AB_POSITIVE: 'bg-violet-600', AB_NEGATIVE: 'bg-violet-700',
      O_POSITIVE: 'bg-emerald-600', O_NEGATIVE: 'bg-emerald-700',
    };
    return map[this.bloodGroup()] ?? 'bg-gray-500';
  });
}
```

### Confirm Dialog

```typescript
@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <p class="text-gray-600">{{ data.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-flat-button [color]="data.confirmColor ?? 'primary'" [mat-dialog-close]="true">
        {{ data.confirmText ?? 'Confirm' }}
      </button>
    </mat-dialog-actions>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfirmDialogComponent {
  readonly data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
}
```

### Loading Skeleton

```typescript
@Component({
  selector: 'app-loading-skeleton',
  standalone: true,
  template: `
    <div class="animate-pulse space-y-4">
      @for (row of rows(); track $index) {
        <div class="flex gap-4">
          <div class="h-4 bg-gray-200 rounded w-1/4"></div>
          <div class="h-4 bg-gray-200 rounded w-1/2"></div>
          <div class="h-4 bg-gray-200 rounded w-1/4"></div>
        </div>
      }
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoadingSkeletonComponent {
  readonly count = input(5);
  readonly rows = computed(() => Array(this.count()));
}
```

---

## 11. State Management

### Signals Only — No NgRx, No BehaviorSubject

```typescript
// Feature-level state in service
@Injectable({ providedIn: 'root' })
export class DonorStateService {
  readonly donors = signal<Donor[]>([]);
  readonly selectedDonor = signal<Donor | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly filters = signal<DonorFilters>({});

  // Computed (derived) state
  readonly donorCount = computed(() => this.donors().length);
  readonly hasError = computed(() => this.error() !== null);
  readonly filteredDonors = computed(() => {
    const filters = this.filters();
    return this.donors().filter(d => {
      if (filters.bloodGroup && d.bloodGroup !== filters.bloodGroup) return false;
      if (filters.search && !d.firstName.toLowerCase().includes(filters.search.toLowerCase())) return false;
      return true;
    });
  });
}
```

### Global State (Branch Context)

```typescript
@Injectable({ providedIn: 'root' })
export class BranchContextService {
  readonly currentBranch = signal<Branch | null>(null);
  readonly branches = signal<Branch[]>([]);

  readonly branchId = computed(() => this.currentBranch()?.id ?? null);
  readonly branchName = computed(() => this.currentBranch()?.name ?? '');
}
```

---

## 12. Forms

### Reactive Forms with Material + Typed FormGroup

```typescript
export class DonorFormComponent {
  private readonly fb = inject(NonNullableFormBuilder);

  readonly donorForm = this.fb.group({
    firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    bloodGroup: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.pattern(/^\+?[1-9]\d{1,14}$/)]],
    dateOfBirth: [null as Date | null, Validators.required],
    gender: ['', Validators.required],
    address: this.fb.group({
      street: [''],
      city: ['', Validators.required],
      state: ['', Validators.required],
      postalCode: [''],
      country: ['', Validators.required],
    }),
    consent: [false, Validators.requiredTrue],
  });
}
```

### Form Template Pattern

```html
<form [formGroup]="donorForm" (ngSubmit)="onSubmit()" class="grid grid-cols-1 md:grid-cols-2 gap-6">
  <mat-form-field appearance="outline">
    <mat-label>First Name</mat-label>
    <input matInput formControlName="firstName" />
    @if (donorForm.controls.firstName.hasError('required')) {
      <mat-error>First name is required</mat-error>
    }
  </mat-form-field>

  <mat-form-field appearance="outline">
    <mat-label>Blood Group</mat-label>
    <mat-select formControlName="bloodGroup">
      @for (group of bloodGroups; track group.value) {
        <mat-option [value]="group.value">{{ group.label }}</mat-option>
      }
    </mat-select>
  </mat-form-field>

  <mat-form-field appearance="outline">
    <mat-label>Date of Birth</mat-label>
    <input matInput [matDatepicker]="picker" formControlName="dateOfBirth" />
    <mat-datepicker-toggle matIconSuffix [for]="picker" />
    <mat-datepicker #picker />
  </mat-form-field>

  <div class="col-span-full flex justify-end gap-4">
    <button mat-button type="button" routerLink="..">Cancel</button>
    <button mat-flat-button color="primary" type="submit" [disabled]="donorForm.invalid || saving()">
      @if (saving()) {
        <mat-spinner diameter="20" class="inline-block mr-2" />
      }
      Register Donor
    </button>
  </div>
</form>
```

### Form Rules

- Always use `NonNullableFormBuilder` (not `FormBuilder`)
- Always use `appearance="outline"` for `mat-form-field`
- Show `<mat-error>` with `@if` control flow
- Disable submit button when `form.invalid || saving()`
- Show spinner in submit button during save
- Grid layout: `grid grid-cols-1 md:grid-cols-2 gap-6`
- Actions bar: `col-span-full flex justify-end gap-4`

---

## 13. Data Tables

### Standard Data Table Pattern

```html
<div class="bg-white rounded-lg shadow-sm">
  <!-- Header bar -->
  <div class="flex items-center justify-between p-4 border-b">
    <h2 class="text-lg font-semibold">Donors</h2>
    <div class="flex items-center gap-4">
      <app-search-bar (search)="onSearch($event)" />
      <button mat-flat-button color="primary" routerLink="new">
        <mat-icon>add</mat-icon> Register Donor
      </button>
    </div>
  </div>

  <!-- Table -->
  @if (loading()) {
    <app-loading-skeleton [count]="10" />
  } @else {
    <mat-table [dataSource]="donors()" matSort (matSortChange)="onSort($event)">
      <ng-container matColumnDef="name">
        <mat-header-cell *matHeaderCellDef mat-sort-header>Name</mat-header-cell>
        <mat-cell *matCellDef="let donor">{{ donor.firstName }} {{ donor.lastName }}</mat-cell>
      </ng-container>

      <ng-container matColumnDef="bloodGroup">
        <mat-header-cell *matHeaderCellDef mat-sort-header>Blood Group</mat-header-cell>
        <mat-cell *matCellDef="let donor">
          <app-blood-group-badge [bloodGroup]="donor.bloodGroup" />
        </mat-cell>
      </ng-container>

      <ng-container matColumnDef="status">
        <mat-header-cell *matHeaderCellDef>Status</mat-header-cell>
        <mat-cell *matCellDef="let donor">
          <app-status-badge [status]="donor.status" [label]="donor.status" />
        </mat-cell>
      </ng-container>

      <ng-container matColumnDef="actions">
        <mat-header-cell *matHeaderCellDef>Actions</mat-header-cell>
        <mat-cell *matCellDef="let donor">
          <button mat-icon-button [routerLink]="[donor.id]">
            <mat-icon>visibility</mat-icon>
          </button>
          <button mat-icon-button [routerLink]="[donor.id, 'edit']">
            <mat-icon>edit</mat-icon>
          </button>
        </mat-cell>
      </ng-container>

      <mat-header-row *matHeaderRowDef="displayedColumns" />
      <mat-row *matRowDef="let row; columns: displayedColumns" />
    </mat-table>

    <mat-paginator
      [length]="totalItems()"
      [pageSize]="pageSize()"
      [pageSizeOptions]="[10, 20, 50, 100]"
      (page)="onPageChange($event)" />
  }
</div>
```

### Table Rules

- Wrap in white card with `rounded-lg shadow-sm`
- Header bar with title, search, and action buttons
- Use `mat-sort` for sortable columns
- Use `mat-paginator` for pagination
- Show `<app-loading-skeleton>` while loading
- Show `<app-empty-state>` when no results
- Virtual scroll (`<cdk-virtual-scroll-viewport>`) for lists > 1000 items

---

## 14. Dashboard & Charts

### KPI Card Pattern

```html
<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
  @for (kpi of kpis(); track kpi.label) {
    <div class="bg-white rounded-lg shadow-sm p-6">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-gray-500">{{ kpi.label }}</p>
          <p class="text-3xl font-bold mt-1" [class]="kpi.colorClass">{{ kpi.value }}</p>
        </div>
        <mat-icon [class]="kpi.iconClass + ' text-4xl opacity-20'">{{ kpi.icon }}</mat-icon>
      </div>
      @if (kpi.trend) {
        <div class="flex items-center gap-1 mt-2 text-sm">
          <mat-icon class="text-base">{{ kpi.trend > 0 ? 'trending_up' : 'trending_down' }}</mat-icon>
          <span [class]="kpi.trend > 0 ? 'text-green-600' : 'text-red-600'">
            {{ kpi.trend }}% vs last month
          </span>
        </div>
      }
    </div>
  }
</div>
```

### Chart Usage

- Use `ng2-charts` with Chart.js
- Bar chart for blood stock levels by group
- Doughnut chart for collection status breakdown
- Line chart for donation trends over time
- Colors from healthcare color system (Section 7)
- Always include chart title and legend

---

## 15. Security

### Authentication (Keycloak OIDC)

```typescript
// src/app/core/auth/auth.init.ts
export function initializeKeycloak(keycloak: KeycloakService): () => Promise<boolean> {
  return () => keycloak.init({
    config: {
      url: environment.keycloakUrl,
      realm: 'bloodbank',
      clientId: 'bloodbank-ui',
    },
    initOptions: {
      onLoad: 'login-required',
      checkLoginIframe: false,
      silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
    },
    enableBearerInterceptor: true,
    bearerPrefix: 'Bearer',
    bearerExcludedUrls: ['/assets', '/i18n'],
  });
}
```

### Security Rules

| Rule | Implementation |
|---|---|
| Token storage | Keycloak manages in-memory (NOT localStorage) |
| Token refresh | Keycloak auto-refresh before expiry |
| CSRF | Not needed — JWT Bearer tokens |
| XSS | Angular auto-sanitizes; NEVER use `bypassSecurityTrust*` |
| CSP | Set via `<meta>` or server header |
| Branch context | Extract `branch_id` from JWT claims; inject as `X-Branch-Id` header |
| Sensitive data | NEVER log tokens, PHI, or passwords to console |
| Route protection | `roleGuard` on ALL routes with `data: { roles: [...] }` |

### Role Guard

```typescript
export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const requiredRoles: string[] = route.data['roles'] ?? [];

  if (requiredRoles.includes('*') || authService.hasAnyRole(requiredRoles)) {
    return true;
  }
  return router.createUrlTree(['/unauthorized']);
};
```

### Structural Directive for Role-Based UI

```typescript
// Use in templates to show/hide elements by role
@Directive({ selector: '[appHasRole]', standalone: true })
export class HasRoleDirective {
  private readonly authService = inject(AuthService);
  private readonly templateRef = inject(TemplateRef<unknown>);
  private readonly viewContainer = inject(ViewContainerRef);

  readonly appHasRole = input.required<string[]>();

  constructor() {
    effect(() => {
      if (this.authService.hasAnyRole(this.appHasRole())) {
        this.viewContainer.createEmbeddedView(this.templateRef);
      } else {
        this.viewContainer.clear();
      }
    });
  }
}
```

---

## 16. Accessibility (a11y)

### Mandatory Rules

| Rule | Standard |
|---|---|
| WCAG level | **AA** minimum for all components |
| Color contrast | 4.5:1 for normal text, 3:1 for large text |
| Keyboard navigation | All interactions reachable by Tab / Enter / Escape |
| Screen reader | ARIA labels on all interactive elements |
| Focus management | Visible focus ring on all focusable elements |
| Image alt text | Required on all `<img>` tags |
| Form labels | Every input MUST have a `<mat-label>` |
| Error messages | Announced by screen reader via `aria-live="polite"` |
| Skip links | "Skip to main content" link at top of page |
| Reduced motion | Respect `prefers-reduced-motion` media query |

### Focus Ring (Global Style)

```scss
// src/styles/styles.scss
*:focus-visible {
  outline: 2px solid var(--mat-sys-primary);
  outline-offset: 2px;
}
```

### ARIA on Custom Components

```html
<app-blood-group-badge
  [bloodGroup]="donor.bloodGroup"
  [attr.aria-label]="'Blood group: ' + donor.bloodGroup" />

<app-status-badge
  [status]="unit.status"
  [label]="unit.status"
  role="status"
  [attr.aria-label]="'Status: ' + unit.status" />
```

---

## 17. Internationalization (i18n)

### Languages: English (default), Spanish, French

```
src/assets/i18n/
├── en.json
├── es.json
└── fr.json
```

### Translation File Structure

```json
{
  "common": {
    "save": "Save",
    "cancel": "Cancel",
    "delete": "Delete",
    "search": "Search",
    "loading": "Loading...",
    "noResults": "No results found",
    "confirm": "Confirm",
    "actions": "Actions"
  },
  "donor": {
    "title": "Donors",
    "register": "Register Donor",
    "firstName": "First Name",
    "lastName": "Last Name",
    "bloodGroup": "Blood Group",
    "email": "Email",
    "phone": "Phone",
    "status": {
      "ACTIVE": "Active",
      "DEFERRED": "Deferred",
      "BLACKLISTED": "Blacklisted"
    }
  },
  "bloodGroup": {
    "A_POSITIVE": "A+",
    "A_NEGATIVE": "A−",
    "B_POSITIVE": "B+",
    "B_NEGATIVE": "B−",
    "AB_POSITIVE": "AB+",
    "AB_NEGATIVE": "AB−",
    "O_POSITIVE": "O+",
    "O_NEGATIVE": "O−"
  }
}
```

### i18n Rules

- Use `@ngx-translate/core` or Angular's built-in `$localize`
- All user-facing text MUST be translatable — NEVER hardcode display strings
- Date/number formatting via Angular locale pipes
- RTL support prepared (use `dir="auto"` on root)

---

## 18. Error Handling UX

### Global Error Interceptor

```typescript
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      switch (error.status) {
        case 401:
          // Keycloak auto-handles token refresh; this means refresh failed
          router.navigate(['/login']);
          break;
        case 403:
          snackBar.open('You do not have permission for this action', 'Dismiss', { duration: 5000 });
          break;
        case 404:
          snackBar.open('Resource not found', 'Dismiss', { duration: 3000 });
          break;
        case 422:
          // Validation errors — show inline (not snackbar)
          break;
        case 0:
          snackBar.open('Network error. Please check your connection.', 'Retry', { duration: 0 });
          break;
        default:
          snackBar.open('An unexpected error occurred', 'Dismiss', { duration: 5000 });
      }
      return throwError(() => error);
    })
  );
};
```

### Error Display Rules

| Error Type | Display Method |
|---|---|
| Validation (422) | Inline `<mat-error>` under form fields |
| Permission (403) | MatSnackBar (warn) |
| Not Found (404) | Empty state component or MatSnackBar |
| Network (0) | MatSnackBar with Retry action (no auto-dismiss) |
| Server (500) | Full-page error component |
| Business logic | MatSnackBar or inline alert |

### Error Page Component

Create dedicated pages for:
- `/unauthorized` — 403 page (you don't have access)
- `/not-found` — 404 page
- `/error` — 500 page (something went wrong)

---

## 19. Performance

### Bundle Size Targets

| Metric | Target |
|---|---|
| Initial bundle | < 200 KB (gzipped) |
| Largest feature chunk | < 100 KB (gzipped) |
| First Contentful Paint | < 1.5s |
| Time to Interactive | < 3s |
| Lighthouse Performance | > 90 |

### Performance Rules

| Rule | Implementation |
|---|---|
| Lazy loading | ALL feature routes via `loadChildren` / `loadComponent` |
| Tree shaking | Import specific Material modules, not entire library |
| Virtual scroll | `<cdk-virtual-scroll-viewport>` for lists > 200 items |
| Skeleton loading | Show `<app-loading-skeleton>` immediately, never blank screen |
| Image optimization | Use `NgOptimizedImage` directive for all images |
| OnPush | EVERY component uses `ChangeDetectionStrategy.OnPush` |
| Track functions | EVERY `@for` loop has `track` expression |
| Pagination | Server-side pagination for all list views |
| Debounce | 300ms debounce on search inputs |
| Memoize | Use `computed()` for derived state, avoid recalculation |

### Preloading Strategy

```typescript
// app.config.ts
provideRouter(
  routes,
  withPreloading(PreloadAllModules), // Preload all lazy modules after initial load
  withComponentInputBinding(),
),
```

---

## 20. Testing Standards

### Unit Tests (Components)

```typescript
describe('DonorListComponent', () => {
  let component: DonorListComponent;
  let fixture: ComponentFixture<DonorListComponent>;
  let donorService: jasmine.SpyObj<DonorService>;

  beforeEach(async () => {
    donorService = jasmine.createSpyObj('DonorService', ['list', 'getById']);

    await TestBed.configureTestingModule({
      imports: [DonorListComponent],
      providers: [
        { provide: DonorService, useValue: donorService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DonorListComponent);
    component = fixture.componentInstance;
  });

  it('should load donors on init', async () => {
    const mockResponse = { content: [mockDonor], totalElements: 1 };
    donorService.list.and.returnValue(Promise.resolve(mockResponse));
    fixture.detectChanges();
    await fixture.whenStable();
    expect(component.donors().length).toBe(1);
  });

  it('should show loading skeleton while fetching', () => {
    component.loading.set(true);
    fixture.detectChanges();
    const skeleton = fixture.nativeElement.querySelector('app-loading-skeleton');
    expect(skeleton).toBeTruthy();
  });
});
```

### Testing Rules

| Rule | Details |
|---|---|
| Framework | Karma + Jasmine (default) or Jest |
| Coverage target | > 80% per feature module |
| Component tests | Test signal state, computed values, template rendering |
| Service tests | Mock HttpClient, test error handling |
| Guard tests | Test role-based access with mock AuthService |
| E2E tests | Playwright — test critical workflows (donor registration, blood issue, etc.) |
| a11y tests | Use `@angular-eslint/template/accessibility` rules |

---

## 21. File Naming & Import Conventions

### File Naming

| File Type | Pattern | Example |
|---|---|---|
| Component | `{name}.component.ts` | `donor-list.component.ts` |
| Service | `{name}.service.ts` | `donor.service.ts` |
| Guard | `{name}.guard.ts` | `role.guard.ts` |
| Interceptor | `{name}.interceptor.ts` | `error.interceptor.ts` |
| Pipe | `{name}.pipe.ts` | `blood-group.pipe.ts` |
| Directive | `{name}.directive.ts` | `has-role.directive.ts` |
| Model/Interface | `{name}.model.ts` | `donor.model.ts` |
| Enum | `{name}.enum.ts` | `blood-group.enum.ts` |
| Routes | `{feature}.routes.ts` | `donor.routes.ts` |
| Spec | `{name}.component.spec.ts` | `donor-list.component.spec.ts` |

### Import Order (Enforced by ESLint)

```typescript
// 1. Angular core
import { Component, inject, signal } from '@angular/core';

// 2. Angular modules (material, router, forms, etc.)
import { MatTableModule } from '@angular/material/table';
import { RouterModule } from '@angular/router';

// 3. Third-party libraries
import { firstValueFrom } from 'rxjs';

// 4. Project aliases (@core, @shared, @features)
import { AuthService } from '@core/auth/auth.service';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';

// 5. Relative imports (same feature)
import { DonorService } from '../../services/donor.service';
import { Donor } from '../../models/donor.model';
```

### ESLint Rules

```json
{
  "rules": {
    "@angular-eslint/prefer-standalone": "error",
    "@angular-eslint/prefer-on-push-component-change-detection": "error",
    "@angular-eslint/template/no-negated-async": "error",
    "@angular-eslint/template/accessibility-alt-text": "error",
    "@angular-eslint/template/accessibility-label-for": "error",
    "@angular-eslint/template/no-any": "error",
    "import/order": ["error", { "groups": ["builtin", "external", "internal", "parent", "sibling"] }]
  }
}
```

---

## 22. Healthcare-Specific UI Rules

### Blood Type Display

- Always show blood group with +/− symbols (A+, O−), never full enum name
- Color-code blood groups consistently (see Section 7)
- Show universal donor (O−) and universal recipient (AB+) with special callouts

### Unit Traceability

- Every blood unit MUST display its Unit ID prominently
- All screens in the blood lifecycle chain (collection → testing → processing → storage → issue → transfusion) MUST show the Unit ID
- Unit ID format: clickable link to unit detail page

### Clinical Safety Colors

- 🔴 **Red** = Danger, stop, quarantine, adverse reaction, critical stock
- 🟡 **Amber** = Warning, pending, deferred, low stock, reserved
- 🟢 **Green** = Safe, available, completed, passed, eligible
- 🔵 **Blue** = Information, issued, in-progress
- ⚫ **Gray** = Inactive, expired, disposed, archived

### Dual Confirmation for Critical Actions

These actions MUST show a `ConfirmDialog` before execution:
- Issuing blood units
- Recording transfusion reactions
- Disposing/discarding units
- Approving test results (dual review — different user)
- Emergency blood release
- Patient blood group override
- Recall initiation

### PHI Display Rules

- Patient name: show only to authorized roles
- Donor personal data: mask partially in lists (show initials + ID)
- All PHI screens must have audit logging
- Print views must include "CONFIDENTIAL" watermark

---

## 23. Common Mistakes to Avoid

| # | ❌ Mistake | ✅ Correct |
|---|---|---|
| 1 | Using `*ngIf`, `*ngFor`, `[ngSwitch]` | Use `@if`, `@for`, `@switch` control flow |
| 2 | Using `@Input()` / `@Output()` decorators | Use `input()` / `output()` signal APIs |
| 3 | Using `BehaviorSubject` for state | Use `signal()` and `computed()` |
| 4 | Creating `NgModule` classes | Use `standalone: true` components |
| 5 | Missing `ChangeDetectionStrategy.OnPush` | Add to EVERY component |
| 6 | Constructor injection in components | Use `inject()` function |
| 7 | `@for` without `track` | Always add `track item.id` or `track $index` |
| 8 | Overriding Material styles with Tailwind | Use Tailwind for layout only, Material for components |
| 9 | Storing JWT in localStorage | Let Keycloak manage tokens in-memory |
| 10 | Hardcoding display strings | Use i18n translation keys |
| 11 | Missing role guard on routes | Every route MUST have `canActivate: [roleGuard]` |
| 12 | Forgetting `aria-label` on icon buttons | Add to all `mat-icon-button` elements |
| 13 | Loading entire Material module | Import specific `Mat*Module` per component |
| 14 | No loading state | Show skeleton/spinner for EVERY async operation |
| 15 | Blank screens on error | Show error card or snackbar, NEVER blank |

---

## 24. Build & Deployment

### Build Commands

```bash
# Development server
cd frontend/bloodbank-ui
ng serve --port 4200

# Production build
ng build --configuration=production

# Run unit tests
ng test --code-coverage

# Run E2E tests
npx playwright test

# Lint
ng lint

# Analyze bundle
npx source-map-explorer dist/bloodbank-ui/browser/*.js
```

### Docker (nginx)

```dockerfile
# frontend/bloodbank-ui/Dockerfile
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration=production

FROM nginx:1.27-alpine
COPY --from=build /app/dist/bloodbank-ui/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

### Environment Configuration

```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',           // API Gateway
  keycloakUrl: 'http://localhost:8180',
  keycloakRealm: 'bloodbank',
  keycloakClientId: 'bloodbank-ui',
  defaultLanguage: 'en',
  supportedLanguages: ['en', 'es', 'fr'],
};
```

---

## 25. 17 Feature Modules

| # | Feature | Route | Roles | Backend Service |
|---|---|---|---|---|
| 1 | Dashboard | `/staff/dashboard` | ALL (role-filtered widgets) | reporting-service |
| 2 | Donor | `/staff/donors` | RECEPTIONIST, PHLEBOTOMIST, BRANCH_ADMIN | donor-service |
| 3 | Collection | `/staff/collections` | PHLEBOTOMIST | donor-service |
| 4 | Camp | `/staff/camps` | CAMP_COORDINATOR | donor-service |
| 5 | Lab | `/staff/lab` | LAB_TECHNICIAN | lab-service |
| 6 | Inventory | `/staff/inventory` | INVENTORY_MANAGER | inventory-service |
| 7 | Transfusion | `/staff/transfusions` | DOCTOR, NURSE | transfusion-service |
| 8 | Hospital | `/staff/hospitals` | BRANCH_ADMIN, BRANCH_MANAGER | hospital-service |
| 9 | Billing | `/staff/billing` | BILLING_CLERK | billing-service |
| 10 | Compliance | `/staff/compliance` | AUDITOR | compliance-service |
| 11 | Notification | `/staff/notifications` | SYSTEM_ADMIN | notification-service |
| 12 | Reporting | `/staff/reports` | REGIONAL_ADMIN, AUDITOR | reporting-service |
| 13 | Branch | `/staff/branch` | BRANCH_ADMIN, REGIONAL_ADMIN | branch-service |
| 14 | User Management | `/staff/users` | SUPER_ADMIN, SYSTEM_ADMIN | Keycloak Admin API |
| 15 | Document | `/staff/documents` | ALL | document-service |
| 16 | Emergency | `/staff/emergency` | DOCTOR, BRANCH_MANAGER | matching-service |
| 17 | Donor Portal | `/donor/*` | DONOR | donor-service |

---

## Changelog

| Date | Version | Changes |
|---|---|---|
| 2026-04-07 | 1.0 | Initial comprehensive Angular 21 guidelines |

---

> **Note:** This document is a living guideline. Update it when Angular versions change, new patterns emerge, or the design system evolves. All frontend developers and AI agents MUST follow these rules.
