import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
} from '@angular/core';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormControl, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FormFieldComponent } from '@shared/components/form-field/form-field.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { NotificationService } from '@core/services/notification.service';
import { HospitalPortalService } from '../services/hospital-portal.service';
import {
  BloodGroupEnum,
  ComponentTypeEnum,
  RequestPriorityEnum,
  BLOOD_GROUP_OPTIONS,
  COMPONENT_TYPE_OPTIONS,
  PRIORITY_OPTIONS,
} from '../models/hospital-portal.model';

@Component({
  selector: 'app-blood-request-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    FormFieldComponent,
    ErrorCardComponent,
  ],
  templateUrl: './blood-request-form.component.html',
  styleUrl: './blood-request-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BloodRequestFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly hospitalPortalService = inject(HospitalPortalService);
  private readonly notification = inject(NotificationService);

  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly today = new Date();

  readonly bloodGroupOptions = BLOOD_GROUP_OPTIONS;
  readonly componentTypeOptions = COMPONENT_TYPE_OPTIONS;
  readonly priorityOptions = PRIORITY_OPTIONS;

  readonly form = this.fb.nonNullable.group({
    bloodGroup: this.fb.nonNullable.control<BloodGroupEnum>(
      '' as BloodGroupEnum,
      [Validators.required],
    ),
    componentType: this.fb.nonNullable.control<ComponentTypeEnum>(
      '' as ComponentTypeEnum,
      [Validators.required],
    ),
    quantity: this.fb.nonNullable.control<number>(1, [
      Validators.required,
      Validators.min(1),
      Validators.max(100),
    ]),
    priority: this.fb.nonNullable.control<RequestPriorityEnum>(
      RequestPriorityEnum.ROUTINE,
      [Validators.required],
    ),
    requiredDate: this.fb.nonNullable.control<string>('', [Validators.required]),
    clinicalNotes: this.fb.nonNullable.control<string>('', [
      Validators.maxLength(1000),
    ]),
  });

  get quantityCtrl(): FormControl<number> {
    return this.form.controls.quantity;
  }

  get clinicalNotesCtrl(): FormControl<string> {
    return this.form.controls.clinicalNotes;
  }

  async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    try {
      const value = this.form.getRawValue();
      const rawDate: unknown = value.requiredDate;
      const requiredDate =
        rawDate instanceof Date
          ? (rawDate as Date).toISOString().split('T')[0]
          : String(rawDate);
      await this.hospitalPortalService.createRequest({
        bloodGroup: value.bloodGroup,
        componentType: value.componentType,
        quantity: value.quantity,
        priority: value.priority,
        requiredDate,
        clinicalNotes: value.clinicalNotes,
      });
      this.notification.success('Blood request submitted successfully.');
      this.router.navigate(['/hospital', 'my-requests']);
    } catch {
      this.error.set('Failed to submit request. Please try again.');
    } finally {
      this.saving.set(false);
    }
  }

  cancel(): void {
    this.router.navigate(['/hospital', 'my-requests']);
  }
}
