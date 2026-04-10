import { Injectable, inject, signal, computed } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { User } from '../models/user.model';
import { Role } from '../models/role.enum';

/**
 * Keycloak authentication wrapper service.
 * Provides reactive user state via Angular Signals.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly keycloakService = inject(KeycloakService);

  /** Current authenticated user */
  readonly currentUser = signal<User | null>(null);

  /** Whether the user is authenticated */
  readonly isAuthenticated = signal(false);

  /** User's display name */
  readonly displayName = computed(() => {
    const user = this.currentUser();
    return user ? `${user.firstName} ${user.lastName}` : '';
  });

  /** User's roles */
  readonly userRoles = computed(() => this.currentUser()?.roles ?? []);

  /** User's branch ID from JWT claims */
  readonly branchId = computed(() => this.currentUser()?.branchId ?? null);

  /**
   * Load user profile from Keycloak and populate signals.
   * Called after Keycloak init succeeds.
   */
  async loadUserProfile(): Promise<void> {
    try {
      const isLoggedIn = await this.keycloakService.isLoggedIn();
      this.isAuthenticated.set(isLoggedIn);

      if (isLoggedIn) {
        const profile = await this.keycloakService.loadUserProfile();
        const realmRoles = this.keycloakService.getUserRoles(false);
        const clientRoles = this.keycloakService.getUserRoles(true);
        const allRoles = [...new Set([...realmRoles, ...clientRoles])];

        const tokenParsed = this.keycloakService.getKeycloakInstance().tokenParsed;

        const user: User = {
          id: profile.id ?? '',
          username: profile.username ?? '',
          email: profile.email ?? '',
          firstName: profile.firstName ?? '',
          lastName: profile.lastName ?? '',
          roles: allRoles.filter((r): r is Role =>
            Object.values(Role).includes(r as Role),
          ),
          branchId: (tokenParsed?.['branch_id'] as string) ?? null,
          branchName: (tokenParsed?.['branch_name'] as string) ?? null,
          realmRoles,
          clientRoles,
        };

        this.currentUser.set(user);
      }
    } catch {
      this.isAuthenticated.set(false);
      this.currentUser.set(null);
    }
  }

  /**
   * Check if user has at least one of the required roles.
   * Wildcard '*' matches all authenticated users.
   */
  hasAnyRole(roles: string[]): boolean {
    if (!this.isAuthenticated()) {
      return false;
    }
    if (roles.includes('*')) {
      return true;
    }
    const userRoles = this.userRoles();
    return roles.some((role) => userRoles.includes(role as Role));
  }

  /** Check if user has a specific role */
  hasRole(role: string): boolean {
    return this.hasAnyRole([role]);
  }

  /** Get the raw JWT access token */
  async getToken(): Promise<string> {
    return this.keycloakService.getToken();
  }

  /** Trigger Keycloak logout */
  logout(): void {
    this.keycloakService.logout(window.location.origin);
  }

  /** Trigger Keycloak account management page */
  manageAccount(): void {
    this.keycloakService.getKeycloakInstance().accountManagement();
  }
}
