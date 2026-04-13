import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormControl,
} from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';

import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { NotificationService } from '@core/services/notification.service';
import { EmergencyService } from '../services/emergency.service';
import {
  DisasterEvent,
  DisasterEventCreate,
  DisasterStatusEnum,
  SEVERITY_OPTIONS,
  DISASTER_STATUS_OPTIONS,
} from '../models/emergency.model';

/**
 * Disaster response management component.
 * Restricted to SUPER_ADMIN and REGIONAL_ADMIN roles.
 * Allows creating disaster events and viewing the active event list.
 */
@Component({
  selector: 'app-disaster-response',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    DatePipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTableModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatChipsModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
    StatusBadgeComponent,
  ],
  templateUrl: './disaster-response.component.html',
  styleUrl: './disaster-response.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DisasterResponseComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly emergencyService = inject(EmergencyService);
  private readonly notification = inject(NotificationService);

  // ── Select options ─────────────────────────────────────────────
  readonly severityOptions = SEVERITY_OPTIONS;
  readonly disasterStatusOptions = DISASTER_STATUS_OPTIONS;

  // ── List state ─────────────────────────────────────────────────
  readonly disasters = signal<DisasterEvent[]>([]);
  readonly loadingList = signal(false);
  readonly listError = signal<string | null>(null);

  // ── Form state ─────────────────────────────────────────────────
  readonly saving = signal(false);
  readonly formError = signal<string | null>(null);

  // ── Detail panel ───────────────────────────────────────────────
  readonly selectedDisaster = signal<DisasterEvent | null>(null);

  // ── Computed ───────────────────────────────────────────────────
  readonly isListEmpty = computed(
    () =>
      this.disasters().length === 0 &&
      !this.loadingList() &&
      !this.listError(),
  );

  readonly activeDisasters = computed(() =>
    this.disasters().filter((d) => d.status === DisasterStatusEnum.ACTIVE),
  );

  readonly displayedColumns: string[] = [
    'disasterCode',
    'name',
    'location',
    'severity',
    'status',
    'totalBloodRequired',
    'totalBloodDispatched',
    'startedAt',
    'actions',
  ];

  // ── Reactive form ──────────────────────────────────────────────
  readonly form = this.fb.group({
    name: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.maxLength(200),
    ]),
    description: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.maxLength(2000),
    ]),
    location: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.maxLength(300),
    ]),
    severity: this.fb.nonNullable.control('', Validators.required),
    startedAt: this.fb.nonNullable.control('', Validators.required),
    expectedDuration: this.fb.nonNullable.control('', Validators.maxLength(100)),
    totalBloodRequired: this.fb.nonNullable.control<number>(0, [
      Validators.required,
      Validators.min(0),
    ]),
    notes: this.fb.nonNullable.control('', Validators.maxLength(2000)),
  });

  // ── Typed control accessors ────────────────────────────────────
  get nameCtrl():        FormControl<string> { return this.form.controls.name; }
  get descriptionCtrl(): FormControl<string> { return this.form.controls.description; }
  get locationCtrl():    FormControl<string> { return this.form.controls.location; }
  get durationCtrl():    FormControl<string> { return this.form.controls.expectedDuration; }
  get notesCtrl():       FormControl<string> { return this.form.controls.notes; }
  get bloodReqCtrl():    FormControl<number> { return this.form.controls.totalBloodRequired; }

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    void this.loadDisasters();
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadDisasters(): Promise<void> {
    this.loadingList.set(true);
    this.listError.set(null);
    try {
      const result = await this.emergencyService.listDisasters(0, 20);
      this.disasters.set(result.content);
    } catch {
      this.listError.set('Failed to load disaster events. Please try again.');
    } finally {
      this.loadingList.set(false);
    }
  }

  // ── Form submission ────────────────────────────────────────────

  async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.formError.set(null);

    try {
      const raw = this.form.getRawValue();
      const payload: DisasterEventCreate = {
        name:               raw.name,
        description:        raw.description,
        location:           raw.location,
        severity:           raw.severity,
        startedAt:          raw.startedAt,
        expectedDuration:   raw.expectedDuration,
        totalBloodRequired: raw.totalBloodRequired,
        notes:              raw.notes,
      };
      const created = await this.emergencyService.createDisaster(payload);
      this.notification.success(`Disaster event "${created.name}" created.`);
      this.form.reset({ totalBloodRequired: 0 });
      void this.loadDisasters();
    } catch {
      this.formError.set('Failed to create disaster event. Please try again.');
      this.notification.error('Failed to create disaster event.');
    } finally {
      this.saving.set(false);
    }
  }

  // ── Detail panel ───────────────────────────────────────────────

  selectDisaster(disaster: DisasterEvent): void {
    this.selectedDisaster.set(disaster);
  }

  closeDetail(): void {
    this.selectedDisaster.set(null);
  }
}
