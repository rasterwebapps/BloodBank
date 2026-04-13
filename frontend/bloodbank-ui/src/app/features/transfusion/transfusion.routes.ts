import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role } from '@core/models/role.enum';

/** Roles allowed to access transfusion management. */
const TRANSFUSION_ROLES = [Role.DOCTOR, Role.NURSE];

export const TRANSFUSION_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import(
        './cross-match-request/cross-match-request.component'
      ).then((m) => m.CrossMatchRequestComponent),
    canActivate: [roleGuard],
    data: { roles: TRANSFUSION_ROLES, breadcrumb: 'Cross-Match' },
  },
  {
    path: 'results',
    loadComponent: () =>
      import(
        './cross-match-result/cross-match-result.component'
      ).then((m) => m.CrossMatchResultComponent),
    canActivate: [roleGuard],
    data: { roles: TRANSFUSION_ROLES, breadcrumb: 'Results' },
  },
  {
    path: 'issue',
    loadComponent: () =>
      import('./blood-issue/blood-issue.component').then(
        (m) => m.BloodIssueComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: TRANSFUSION_ROLES, breadcrumb: 'Issue Blood' },
  },
  {
    path: 'record',
    loadComponent: () =>
      import(
        './transfusion-record/transfusion-record.component'
      ).then((m) => m.TransfusionRecordComponent),
    canActivate: [roleGuard],
    data: { roles: TRANSFUSION_ROLES, breadcrumb: 'Record Transfusion' },
  },
  {
    path: 'reactions',
    loadComponent: () =>
      import('./reaction-report/reaction-report.component').then(
        (m) => m.ReactionReportComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: TRANSFUSION_ROLES, breadcrumb: 'Reaction Report' },
  },
  {
    path: 'hemovigilance',
    loadComponent: () =>
      import(
        './hemovigilance-list/hemovigilance-list.component'
      ).then((m) => m.HemovigilanceListComponent),
    canActivate: [roleGuard],
    data: { roles: TRANSFUSION_ROLES, breadcrumb: 'Hemovigilance' },
  },
];
