import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role } from '@core/models/role.enum';

/** Roles allowed for donor management. */
const DONOR_ROLES = [
  Role.RECEPTIONIST,
  Role.PHLEBOTOMIST,
  Role.BRANCH_ADMIN,
  Role.BRANCH_MANAGER,
];

export const DONOR_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./donor-list/donor-list.component').then(
        (m) => m.DonorListComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: DONOR_ROLES, breadcrumb: 'List' },
  },
  {
    path: 'search',
    loadComponent: () =>
      import('./donor-search/donor-search.component').then(
        (m) => m.DonorSearchComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: DONOR_ROLES, breadcrumb: 'Search' },
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./donor-form/donor-form.component').then(
        (m) => m.DonorFormComponent,
      ),
    canActivate: [roleGuard],
    data: {
      roles: [Role.RECEPTIONIST, Role.BRANCH_ADMIN, Role.BRANCH_MANAGER],
      breadcrumb: 'Register',
    },
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./donor-detail/donor-detail.component').then(
        (m) => m.DonorDetailComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: DONOR_ROLES, breadcrumb: 'Detail' },
  },
  {
    path: ':id/edit',
    loadComponent: () =>
      import('./donor-form/donor-form.component').then(
        (m) => m.DonorFormComponent,
      ),
    canActivate: [roleGuard],
    data: {
      roles: [Role.RECEPTIONIST, Role.BRANCH_ADMIN, Role.BRANCH_MANAGER],
      breadcrumb: 'Edit',
    },
  },
];
