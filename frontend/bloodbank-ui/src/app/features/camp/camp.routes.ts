import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role } from '@core/models/role.enum';

/** Roles allowed for camp management. */
const CAMP_ROLES = [Role.CAMP_COORDINATOR];

export const CAMP_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./camp-list/camp-list.component').then(
        (m) => m.CampListComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: CAMP_ROLES, breadcrumb: 'List' },
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./camp-form/camp-form.component').then(
        (m) => m.CampFormComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: CAMP_ROLES, breadcrumb: 'Plan Camp' },
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./camp-detail/camp-detail.component').then(
        (m) => m.CampDetailComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: CAMP_ROLES, breadcrumb: 'Detail' },
  },
  {
    path: ':id/edit',
    loadComponent: () =>
      import('./camp-form/camp-form.component').then(
        (m) => m.CampFormComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: CAMP_ROLES, breadcrumb: 'Edit' },
  },
  {
    path: ':id/register-donor',
    loadComponent: () =>
      import(
        './camp-donor-registration/camp-donor-registration.component'
      ).then((m) => m.CampDonorRegistrationComponent),
    canActivate: [roleGuard],
    data: { roles: CAMP_ROLES, breadcrumb: 'Register Donor' },
  },
];
