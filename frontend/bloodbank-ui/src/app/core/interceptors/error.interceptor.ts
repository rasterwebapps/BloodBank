import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';

/**
 * Global HTTP error interceptor.
 * Handles common HTTP error statuses with appropriate user feedback.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      switch (error.status) {
        case 401:
          // Keycloak auto-handles token refresh; this means refresh failed
          router.navigate(['/login']);
          break;
        case 403:
          snackBar.open('You do not have permission for this action', 'Dismiss', {
            duration: 5000,
            panelClass: ['snackbar-warn'],
          });
          break;
        case 404:
          snackBar.open('Resource not found', 'Dismiss', {
            duration: 3000,
          });
          break;
        case 422:
          // Validation errors — handled inline by form components
          break;
        case 0:
          snackBar.open('Network error. Please check your connection.', 'Retry', {
            duration: 0, // No auto-dismiss for network errors
            panelClass: ['snackbar-error'],
          });
          break;
        default:
          if (error.status >= 500) {
            snackBar.open('An unexpected server error occurred', 'Dismiss', {
              duration: 5000,
              panelClass: ['snackbar-error'],
            });
          }
          break;
      }
      return throwError(() => error);
    }),
  );
};
