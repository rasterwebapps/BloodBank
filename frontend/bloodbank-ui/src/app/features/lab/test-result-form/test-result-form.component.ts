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
  FormArray,
  Validators,
} from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatRadioModule } from '@angular/material/radio';
import { DatePipe } from '@angular/common';

import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { NotificationService } from '@core/services/notification.service';
import { LabService } from '../services/lab.service';
import {
  TestOrder,
  TestResult,
  TestTypeEnum,
  TestResultValueEnum,
  ReviewStatusEnum,
  TestResultCreateRequest,
  TestResultReviewRequest,
  TEST_TYPE_OPTIONS,
  RESULT_VALUE_OPTIONS,
} from '../models/lab.model';

/**
 * Enter test results for a test order.
 * Supports dual-review workflow: first tech enters, second tech confirms/rejects.
 */
@Component({
  selector: 'app-test-result-form',
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
    MatDividerModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatRadioModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './test-result-form.component.html',
  styleUrl: './test-result-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TestResultFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly labService = inject(LabService);
  private readonly notification = inject(NotificationService);

  // ── State ──────────────────────────────────────────────────────
  readonly testOrder = signal<TestOrder | null>(null);
  readonly existingResults = signal<TestResult[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly isReviewMode = signal(false);

  // ── Computed ───────────────────────────────────────────────────
  readonly orderSummary = computed(() => {
    const order = this.testOrder();
    if (!order) return '';
    return `${order.sampleNumber} — ${order.donorName}`;
  });

  readonly hasPendingReviews = computed(() =>
    this.existingResults().some(
      (r) => r.reviewStatus === ReviewStatusEnum.PENDING_REVIEW,
    ),
  );

  readonly pageTitle = computed(() =>
    this.isReviewMode() ? 'Review Test Results' : 'Enter Test Results',
  );

  // ── Options ────────────────────────────────────────────────────
  readonly testTypeOptions = TEST_TYPE_OPTIONS;
  readonly resultValueOptions = RESULT_VALUE_OPTIONS;

  // ── Form for entering results ─────────────────────────────────
  readonly resultForm = this.fb.group({
    testType: this.fb.nonNullable.control('', Validators.required),
    result: this.fb.nonNullable.control('', Validators.required),
    value: this.fb.nonNullable.control(''),
    unit: this.fb.nonNullable.control(''),
    referenceRange: this.fb.nonNullable.control(''),
    method: this.fb.nonNullable.control(''),
    instrumentId: this.fb.nonNullable.control(''),
  });

  // ── Form for reviewing results ────────────────────────────────
  readonly reviewForm = this.fb.group({
    reviewStatus: this.fb.nonNullable.control('', Validators.required),
    reviewNotes: this.fb.nonNullable.control(''),
  });

  // ── Form control accessors ────────────────────────────────────
  get testTypeCtrl(): FormControl<string> {
    return this.resultForm.controls.testType;
  }
  get resultCtrl(): FormControl<string> {
    return this.resultForm.controls.result;
  }
  get valueCtrl(): FormControl<string> {
    return this.resultForm.controls.value;
  }
  get unitCtrl(): FormControl<string> {
    return this.resultForm.controls.unit;
  }
  get referenceRangeCtrl(): FormControl<string> {
    return this.resultForm.controls.referenceRange;
  }
  get methodCtrl(): FormControl<string> {
    return this.resultForm.controls.method;
  }
  get instrumentIdCtrl(): FormControl<string> {
    return this.resultForm.controls.instrumentId;
  }
  get reviewStatusCtrl(): FormControl<string> {
    return this.reviewForm.controls.reviewStatus;
  }
  get reviewNotesCtrl(): FormControl<string> {
    return this.reviewForm.controls.reviewNotes;
  }

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    const orderId = this.route.snapshot.paramMap.get('id');
    if (orderId) {
      void this.loadTestOrder(orderId);
    }
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadTestOrder(id: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const [order, results] = await Promise.all([
        this.labService.getTestOrder(id),
        this.labService.getTestResults(id),
      ]);
      this.testOrder.set(order);
      this.existingResults.set(results);

      // If there are results pending review, switch to review mode
      if (results.some((r) => r.reviewStatus === ReviewStatusEnum.PENDING_REVIEW)) {
        this.isReviewMode.set(true);
      }
    } catch {
      this.error.set('Failed to load test order details.');
    } finally {
      this.loading.set(false);
    }
  }

  // ── Submit result ──────────────────────────────────────────────

  async onSubmitResult(): Promise<void> {
    if (this.resultForm.invalid) {
      this.resultForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    try {
      const formValue = this.resultForm.getRawValue();
      const order = this.testOrder();
      if (!order) return;

      const request: TestResultCreateRequest = {
        testOrderId: order.id,
        testType: formValue.testType as TestTypeEnum,
        result: formValue.result as TestResultValueEnum,
        value: formValue.value,
        unit: formValue.unit,
        referenceRange: formValue.referenceRange,
        method: formValue.method,
        instrumentId: formValue.instrumentId,
      };

      await this.labService.createTestResult(request);
      this.notification.success('Test result submitted for review.');
      this.resultForm.reset();

      // Reload results to show new entry
      const results = await this.labService.getTestResults(order.id);
      this.existingResults.set(results);
    } catch {
      this.error.set('Failed to submit test result.');
      this.notification.error('Failed to submit test result.');
    } finally {
      this.saving.set(false);
    }
  }

  // ── Review result (dual-review workflow) ───────────────────────

  async onReviewResult(resultId: string): Promise<void> {
    if (this.reviewForm.invalid) {
      this.reviewForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    try {
      const formValue = this.reviewForm.getRawValue();
      const request: TestResultReviewRequest = {
        reviewStatus: formValue.reviewStatus as ReviewStatusEnum,
        reviewNotes: formValue.reviewNotes,
      };

      await this.labService.reviewTestResult(resultId, request);
      this.notification.success('Test result review submitted.');
      this.reviewForm.reset();

      // Reload results
      const order = this.testOrder();
      if (order) {
        const results = await this.labService.getTestResults(order.id);
        this.existingResults.set(results);
      }
    } catch {
      this.error.set('Failed to submit review.');
      this.notification.error('Failed to submit review.');
    } finally {
      this.saving.set(false);
    }
  }

  // ── Navigation ─────────────────────────────────────────────────

  cancel(): void {
    void this.router.navigate(['/staff/lab']);
  }

  toggleMode(): void {
    this.isReviewMode.update((v) => !v);
  }

  getResultLabel(value: string): string {
    const option = this.resultValueOptions.find((o) => o.value === value);
    return option ? option.label : value;
  }

  getTestTypeLabel(value: string): string {
    const option = this.testTypeOptions.find((o) => o.value === value);
    return option ? option.label : value;
  }

  isReactive(result: TestResultValueEnum): boolean {
    return (
      result === TestResultValueEnum.REACTIVE ||
      result === TestResultValueEnum.POSITIVE
    );
  }
}
