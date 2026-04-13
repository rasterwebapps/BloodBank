import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
} from '@angular/core';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';

import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { NotificationService } from '@core/services/notification.service';
import { LogisticsService } from '../services/logistics.service';
import {
  BloodUnit,
  COMPONENT_TYPE_OPTIONS,
  getComponentTypeLabel,
} from '../models/inventory.model';

/**
 * Form to process a whole blood unit into individual component types.
 */
@Component({
  selector: 'app-component-processing',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatListModule,
    ErrorCardComponent,
  ],
  templateUrl: './component-processing.component.html',
  styleUrl: './component-processing.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ComponentProcessingComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly logisticsService = inject(LogisticsService);
  private readonly notification = inject(NotificationService);

  // ── State ──────────────────────────────────────────────────────
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly producedUnits = signal<BloodUnit[]>([]);
  readonly successState = signal(false);
  readonly selectedComponentTypes = signal<string[]>([]);

  // ── Options ────────────────────────────────────────────────────
  readonly componentTypeOptions = COMPONENT_TYPE_OPTIONS;

  // ── Computed ───────────────────────────────────────────────────
  readonly hasSelections = computed(() => this.selectedComponentTypes().length > 0);

  // ── Form ───────────────────────────────────────────────────────
  readonly form = this.fb.group({
    sourceUnitId: this.fb.nonNullable.control('', [Validators.required]),
    notes: this.fb.nonNullable.control(''),
  });

  get sourceUnitIdCtrl() {
    return this.form.controls.sourceUnitId;
  }

  get notesCtrl() {
    return this.form.controls.notes;
  }

  // ── Event handlers ─────────────────────────────────────────────

  onComponentTypeChange(value: string, checked: boolean): void {
    const current = this.selectedComponentTypes();
    if (checked) {
      this.selectedComponentTypes.set([...current, value]);
    } else {
      this.selectedComponentTypes.set(current.filter((v) => v !== value));
    }
  }

  isSelected(value: string): boolean {
    return this.selectedComponentTypes().includes(value);
  }

  async onSubmit(): Promise<void> {
    if (this.form.invalid || !this.hasSelections()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    try {
      const formValue = this.form.getRawValue();
      const units = await this.logisticsService.processComponents({
        sourceUnitId: formValue.sourceUnitId,
        componentTypes: this.selectedComponentTypes(),
        notes: formValue.notes || undefined,
      });
      this.producedUnits.set(units);
      this.successState.set(true);
      this.notification.success(
        `Successfully processed ${units.length} component(s).`,
      );
    } catch {
      this.error.set('Failed to process components. Please try again.');
      this.notification.error('Component processing failed.');
    } finally {
      this.saving.set(false);
    }
  }

  reset(): void {
    this.form.reset();
    this.selectedComponentTypes.set([]);
    this.producedUnits.set([]);
    this.successState.set(false);
    this.error.set(null);
  }

  goToDashboard(): void {
    void this.router.navigate(['/staff/inventory']);
  }

  getComponentTypeLabel(value: string): string {
    return getComponentTypeLabel(value);
  }
}
