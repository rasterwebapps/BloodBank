import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { TransfusionService } from '../services/transfusion.service';
import { NotificationService } from '@core/services/notification.service';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import {
  CrossMatchRequest,
  CrossMatchStatusEnum,
  CrossMatchRequestCreate,
  CROSS_MATCH_STATUS_OPTIONS,
} from '../models/transfusion.model';

/**
 * Component for submitting and viewing cross-match requests.
 */
@Component({
  selector: 'app-cross-match-request',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatTableModule,
    MatIconModule,
    MatProgressSpinnerModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './cross-match-request.component.html',
  styleUrl: './cross-match-request.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CrossMatchRequestComponent implements OnInit {
  private readonly transfusionService = inject(TransfusionService);
  private readonly notification = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  readonly requests = signal<CrossMatchRequest[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly statusOptions = CROSS_MATCH_STATUS_OPTIONS;
  readonly displayedColumns: string[] = [
    'requestNumber',
    'patientName',
    'bloodGroup',
    'componentType',
    'unitsRequested',
    'urgency',
    'status',
    'requestedAt',
  ];

  readonly form = this.fb.group({
    patientName: this.fb.nonNullable.control('', Validators.required),
    patientId: this.fb.nonNullable.control('', Validators.required),
    bloodGroup: this.fb.nonNullable.control('', Validators.required),
    rhFactor: this.fb.nonNullable.control('', Validators.required),
    componentType: this.fb.nonNullable.control('', Validators.required),
    unitsRequested: this.fb.nonNullable.control(1, [
      Validators.required,
      Validators.min(1),
    ]),
    urgency: this.fb.nonNullable.control('ROUTINE', Validators.required),
  });

  ngOnInit(): void {
    void this.loadRequests();
  }

  async loadRequests(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.transfusionService.listCrossMatchRequests();
      this.requests.set(result.content);
    } catch {
      this.error.set('Failed to load cross-match requests.');
    } finally {
      this.loading.set(false);
    }
  }

  async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    try {
      const payload: CrossMatchRequestCreate = this.form.getRawValue();
      await this.transfusionService.createCrossMatchRequest(payload);
      this.notification.success('Cross-match request submitted successfully.');
      this.form.reset({ unitsRequested: 1, urgency: 'ROUTINE' });
      void this.loadRequests();
    } catch {
      this.notification.error('Failed to submit cross-match request.');
    } finally {
      this.saving.set(false);
    }
  }

  getStatusClass(status: CrossMatchStatusEnum): string {
    const map: Record<CrossMatchStatusEnum, string> = {
      [CrossMatchStatusEnum.PENDING]: 'status-pending',
      [CrossMatchStatusEnum.COMPATIBLE]: 'status-compatible',
      [CrossMatchStatusEnum.INCOMPATIBLE]: 'status-incompatible',
      [CrossMatchStatusEnum.DEFERRED]: 'status-deferred',
    };
    return map[status] ?? '';
  }
}
