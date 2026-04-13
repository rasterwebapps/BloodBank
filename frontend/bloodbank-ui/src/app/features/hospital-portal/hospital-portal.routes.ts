import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';

export const HOSPITAL_PORTAL_ROUTES: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./hospital-dashboard/hospital-dashboard.component').then(
        (m) => m.HospitalDashboardComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: ['HOSPITAL_USER'], breadcrumb: 'Dashboard' },
  },
  {
    path: 'request-blood',
    loadComponent: () =>
      import('./blood-request-form/blood-request-form.component').then(
        (m) => m.BloodRequestFormComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: ['HOSPITAL_USER'], breadcrumb: 'Request Blood' },
  },
  {
    path: 'my-requests',
    loadComponent: () =>
      import('./request-tracking/request-tracking.component').then(
        (m) => m.RequestTrackingComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: ['HOSPITAL_USER'], breadcrumb: 'My Requests' },
  },
  {
    path: 'contract',
    loadComponent: () =>
      import('./contract-view/contract-view.component').then(
        (m) => m.ContractViewComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: ['HOSPITAL_USER'], breadcrumb: 'Contract' },
  },
  {
    path: 'feedback',
    loadComponent: () =>
      import('./feedback-form/feedback-form.component').then(
        (m) => m.FeedbackFormComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: ['HOSPITAL_USER'], breadcrumb: 'Feedback' },
  },
];
