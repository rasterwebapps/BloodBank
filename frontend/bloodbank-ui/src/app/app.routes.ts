import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role, STAFF_ROLES } from '@core/models/role.enum';

export const routes: Routes = [
  // Default redirect
  { path: '', redirectTo: 'staff/dashboard', pathMatch: 'full' },

  // Staff Portal — all staff roles
  {
    path: 'staff',
    canActivate: [roleGuard],
    data: { roles: STAFF_ROLES },
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadChildren: () =>
          import('@features/dashboard/dashboard.routes').then(
            (m) => m.DASHBOARD_ROUTES,
          ),
      },
    ],
  },

  // Hospital Portal
  {
    path: 'hospital',
    canActivate: [roleGuard],
    data: { roles: [Role.HOSPITAL_USER] },
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadChildren: () =>
          import('@features/dashboard/dashboard.routes').then(
            (m) => m.DASHBOARD_ROUTES,
          ),
      },
    ],
  },

  // Donor Portal
  {
    path: 'donor',
    canActivate: [roleGuard],
    data: { roles: [Role.DONOR] },
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadChildren: () =>
          import('@features/dashboard/dashboard.routes').then(
            (m) => m.DASHBOARD_ROUTES,
          ),
      },
    ],
  },

  // Error pages (no guard required)
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

