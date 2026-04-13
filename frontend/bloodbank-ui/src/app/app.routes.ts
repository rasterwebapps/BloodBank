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
      {
        path: 'donors',
        data: { breadcrumb: 'Donors' },
        loadChildren: () =>
          import('@features/donor/donor.routes').then(
            (m) => m.DONOR_ROUTES,
          ),
      },
      {
        path: 'collections',
        data: { breadcrumb: 'Collections' },
        loadChildren: () =>
          import('@features/collection/collection.routes').then(
            (m) => m.COLLECTION_ROUTES,
          ),
      },
      {
        path: 'camps',
        data: { breadcrumb: 'Camps' },
        loadChildren: () =>
          import('@features/camp/camp.routes').then(
            (m) => m.CAMP_ROUTES,
          ),
      },
      {
        path: 'lab',
        data: { breadcrumb: 'Lab' },
        loadChildren: () =>
          import('@features/lab/lab.routes').then(
            (m) => m.LAB_ROUTES,
          ),
      },
      {
        path: 'inventory',
        data: { breadcrumb: 'Inventory' },
        loadChildren: () =>
          import('@features/inventory/inventory.routes').then(
            (m) => m.INVENTORY_ROUTES,
          ),
      },
      {
        path: 'transfusion',
        data: { breadcrumb: 'Transfusion' },
        loadChildren: () =>
          import('@features/transfusion/transfusion.routes').then(
            (m) => m.TRANSFUSION_ROUTES,
          ),
      },
      {
        path: 'emergency',
        data: { breadcrumb: 'Emergency' },
        loadChildren: () =>
          import('@features/emergency/emergency.routes').then(
            (m) => m.EMERGENCY_ROUTES,
          ),
      },
      {
        path: 'branches',
        data: { breadcrumb: 'Branches' },
        loadChildren: () =>
          import('@features/branch/branch.routes').then(
            (m) => m.BRANCH_ROUTES,
          ),
      },
      {
        path: 'users',
        data: { breadcrumb: 'Users' },
        loadChildren: () =>
          import('@features/user-management/user-management.routes').then(
            (m) => m.USER_MANAGEMENT_ROUTES,
          ),
      },
      {
        path: 'settings',
        data: { breadcrumb: 'Settings' },
        loadChildren: () =>
          import('@features/settings/settings.routes').then(
            (m) => m.SETTINGS_ROUTES,
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

