import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role } from '@core/models/role.enum';

export const SETTINGS_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'system',
    pathMatch: 'full',
  },
  {
    path: 'system',
    loadComponent: () =>
      import('./system-settings/system-settings.component').then(
        m => m.SystemSettingsComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: [Role.BRANCH_ADMIN, Role.SUPER_ADMIN], breadcrumb: 'System Settings' },
  },
  {
    path: 'feature-flags',
    loadComponent: () =>
      import('./feature-flags/feature-flags.component').then(
        m => m.FeatureFlagsComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: [Role.SUPER_ADMIN], breadcrumb: 'Feature Flags' },
  },
];
