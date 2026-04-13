import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role } from '@core/models/role.enum';

const REPORTING_ROLES = [Role.AUDITOR, Role.REGIONAL_ADMIN, Role.SUPER_ADMIN];

export const REPORTING_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./audit-log/audit-log.component').then(
        (m) => m.AuditLogComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: REPORTING_ROLES, breadcrumb: 'Audit Log' },
  },
  {
    path: 'builder',
    loadComponent: () =>
      import('./report-builder/report-builder.component').then(
        (m) => m.ReportBuilderComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: REPORTING_ROLES, breadcrumb: 'Report Builder' },
  },
  {
    path: 'chain-of-custody',
    loadComponent: () =>
      import('./chain-of-custody/chain-of-custody.component').then(
        (m) => m.ChainOfCustodyComponent,
      ),
    canActivate: [roleGuard],
    data: {
      roles: [...REPORTING_ROLES, Role.BRANCH_ADMIN],
      breadcrumb: 'Chain of Custody',
    },
  },
  {
    path: 'scheduled',
    loadComponent: () =>
      import('./scheduled-reports/scheduled-reports.component').then(
        (m) => m.ScheduledReportsComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: REPORTING_ROLES, breadcrumb: 'Scheduled Reports' },
  },
];
