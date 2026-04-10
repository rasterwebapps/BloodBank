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
import { AuthService } from '@core/auth/auth.service';

/**
 * Navigation item definition.
 */
export interface NavItem {
  label: string;
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
    { label: 'Dashboard', icon: 'dashboard', route: '/staff/dashboard', roles: ['*'] },
    { label: 'Donors', icon: 'people', route: '/staff/donors', roles: ['RECEPTIONIST', 'PHLEBOTOMIST', 'BRANCH_ADMIN'] },
    { label: 'Collections', icon: 'bloodtype', route: '/staff/collections', roles: ['PHLEBOTOMIST'] },
    { label: 'Lab', icon: 'science', route: '/staff/lab', roles: ['LAB_TECHNICIAN'] },
    { label: 'Inventory', icon: 'inventory_2', route: '/staff/inventory', roles: ['INVENTORY_MANAGER'] },
    { label: 'Transfusions', icon: 'medical_services', route: '/staff/transfusions', roles: ['DOCTOR', 'NURSE'] },
    { label: 'Hospitals', icon: 'local_hospital', route: '/staff/hospitals', roles: ['BRANCH_ADMIN', 'BRANCH_MANAGER'] },
    { label: 'Billing', icon: 'receipt_long', route: '/staff/billing', roles: ['BILLING_CLERK'] },
    { label: 'Blood Camps', icon: 'camping', route: '/staff/camps', roles: ['CAMP_COORDINATOR'] },
    { label: 'Reports', icon: 'assessment', route: '/staff/reports', roles: ['REGIONAL_ADMIN', 'AUDITOR'] },
    { label: 'Compliance', icon: 'verified', route: '/staff/compliance', roles: ['AUDITOR'] },
    { label: 'Notifications', icon: 'notifications', route: '/staff/notifications', roles: ['SYSTEM_ADMIN'] },
    { label: 'Documents', icon: 'description', route: '/staff/documents', roles: ['*'] },
    { label: 'Emergency', icon: 'emergency', route: '/staff/emergency', roles: ['DOCTOR', 'BRANCH_MANAGER'] },
    { label: 'Settings', icon: 'settings', route: '/staff/settings', roles: ['BRANCH_ADMIN', 'SUPER_ADMIN'] },
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
