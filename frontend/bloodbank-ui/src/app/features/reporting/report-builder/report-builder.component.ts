import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { PageEvent } from '@angular/material/paginator';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { ReportingService } from '../services/reporting.service';
import { Report, ReportTypeEnum, ReportFormatEnum } from '../models/reporting.model';

/**
 * Report builder — generate custom reports with configurable filters.
 */
@Component({
  selector: 'app-report-builder',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTableModule,
    MatChipsModule,
    DataTableComponent,
    EmptyStateComponent,
  ],
  templateUrl: './report-builder.component.html',
  styleUrl: './report-builder.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReportBuilderComponent implements OnInit {
  private readonly reportingService = inject(ReportingService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly reports = signal<Report[]>([]);
  readonly totalElements = signal(0);
  readonly generating = signal(false);

  readonly displayedColumns = ['name', 'type', 'format', 'generatedAt', 'actions'];
  readonly reportTypes = Object.values(ReportTypeEnum);
  readonly reportFormats = Object.values(ReportFormatEnum);

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    type: [ReportTypeEnum.COLLECTION_SUMMARY as string, Validators.required],
    format: [ReportFormatEnum.PDF as string, Validators.required],
    fromDate: [''],
    toDate: [''],
  });

  ngOnInit(): void {
    void this.loadReports();
  }

  async loadReports(): Promise<void> {
    try {
      const result = await this.reportingService.listReports();
      this.reports.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.snackBar.open('Failed to load reports', 'Dismiss', { duration: 3000 });
    }
  }

  async generateReport(): Promise<void> {
    if (this.form.invalid) return;
    this.generating.set(true);
    try {
      const value = this.form.getRawValue();
      const params: Record<string, string> = {};
      if (value.fromDate) params['fromDate'] = value.fromDate;
      if (value.toDate) params['toDate'] = value.toDate;
      await this.reportingService.generateReport({
        name: value.name,
        type: value.type as ReportTypeEnum,
        format: value.format as ReportFormatEnum,
        parameters: params,
      });
      this.snackBar.open('Report generation started', 'Dismiss', { duration: 3000 });
      void this.loadReports();
    } catch {
      this.snackBar.open('Failed to generate report', 'Dismiss', { duration: 3000 });
    } finally {
      this.generating.set(false);
    }
  }

  async downloadReport(report: Report): Promise<void> {
    try {
      const blob = await this.reportingService.downloadReport(report.id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${report.name}.${report.format.toLowerCase()}`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      this.snackBar.open('Failed to download report', 'Dismiss', { duration: 3000 });
    }
  }
}
