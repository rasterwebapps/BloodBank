import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';

export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./dashboard-placeholder.component').then(
        (m) => m.DashboardPlaceholderComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: ['*'] },
  },
];
