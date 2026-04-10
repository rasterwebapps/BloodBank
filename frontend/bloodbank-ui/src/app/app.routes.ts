import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role, STAFF_ROLES } from '@core/models/role.enum';

export const routes: Routes = [
  // Default redirect
  { path: '', redirectTo: 'staff/dashboard', pathMatch: 'full' },

  // Staff Portal — all staff roles (wrapped in shell layout)
  {
    path: 'staff',
    loadComponent: () =>
      import('@shared/layout/shell/shell.component').then(
        (m) => m.ShellComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: STAFF_ROLES },
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        data: { breadcrumb: 'Dashboard' },
        loadChildren: () =>
          import('@features/dashboard/dashboard.routes').then(
            (m) => m.DASHBOARD_ROUTES,
          ),
      },
    ],
  },

  // Hospital Portal (wrapped in shell layout)
  {
    path: 'hospital',
    loadComponent: () =>
      import('@shared/layout/shell/shell.component').then(
        (m) => m.ShellComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: [Role.HOSPITAL_USER] },
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        data: { breadcrumb: 'Dashboard' },
        loadChildren: () =>
          import('@features/dashboard/dashboard.routes').then(
            (m) => m.DASHBOARD_ROUTES,
          ),
      },
    ],
  },

  // Donor Portal (wrapped in shell layout)
  {
    path: 'donor',
    loadComponent: () =>
      import('@shared/layout/shell/shell.component').then(
        (m) => m.ShellComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: [Role.DONOR] },
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        data: { breadcrumb: 'Dashboard' },
        loadChildren: () =>
          import('@features/dashboard/dashboard.routes').then(
            (m) => m.DASHBOARD_ROUTES,
          ),
      },
    ],
  },

  // Error pages (no guard required — no shell)
  {
    path: 'unauthorized',
    loadComponent: () =>
      import('@shared/components/error-page/unauthorized.component').then(
        (m) => m.UnauthorizedComponent,
      ),
  },

  // Catch-all — redirect to staff dashboard
  { path: '**', redirectTo: 'staff/dashboard' },
];

