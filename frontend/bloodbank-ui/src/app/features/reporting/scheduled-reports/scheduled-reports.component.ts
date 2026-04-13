import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { ReportingService } from '../services/reporting.service';
import {
  ScheduledReport,
  ReportTypeEnum,
  ReportFormatEnum,
  ScheduleFrequencyEnum,
} from '../models/reporting.model';

@Component({
  selector: 'app-scheduled-reports',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatChipsModule,
    MatSlideToggleModule,
    DataTableComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './scheduled-reports.component.html',
  styleUrl: './scheduled-reports.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ScheduledReportsComponent implements OnInit {
  private readonly reportingService = inject(ReportingService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly scheduledReports = signal<ScheduledReport[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);
  readonly showForm = signal(false);
  readonly submitting = signal(false);

  readonly isEmpty = computed(
    () => this.scheduledReports().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns = ['name', 'reportType', 'frequency', 'format', 'nextRunAt', 'isActive', 'actions'];
  readonly reportTypes = Object.values(ReportTypeEnum);
  readonly reportFormats = Object.values(ReportFormatEnum);
  readonly frequencies = Object.values(ScheduleFrequencyEnum);

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    reportType: [ReportTypeEnum.COLLECTION_SUMMARY as string, Validators.required],
    format: [ReportFormatEnum.PDF as string, Validators.required],
    frequency: [ScheduleFrequencyEnum.WEEKLY as string, Validators.required],
    recipientEmails: ['', Validators.required],
  });

  ngOnInit(): void {
    void this.loadScheduledReports();
  }

  async loadScheduledReports(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.reportingService.listScheduledReports(this.currentPage(), this.pageSize());
      this.scheduledReports.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load scheduled reports.');
    } finally {
      this.loading.set(false);
    }
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadScheduledReports();
  }

  toggleForm(): void {
    this.showForm.update((v) => !v);
    if (!this.showForm()) this.form.reset();
  }

  async saveSchedule(): Promise<void> {
    if (this.form.invalid) return;
    this.submitting.set(true);
    try {
      const value = this.form.getRawValue();
      await this.reportingService.createScheduledReport({
        name: value.name,
        reportType: value.reportType as ReportTypeEnum,
        format: value.format as ReportFormatEnum,
        frequency: value.frequency as ScheduleFrequencyEnum,
        parameters: {},
        recipientEmails: value.recipientEmails.split(',').map((e) => e.trim()),
      });
      this.snackBar.open('Schedule created', 'Dismiss', { duration: 3000 });
      this.form.reset();
      this.showForm.set(false);
      void this.loadScheduledReports();
    } catch {
      this.snackBar.open('Failed to create schedule', 'Dismiss', { duration: 3000 });
    } finally {
      this.submitting.set(false);
    }
  }

  async toggleActive(report: ScheduledReport): Promise<void> {
    try {
      await this.reportingService.toggleScheduledReport(report.id, !report.isActive);
      void this.loadScheduledReports();
    } catch {
      this.snackBar.open('Failed to update schedule', 'Dismiss', { duration: 3000 });
    }
  }

  async deleteSchedule(report: ScheduledReport): Promise<void> {
    try {
      await this.reportingService.deleteScheduledReport(report.id);
      this.snackBar.open('Schedule deleted', 'Dismiss', { duration: 3000 });
      void this.loadScheduledReports();
    } catch {
      this.snackBar.open('Failed to delete schedule', 'Dismiss', { duration: 3000 });
    }
  }
}
