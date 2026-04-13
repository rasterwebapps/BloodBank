import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role } from '@core/models/role.enum';

/** Roles allowed for collection management. */
const COLLECTION_ROLES = [Role.PHLEBOTOMIST];

export const COLLECTION_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./collection-list/collection-list.component').then(
        (m) => m.CollectionListComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: COLLECTION_ROLES, breadcrumb: 'List' },
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./collection-form/collection-form.component').then(
        (m) => m.CollectionFormComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: COLLECTION_ROLES, breadcrumb: 'Record Collection' },
  },
  {
    path: ':id/adverse-reaction',
    loadComponent: () =>
      import(
        './adverse-reaction-form/adverse-reaction-form.component'
      ).then((m) => m.AdverseReactionFormComponent),
    canActivate: [roleGuard],
    data: { roles: COLLECTION_ROLES, breadcrumb: 'Adverse Reaction' },
  },
  {
    path: ':id/samples',
    loadComponent: () =>
      import('./sample-registration/sample-registration.component').then(
        (m) => m.SampleRegistrationComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: COLLECTION_ROLES, breadcrumb: 'Register Sample' },
  },
];
