import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role } from '@core/models/role.enum';

/** Roles that may access general emergency functionality. */
const EMERGENCY_ROLES = [Role.DOCTOR, Role.BRANCH_MANAGER];

export const EMERGENCY_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./emergency-dashboard/emergency-dashboard.component').then(
        (m) => m.EmergencyDashboardComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: EMERGENCY_ROLES, breadcrumb: 'Dashboard' },
  },
  {
    path: 'request',
    loadComponent: () =>
      import('./emergency-request/emergency-request.component').then(
        (m) => m.EmergencyRequestComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: EMERGENCY_ROLES, breadcrumb: 'New Request' },
  },
  {
    path: 'disasters',
    loadComponent: () =>
      import('./disaster-response/disaster-response.component').then(
        (m) => m.DisasterResponseComponent,
      ),
    canActivate: [roleGuard],
    data: {
      roles: [Role.SUPER_ADMIN, Role.REGIONAL_ADMIN],
      breadcrumb: 'Disaster Response',
    },
  },
];
