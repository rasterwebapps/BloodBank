import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { TransfusionService } from '../services/transfusion.service';
import { NotificationService } from '@core/services/notification.service';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import {
  Transfusion,
  ReactionTypeEnum,
  ReactionSeverityEnum,
  ReactionReportRequest,
  REACTION_TYPE_OPTIONS,
  REACTION_SEVERITY_OPTIONS,
} from '../models/transfusion.model';

/**
 * Component for reporting adverse transfusion reactions.
 */
@Component({
  selector: 'app-reaction-report',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './reaction-report.component.html',
  styleUrl: './reaction-report.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReactionReportComponent implements OnInit {
  private readonly transfusionService = inject(TransfusionService);
  private readonly notification = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  readonly transfusions = signal<Transfusion[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly reactionTypeOptions = REACTION_TYPE_OPTIONS;
  readonly reactionSeverityOptions = REACTION_SEVERITY_OPTIONS;

  readonly form = this.fb.group({
    transfusionId: this.fb.nonNullable.control('', Validators.required),
    reactionType: this.fb.nonNullable.control<ReactionTypeEnum>(
      ReactionTypeEnum.FEBRILE,
      Validators.required,
    ),
    severity: this.fb.nonNullable.control<ReactionSeverityEnum>(
      ReactionSeverityEnum.MILD,
      Validators.required,
    ),
    onsetTime: this.fb.nonNullable.control('', Validators.required),
    actionsTaken: this.fb.nonNullable.control('', Validators.required),
    notes: this.fb.nonNullable.control(''),
  });

  get isCriticalReaction(): boolean {
    const sev = this.form.controls.severity.value;
    return (
      sev === ReactionSeverityEnum.SEVERE ||
      sev === ReactionSeverityEnum.LIFE_THREATENING
    );
  }

  ngOnInit(): void {
    void this.loadTransfusions();
  }

  async loadTransfusions(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.transfusionService.listTransfusions(0, 50);
      this.transfusions.set(result.content);
    } catch {
      this.error.set('Failed to load transfusions.');
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
      const payload: ReactionReportRequest = this.form.getRawValue();
      await this.transfusionService.reportReaction(payload);
      this.notification.success('Reaction reported successfully.');
      this.resetForm();
    } catch {
      this.notification.error('Failed to report reaction.');
    } finally {
      this.saving.set(false);
    }
  }

  resetForm(): void {
    this.form.reset({
      reactionType: ReactionTypeEnum.FEBRILE,
      severity: ReactionSeverityEnum.MILD,
    });
  }
}
