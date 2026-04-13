import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role } from '@core/models/role.enum';

/** Roles with access to the inventory feature. */
const INVENTORY_ROLES = [Role.INVENTORY_MANAGER];

export const INVENTORY_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./stock-dashboard/stock-dashboard.component').then(
        (m) => m.StockDashboardComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: INVENTORY_ROLES, breadcrumb: 'Dashboard' },
  },
  {
    path: 'units',
    loadComponent: () =>
      import('./blood-unit-list/blood-unit-list.component').then(
        (m) => m.BloodUnitListComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: INVENTORY_ROLES, breadcrumb: 'Blood Units' },
  },
  {
    path: 'processing',
    loadComponent: () =>
      import('./component-processing/component-processing.component').then(
        (m) => m.ComponentProcessingComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: INVENTORY_ROLES, breadcrumb: 'Processing' },
  },
  {
    path: 'storage',
    loadComponent: () =>
      import('./storage-management/storage-management.component').then(
        (m) => m.StorageManagementComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: INVENTORY_ROLES, breadcrumb: 'Storage' },
  },
  {
    path: 'transfer',
    loadComponent: () =>
      import('./transfer-form/transfer-form.component').then(
        (m) => m.TransferFormComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: INVENTORY_ROLES, breadcrumb: 'Transfer' },
  },
  {
    path: 'disposal',
    loadComponent: () =>
      import('./disposal-form/disposal-form.component').then(
        (m) => m.DisposalFormComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: INVENTORY_ROLES, breadcrumb: 'Disposal' },
  },
];
