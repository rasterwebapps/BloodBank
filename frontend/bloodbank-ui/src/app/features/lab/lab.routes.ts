import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role } from '@core/models/role.enum';

/** Roles allowed for lab operations. */
const LAB_ROLES = [Role.LAB_TECHNICIAN];

export const LAB_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./test-order-list/test-order-list.component').then(
        (m) => m.TestOrderListComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: LAB_ROLES, breadcrumb: 'Test Orders' },
  },
  {
    path: 'orders/:id/results',
    loadComponent: () =>
      import('./test-result-form/test-result-form.component').then(
        (m) => m.TestResultFormComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: LAB_ROLES, breadcrumb: 'Enter Results' },
  },
  {
    path: 'qc',
    loadComponent: () =>
      import('./qc-dashboard/qc-dashboard.component').then(
        (m) => m.QcDashboardComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: LAB_ROLES, breadcrumb: 'Quality Control' },
  },
  {
    path: 'instruments',
    loadComponent: () =>
      import('./instrument-list/instrument-list.component').then(
        (m) => m.InstrumentListComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: LAB_ROLES, breadcrumb: 'Instruments' },
  },
];
