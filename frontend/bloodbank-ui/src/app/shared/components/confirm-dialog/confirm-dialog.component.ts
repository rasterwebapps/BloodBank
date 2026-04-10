import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

/**
 * Data passed to the confirm dialog.
 */
export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  color?: 'primary' | 'accent' | 'warn';
}

/**
 * Reusable confirmation dialog with title, message, confirm/cancel buttons.
 *
 * Usage:
 * ```typescript
 * const dialogRef = this.dialog.open(ConfirmDialogComponent, {
 *   data: { title: 'Delete?', message: 'This action cannot be undone.' }
 * });
 * dialogRef.afterClosed().subscribe(result => { if (result) { ... } });
 * ```
 */
@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
  templateUrl: './confirm-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfirmDialogComponent {
  readonly data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<ConfirmDialogComponent>);

  get confirmText(): string {
    return this.data.confirmText ?? 'Confirm';
  }

  get cancelText(): string {
    return this.data.cancelText ?? 'Cancel';
  }

  get color(): string {
    return this.data.color ?? 'primary';
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}
