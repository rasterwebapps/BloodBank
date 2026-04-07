# Skill: Create Angular Feature Module

Generate an Angular 21 feature module following BloodBank UI patterns.

> **Full reference:** `docs/ANGULAR_GUIDELINES.md`

## Rules

1. **Standalone components** — no `NgModule`, use `standalone: true`
2. **Signals** — use Angular signals for reactive state, not RxJS BehaviorSubject
3. **Signal inputs/outputs** — use `input()` / `output()`, not `@Input()` / `@Output()`
4. **Zoneless change detection** — `ChangeDetectionStrategy.OnPush` everywhere
5. **Built-in control flow** — `@if`, `@for` (with `track`), `@switch` — not `*ngIf`, `*ngFor`
6. **inject() function** — use `inject()` for DI, not constructor injection
7. **Lazy loaded** — feature routes loaded via `loadChildren` in app routes
8. **Material for components** — buttons, dialogs, tables, forms, menus, snackbars
9. **Tailwind for layout** — flex, grid, padding, margin, responsive breakpoints
10. **NEVER override Material with Tailwind** — no `bg-*`, `text-*` on `mat-*` components
11. **Role guard on every route** — `canActivate: [roleGuard]` with `data: { roles: [...] }`
12. **firstValueFrom** — for HTTP calls in services
13. **ARIA labels** — on all icon buttons and custom components
14. Directory: `frontend/bloodbank-ui/src/app/features/{feature-name}/`

## Feature Module Structure

```
features/{feature-name}/
├── {feature-name}.routes.ts           # Feature routes
├── components/
│   ├── {feature}-list/
│   │   ├── {feature}-list.component.ts
│   │   ├── {feature}-list.component.html
│   │   ├── {feature}-list.component.scss
│   │   └── {feature}-list.component.spec.ts
│   ├── {feature}-detail/
│   │   ├── {feature}-detail.component.ts
│   │   ├── {feature}-detail.component.html
│   │   ├── {feature}-detail.component.scss
│   │   └── {feature}-detail.component.spec.ts
│   ├── {feature}-form/
│   │   ├── {feature}-form.component.ts
│   │   ├── {feature}-form.component.html
│   │   ├── {feature}-form.component.scss
│   │   └── {feature}-form.component.spec.ts
│   └── {feature}-dashboard/
│       └── ...
├── services/
│   └── {feature}.service.ts           # API service
├── models/
│   ├── {feature}.model.ts             # TypeScript interfaces
│   └── {feature}-enums.ts             # Enums
├── guards/
│   └── {feature}-role.guard.ts        # Role-based route guard
└── pipes/
    └── {feature}-status.pipe.ts       # Custom pipes
```

## Component Template

```typescript
import { Component, ChangeDetectionStrategy, inject, signal, computed, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { {Feature}Service } from '../../services/{feature}.service';
import { {Feature} } from '../../models/{feature}.model';

@Component({
  selector: 'app-{feature}-list',
  standalone: true,
  imports: [CommonModule, RouterModule, MatTableModule, MatPaginatorModule, MatButtonModule, MatIconModule],
  templateUrl: './{feature}-list.component.html',
  styleUrl: './{feature}-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class {Feature}ListComponent {
  private readonly {feature}Service = inject({Feature}Service);

  readonly items = signal<{Feature}[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly totalItems = signal(0);

  readonly isEmpty = computed(() => this.items().length === 0 && !this.loading());

  constructor() {
    this.loadItems();
  }

  async loadItems(page = 0, size = 20): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const response = await this.{feature}Service.list(page, size);
      this.items.set(response.content);
      this.totalItems.set(response.totalElements);
    } catch (e) {
      this.error.set('Failed to load {features}');
    } finally {
      this.loading.set(false);
    }
  }
}
```

### Component Template (HTML)

```html
<div class="bg-white rounded-lg shadow-sm">
  <!-- Header -->
  <div class="flex items-center justify-between p-4 border-b">
    <h2 class="text-lg font-semibold">{Features}</h2>
    <button mat-flat-button color="primary" routerLink="new">
      <mat-icon>add</mat-icon> Add {Feature}
    </button>
  </div>

  <!-- Content -->
  @if (loading()) {
    <app-loading-skeleton [count]="10" />
  } @else if (isEmpty()) {
    <app-empty-state message="No {features} found" />
  } @else {
    <mat-table [dataSource]="items()">
      <!-- columns here -->
      <mat-header-row *matHeaderRowDef="displayedColumns" />
      <mat-row *matRowDef="let row; columns: displayedColumns" />
    </mat-table>

    <mat-paginator
      [length]="totalItems()"
      [pageSize]="20"
      [pageSizeOptions]="[10, 20, 50]"
      (page)="loadItems($event.pageIndex, $event.pageSize)" />
  }
</div>
```

### Signal Input/Output Component

