import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { from, switchMap } from 'rxjs';

/**
 * HTTP interceptor that injects the JWT Bearer token on every outgoing request.
 * Excludes asset and i18n requests.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const keycloak = inject(KeycloakService);

  // Skip token injection for static assets
  const excludedPaths = ['/assets', '/i18n'];
  if (excludedPaths.some((path) => req.url.includes(path))) {
    return next(req);
  }

  return from(keycloak.getToken()).pipe(
    switchMap((token) => {
      if (token) {
        const authReq = req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`,
          },
        });
        return next(authReq);
      }
      return next(req);
    }),
  );
};
