import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { PageEvent } from '@angular/material/paginator';
import { DatePipe } from '@angular/common';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { LabService } from '../services/lab.service';
import {
  TestOrder,
  TestOrderStatusEnum,
  TestOrderPriorityEnum,
  TEST_ORDER_STATUS_OPTIONS,
  getPriorityClass,
} from '../models/lab.model';

/**
 * Table of pending/in-progress test orders with priority badges and status filtering.
 */
@Component({
  selector: 'app-test-order-list',
  standalone: true,
  imports: [
    DatePipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatChipsModule,
    MatSelectModule,
    MatFormFieldModule,
    DataTableComponent,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './test-order-list.component.html',
  styleUrl: './test-order-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TestOrderListComponent implements OnInit {
  private readonly labService = inject(LabService);
  private readonly router = inject(Router);

  // ── State signals ──────────────────────────────────────────────
  readonly testOrders = signal<TestOrder[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);
  readonly statusFilter = signal<TestOrderStatusEnum | ''>('');

  // ── Computed ───────────────────────────────────────────────────
  readonly isEmpty = computed(
    () => this.testOrders().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns: string[] = [
    'sampleNumber',
    'donorName',
    'testPanel',
    'priority',
    'status',
    'orderedAt',
    'actions',
  ];

  readonly statusOptions = TEST_ORDER_STATUS_OPTIONS;

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    void this.loadTestOrders();
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadTestOrders(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const filter = this.statusFilter();
      const result = await this.labService.listTestOrders(
        this.currentPage(),
        this.pageSize(),
        filter || undefined,
      );
      this.testOrders.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load test orders. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  // ── Event handlers ─────────────────────────────────────────────

  onStatusFilterChange(status: TestOrderStatusEnum | ''): void {
    this.statusFilter.set(status);
    this.currentPage.set(0);
    void this.loadTestOrders();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadTestOrders();
  }

  enterResults(order: TestOrder): void {
    void this.router.navigate(['/staff/lab/orders', order.id, 'results']);
  }

  viewOrder(order: TestOrder): void {
    void this.router.navigate(['/staff/lab/orders', order.id]);
  }

  getPriorityBadgeClass(priority: TestOrderPriorityEnum): string {
    return getPriorityClass(priority);
  }

  getPriorityLabel(priority: TestOrderPriorityEnum): string {
    switch (priority) {
      case TestOrderPriorityEnum.STAT:
        return 'STAT';
      case TestOrderPriorityEnum.URGENT:
        return 'Urgent';
      default:
        return 'Routine';
    }
  }
}
