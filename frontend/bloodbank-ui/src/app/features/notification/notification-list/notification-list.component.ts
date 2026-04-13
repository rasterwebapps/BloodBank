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
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { PageEvent } from '@angular/material/paginator';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { NotificationService } from '../services/notification.service';
import { Notification } from '../models/notification.model';

@Component({
  selector: 'app-notification-list',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatChipsModule,
    DataTableComponent,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './notification-list.component.html',
  styleUrl: './notification-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationListComponent implements OnInit {
  private readonly notificationService = inject(NotificationService);

  readonly notifications = signal<Notification[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);

  readonly isEmpty = computed(
    () => this.notifications().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns = [
    'recipientName',
    'type',
    'subject',
    'status',
    'sentAt',
  ];

  ngOnInit(): void {
    void this.loadNotifications();
  }

  async loadNotifications(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.notificationService.listNotifications(
        this.currentPage(),
        this.pageSize(),
      );
      this.notifications.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load notifications.');
    } finally {
      this.loading.set(false);
    }
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadNotifications();
  }
}
