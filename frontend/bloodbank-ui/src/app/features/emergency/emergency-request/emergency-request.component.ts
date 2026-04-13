import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { Router } from '@angular/router';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormControl,
} from '@angular/forms';
import { DatePipe } from '@angular/common';
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

import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { NotificationService } from '@core/services/notification.service';
import { EmergencyService } from '../services/emergency.service';
import {
  EmergencyRequest,
  EmergencyRequestCreate,
  PriorityLevelEnum,
  PRIORITY_LEVEL_OPTIONS,
  BLOOD_GROUP_OPTIONS,
  RH_FACTOR_OPTIONS,
  COMPONENT_TYPE_OPTIONS,
  getPriorityClass,
  getPriorityLabel,
} from '../models/emergency.model';

/**
 * Form component for creating a new emergency blood request.
 * Displays a create form and the most recent emergency requests below.
 */
@Component({
  selector: 'app-emergency-request',
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
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
    StatusBadgeComponent,
  ],
  templateUrl: './emergency-request.component.html',
  styleUrl: './emergency-request.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmergencyRequestComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly emergencyService = inject(EmergencyService);
  private readonly notification = inject(NotificationService);

  // ── Select options ─────────────────────────────────────────────
  readonly priorityOptions = PRIORITY_LEVEL_OPTIONS;
  readonly bloodGroupOptions = BLOOD_GROUP_OPTIONS;
  readonly rhFactorOptions = RH_FACTOR_OPTIONS;
  readonly componentTypeOptions = COMPONENT_TYPE_OPTIONS;

  // ── Form state ─────────────────────────────────────────────────
  readonly saving = signal(false);
  readonly formError = signal<string | null>(null);

  // ── Recent requests state ──────────────────────────────────────
  readonly recentRequests = signal<EmergencyRequest[]>([]);
  readonly loadingRecent = signal(false);
  readonly recentError = signal<string | null>(null);

  // ── Computed ───────────────────────────────────────────────────
  readonly isRecentEmpty = computed(
    () =>
      this.recentRequests().length === 0 &&
      !this.loadingRecent() &&
      !this.recentError(),
  );

  /** Submit button color is warn for P1/P2, primary otherwise. */
  readonly submitButtonColor = computed<'warn' | 'primary'>(() => {
    const level = this.form.controls.priorityLevel.value as PriorityLevelEnum | '';
    return level === PriorityLevelEnum.P1_CRITICAL ||
      level === PriorityLevelEnum.P2_URGENT
      ? 'warn'
      : 'primary';
  });

  readonly displayedColumns: string[] = [
    'requestNumber',
    'patientName',
    'bloodGroup',
    'priorityLevel',
    'status',
    'requestedAt',
  ];

  // ── Reactive form ──────────────────────────────────────────────
  readonly form = this.fb.group({
    patientName:   this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(200)]),
    patientId:     this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(100)]),
    bloodGroup:    this.fb.nonNullable.control('', Validators.required),
    rhFactor:      this.fb.nonNullable.control('', Validators.required),
    componentType: this.fb.nonNullable.control('', Validators.required),
    unitsRequired: this.fb.nonNullable.control<number>(1, [
      Validators.required,
      Validators.min(1),
      Validators.max(20),
    ]),
    priorityLevel: this.fb.nonNullable.control<PriorityLevelEnum>(
      PriorityLevelEnum.P3_HIGH,
      Validators.required,
    ),
    hospitalName:  this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(200)]),
    notes:         this.fb.nonNullable.control('', Validators.maxLength(1000)),
  });

  // ── Typed control accessors ────────────────────────────────────
  get patientNameCtrl(): FormControl<string> { return this.form.controls.patientName; }
  get patientIdCtrl():   FormControl<string> { return this.form.controls.patientId; }
  get hospitalNameCtrl():FormControl<string> { return this.form.controls.hospitalName; }
  get notesCtrl():       FormControl<string> { return this.form.controls.notes; }
  get unitsCtrl():       FormControl<number> { return this.form.controls.unitsRequired; }

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    void this.loadRecentRequests();
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadRecentRequests(): Promise<void> {
    this.loadingRecent.set(true);
    this.recentError.set(null);
    try {
      const result = await this.emergencyService.listEmergencies(0, 10);
      this.recentRequests.set(result.content);
    } catch {
      this.recentError.set('Failed to load recent emergency requests.');
    } finally {
      this.loadingRecent.set(false);
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
      const payload: EmergencyRequestCreate = {
        patientName:   raw.patientName,
        patientId:     raw.patientId,
        bloodGroup:    raw.bloodGroup,
        rhFactor:      raw.rhFactor,
        componentType: raw.componentType,
        unitsRequired: raw.unitsRequired,
        priorityLevel: raw.priorityLevel,
        hospitalName:  raw.hospitalName,
        notes:         raw.notes,
      };
      await this.emergencyService.createEmergency(payload);
      this.notification.success('Emergency request created successfully.');
      this.form.reset({
        unitsRequired: 1,
        priorityLevel: PriorityLevelEnum.P3_HIGH,
      });
      void this.loadRecentRequests();
    } catch {
      this.formError.set('Failed to create emergency request. Please try again.');
      this.notification.error('Failed to create emergency request.');
    } finally {
      this.saving.set(false);
    }
  }

  // ── Navigation ─────────────────────────────────────────────────

  goToDashboard(): void {
    void this.router.navigate(['/staff/emergency']);
  }

  // ── Helpers (exposed to template) ─────────────────────────────

  getPriorityClass(priority: PriorityLevelEnum): string {
    return getPriorityClass(priority);
  }

  getPriorityLabel(priority: PriorityLevelEnum): string {
    return getPriorityLabel(priority);
  }
}
