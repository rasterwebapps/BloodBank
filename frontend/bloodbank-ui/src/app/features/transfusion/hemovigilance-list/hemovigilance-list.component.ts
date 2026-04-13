import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

import { TransfusionService } from '../services/transfusion.service';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import {
  HemovigilanceReport,
  HemovigilanceStatusEnum,
  ReactionSeverityEnum,
  HEMOVIGILANCE_STATUS_OPTIONS,
} from '../models/transfusion.model';

/**
 * Component for listing hemovigilance reports with pagination and status filter.
 */
@Component({
  selector: 'app-hemovigilance-list',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatTableModule,
    MatPaginatorModule,
    MatIconModule,
    MatButtonModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './hemovigilance-list.component.html',
  styleUrl: './hemovigilance-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HemovigilanceListComponent implements OnInit {
  private readonly transfusionService = inject(TransfusionService);

  readonly selectedStatus = signal<HemovigilanceStatusEnum | null>(null);
  readonly reports = signal<HemovigilanceReport[]>([]);
  readonly totalElements = signal(0);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly statusOptions = HEMOVIGILANCE_STATUS_OPTIONS;
  readonly displayedColumns: string[] = [
    'reportNumber',
    'patientName',
    'transfusionNumber',
    'reactionType',
    'severity',
    'reportedBy',
    'reportedAt',
    'status',
  ];

  ngOnInit(): void {
    void this.loadReports();
  }

  async loadReports(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const status = this.selectedStatus() ?? undefined;
      const result = await this.transfusionService.listHemovigilanceReports(
        this.currentPage(),
        this.pageSize(),
        status,
      );
      this.reports.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load hemovigilance reports.');
    } finally {
      this.loading.set(false);
    }
  }

  onStatusChange(status: HemovigilanceStatusEnum | null): void {
    this.selectedStatus.set(status);
    this.currentPage.set(0);
    void this.loadReports();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadReports();
  }

  getSeverityClass(severity: ReactionSeverityEnum): string {
    const map: Record<ReactionSeverityEnum, string> = {
      [ReactionSeverityEnum.MILD]: 'severity-mild',
      [ReactionSeverityEnum.MODERATE]: 'severity-moderate',
      [ReactionSeverityEnum.SEVERE]: 'severity-severe',
      [ReactionSeverityEnum.LIFE_THREATENING]: 'severity-life-threatening',
    };
    return map[severity] ?? '';
  }
}
