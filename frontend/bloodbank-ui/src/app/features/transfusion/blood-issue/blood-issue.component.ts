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
  BloodIssue,
  CrossMatchStatusEnum,
  BloodIssueCreateRequest,
} from '../models/transfusion.model';

/**
 * Component for issuing blood units against compatible cross-match requests.
 */
@Component({
  selector: 'app-blood-issue',
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
  templateUrl: './blood-issue.component.html',
  styleUrl: './blood-issue.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BloodIssueComponent implements OnInit {
  private readonly transfusionService = inject(TransfusionService);
  private readonly notification = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  readonly compatibleRequests = signal<CrossMatchRequest[]>([]);
  readonly recentIssues = signal<BloodIssue[]>([]);
  readonly selectedRequest = signal<CrossMatchRequest | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly displayedColumns: string[] = [
    'issueNumber',
    'patientName',
    'unitNumber',
    'componentType',
    'issuedBy',
    'issuedAt',
    'status',
  ];

  readonly form = this.fb.group({
    crossMatchRequestId: this.fb.nonNullable.control('', Validators.required),
    bloodUnitId: this.fb.nonNullable.control('', Validators.required),
    notes: this.fb.nonNullable.control(''),
  });

  ngOnInit(): void {
    void this.loadData();
  }

  async loadData(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const [requestsResult, issuesResult] = await Promise.all([
        this.transfusionService.listCrossMatchRequests(
          0,
          50,
          CrossMatchStatusEnum.COMPATIBLE,
        ),
        this.transfusionService.listBloodIssues(),
      ]);
      this.compatibleRequests.set(requestsResult.content);
      this.recentIssues.set(issuesResult.content);
    } catch {
      this.error.set('Failed to load data.');
    } finally {
      this.loading.set(false);
    }
  }

  onRequestChange(requestId: string): void {
    const req = this.compatibleRequests().find((r) => r.id === requestId) ?? null;
    this.selectedRequest.set(req);
  }

  async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    try {
      const payload: BloodIssueCreateRequest = this.form.getRawValue();
      await this.transfusionService.issueBlood(payload);
      this.notification.success('Blood issued successfully.');
      this.form.reset();
      this.selectedRequest.set(null);
      void this.loadData();
    } catch {
      this.notification.error('Failed to issue blood.');
    } finally {
      this.saving.set(false);
    }
  }
}
