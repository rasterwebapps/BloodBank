import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PageEvent } from '@angular/material/paginator';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { UserService } from '../services/user.service';
import { UserActivity } from '../models/user-management.model';

/**
 * User activity log with paginated audit trail.
 */
@Component({
  selector: 'app-user-activity',
  standalone: true,
  imports: [
    DatePipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatTooltipModule,
    DataTableComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './user-activity.component.html',
  styleUrl: './user-activity.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserActivityComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly userService = inject(UserService);

  readonly activities = signal<UserActivity[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(20);
  readonly currentUserId = signal('');

  readonly isEmpty = computed(
    () => this.activities().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns: string[] = [
    'occurredAt',
    'action',
    'resourceType',
    'description',
    'ipAddress',
  ];

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.currentUserId.set(id);
      void this.loadActivity(id);
    }
  }

  async loadActivity(userId: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.userService.getActivity(
        userId,
        this.currentPage(),
        this.pageSize(),
      );
      this.activities.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load activity log. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadActivity(this.currentUserId());
  }

  goBack(): void {
    void this.router.navigate(['/staff/users']);
  }
}