```typescript
@Component({
  selector: 'app-{feature}-card',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule],
  template: `
    <mat-card class="cursor-pointer" (click)="selected.emit(item())">
      <mat-card-header>
        <mat-card-title>{{ item().name }}</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <app-status-badge [status]="item().status" [label]="item().status" />
      </mat-card-content>
    </mat-card>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class {Feature}CardComponent {
  readonly item = input.required<{Feature}>();
  readonly selected = output<{Feature}>();
}
```

## Service Template

```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { ApiResponse, PagedResponse } from '@shared/models/api.model';
import { {Feature}, {Feature}Create, {Feature}Update } from '../models/{feature}.model';
import { environment } from '@env/environment';

@Injectable({ providedIn: 'root' })
export class {Feature}Service {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/{features}`;

  async list(page = 0, size = 20): Promise<PagedResponse<{Feature}>> {
    return firstValueFrom(
      this.http.get<PagedResponse<{Feature}>>(this.baseUrl, { params: { page, size } })
    );
  }

  async getById(id: string): Promise<ApiResponse<{Feature}>> {
    return firstValueFrom(this.http.get<ApiResponse<{Feature}>>(`${this.baseUrl}/${id}`));
  }

  async create(data: {Feature}Create): Promise<ApiResponse<{Feature}>> {
    return firstValueFrom(this.http.post<ApiResponse<{Feature}>>(this.baseUrl, data));
  }

  async update(id: string, data: {Feature}Update): Promise<ApiResponse<{Feature}>> {
    return firstValueFrom(this.http.put<ApiResponse<{Feature}>>(`${this.baseUrl}/${id}`, data));
  }

  async delete(id: string): Promise<ApiResponse<void>> {
    return firstValueFrom(this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`));
  }
}
```

## Route Template

```typescript
import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';

export const {FEATURE}_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/{feature}-list/{feature}-list.component')
      .then(m => m.{Feature}ListComponent),
    canActivate: [roleGuard],
    data: { roles: ['BRANCH_ADMIN', 'BRANCH_MANAGER', '{ROLE}'] },
  },
  {
    path: ':id',
    loadComponent: () => import('./components/{feature}-detail/{feature}-detail.component')
      .then(m => m.{Feature}DetailComponent),
    canActivate: [roleGuard],
    data: { roles: ['BRANCH_ADMIN', 'BRANCH_MANAGER', '{ROLE}'] },
  },
  {
    path: 'new',
    loadComponent: () => import('./components/{feature}-form/{feature}-form.component')
      .then(m => m.{Feature}FormComponent),
    canActivate: [roleGuard],
    data: { roles: ['BRANCH_ADMIN', 'BRANCH_MANAGER', '{ROLE}'] },
  },
];
```

## 17 Feature Modules

| Feature | Roles | Backend Service |
|---|---|---|
| donor | RECEPTIONIST, PHLEBOTOMIST | donor-service |
| collection | PHLEBOTOMIST | donor-service |
| camp | CAMP_COORDINATOR | donor-service |
| lab | LAB_TECHNICIAN | lab-service |
| inventory | INVENTORY_MANAGER | inventory-service |
| transfusion | DOCTOR, NURSE | transfusion-service |
| hospital | HOSPITAL_USER | hospital-service |
| billing | BILLING_CLERK | billing-service |
| compliance | AUDITOR | compliance-service |
| notification | SYSTEM_ADMIN | notification-service |
| reporting | REGIONAL_ADMIN, AUDITOR | reporting-service |
| branch | BRANCH_ADMIN, REGIONAL_ADMIN | branch-service |
| user-management | SUPER_ADMIN, SYSTEM_ADMIN | Keycloak Admin API |
| dashboard | ALL (role-filtered) | reporting-service |
| document | ALL | document-service |
| emergency | DOCTOR, BRANCH_MANAGER | matching-service |
| donor-portal | DONOR | donor-service |

## Validation

- [ ] Standalone components (no NgModule)
- [ ] `ChangeDetectionStrategy.OnPush` on every component
- [ ] Signals for state management (`signal()`, `computed()`)
- [ ] `inject()` function for DI (not constructor injection)
- [ ] `input()` / `output()` signal APIs (not `@Input()` / `@Output()`)
- [ ] `@if` / `@for` / `@switch` control flow (not `*ngIf` / `*ngFor`)
- [ ] Every `@for` has `track` expression
- [ ] Lazy-loaded routes via `loadComponent`
- [ ] Role guards on all routes with `data: { roles: [...] }`
- [ ] Service uses `firstValueFrom` for HTTP calls
- [ ] Material for interactive components (buttons, forms, tables, dialogs)
- [ ] Tailwind for layout only (flex, grid, padding, margin, responsive)
- [ ] No Tailwind color/style overrides on Material components
- [ ] ARIA labels on all icon buttons and custom components
- [ ] Loading skeleton shown during async operations
- [ ] Empty state shown when no results
- [ ] Error handling (snackbar or inline error)

## Reference

See `docs/ANGULAR_GUIDELINES.md` for comprehensive rules on:
- Design system (colors, typography, healthcare-specific)
- Form patterns
- Data table patterns
- Dashboard & chart patterns
- Security (Keycloak, XSS, CSP)
- Accessibility (WCAG AA)
- i18n (en, es, fr)
- Performance (bundle size, virtual scroll, preloading)
- Testing standards
