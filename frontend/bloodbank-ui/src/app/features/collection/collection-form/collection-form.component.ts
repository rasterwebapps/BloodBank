import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
} from '@angular/core';
import { Router } from '@angular/router';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormControl,
  Validators,
} from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { FormFieldComponent } from '@shared/components/form-field/form-field.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { NotificationService } from '@core/services/notification.service';
import { CollectionService } from '../services/collection.service';
import { CollectionCreateRequest } from '../models/collection.model';

/**
 * Form for recording a blood collection.
 * Captures donor, bag info, vitals, and timing.
 */
@Component({
  selector: 'app-collection-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    FormFieldComponent,
    ErrorCardComponent,
  ],
  templateUrl: './collection-form.component.html',
  styleUrl: './collection-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CollectionFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly collectionService = inject(CollectionService);
  private readonly notification = inject(NotificationService);

  // ── State ──────────────────────────────────────────────────────
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  // ── Form ───────────────────────────────────────────────────────
  readonly form = this.fb.group({
    donorId: this.fb.nonNullable.control('', Validators.required),
    bagNumber: this.fb.nonNullable.control('', Validators.required),
    volumeMl: this.fb.nonNullable.control(450, [
      Validators.required,
      Validators.min(100),
      Validators.max(600),
    ]),
    startTime: this.fb.nonNullable.control('', Validators.required),
    endTime: this.fb.nonNullable.control('', Validators.required),
    systolicBP: this.fb.nonNullable.control(120, [
      Validators.required,
      Validators.min(60),
      Validators.max(250),
    ]),
    diastolicBP: this.fb.nonNullable.control(80, [
      Validators.required,
      Validators.min(40),
      Validators.max(150),
    ]),
    pulse: this.fb.nonNullable.control(72, [
      Validators.required,
      Validators.min(40),
      Validators.max(200),
    ]),
    weight: this.fb.nonNullable.control(60, [
      Validators.required,
      Validators.min(45),
      Validators.max(300),
    ]),
    notes: this.fb.nonNullable.control(''),
  });

  // ── Form control accessors ─────────────────────────────────────
  get donorIdCtrl(): FormControl<string> {
    return this.form.controls.donorId;
  }
  get bagNumberCtrl(): FormControl<string> {
    return this.form.controls.bagNumber;
  }
  get notesCtrl(): FormControl<string> {
    return this.form.controls.notes;
  }

  readonly errorMessages: Record<string, Record<string, string>> = {
    donorId: { required: 'Donor ID is required' },
    bagNumber: { required: 'Bag number is required' },
    volumeMl: {
      required: 'Volume is required',
      min: 'Minimum 100 mL',
      max: 'Maximum 600 mL',
    },
    startTime: { required: 'Start time is required' },
    endTime: { required: 'End time is required' },
    systolicBP: {
      required: 'Systolic BP is required',
      min: 'Minimum 60 mmHg',
      max: 'Maximum 250 mmHg',
    },
    diastolicBP: {
      required: 'Diastolic BP is required',
      min: 'Minimum 40 mmHg',
      max: 'Maximum 150 mmHg',
    },
    pulse: {
      required: 'Pulse is required',
      min: 'Minimum 40 bpm',
      max: 'Maximum 200 bpm',
    },
    weight: {
      required: 'Weight is required',
      min: 'Minimum 45 kg',
      max: 'Maximum 300 kg',
    },
  };

  // ── Form submission ────────────────────────────────────────────

  async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    try {
      const v = this.form.getRawValue();
      const request: CollectionCreateRequest = {
        donorId: v.donorId,
        bagNumber: v.bagNumber,
        volumeMl: v.volumeMl,
        startTime: v.startTime,
        endTime: v.endTime,
        systolicBP: v.systolicBP,
        diastolicBP: v.diastolicBP,
        pulse: v.pulse,
        weight: v.weight,
        notes: v.notes,
      };

      await this.collectionService.create(request);
      this.notification.success('Collection recorded successfully.');
      void this.router.navigate(['/staff/collections']);
    } catch {
      this.error.set('Failed to record collection. Please try again.');
      this.notification.error('Failed to record collection.');
    } finally {
      this.saving.set(false);
    }
  }

  // ── Navigation ─────────────────────────────────────────────────

  cancel(): void {
    void this.router.navigate(['/staff/collections']);
  }
}
