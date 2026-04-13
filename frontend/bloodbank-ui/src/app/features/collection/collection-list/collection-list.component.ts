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
import { PageEvent } from '@angular/material/paginator';
import { DatePipe } from '@angular/common';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { SearchBarComponent } from '@shared/components/search-bar/search-bar.component';
import { BloodGroupBadgeComponent } from '@shared/components/blood-group-badge/blood-group-badge.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { CollectionService } from '../services/collection.service';
import { Collection, CollectionStatus } from '../models/collection.model';

/**
 * Displays today's blood collections with search, pagination, and actions.
 */
@Component({
  selector: 'app-collection-list',
  standalone: true,
  imports: [
    DatePipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    DataTableComponent,
    SearchBarComponent,
    BloodGroupBadgeComponent,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './collection-list.component.html',
  styleUrl: './collection-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CollectionListComponent implements OnInit {
  private readonly collectionService = inject(CollectionService);
  private readonly router = inject(Router);

  // ── State signals ──────────────────────────────────────────────
  readonly collections = signal<Collection[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);
  readonly searchQuery = signal('');

  // ── Computed ───────────────────────────────────────────────────
  readonly isEmpty = computed(
    () =>
      this.collections().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns: string[] = [
    'donorName',
    'donorBloodGroup',
    'bagNumber',
    'volumeMl',
    'startTime',
    'status',
    'actions',
  ];

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    void this.loadCollections();
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadCollections(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const result = await this.collectionService.getTodayCollections(
        this.currentPage(),
        this.pageSize(),
        this.searchQuery() || undefined,
      );
      this.collections.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load collections. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  // ── Event handlers ─────────────────────────────────────────────

  onSearch(query: string): void {
    this.searchQuery.set(query);
    this.currentPage.set(0);
    void this.loadCollections();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadCollections();
  }

  recordCollection(): void {
    void this.router.navigate(['/staff/collections/new']);
  }

  reportAdverseReaction(collection: Collection): void {
    void this.router.navigate([
      '/staff/collections',
      collection.id,
      'adverse-reaction',
    ]);
  }

  registerSamples(collection: Collection): void {
    void this.router.navigate([
      '/staff/collections',
      collection.id,
      'samples',
    ]);
  }

  canReportReaction(collection: Collection): boolean {
    return (
      collection.status === CollectionStatus.IN_PROGRESS ||
      collection.status === CollectionStatus.COMPLETED
    );
  }
}
