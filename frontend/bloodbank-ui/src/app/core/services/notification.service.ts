import { Injectable, inject } from '@angular/core';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';

/**
 * MatSnackBar wrapper for consistent notification display.
 * Maps notification types to appropriate durations and styles.
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);

  private readonly defaultConfigs: Record<NotificationType, MatSnackBarConfig> = {
    success: {
      duration: 3000,
      panelClass: ['snackbar-success'],
    },
    error: {
      duration: 5000,
      panelClass: ['snackbar-error'],
    },
    warning: {
      duration: 5000,
      panelClass: ['snackbar-warning'],
    },
    info: {
      duration: 3000,
      panelClass: ['snackbar-info'],
    },
  };

  /** Show a success notification */
  success(message: string, action = 'OK'): void {
    this.show(message, action, 'success');
  }

  /** Show an error notification */
  error(message: string, action = 'Dismiss'): void {
    this.show(message, action, 'error');
  }

  /** Show a warning notification */
  warning(message: string, action = 'Dismiss'): void {
    this.show(message, action, 'warning');
  }

  /** Show an info notification */
  info(message: string, action = 'OK'): void {
    this.show(message, action, 'info');
  }

  /** Show a notification with custom configuration */
  show(message: string, action: string, type: NotificationType): void {
    this.snackBar.open(message, action, this.defaultConfigs[type]);
  }
}
