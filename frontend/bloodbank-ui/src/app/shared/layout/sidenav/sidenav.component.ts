import {
  Component,
  ChangeDetectionStrategy,
  inject,
  input,
  output,
  computed,
} from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '@core/auth/auth.service';

/**
 * Navigation item definition.
 * `labelKey` is an i18n key resolved via TranslatePipe.
 */
export interface NavItem {
  labelKey: string;
  icon: string;
  route: string;
  roles: string[];
}

/**
 * Sidenav with role-filtered navigation menu.
 * Items are filtered based on the authenticated user's roles.
 */
@Component({
  selector: 'app-sidenav',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    MatListModule,
    MatIconModule,
    MatTooltipModule,
    TranslatePipe,
  ],
  templateUrl: './sidenav.component.html',
  styleUrl: './sidenav.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SidenavComponent {
  private readonly authService = inject(AuthService);

  readonly collapsed = input(false);
  readonly isMobile = input(false);
  readonly collapsedChange = output<boolean>();

  /** All possible navigation items */
  private readonly allNavItems: NavItem[] = [
    { labelKey: 'NAV.DASHBOARD', icon: 'dashboard', route: '/staff/dashboard', roles: ['*'] },
    { labelKey: 'NAV.DONORS', icon: 'people', route: '/staff/donors', roles: ['RECEPTIONIST', 'PHLEBOTOMIST', 'BRANCH_ADMIN'] },
    { labelKey: 'NAV.COLLECTIONS', icon: 'bloodtype', route: '/staff/collections', roles: ['PHLEBOTOMIST'] },
    { labelKey: 'NAV.LAB', icon: 'science', route: '/staff/lab', roles: ['LAB_TECHNICIAN'] },
    { labelKey: 'NAV.INVENTORY', icon: 'inventory_2', route: '/staff/inventory', roles: ['INVENTORY_MANAGER'] },
    { labelKey: 'NAV.TRANSFUSIONS', icon: 'medical_services', route: '/staff/transfusions', roles: ['DOCTOR', 'NURSE'] },
    { labelKey: 'NAV.HOSPITALS', icon: 'local_hospital', route: '/staff/hospitals', roles: ['BRANCH_ADMIN', 'BRANCH_MANAGER'] },
    { labelKey: 'NAV.BILLING', icon: 'receipt_long', route: '/staff/billing', roles: ['BILLING_CLERK'] },
    { labelKey: 'NAV.BLOOD_CAMPS', icon: 'camping', route: '/staff/camps', roles: ['CAMP_COORDINATOR'] },
    { labelKey: 'NAV.REPORTS', icon: 'assessment', route: '/staff/reports', roles: ['REGIONAL_ADMIN', 'AUDITOR'] },
    { labelKey: 'NAV.COMPLIANCE', icon: 'verified', route: '/staff/compliance', roles: ['AUDITOR'] },
    { labelKey: 'NAV.NOTIFICATIONS', icon: 'notifications', route: '/staff/notifications', roles: ['SYSTEM_ADMIN'] },
    { labelKey: 'NAV.DOCUMENTS', icon: 'description', route: '/staff/documents', roles: ['*'] },
    { labelKey: 'NAV.EMERGENCY', icon: 'emergency', route: '/staff/emergency', roles: ['DOCTOR', 'BRANCH_MANAGER'] },
    { labelKey: 'NAV.SETTINGS', icon: 'settings', route: '/staff/settings', roles: ['BRANCH_ADMIN', 'SUPER_ADMIN'] },
    // Hospital Portal navigation
    { labelKey: 'NAV.DASHBOARD', icon: 'dashboard', route: '/hospital/dashboard', roles: ['HOSPITAL_USER'] },
    { labelKey: 'NAV.REQUEST_BLOOD', icon: 'water_drop', route: '/hospital/request-blood', roles: ['HOSPITAL_USER'] },
    { labelKey: 'NAV.MY_REQUESTS', icon: 'list_alt', route: '/hospital/my-requests', roles: ['HOSPITAL_USER'] },
    { labelKey: 'NAV.CONTRACT', icon: 'handshake', route: '/hospital/contract', roles: ['HOSPITAL_USER'] },
    { labelKey: 'NAV.FEEDBACK', icon: 'rate_review', route: '/hospital/feedback', roles: ['HOSPITAL_USER'] },
  ];

  /** Filtered navigation items based on user roles */
  readonly navItems = computed(() => {
    return this.allNavItems.filter((item) =>
      this.authService.hasAnyRole(item.roles),
    );
  });

  onBackdropClick(): void {
    if (this.isMobile()) {
      this.collapsedChange.emit(true);
    }
  }
}
