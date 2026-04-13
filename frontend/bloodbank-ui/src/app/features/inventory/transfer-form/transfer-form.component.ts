import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
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
import { COMMA, ENTER } from '@angular/cdk/keycodes';

import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { NotificationService } from '@core/services/notification.service';
import { BranchContextService } from '@core/services/branch-context.service';
import { Branch } from '@core/services/branch-context.service';
import { TransferService } from '../services/transfer.service';
import { Transfer } from '../models/inventory.model';

/**
 * Form for initiating an inter-branch blood unit transfer.
 */
@Component({
  selector: 'app-transfer-form',
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
    ErrorCardComponent,
  ],
  templateUrl: './transfer-form.component.html',
  styleUrl: './transfer-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransferFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly transferService = inject(TransferService);
  private readonly notification = inject(NotificationService);
  readonly branchContext = inject(BranchContextService);

  // ── State ──────────────────────────────────────────────────────
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly createdTransfer = signal<Transfer | null>(null);
  readonly unitIds = signal<string[]>([]);

  // ── Chip input ─────────────────────────────────────────────────
  readonly separatorKeyCodes = [ENTER, COMMA] as const;

  // ── Computed ───────────────────────────────────────────────────
  readonly availableBranches = computed<Branch[]>(() =>
    this.branchContext
      .branches()
      .filter((b) => b.id !== this.branchContext.branchId()),
  );

  readonly hasUnits = computed(() => this.unitIds().length > 0);

  // ── Form ───────────────────────────────────────────────────────
  readonly form = this.fb.group({
    destinationBranchId: this.fb.nonNullable.control('', [Validators.required]),
    notes: this.fb.nonNullable.control(''),
  });

  get destinationBranchIdCtrl(): FormControl<string> {
    return this.form.controls.destinationBranchId;
  }

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    // Branch list already loaded by BranchContextService during app init
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

    this.saving.set(true);
    this.error.set(null);

    try {
      const formValue = this.form.getRawValue();
      const transfer = await this.transferService.createTransfer({
        destinationBranchId: formValue.destinationBranchId,
        unitIds: this.unitIds(),
        notes: formValue.notes || undefined,
      });
      this.createdTransfer.set(transfer);
      this.notification.success(
        `Transfer initiated with ${this.unitIds().length} unit(s).`,
      );
    } catch {
      this.error.set('Failed to initiate transfer. Please try again.');
      this.notification.error('Transfer creation failed.');
    } finally {
      this.saving.set(false);
    }
  }

  reset(): void {
    this.form.reset();
    this.unitIds.set([]);
    this.createdTransfer.set(null);
    this.error.set(null);
  }

  goToDashboard(): void {
    void this.router.navigate(['/staff/inventory']);
  }
}
