import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { FormFieldComponent } from '@shared/components/form-field/form-field.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { NotificationService } from '@core/services/notification.service';
import { BranchContextService } from '@core/services/branch-context.service';
import { DonorService } from '../services/donor.service';
import {
  Donor,
  DonorCreateRequest,
  DonorUpdateRequest,
  GenderEnum,
  DonorTypeEnum,
  BLOOD_GROUP_OPTIONS,
  GENDER_OPTIONS,
  DONOR_TYPE_OPTIONS,
} from '../models/donor.model';

/**
 * Donor registration and edit form.
 * Mode is determined by route: '/new' → create, '/:id/edit' → edit.
 */
@Component({
  selector: 'app-donor-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    FormFieldComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './donor-form.component.html',
  styleUrl: './donor-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DonorFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly donorService = inject(DonorService);
  private readonly notification = inject(NotificationService);
  private readonly branchContext = inject(BranchContextService);

  // ── State ──────────────────────────────────────────────────────
  readonly isEditMode = signal(false);
  readonly donorId = signal<string | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  // ── Form select options ────────────────────────────────────────
  readonly bloodGroupOptions = BLOOD_GROUP_OPTIONS;
  readonly genderOptions = GENDER_OPTIONS;
  readonly donorTypeOptions = DONOR_TYPE_OPTIONS;

  // ── Computed ───────────────────────────────────────────────────
  readonly pageTitle = computed(() =>
    this.isEditMode() ? 'Edit Donor' : 'Register Donor',
  );
  readonly submitLabel = computed(() =>
    this.isEditMode() ? 'Update' : 'Register',
  );

  // ── Form ───────────────────────────────────────────────────────
  readonly form = this.fb.group({
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
    dateOfBirth: this.fb.nonNullable.control('', Validators.required),
    gender: this.fb.nonNullable.control('', Validators.required),
    bloodGroupId: this.fb.nonNullable.control('', Validators.required),
    rhFactor: this.fb.nonNullable.control(''),
    email: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.email,
    ]),
    phone: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/^\+?[\d\s-]{7,15}$/),
    ]),
    addressLine1: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.maxLength(255),
    ]),
    addressLine2: this.fb.nonNullable.control('', Validators.maxLength(255)),
    postalCode: this.fb.nonNullable.control('', Validators.maxLength(20)),
    nationalId: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.maxLength(50),
    ]),
    nationality: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.maxLength(100),
    ]),
    occupation: this.fb.nonNullable.control('', Validators.maxLength(100)),
    donorType: this.fb.nonNullable.control(
      DonorTypeEnum.VOLUNTARY,
      Validators.required,
    ),
    consent: this.fb.nonNullable.control(false),
  });

  // ── Form control accessors for template ────────────────────────
  get firstNameCtrl(): FormControl<string> {
    return this.form.controls.firstName;
  }
  get lastNameCtrl(): FormControl<string> {
    return this.form.controls.lastName;
  }
  get emailCtrl(): FormControl<string> {
    return this.form.controls.email;
  }
  get phoneCtrl(): FormControl<string> {
    return this.form.controls.phone;
  }
  get addressLine1Ctrl(): FormControl<string> {
    return this.form.controls.addressLine1;
  }
  get addressLine2Ctrl(): FormControl<string> {
    return this.form.controls.addressLine2;
  }
  get postalCodeCtrl(): FormControl<string> {
    return this.form.controls.postalCode;
  }
  get nationalIdCtrl(): FormControl<string> {
    return this.form.controls.nationalId;
  }
  get nationalityCtrl(): FormControl<string> {
    return this.form.controls.nationality;
  }
  get occupationCtrl(): FormControl<string> {
    return this.form.controls.occupation;
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
    email: {
      required: 'Email is required',
      email: 'Please enter a valid email',
    },
    phone: {
      required: 'Phone number is required',
      pattern: 'Please enter a valid phone number',
    },
    addressLine1: {
      required: 'Address is required',
      maxlength: 'Maximum 255 characters',
    },
    nationalId: {
      required: 'National ID is required',
      maxlength: 'Maximum 50 characters',
    },
    nationality: {
      required: 'Nationality is required',
      maxlength: 'Maximum 100 characters',
    },
  };

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.donorId.set(id);
      void this.loadDonor(id);
    }
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadDonor(id: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const donor = await this.donorService.getById(id);
      this.patchForm(donor);
    } catch {
      this.error.set('Failed to load donor data.');
    } finally {
      this.loading.set(false);
    }
  }

  private patchForm(donor: Donor): void {
    this.form.patchValue({
      firstName: donor.firstName,
      lastName: donor.lastName,
      dateOfBirth: donor.dateOfBirth,
      gender: donor.gender,
      bloodGroupId: donor.bloodGroupId,
      rhFactor: donor.rhFactor,
      email: donor.email,
      phone: donor.phone,
      addressLine1: donor.addressLine1,
      addressLine2: donor.addressLine2,
      postalCode: donor.postalCode,
      nationalId: donor.nationalId,
      nationality: donor.nationality,
      occupation: donor.occupation,
      donorType: donor.donorType,
    });
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
      if (this.isEditMode()) {
        await this.updateDonor();
      } else {
        await this.createDonor();
      }
    } catch {
      const action = this.isEditMode() ? 'update' : 'register';
      this.error.set(`Failed to ${action} donor. Please try again.`);
      this.notification.error(`Failed to ${action} donor.`);
    } finally {
      this.saving.set(false);
    }
  }

  private async createDonor(): Promise<void> {
    const formValue = this.form.getRawValue();
    const request: DonorCreateRequest = {
      firstName: formValue.firstName,
      lastName: formValue.lastName,
      dateOfBirth: this.formatDate(formValue.dateOfBirth),
      gender: formValue.gender as GenderEnum,
      bloodGroupId: formValue.bloodGroupId,
      rhFactor: formValue.rhFactor,
      email: formValue.email,
      phone: formValue.phone,
      addressLine1: formValue.addressLine1,
      addressLine2: formValue.addressLine2,
      cityId: '',
      postalCode: formValue.postalCode,
      nationalId: formValue.nationalId,
      nationality: formValue.nationality,
      occupation: formValue.occupation,
      donorType: formValue.donorType as DonorTypeEnum,
      branchId: this.branchContext.branchId() ?? '',
    };

    const donor = await this.donorService.create(request);
    this.notification.success('Donor registered successfully.');
    void this.router.navigate(['/staff/donors', donor.id]);
  }

  private async updateDonor(): Promise<void> {
    const formValue = this.form.getRawValue();
    const request: DonorUpdateRequest = {
      firstName: formValue.firstName,
      lastName: formValue.lastName,
      dateOfBirth: this.formatDate(formValue.dateOfBirth),
      gender: formValue.gender as GenderEnum,
      bloodGroupId: formValue.bloodGroupId,
      rhFactor: formValue.rhFactor,
      email: formValue.email,
      phone: formValue.phone,
      addressLine1: formValue.addressLine1,
      addressLine2: formValue.addressLine2,
      cityId: '',
      postalCode: formValue.postalCode,
      nationalId: formValue.nationalId,
      nationality: formValue.nationality,
      occupation: formValue.occupation,
    };

    const id = this.donorId()!;
    await this.donorService.update(id, request);
    this.notification.success('Donor updated successfully.');
    void this.router.navigate(['/staff/donors', id]);
  }

  // ── Navigation ─────────────────────────────────────────────────

  cancel(): void {
    if (this.isEditMode() && this.donorId()) {
      void this.router.navigate(['/staff/donors', this.donorId()]);
    } else {
      void this.router.navigate(['/staff/donors']);
    }
  }

  // ── Helpers ────────────────────────────────────────────────────

  private formatDate(value: string | Date): string {
    if (!value) return '';
    const date = value instanceof Date ? value : new Date(value);
    if (isNaN(date.getTime())) return '';
    return date.toISOString().split('T')[0];
  }
}
