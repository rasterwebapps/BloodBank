import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role } from '@core/models/role.enum';

const COMPLIANCE_ROLES = [Role.AUDITOR, Role.BRANCH_ADMIN, Role.BRANCH_MANAGER];

export const COMPLIANCE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./framework-list/framework-list.component').then(
        (m) => m.FrameworkListComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: COMPLIANCE_ROLES, breadcrumb: 'Frameworks' },
  },
  {
    path: 'sops',
    loadComponent: () =>
      import('./sop-list/sop-list.component').then(
        (m) => m.SopListComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: COMPLIANCE_ROLES, breadcrumb: 'SOPs' },
  },
  {
    path: 'licenses',
    loadComponent: () =>
      import('./license-list/license-list.component').then(
        (m) => m.LicenseListComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: COMPLIANCE_ROLES, breadcrumb: 'Licenses' },
  },
  {
    path: 'deviations',
    loadComponent: () =>
      import('./deviation-list/deviation-list.component').then(
        (m) => m.DeviationListComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: COMPLIANCE_ROLES, breadcrumb: 'Deviations' },
  },
  {
    path: 'recalls',
    loadComponent: () =>
      import('./recall-list/recall-list.component').then(
        (m) => m.RecallListComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: COMPLIANCE_ROLES, breadcrumb: 'Recalls' },
  },
];
