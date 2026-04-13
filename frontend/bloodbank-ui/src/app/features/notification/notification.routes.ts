import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role } from '@core/models/role.enum';

export const NOTIFICATION_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./notification-list/notification-list.component').then(
        (m) => m.NotificationListComponent,
      ),
    canActivate: [roleGuard],
    data: {
      roles: [Role.BRANCH_ADMIN, Role.BRANCH_MANAGER],
      breadcrumb: 'Notifications',
    },
  },
  {
    path: 'templates',
    loadComponent: () =>
      import('./template-management/template-management.component').then(
        (m) => m.TemplateManagementComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: [Role.BRANCH_ADMIN], breadcrumb: 'Templates' },
  },
  {
    path: 'campaigns',
    loadComponent: () =>
      import('./campaign/campaign.component').then(
        (m) => m.CampaignComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: [Role.BRANCH_ADMIN], breadcrumb: 'Campaigns' },
  },
  {
    path: 'preferences',
    loadComponent: () =>
      import('./preference/preference.component').then(
        (m) => m.PreferenceComponent,
      ),
    canActivate: [roleGuard],
    data: {
      roles: [
        Role.BRANCH_ADMIN,
        Role.BRANCH_MANAGER,
        Role.DOCTOR,
        Role.LAB_TECHNICIAN,
        Role.PHLEBOTOMIST,
        Role.NURSE,
      ],
      breadcrumb: 'Preferences',
    },
  },
];
