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
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { FormFieldComponent } from '@shared/components/form-field/form-field.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { NotificationService } from '@core/services/notification.service';
import { CampService } from '../services/camp.service';
import { Camp, CampCreateRequest } from '../models/camp.model';

/**
 * Form for planning a new camp or editing an existing one.
 * Mode is determined by route: '/new' → create, '/:id/edit' → edit.
 */
@Component({
  selector: 'app-camp-form',
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
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './camp-form.component.html',
  styleUrl: './camp-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CampFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly campService = inject(CampService);
  private readonly notification = inject(NotificationService);

  // ── State ──────────────────────────────────────────────────────
  readonly isEditMode = signal(false);
  readonly campId = signal<string | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  // ── Computed ───────────────────────────────────────────────────
  readonly pageTitle = computed(() =>
    this.isEditMode() ? 'Edit Camp' : 'Plan New Camp',
  );
  readonly submitLabel = computed(() =>
    this.isEditMode() ? 'Update' : 'Create Camp',
  );

  // ── Form ───────────────────────────────────────────────────────
  readonly form = this.fb.group({
    name: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.maxLength(200),
    ]),
    location: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.maxLength(200),
    ]),
    address: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.maxLength(500),
    ]),
    campDate: this.fb.nonNullable.control('', Validators.required),
    startTime: this.fb.nonNullable.control('', Validators.required),
    endTime: this.fb.nonNullable.control('', Validators.required),
    targetDonors: this.fb.nonNullable.control(50, [
      Validators.required,
      Validators.min(1),
      Validators.max(10000),
    ]),
    organizerName: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.maxLength(200),
    ]),
    contactPhone: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/^\+?[\d\s-]{7,15}$/),
    ]),
    notes: this.fb.nonNullable.control(''),
  });

  // ── Form control accessors ─────────────────────────────────────
  get nameCtrl(): FormControl<string> {
    return this.form.controls.name;
  }
  get locationCtrl(): FormControl<string> {
    return this.form.controls.location;
  }
  get addressCtrl(): FormControl<string> {
    return this.form.controls.address;
  }
  get organizerNameCtrl(): FormControl<string> {
    return this.form.controls.organizerName;
  }
  get contactPhoneCtrl(): FormControl<string> {
    return this.form.controls.contactPhone;
  }
  get notesCtrl(): FormControl<string> {
    return this.form.controls.notes;
  }

  readonly errorMessages: Record<string, Record<string, string>> = {
    name: {
      required: 'Camp name is required',
      maxlength: 'Maximum 200 characters',
    },
    location: {
      required: 'Location is required',
      maxlength: 'Maximum 200 characters',
    },
    address: {
      required: 'Address is required',
      maxlength: 'Maximum 500 characters',
    },
    organizerName: {
      required: 'Organizer name is required',
      maxlength: 'Maximum 200 characters',
    },
    contactPhone: {
      required: 'Contact phone is required',
      pattern: 'Please enter a valid phone number',
    },
  };

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.campId.set(id);
      void this.loadCamp(id);
    }
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadCamp(id: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const camp = await this.campService.getById(id);
      this.patchForm(camp);
    } catch {
      this.error.set('Failed to load camp data.');
    } finally {
      this.loading.set(false);
    }
  }

  private patchForm(camp: Camp): void {
    this.form.patchValue({
      name: camp.name,
      location: camp.location,
      address: camp.address,
      campDate: camp.campDate,
      startTime: camp.startTime,
      endTime: camp.endTime,
      targetDonors: camp.targetDonors,
      organizerName: camp.organizerName,
      contactPhone: camp.contactPhone,
      notes: camp.notes ?? '',
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
      const v = this.form.getRawValue();
      const request: CampCreateRequest = {
        name: v.name,
        location: v.location,
        address: v.address,
        campDate: v.campDate,
        startTime: v.startTime,
        endTime: v.endTime,
        targetDonors: v.targetDonors,
        organizerName: v.organizerName,
        contactPhone: v.contactPhone,
        notes: v.notes,
      };

      if (this.isEditMode()) {
        await this.campService.update(this.campId()!, request);
        this.notification.success('Camp updated successfully.');
        void this.router.navigate(['/staff/camps', this.campId()]);
      } else {
        const camp = await this.campService.create(request);
        this.notification.success('Camp created successfully.');
        void this.router.navigate(['/staff/camps', camp.id]);
      }
    } catch {
      const action = this.isEditMode() ? 'update' : 'create';
      this.error.set(`Failed to ${action} camp. Please try again.`);
      this.notification.error(`Failed to ${action} camp.`);
    } finally {
      this.saving.set(false);
    }
  }

  // ── Navigation ─────────────────────────────────────────────────

  cancel(): void {
    if (this.isEditMode() && this.campId()) {
      void this.router.navigate(['/staff/camps', this.campId()]);
    } else {
      void this.router.navigate(['/staff/camps']);
    }
  }
}
