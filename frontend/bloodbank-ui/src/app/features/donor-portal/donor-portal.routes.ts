import { Routes } from '@angular/router';
import { roleGuard } from '@core/guards/role.guard';

export const DONOR_PORTAL_ROUTES: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./donor-dashboard/donor-dashboard.component').then(
        (m) => m.DonorDashboardComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: ['DONOR'], breadcrumb: 'Dashboard' },
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./donor-self-registration/donor-self-registration.component').then(
        (m) => m.DonorSelfRegistrationComponent,
      ),
    data: { breadcrumb: 'Register' },
  },
  {
    path: 'history',
    loadComponent: () =>
      import('./donation-history/donation-history.component').then(
        (m) => m.DonationHistoryComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: ['DONOR'], breadcrumb: 'My History' },
  },
  {
    path: 'appointments/book',
    loadComponent: () =>
      import('./appointment-booking/appointment-booking.component').then(
        (m) => m.AppointmentBookingComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: ['DONOR'], breadcrumb: 'Book Appointment' },
  },
  {
    path: 'eligibility',
    loadComponent: () =>
      import('./eligibility-check/eligibility-check.component').then(
        (m) => m.EligibilityCheckComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: ['DONOR'], breadcrumb: 'Check Eligibility' },
  },
  {
    path: 'camps',
    loadComponent: () =>
      import('./nearby-camp-finder/nearby-camp-finder.component').then(
        (m) => m.NearbyCampFinderComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: ['DONOR'], breadcrumb: 'Find Camps' },
  },
  {
    path: 'card',
    loadComponent: () =>
      import('./digital-donor-card/digital-donor-card.component').then(
        (m) => m.DigitalDonorCardComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: ['DONOR'], breadcrumb: 'My Card' },
  },
  {
    path: 'refer',
    loadComponent: () =>
      import('./referral/referral.component').then(
        (m) => m.ReferralComponent,
      ),
    canActivate: [roleGuard],
    data: { roles: ['DONOR'], breadcrumb: 'Refer a Friend' },
  },
];
