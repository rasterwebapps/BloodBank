import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role } from '@core/models/role.enum';

export const BRANCH_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./branch-list/branch-list.component').then(m => m.BranchListComponent),
    canActivate: [roleGuard],
    data: { roles: [Role.BRANCH_ADMIN, Role.SUPER_ADMIN], breadcrumb: 'List' },
  },
  {
    path: 'master-data',
    loadComponent: () =>
      import('./master-data/master-data.component').then(m => m.MasterDataComponent),
    canActivate: [roleGuard],
    data: { roles: [Role.SUPER_ADMIN], breadcrumb: 'Master Data' },
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./branch-form/branch-form.component').then(m => m.BranchFormComponent),
    canActivate: [roleGuard],
    data: { roles: [Role.SUPER_ADMIN], breadcrumb: 'Add Branch' },
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./branch-detail/branch-detail.component').then(m => m.BranchDetailComponent),
    canActivate: [roleGuard],
    data: { roles: [Role.BRANCH_ADMIN, Role.SUPER_ADMIN], breadcrumb: 'Detail' },
  },
  {
    path: ':id/edit',
    loadComponent: () =>
      import('./branch-form/branch-form.component').then(m => m.BranchFormComponent),
    canActivate: [roleGuard],
    data: { roles: [Role.BRANCH_ADMIN, Role.SUPER_ADMIN], breadcrumb: 'Edit' },
  },
];
