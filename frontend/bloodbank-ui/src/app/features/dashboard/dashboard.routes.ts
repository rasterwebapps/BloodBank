import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';

export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./dashboard.component').then((m) => m.DashboardComponent),
    canActivate: [roleGuard],
    data: { roles: ['*'] },
  },
];
