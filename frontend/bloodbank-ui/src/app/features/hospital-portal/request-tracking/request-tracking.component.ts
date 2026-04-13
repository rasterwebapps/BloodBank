import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { HospitalPortalService } from '../services/hospital-portal.service';
import { HospitalRequest, RequestStatusEnum } from '../models/hospital-portal.model';

@Component({
  selector: 'app-request-tracking',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatTooltipModule,
    DataTableComponent,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './request-tracking.component.html',
  styleUrl: './request-tracking.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RequestTrackingComponent implements OnInit {
  private readonly hospitalPortalService = inject(HospitalPortalService);

  readonly requests = signal<HospitalRequest[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);

  readonly isEmpty = computed(
    () => this.requests().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns = [
    'requestNumber',
    'bloodGroup',
    'componentType',
    'quantity',
    'priority',
    'status',
    'requiredDate',
    'requestedAt',
  ];

  ngOnInit(): void {
    this.loadRequests();
  }

  async loadRequests(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.hospitalPortalService.getRequests(
        this.currentPage(),
        this.pageSize(),
      );
      this.requests.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load requests. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadRequests();
  }

  getStatusColor(status: RequestStatusEnum): 'primary' | 'accent' | 'warn' {
    switch (status) {
      case RequestStatusEnum.DELIVERED:
        return 'primary';
      case RequestStatusEnum.MATCHED:
      case RequestStatusEnum.ISSUED:
        return 'accent';
      case RequestStatusEnum.CANCELLED:
        return 'warn';
      default:
        return 'primary';
    }
  }
}
