import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { BranchContextService } from '../services/branch-context.service';

/**
 * HTTP interceptor that injects the X-Branch-Id header on every API request.
 * The branch ID comes from BranchContextService (populated from JWT claims or user selection).
 */
export const branchInterceptor: HttpInterceptorFn = (req, next) => {
  const branchContext = inject(BranchContextService);
  const branchId = branchContext.branchId();

  // Skip header injection for non-API requests
  if (!req.url.includes('/api/') || !branchId) {
    return next(req);
  }

  const branchReq = req.clone({
    setHeaders: {
      'X-Branch-Id': branchId,
    },
  });

  return next(branchReq);
};
