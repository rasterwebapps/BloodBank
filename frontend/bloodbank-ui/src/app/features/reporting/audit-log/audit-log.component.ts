import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { PageEvent } from '@angular/material/paginator';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { ReportingService } from '../services/reporting.service';
import { AuditLog, AuditActionEnum } from '../models/reporting.model';

/**
 * Audit log — immutable read-only audit trail view.
 */
@Component({
  selector: 'app-audit-log',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatChipsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    DataTableComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './audit-log.component.html',
  styleUrl: './audit-log.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuditLogComponent implements OnInit {
  private readonly reportingService = inject(ReportingService);
  private readonly fb = inject(FormBuilder);

  readonly logs = signal<AuditLog[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(20);

  readonly isEmpty = computed(
    () => this.logs().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns = ['timestamp', 'username', 'action', 'entityType', 'entityId', 'ipAddress'];
  readonly actions = Object.values(AuditActionEnum);

  readonly filterForm = this.fb.nonNullable.group({
    action: [''],
    entityType: [''],
    fromDate: [''],
    toDate: [''],
  });

  ngOnInit(): void {
    void this.loadLogs();
  }

  async loadLogs(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const filters = this.filterForm.getRawValue();
      const result = await this.reportingService.listAuditLogs(this.currentPage(), this.pageSize(), {
        action: (filters.action as AuditActionEnum) || undefined,
        entityType: filters.entityType || undefined,
        fromDate: filters.fromDate || undefined,
        toDate: filters.toDate || undefined,
      });
      this.logs.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load audit logs.');
    } finally {
      this.loading.set(false);
    }
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadLogs();
  }

  applyFilters(): void {
    this.currentPage.set(0);
    void this.loadLogs();
  }
}
