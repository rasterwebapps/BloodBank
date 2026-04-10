import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { BranchContextService } from '../services/branch-context.service';

/**
 * Branch context guard.
 * Ensures a branch is selected before allowing navigation to branch-scoped routes.
 * Redirects to branch selection page if no branch is active.
 */
export const branchGuard: CanActivateFn = () => {
  const branchContext = inject(BranchContextService);
  const router = inject(Router);

  if (branchContext.branchId()) {
    return true;
  }

  return router.createUrlTree(['/select-branch']);
};
