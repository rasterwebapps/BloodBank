import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role } from '@core/models/role.enum';

export const USER_MANAGEMENT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./user-list/user-list.component').then(m => m.UserListComponent),
    canActivate: [roleGuard],
    data: { roles: [Role.BRANCH_ADMIN, Role.SUPER_ADMIN], breadcrumb: 'List' },
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./user-form/user-form.component').then(m => m.UserFormComponent),
    canActivate: [roleGuard],
    data: { roles: [Role.BRANCH_ADMIN, Role.SUPER_ADMIN], breadcrumb: 'Add User' },
  },
  {
    path: ':id/edit',
    loadComponent: () =>
      import('./user-form/user-form.component').then(m => m.UserFormComponent),
    canActivate: [roleGuard],
    data: { roles: [Role.BRANCH_ADMIN, Role.SUPER_ADMIN], breadcrumb: 'Edit' },
  },
  {
    path: ':id/activity',
    loadComponent: () =>
      import('./user-activity/user-activity.component').then(m => m.UserActivityComponent),
    canActivate: [roleGuard],
    data: { roles: [Role.BRANCH_ADMIN, Role.SUPER_ADMIN], breadcrumb: 'Activity' },
  },
];
