import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PageEvent } from '@angular/material/paginator';
import { DatePipe } from '@angular/common';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { CampService } from '../services/camp.service';
import { Camp, CAMP_STATUS_OPTIONS } from '../models/camp.model';

/**
 * Displays camps with status filter toggle, pagination, and actions.
 */
@Component({
  selector: 'app-camp-list',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    MatTableModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatIconModule,
    MatTooltipModule,
    DataTableComponent,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './camp-list.component.html',
  styleUrl: './camp-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CampListComponent implements OnInit {
  private readonly campService = inject(CampService);
  private readonly router = inject(Router);

  // ── State signals ──────────────────────────────────────────────
  readonly camps = signal<Camp[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);
  readonly statusFilter = signal<string>('');

  // ── Options ────────────────────────────────────────────────────
  readonly statusOptions = CAMP_STATUS_OPTIONS;

  // ── Computed ───────────────────────────────────────────────────
  readonly isEmpty = computed(
    () => this.camps().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns: string[] = [
    'name',
    'location',
    'campDate',
    'targetDonors',
    'registeredDonors',
    'status',
    'actions',
  ];

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    void this.loadCamps();
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadCamps(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const result = await this.campService.list(
        this.currentPage(),
        this.pageSize(),
        this.statusFilter() || undefined,
      );
      this.camps.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load camps. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  // ── Event handlers ─────────────────────────────────────────────

  onStatusFilterChange(status: string): void {
    this.statusFilter.set(status);
    this.currentPage.set(0);
    void this.loadCamps();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadCamps();
  }

  planNewCamp(): void {
    void this.router.navigate(['/staff/camps/new']);
  }

  viewCamp(camp: Camp): void {
    void this.router.navigate(['/staff/camps', camp.id]);
  }

  editCamp(camp: Camp): void {
    void this.router.navigate(['/staff/camps', camp.id, 'edit']);
  }
}
