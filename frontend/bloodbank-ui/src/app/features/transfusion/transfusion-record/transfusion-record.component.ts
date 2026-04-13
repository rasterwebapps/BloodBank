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
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { TransfusionService } from '../services/transfusion.service';
import { NotificationService } from '@core/services/notification.service';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import {
  Transfusion,
  TransfusionStatusEnum,
  TransfusionOutcomeEnum,
  TransfusionCreateRequest,
  TransfusionCompleteRequest,
  TRANSFUSION_OUTCOME_OPTIONS,
} from '../models/transfusion.model';

/**
 * Component for starting and completing transfusion records.
 */
@Component({
  selector: 'app-transfusion-record',
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
    MatTabsModule,
    MatProgressSpinnerModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './transfusion-record.component.html',
  styleUrl: './transfusion-record.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransfusionRecordComponent implements OnInit {
  private readonly transfusionService = inject(TransfusionService);
  private readonly notification = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  readonly inProgressTransfusions = signal<Transfusion[]>([]);
  readonly recentTransfusions = signal<Transfusion[]>([]);
  readonly selectedTransfusion = signal<Transfusion | null>(null);
  readonly loading = signal(false);
  readonly savingStart = signal(false);
  readonly savingComplete = signal(false);
  readonly error = signal<string | null>(null);

  readonly outcomeOptions = TRANSFUSION_OUTCOME_OPTIONS;
  readonly displayedColumns: string[] = [
    'transfusionNumber',
    'patientName',
    'unitNumber',
    'componentType',
    'volumeMl',
    'status',
    'startTime',
    'endTime',
  ];

  readonly startForm = this.fb.group({
    bloodUnitId: this.fb.nonNullable.control('', Validators.required),
    patientName: this.fb.nonNullable.control('', Validators.required),
    patientId: this.fb.nonNullable.control('', Validators.required),
    volumeMl: this.fb.nonNullable.control(250, [
      Validators.required,
      Validators.min(1),
    ]),
    startTime: this.fb.nonNullable.control('', Validators.required),
    preTransfusionVitals: this.fb.nonNullable.control(''),
    notes: this.fb.nonNullable.control(''),
  });

  readonly completeForm = this.fb.group({
    transfusionId: this.fb.nonNullable.control('', Validators.required),
    endTime: this.fb.nonNullable.control('', Validators.required),
    volumeMl: this.fb.nonNullable.control(250, [
      Validators.required,
      Validators.min(1),
    ]),
    outcome: this.fb.nonNullable.control<TransfusionOutcomeEnum>(
      TransfusionOutcomeEnum.SUCCESSFUL,
      Validators.required,
    ),
    postTransfusionVitals: this.fb.nonNullable.control(''),
    notes: this.fb.nonNullable.control(''),
  });

  ngOnInit(): void {
    void this.loadData();
  }

  async loadData(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const [inProgress, recent] = await Promise.all([
        this.transfusionService.listTransfusions(
          0,
          50,
          TransfusionStatusEnum.IN_PROGRESS,
        ),
        this.transfusionService.listTransfusions(0, 10),
      ]);
      this.inProgressTransfusions.set(inProgress.content);
      this.recentTransfusions.set(recent.content);
    } catch {
      this.error.set('Failed to load transfusion data.');
    } finally {
      this.loading.set(false);
    }
  }

  onTransfusionSelect(transfusionId: string): void {
    const found =
      this.inProgressTransfusions().find((t) => t.id === transfusionId) ?? null;
    this.selectedTransfusion.set(found);
    if (found) {
      this.completeForm.controls.volumeMl.setValue(found.volumeMl);
    }
  }

  async onStartSubmit(): Promise<void> {
    if (this.startForm.invalid) {
      this.startForm.markAllAsTouched();
      return;
    }
    this.savingStart.set(true);
    try {
      const raw = this.startForm.getRawValue();
      const payload: TransfusionCreateRequest = {
        bloodUnitId: raw.bloodUnitId,
        patientName: raw.patientName,
        patientId: raw.patientId,
        volumeMl: raw.volumeMl,
        startTime: raw.startTime,
        preTransfusionVitals: raw.preTransfusionVitals,
        notes: raw.notes,
      };
      await this.transfusionService.startTransfusion(payload);
      this.notification.success('Transfusion started successfully.');
      this.startForm.reset({ volumeMl: 250 });
      void this.loadData();
    } catch {
      this.notification.error('Failed to start transfusion.');
    } finally {
      this.savingStart.set(false);
    }
  }

  async onCompleteSubmit(): Promise<void> {
    if (this.completeForm.invalid) {
      this.completeForm.markAllAsTouched();
      return;
    }
    this.savingComplete.set(true);
    try {
      const raw = this.completeForm.getRawValue();
      const payload: TransfusionCompleteRequest = {
        endTime: raw.endTime,
        volumeMl: raw.volumeMl,
        outcome: raw.outcome,
        postTransfusionVitals: raw.postTransfusionVitals,
        notes: raw.notes,
      };
      await this.transfusionService.completeTransfusion(
        raw.transfusionId,
        payload,
      );
      this.notification.success('Transfusion completed successfully.');
      this.resetCompleteForm();
      this.selectedTransfusion.set(null);
      void this.loadData();
    } catch {
      this.notification.error('Failed to complete transfusion.');
    } finally {
      this.savingComplete.set(false);
    }
  }

  resetCompleteForm(): void {
    this.completeForm.reset({
      volumeMl: 250,
      outcome: TransfusionOutcomeEnum.SUCCESSFUL,
    });
  }
}
