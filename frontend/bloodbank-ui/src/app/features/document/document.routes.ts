import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';
import { Role } from '@core/models/role.enum';

const DOCUMENT_VIEW_ROLES = [
  Role.BRANCH_ADMIN,
  Role.BRANCH_MANAGER,
  Role.DOCTOR,
  Role.LAB_TECHNICIAN,
  Role.PHLEBOTOMIST,
  Role.NURSE,
  Role.AUDITOR,
  Role.BILLING_CLERK,
  Role.RECEPTIONIST,
];

const DOCUMENT_UPLOAD_ROLES = [
  Role.BRANCH_ADMIN,
  Role.BRANCH_MANAGER,
  Role.DOCTOR,
  Role.LAB_TECHNICIAN,
];

export const DOCUMENT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./document-browser/document-browser.component').then(
        (m) => m.DocumentBrowserComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: DOCUMENT_VIEW_ROLES, breadcrumb: 'Documents' },
  },
  {
    path: 'upload',
    loadComponent: () =>
      import('./document-upload/document-upload.component').then(
        (m) => m.DocumentUploadComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: DOCUMENT_UPLOAD_ROLES, breadcrumb: 'Upload Document' },
  },
  {
    path: ':id/versions',
    loadComponent: () =>
      import('./document-version/document-version.component').then(
        (m) => m.DocumentVersionComponent,
      ),
    canActivate: [roleGuard],
    data: {
      roles: [
        Role.BRANCH_ADMIN,
        Role.BRANCH_MANAGER,
        Role.DOCTOR,
        Role.LAB_TECHNICIAN,
        Role.AUDITOR,
      ],
      breadcrumb: 'Version History',
    },
  },
];
