import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

/**
 * Role-based route guard.
 * Checks that the authenticated user has at least one of the required roles
 * specified in `route.data['roles']`.
 *
 * Usage:
 * ```typescript
 * {
 *   path: 'donors',
 *   component: DonorListComponent,
 *   canActivate: [roleGuard],
 *   data: { roles: ['BRANCH_ADMIN', 'RECEPTIONIST'] },
 * }
 * ```
 */
export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const requiredRoles: string[] = route.data['roles'] ?? [];

  if (requiredRoles.length === 0 || requiredRoles.includes('*') || authService.hasAnyRole(requiredRoles)) {
    return true;
  }

  return router.createUrlTree(['/unauthorized']);
};
