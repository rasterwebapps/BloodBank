# 🎨  — UI/UX AI Agent Guidelines

> **Purpose**: Complete rules, skills, instructions, and guidelines for any AI coding agent building frontend UI in the   project. Ensures consistent, production-quality, accessible UI using **Angular 19 + Angular Material 19 (M3) + Tailwind CSS 4**.

---

## Table of Contents

1. [Core Technology Rules](#1-core-technology-rules)
2. [Component Architecture Rules](#2-component-architecture-rules)
3. [UI/UX Design System Rules](#3-uiux-design-system-rules)
4. [UX Interaction Patterns](#4-ux-interaction-patterns)
5. [Multi-Branch UX Rules](#5-multi-branch-ux-rules)
6. [Accessibility (a11y) Rules](#6-accessibility-a11y-rules)
7. [Performance Rules](#7-performance-rules)
8. [ Domain-Specific UI Rules](#8--domain-specific-ui-rules)
9. [File & Code Conventions](#9-file--code-conventions)
10. [What the AI Agent Must NEVER Do](#10-what-the-ai-agent-must-never-do)
11. [Quality Checkt](#11-quality-checkt)

---

## 1. Core Technology Rules

### Angular 19 Mandates

| Rule | Detail |
|------|--------|
| Standalone components | ALWAYS use `standalone: true` — **NO NgModules** |
| Change detection | ALWAYS use `ChangeDetectionStrategy.OnPush` (configured via schematics) |
| Dependency injection | ALWAYS use `inject()` function — **NEVER** constructor injection in components |
| Control flow | ALWAYS use `@if`, `@for`, `@switch` — **NEVER** `*ngIf`, `*ngFor`, `*ngSwitch` |
| State management | ALWAYS use Angular Signals (`signal()`, `computed()`, `effect()`) for component state |
| Forms | ALWAYS use Reactive Forms with `FormBuilder` — **NEVER** template-driven forms |
| Guards | ALWAYS use functional guards (`CanActivateFn`) — **NEVER** class-based guards |
| Interceptors | ALWAYS use functional interceptors (`HttpInterceptorFn`) — **NEVER** class-based |
| Routing | ALWAYS lazy-load feature routes via `loadChildren` / `loadComponent` |
| Track expressions | ALWAYS use `@for` with `track` expression (`track item.id`) |
| RxJS | Use **ONLY** for HTTP streams and event-based operations — **NOT** for state management |
| Dates | Use `date-fns` for all date operations — **NEVER** Moment.js |
| Complex state | Use `@ngrx/signals` SignalStore for complex cross-component state |

### Angular Material 19 (M3) Mandates

- Use **M3 (Material Design 3)** theming — project uses `mat.$blue-palette` primary, `mat.$green-palette` tertiary
- Import **ONLY** the specific Material modules needed per component (`MatTableModule`, `MatButtonModule`, etc.)
- **NEVER** import an entire `MaterialModule` — tree-shaking depends on granular imports
- Use `mat-form-field` with `appearance="outline"` as default form field style
- Use `mat-icon` with Material Icons font (already loaded globally)
- Use `MatSnackBar` for transient notifications (success, error, warning, info)
- Use `MatDialog` for confirmations and modal forms
- Use `MatTable` with `MatPaginator` + `MatSort` for all data tables
- Use `MatStepper` for multi-step workflows (patient registration, order creation)
- Use `MatTabs` for sectioned detail views (patient detail, invoice detail)
- Use `MatMenu` for action dropdowns and user menus
- Use `MatSidenav` for main navigation (responsive: side mode desktop, overlay mobile)
- Use `MatChips` for status badges and tag displays
- Use `MatAutocomplete` for search fields (patient search, test search, doctor search)
- Use `MatDatepicker` for all date inputs — configured with date-fns adapter
- Use `MatProgressSpinner` or `MatProgressBar` for loading states — **NEVER** custom spinners
- Prefix all Material selectors properly: `mat-button`, `mat-raised-button`, `mat-icon-button`

### Tailwind CSS 4 Mandates

- Tailwind v4 uses **CSS-first config** — NO `tailwind.config.js` file
- PostCSS integration via `@tailwindcss/postcss` plugin (`.postcssrc.json`)
- Import via: `@import 'tailwindcss';` in `styles.scss`
- Use Tailwind for **layout** (flex, grid, gap, padding, margin, responsive breakpoints)
- Use Tailwind for **spacing, sizing, typography** utilities
- Use Tailwind for color utilities **ONLY** when they complement Material — NOT replace it
- **NEVER** use Tailwind color classes that conflict with Material theme colors
- Use Tailwind responsive prefixes: `sm:`, `md:`, `lg:`, `xl:` for breakpoint-specific layouts
- Use Tailwind for quick utility styling; use SCSS for complex component-specific styles

---

## 2. Component Architecture Rules

### Smart vs Dumb Component Pattern

#### Smart Components (Pages/Containers)

- **Location**: `features/{feature}/pages/`
- **Suffix**: `.page.ts` or `-t.page.ts`, `-form.page.ts`
- **Responsibilities**:
  - Inject services
  - Hold state via Signals
  - Handle user actions and HTTP calls
  - Route navigation logic
  - Pass data **DOWN** to dumb components via `@Input()`
  - Receive events **UP** from dumb components via `@Output()`

#### Dumb Components (Presentational)

- **Location**: `features/{feature}/components/`
- **Suffix**: `.component.ts`
- **Responsibilities**:
  - Receive data **ONLY** via `@Input()`
  - Emit events **ONLY** via `@Output()`
  - **ZERO** service injection (except utility services like `DatePipe`)
  - Pure UI rendering with `OnPush` change detection
  - All styling and template logic

### Feature Module Structure (ALWAYS follow this)

```
features/{feature}/
├── routes.ts                              # Lazy-loaded route definitions
├── models/{entity}.model.ts               # TypeScript interfaces & enums
├── services/{entity}.service.ts           # HttpClient API service
├── store/{entity}.store.ts                # @ngrx/signals SignalStore (if complex state)
├── pages/
│   ├── {entity}-t.page.ts              # t page (smart)
│   └── {entity}-form.page.ts              # Create/Edit page (smart)
└── components/
    ├── {entity}-t/
    │   ├── {entity}-t.component.ts     # Table display (dumb)
    │   └── {entity}-t.component.html   # Template
    ├── {entity}-form/
    │   ├── {entity}-form.component.ts     # Form UI (dumb)
    │   └── {entity}-form.component.html   # Template
    └── {entity}-detail/
        ├── {entity}-detail.component.ts   # Detail view (dumb)
        └── {entity}-detail.component.html # Template
```

### Shared Component Guidelines

- **Location**: `shared/components/`, `shared/directives/`, `shared/pipes/`
- **Use for**: Cross-feature reusable UI elements
- **Examples already in project**:
  - `BreadcrumbComponent` — dynamic route-based breadcrumbs
  - `BranchSelectorComponent` — branch switching dropdown
  - `PermissionDirective` — role-based element visibility (`*appPermission`)
  - `DateFormatPipe` — date-fns formatting
  - `CurrencyFormatPipe` — INR/multi-currency formatting
- **When to create a shared component**:
  - Used by 2+ feature modules
  - Has no feature-specific business logic
  - Is purely presentational

---

## 3. UI/UX Design System Rules

### Color System

| Purpose | Color | Hex/Class |
|---------|-------|-----------|
| Primary actions | Material Blue | `mat.$blue-palette` — buttons, links, active states |
| Secondary/Tertiary | Material Green | `mat.$green-palette` — success states, confirmations |
| Danger/Error | Red | `#d32f2f` — delete actions, critical values, validation errors |
| Warning | Orange | `#f57c00` — warnings, high flags, attention states |
| Info | Blue | `#1976d2` — informational, low flags |
| Success | Green | `#388e3c` — success messages, completed states |

### Status Badge Colors (use consistently across ALL modules)

| Status | Tailwind Classes |
|--------|-----------------|
| PENDING | `bg-yellow-100 text-yellow-700` |
| ENTERED | `bg-blue-100 text-blue-700` |
| IN_PROGRESS | `bg-indigo-100 text-indigo-700` |
| VALIDATED | `bg-green-100 text-green-700` |
| AUTHORIZED | `bg-purple-100 text-purple-700` |
| RELEASED | `bg-teal-100 text-teal-700` |
| AMENDED | `bg-orange-100 text-orange-700` |
| CANCELLED | `bg-red-100 text-red-700` |
| DRAFT | `bg-gray-100 text-gray-700` |
| PAID | `bg-green-100 text-green-700` |
| PARTIALLY_PAID | `bg-yellow-100 text-yellow-700` |
| OVERDUE | `bg-red-100 text-red-700` |

### Lab-Specific Result Flags

| Flag | CSS Class | Style |
|------|-----------|-------|
| Critical | `.critical-value` | `color: #d32f2f; font-weight: bold;` — LIFE-THREATENING values |
| High | `.high-flag` | `color: #f57c00;` — Above normal range |
| Low | `.low-flag` | `color: #1976d2;` — Below normal range |
| Normal | default | Default text color — Within range |

### Typography System

- **Font Family**: Roboto (loaded via Google Fonts — weights 300, 400, 500)
- **Body class**: `mat-typography` applied globally

| Element | Usage |
|---------|-------|
| `<h1>` / `mat-headline-5` | Page titles — e.g., "Patient Registration" |
| `<h2>` / `mat-headline-6` | Section titles — e.g., "Personal Information" |
| `<h3>` / `mat-subtitle-1` | Card titles — e.g., in `mat-card-title` |
| `mat-body-1` | Body text (default) |
| `mat-caption` | Metadata, timestamps, secondary info |
| `font-light` / `font-normal` / `font-medium` | Tailwind weight utilities |
| `text-sm` / `text-base` / `text-lg` | Tailwind size utilities |
| `text-gray-500` | Secondary/muted text |
| `text-gray-900` | Primary text |

### Spacing & Layout System

#### Page Layout
- Main content wrapped in `<div class="p-6">` (24px padding)
- Cards use `<mat-card class="mb-4">` with 16px bottom margin
- Form sections separated by `<mat-divider class="my-4">`
- Grid layouts: Tailwind grid classes (`grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4`)

#### Form Layout
- Two-column on desktop (`md:grid-cols-2`), single column on mobile
- Three-column for compact forms (`lg:grid-cols-3`)
- Form fields use full width within grid cells
- Action buttons right-aligned: `<div class="flex justify-end gap-2">`

#### Table Layout
- Full width `mat-table` wrapped in `mat-card`
- Search bar + action button row above table
- Paginator below table
- Responsive: horizontal scroll on mobile (`overflow-x-auto`)

#### Responsive Breakpoints (Tailwind)

| Prefix | Width | Target |
|--------|-------|--------|
| `sm:` | 640px | Small tablets |
| `md:` | 768px | Tablets |
| `lg:` | 1024px | Small laptops |
| `xl:` | 1280px | Desktops |
| `2xl:` | 1536px | Large screens |

### Elevation & Surfaces

- `mat-card`: Default Material elevation (`mat-elevation-z1`)
- Dialog/Modal: `mat-elevation-z24`
- Toolbar: `mat-elevation-z4`
- Floating action buttons: `mat-elevation-z6`
- **AVOID** custom `box-shadow` — use Material elevation classes
- Use `mat-card` for all content containers — **NEVER** plain `<div>` for content sections

---

## 4. UX Interaction Patterns

### Loading States (MANDATORY for every data-fetching component)

```html
@if (loading()) {
  <div class="flex justify-center items-center py-12">
    <mat-spinner diameter="48" />
  </div>
} @else if (error()) {
  <div class="text-center py-12">
    <mat-icon class="text-red-500 text-5xl">error_outline</mat-icon>
    <p class="text-gray-500 mt-2">{{ error() }}</p>
    <button mat-raised-button color="primary" (click)="retry()">Retry</button>
  </div>
} @else if (items().length === 0) {
  <div class="text-center py-12">
    <mat-icon class="text-gray-400 text-5xl">inbox</mat-icon>
    <p class="text-gray-500 mt-2">No records found</p>
  </div>
} @else {
  <!-- Data content -->
}
```

### Form UX Rules

1. **Validation**:
   - Show validation errors **ONLY** after field is touched (`field.touched && field.invalid`)
   - Use `<mat-error>` inside `<mat-form-field>` — **NEVER** custom error divs
   - Display specific error messages: `"Patient name is required"`, NOT `"Field required"`
   - Disable submit button when form is invalid: `[disabled]="form.invalid || saving()"`

2. **Save Flow**:
   - Show saving spinner on submit button: `saving()` signal
   - Disable form during save to prevent double submission
   - On success: navigate back + show success snackbar
   - On error: show error snackbar + keep form open with data preserved

3. **Edit Mode**:
   - Pre-populate form with existing data via route param + service call
   - Show loading spinner while fetching existing data
   - Confirm unsaved changes on navigation (`canDeactivate` guard)

4. **Autocomplete**:
   - Patient search: search by name, phone, UHID
   - Doctor search: search by name, registration number
   - Test search: search by name, code, department
   - Debounce at **300ms**, minimum **2 characters**

5. **Date Inputs**:
   - ALWAYS use `mat-datepicker`
   - Date of birth: `max` date = today
   - Collection date: default = now
   - Report date: default = now
   - Display format: `dd/MM/yyyy` (Indian standard)

### Table UX Rules

1. ALWAYS include: search bar, column sort, pagination, row actions
2. Pagination: default page size = **20**, options = `[10, 20, 50, 100]`
3. Search: debounce at **300ms**, search on backend (**NOT** client-side filter)
4. Status columns: use colored chips/badges (`MatChip` or Tailwind badge)
5. Action column: `mat-icon-button` with `mat-menu` for multiple actions
6. Row click: navigate to detail page (`cursor-pointer` on rows)
7. Empty state: centered message with icon when no data
8. Loading: `mat-progress-bar` on top of table during fetch
9. Selection: `MatCheckbox` column for batch operations when needed
10. Date columns: use `DateFormatPipe` for consistent formatting
11. Currency columns: use `CurrencyFormatPipe` (default INR)

### Navigation UX

1. **Sidenav**:
   - Desktop (>1024px): permanently open side mode
   - Mobile (<1024px): overlay mode, toggle via hamburger button
   - Active route: highlighted with primary color
   - Icons + labels for all nav items
   - Grouped by function: Operations, Lab, Finance, Admin

2. **Breadcrumbs**:
   - Always show below toolbar: `Dashboard > Patients > Patient Detail`
   - Use `BreadcrumbComponent` (shared)
   - Clickable crumbs for navigation back

3. **Page Header**:
   - Every page: `<h1>` title + optional subtitle + action button
   - Example: `"Patients"` + `"Manage patient records"` + `[Register Patient]` button
   - Right-align action buttons

4. **Tab Navigation**:
   - Use `mat-tab-group` for multi-section views
   - Patient detail: Demographics | Orders | Results | Billing | History
   - Lazy load tab content

### Notification/Feedback UX

| Type | Behavior |
|------|----------|
| SUCCESS | `MatSnackBar` — green accent, **3s** auto-dismiss, bottom-center |
| ERROR | `MatSnackBar` — red/warn, **5s** auto-dismiss, with "Dismiss" action |
| WARNING | `MatSnackBar` — orange, **5s** auto-dismiss |
| INFO | `MatSnackBar` — default, **3s** auto-dismiss |

**Confirmations**:
- Delete: `MatDialog` with "Are you sure? This action cannot be undone."
- Cancel form with unsaved changes: `MatDialog` confirmation
- Critical operations: Type-to-confirm pattern

**Use the existing `NotificationService`**:
```typescript
notificationService.success('Patient registered successfully');
notificationService.error('Failed to save record');
notificationService.warning('Duplicate patient detected');
notificationService.info('Report is being generated...');
```

---

## 5. Multi-Branch UX Rules

> **CRITICAL for the  multi-tenant architecture**

1. `BranchSelectorComponent` **ALWAYS** visible in toolbar
2. Branch switch triggers full data refresh for current view
3. `BranchInterceptor` automatically adds `X-Branch-Id` header to ALL HTTP requests
4. Branch ID persisted in `localStorage` (survives refresh)
5. Branch name displayed in toolbar: `"Branch: Central Lab, Mumbai"`
6. `SUPER_ADMIN` can see cross-branch data with a toggle
7. All data tables implicitly show **ONLY** current branch data
8. Branch context shown in print/PDF reports

---

## 6. Accessibility (a11y) Rules

1. ALL interactive elements must have `aria-label` or `aria-labelledby`
2. `mat-icon` buttons **MUST** have `aria-label`: `<button mat-icon-button aria-label="Edit patient">`
3. Form fields **MUST** have `<mat-label>` — **NEVER** placeholder-only
4. Tables **MUST** have `aria-label` on `<table>`
5. Status badges **MUST** have `aria-label` for screen readers
6. Focus management: auto-focus first field on form open, return focus on dialog close
7. Keyboard navigation: all actions reachable via Tab + Enter/Space
8. Color contrast: WCAG AA minimum (4.5:1 for text, 3:1 for large text)
9. Error announcements: use `aria-live="polite"` on error message containers
10. Skip-to-content link for keyboard users

---

## 7. Performance Rules

1. **Lazy load** all feature routes (already configured in `app.routes.ts`)
2. `OnPush` change detection on **ALL** components (configured via schematics)
3. Use `track` in `@for` loops: `@for (item of items(); track item.id)`
4. Avoid complex computations in templates — use `computed()` signals
5. Virtual scrolling (`ScrollingModule`) for ts > 100 items
6. Debounce search inputs: **300ms**
7. Pagination: **server-side ALWAYS** — NEVER load all records client-side
8. Image lazy loading: `loading="lazy"` attribute
9. Bundle budgets enforced: **500kB warning**, **1MB error** (`angular.json`)
10. Component style budget: **2kB warning**, **4kB error**
11. Use `provideAnimationsAsync()` — NOT `provideAnimations()` (already configured)
12. Minimize Material module imports per component

---

## 8.  Domain-Specific UI Rules

### Patient Registration

- UHID auto-generated by backend — display as read-only chip after creation
- Duplicate detection: check before save, warn user with found matches
- Age auto-calculated from DOB (display as `"32Y 5M"` format)
- Photo upload: optional, circular avatar with camera icon placeholder
- ID proof: Aadhaar/PAN/Passport with masked display

### Sample Collection

- Barcode display: large, scannable Code128 format
- Sample status timeline: `Collected → Received → Processing → Completed`
- Color-coded tube types: Purple (EDTA), Red (Serum), Green (Lithium Heparin), Blue (Citrate)
- Rejection reasons: dropdown with common reasons + free text

### Result Entry

- Department-specific components (`BiochemResultEntry`, `HematoResultEntry`, etc.)
- Auto-flagging: **H** (High), **L** (Low), **C** (Critical) based on reference ranges
- Critical value alerts: RED background + popup notification
- Result validation workflow: `Entry → Technical Validation → Authorization`
- Amendment tracking: show previous values with strikethrough
- Tab-through numeric entry for biochemistry grids

### Report Generation

- PDF preview in-app using `ngx-extended-pdf-viewer`
- Print button: `window.print()` with `@media print` styles
- Doctor/patient portal: read-only report access
- Digital signature placeholder for authorized results
- Letterhead template: branch-specific header/footer

### Billing

- Rate t switching: Walk-in / Corporate / Insurance / Doctor Referral
- Discount: percentage or flat amount, requires reason
- Payment methods: Cash, Card, UPI, Insurance, Credit
- Partial payment support with balance tracking
- Invoice status: color-coded chips (Draft, Generated, Paid, Overdue)
- Receipt print: thermal printer format (80mm width)

### Quality Control

- Levey-Jennings charts: line chart with ±1SD, ±2SD, ±3SD zones
- Westgard rule violations: highlighted with specific rule codes
- QC lot management: expiry date tracking with visual warnings
- EQA/proficiency testing: external scheme result entry

---

## 9. File & Code Conventions

### Naming Conventions

| Type | Convention | Example |
|------|-----------|---------|
| Component files | `kebab-case.component.ts` | `patient-t.component.ts` |
| Service files | `kebab-case.service.ts` | `patient.service.ts` |
| Model files | `kebab-case.model.ts` | `patient.model.ts` |
| Pipe files | `kebab-case.pipe.ts` | `date-format.pipe.ts` |
| Directive files | `kebab-case.directive.ts` | `permission.directive.ts` |
| Guard files | `kebab-case.guard.ts` | `auth.guard.ts` |
| Store files | `kebab-case.store.ts` | `patient.store.ts` |
| Component selectors | `app-{feature}-{name}` | `app-patient-t`, `app-invoice-detail` |
| Directive selectors | `appCamelCase` | `appPermission`, `appDebounceClick` |
| Classes | PascalCase with suffix | `PatienttComponent`, `PatientService` |
| Interfaces | PascalCase, NO `I` prefix | `Patient` (not `IPatient`) |
| Signals | `readonly camelCase` | `readonly patients = signal<Patient[]>([]);` |
| Computed signals | `readonly` | `readonly filteredPatients = computed(() => ...);` |
| Boolean signals | `loading` / `saving` | `readonly loading = signal(false);` |

### Template Patterns

```html
<!-- PAGE STRUCTURE -->
<div class="p-6">
  <!-- Page Header -->
  <div class="flex justify-between items-center mb-6">
    <div>
      <h1 class="text-2xl font-medium">Page Title</h1>
      <p class="text-gray-500">Page description</p>
    </div>
    <button mat-raised-button color="primary" routerLink="create">
      <mat-icon>add</mat-icon> Create New
    </button>
  </div>

  <!-- Content Card -->
  <mat-card>
    <mat-card-content>
      <!-- Search + Filters -->
      <div class="flex gap-4 mb-4">
        <mat-form-field class="flex-1">
          <mat-label>Search</mat-label>
          <input matInput placeholder="Search..." (input)="onSearch($event)">
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>
      </div>

      <!-- Data Table or Content -->
    </mat-card-content>
  </mat-card>
</div>
```

### Service Pattern

```typescript
@Injectable({ providedIn: 'root' })
export class EntityService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/entities`;

  getAll(page: number, size: number): Observable<PageResponse<Entity>> {
    return this.http.get<PageResponse<Entity>>(this.apiUrl, {
      params: { page: page.toString(), size: size.toString() }
    });
  }

  getById(id: string): Observable<DataResponse<Entity>> {
    return this.http.get<DataResponse<Entity>>(`${this.apiUrl}/${id}`);
  }

  create(request: EntityRequest): Observable<DataResponse<Entity>> {
    return this.http.post<DataResponse<Entity>>(this.apiUrl, request);
  }

  update(id: string, request: EntityRequest): Observable<DataResponse<Entity>> {
    return this.http.put<DataResponse<Entity>>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

### API Response Handling

```typescript
// ALWAYS expect wrapped responses from backend:
interface PageResponse<T> {
  data: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

interface DataResponse<T> {
  data: T;
}

// In components — access .data from response:
this.entityService.getAll(page, size).pipe(
  tap(response => {
    this.items.set(response.data);
    this.totalElements.set(response.totalElements);
  }),
  catchError(error => {
    this.notificationService.error('Failed to load data');
    return EMPTY;
  })
).subscribe();
```

---

## 10. What the AI Agent Must NEVER Do

| ❌ NEVER | Use Instead |
|----------|-------------|
| Use NgModules | `standalone: true` components |
| Use `*ngIf` / `*ngFor` / `*ngSwitch` | `@if` / `@for` / `@switch` |
| Use Moment.js | `date-fns` |
| Use constructor injection in components | `inject()` function |
| Use `BehaviorSubject` / `ReplaySubject` for state | Angular Signals |
| Use `any` type | Proper TypeScript interfaces |
| Expose JPA entity shapes directly | Match backend DTOs |
| Use inline styles | Tailwind utilities or SCSS |
| Skip loading/empty/error states | Every data view needs all three |
| Do client-side pagination for ts | Server-side pagination |
| Skip `aria-labels` on icon buttons | Always add `aria-label` |
| Use `mat-raised-button color="warn"` for non-destructive actions | `color="primary"` or default |
| Put business logic in templates | Use `computed()` or methods |
| Hardcode API URLs | Use `environment` config |
| Use `setTimeout` for UI timing | Angular lifecycle or RxJS `timer` |
| Skip form validation | Every input must have validators |
| Show raw error messages from API | Map to user-friendly messages |

---

## 11. Quality Checkt

> **AI agent must verify ALL items before marking a component/feature as complete.**

- [ ] Component uses `standalone: true` and `OnPush`
- [ ] Template uses `@if` / `@for` / `@switch` (NOT `*ngIf` / `*ngFor`)
- [ ] All signals are `readonly`
- [ ] Loading, empty, and error states are handled
- [ ] Form fields have `mat-label` and validation messages
- [ ] Tables have search, sort, pagination
- [ ] Status values use consistent color-coded badges
- [ ] All icon buttons have `aria-labels`
- [ ] Service uses `inject()` and `HttpClient` correctly
- [ ] Routes are lazy-loaded
- [ ] No `any` types in the code
- [ ] Date formatting uses `DateFormatPipe` (date-fns)
- [ ] Currency uses `CurrencyFormatPipe`
- [ ] Responsive: works on mobile (test `md:` breakpoint)
- [ ] Actions show confirmation dialogs where appropriate
- [ ] Snackbar notifications for success/error feedback
