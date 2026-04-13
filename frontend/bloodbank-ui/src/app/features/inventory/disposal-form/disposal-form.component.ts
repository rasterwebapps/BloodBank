import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
} from '@angular/core';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators, FormControl } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule, MatChipInputEvent } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { firstValueFrom } from 'rxjs';

import { ConfirmDialogComponent } from '@shared/components/confirm-dialog/confirm-dialog.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { NotificationService } from '@core/services/notification.service';
import { LogisticsService } from '../services/logistics.service';
import {
  DisposalReason,
  DISPOSAL_REASON_OPTIONS,
} from '../models/inventory.model';

/**
 * Form for disposing of blood units with reason tracking and confirmation.
 */
@Component({
  selector: 'app-disposal-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatDialogModule,
    ErrorCardComponent,
  ],
  templateUrl: './disposal-form.component.html',
  styleUrl: './disposal-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DisposalFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly logisticsService = inject(LogisticsService);
  private readonly notification = inject(NotificationService);
  private readonly dialog = inject(MatDialog);

  // ── State ──────────────────────────────────────────────────────
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly successState = signal(false);
  readonly disposedCount = signal(0);
  readonly unitIds = signal<string[]>([]);

  // ── Chip input ─────────────────────────────────────────────────
  readonly separatorKeyCodes = [ENTER, COMMA] as const;

  // ── Options ────────────────────────────────────────────────────
  readonly disposalReasonOptions = DISPOSAL_REASON_OPTIONS;

  // ── Computed ───────────────────────────────────────────────────
  readonly hasUnits = computed(() => this.unitIds().length > 0);

  // ── Form ───────────────────────────────────────────────────────
  readonly form = this.fb.group({
    reason: this.fb.nonNullable.control<DisposalReason>(
      DisposalReason.EXPIRED,
      [Validators.required],
    ),
    notes: this.fb.nonNullable.control(''),
  });

  get reasonCtrl(): FormControl<DisposalReason> {
    return this.form.controls.reason;
  }

  // ── Chip handlers ──────────────────────────────────────────────

  addUnitId(event: MatChipInputEvent): void {
    const value = (event.value ?? '').trim();
    if (value && !this.unitIds().includes(value)) {
      this.unitIds.set([...this.unitIds(), value]);
    }
    event.chipInput.clear();
  }

  removeUnitId(id: string): void {
    this.unitIds.set(this.unitIds().filter((u) => u !== id));
  }

  // ── Submission ─────────────────────────────────────────────────

  async onSubmit(): Promise<void> {
    if (this.form.invalid || !this.hasUnits()) {
      this.form.markAllAsTouched();
      return;
    }

    // Show confirmation dialog
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirm Disposal',
        message: `You are about to dispose of ${this.unitIds().length} blood unit(s). This action cannot be undone.`,
        confirmText: 'Dispose',
        cancelText: 'Cancel',
        color: 'warn',
      },
    });

    const confirmed = await firstValueFrom(dialogRef.afterClosed());
    if (!confirmed) return;

    this.saving.set(true);
    this.error.set(null);

    try {
      const formValue = this.form.getRawValue();
      await this.logisticsService.disposeUnits({
        unitIds: this.unitIds(),
        reason: formValue.reason,
        notes: formValue.notes || undefined,
      });
      this.disposedCount.set(this.unitIds().length);
      this.successState.set(true);
      this.notification.success(
        `${this.unitIds().length} unit(s) disposed successfully.`,
      );
    } catch {
      this.error.set('Failed to dispose units. Please try again.');
      this.notification.error('Disposal failed.');
    } finally {
      this.saving.set(false);
    }
  }

  reset(): void {
    this.form.reset({
      reason: DisposalReason.EXPIRED,
      notes: '',
    });
    this.unitIds.set([]);
    this.successState.set(false);
    this.disposedCount.set(0);
    this.error.set(null);
  }

  goToDashboard(): void {
    void this.router.navigate(['/staff/inventory']);
  }
}
