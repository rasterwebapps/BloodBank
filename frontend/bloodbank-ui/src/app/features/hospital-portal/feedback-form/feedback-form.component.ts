import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormControl, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { FormFieldComponent } from '@shared/components/form-field/form-field.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { NotificationService } from '@core/services/notification.service';
import { HospitalPortalService } from '../services/hospital-portal.service';
import { HospitalFeedback } from '../models/hospital-portal.model';

@Component({
  selector: 'app-feedback-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    DatePipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatTableModule,
    FormFieldComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './feedback-form.component.html',
  styleUrl: './feedback-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedbackFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly hospitalPortalService = inject(HospitalPortalService);
  private readonly notification = inject(NotificationService);

  readonly saving = signal(false);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly feedbacks = signal<HospitalFeedback[]>([]);
  readonly totalElements = signal(0);

  readonly ratingOptions = [1, 2, 3, 4, 5].map((n) => ({
    label: `${n} Star${n > 1 ? 's' : ''}`,
    value: n,
  }));
  readonly feedbackColumns = ['requestId', 'rating', 'comments', 'submittedAt'];

  readonly form = this.fb.nonNullable.group({
    requestId: this.fb.nonNullable.control<string>('', [Validators.required]),
    rating: this.fb.nonNullable.control<number>(0, [
      Validators.required,
      Validators.min(1),
      Validators.max(5),
    ]),
    comments: this.fb.nonNullable.control<string>('', [Validators.maxLength(500)]),
  });

  get requestIdCtrl(): FormControl<string> {
    return this.form.controls.requestId;
  }

  get commentsCtrl(): FormControl<string> {
    return this.form.controls.comments;
  }

  ngOnInit(): void {
    this.loadFeedbacks();
  }

  async loadFeedbacks(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.hospitalPortalService.getMyFeedback(0, 20);
      this.feedbacks.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load feedback history.');
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
      const value = this.form.getRawValue();
      await this.hospitalPortalService.submitFeedback({
        requestId: value.requestId,
        rating: value.rating,
        comments: value.comments,
      });
      this.notification.success('Feedback submitted successfully.');
      this.form.reset();
      await this.loadFeedbacks();
    } catch {
      this.notification.error('Failed to submit feedback. Please try again.');
    } finally {
      this.saving.set(false);
    }
  }
}
