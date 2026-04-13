import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role } from '@core/models/role.enum';

const BILLING_ROLES = [Role.BILLING_CLERK, Role.BRANCH_MANAGER, Role.BRANCH_ADMIN];

export const BILLING_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./invoice-list/invoice-list.component').then(
        (m) => m.InvoiceListComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: BILLING_ROLES, breadcrumb: 'Invoices' },
  },
  {
    path: 'rates',
    loadComponent: () =>
      import('./rate-management/rate-management.component').then(
        (m) => m.RateManagementComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: [Role.BRANCH_ADMIN], breadcrumb: 'Rate Management' },
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./invoice-detail/invoice-detail.component').then(
        (m) => m.InvoiceDetailComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: BILLING_ROLES, breadcrumb: 'Invoice Detail' },
  },
  {
    path: ':id/payment',
    loadComponent: () =>
      import('./payment-form/payment-form.component').then(
        (m) => m.PaymentFormComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: BILLING_ROLES, breadcrumb: 'Record Payment' },
  },
];
