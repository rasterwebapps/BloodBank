import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormControl,
  Validators,
} from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { FormFieldComponent } from '@shared/components/form-field/form-field.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { NotificationService } from '@core/services/notification.service';
import { CampService } from '../services/camp.service';
import { CampDonorRegistrationRequest } from '../models/camp.model';
import { BLOOD_GROUP_OPTIONS } from '@features/donor/models/donor.model';

/**
 * Form for registering walk-in donors at a blood camp.
 */
@Component({
  selector: 'app-camp-donor-registration',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    FormFieldComponent,
    ErrorCardComponent,
  ],
  templateUrl: './camp-donor-registration.component.html',
  styleUrl: './camp-donor-registration.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CampDonorRegistrationComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly campService = inject(CampService);
  private readonly notification = inject(NotificationService);

  // ── State ──────────────────────────────────────────────────────
  readonly campId = signal<string>('');
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  // ── Options ────────────────────────────────────────────────────
  readonly bloodGroupOptions = BLOOD_GROUP_OPTIONS;

  // ── Form ───────────────────────────────────────────────────────
  readonly form = this.fb.group({
    donorId: this.fb.nonNullable.control(''),
    firstName: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.minLength(2),
      Validators.maxLength(100),
    ]),
    lastName: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.minLength(2),
      Validators.maxLength(100),
    ]),
    bloodGroup: this.fb.nonNullable.control('', Validators.required),
    phone: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/^\+?[\d\s-]{7,15}$/),
    ]),
    email: this.fb.nonNullable.control('', Validators.email),
  });

  // ── Form control accessors ─────────────────────────────────────
  get donorIdCtrl(): FormControl<string> {
    return this.form.controls.donorId;
  }
  get firstNameCtrl(): FormControl<string> {
    return this.form.controls.firstName;
  }
  get lastNameCtrl(): FormControl<string> {
    return this.form.controls.lastName;
  }
  get phoneCtrl(): FormControl<string> {
    return this.form.controls.phone;
  }
  get emailCtrl(): FormControl<string> {
    return this.form.controls.email;
  }

  readonly errorMessages: Record<string, Record<string, string>> = {
    firstName: {
      required: 'First name is required',
      minlength: 'Minimum 2 characters',
      maxlength: 'Maximum 100 characters',
    },
    lastName: {
      required: 'Last name is required',
      minlength: 'Minimum 2 characters',
      maxlength: 'Maximum 100 characters',
    },
    phone: {
      required: 'Phone number is required',
      pattern: 'Please enter a valid phone number',
    },
    email: {
      email: 'Please enter a valid email',
    },
  };

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.campId.set(id);
    }
  }

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
      const request: CampDonorRegistrationRequest = {
        donorId: v.donorId,
        firstName: v.firstName,
        lastName: v.lastName,
        bloodGroup: v.bloodGroup,
        phone: v.phone,
        email: v.email,
      };

      await this.campService.registerDonor(this.campId(), request);
      this.notification.success('Donor registered successfully.');
      void this.router.navigate(['/staff/camps', this.campId()]);
    } catch {
      this.error.set('Failed to register donor. Please try again.');
      this.notification.error('Failed to register donor.');
    } finally {
      this.saving.set(false);
    }
  }

  // ── Navigation ─────────────────────────────────────────────────

  cancel(): void {
    void this.router.navigate(['/staff/camps', this.campId()]);
  }
}
