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
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { PageEvent } from '@angular/material/paginator';
import { DatePipe } from '@angular/common';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { SearchBarComponent } from '@shared/components/search-bar/search-bar.component';
import { BloodGroupBadgeComponent } from '@shared/components/blood-group-badge/blood-group-badge.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { DonorService } from '../services/donor.service';
import { Donor, getDonorFullName } from '../models/donor.model';

/**
 * Donor list view with search, pagination, and CRUD actions.
 */
@Component({
  selector: 'app-donor-list',
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
    SearchBarComponent,
    BloodGroupBadgeComponent,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './donor-list.component.html',
  styleUrl: './donor-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DonorListComponent implements OnInit {
  private readonly donorService = inject(DonorService);
  private readonly router = inject(Router);

  // ── State signals ──────────────────────────────────────────────
  readonly donors = signal<Donor[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);
  readonly searchQuery = signal('');

  // ── Computed ───────────────────────────────────────────────────
  readonly isEmpty = computed(
    () => this.donors().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns: string[] = [
    'donorNumber',
    'name',
    'bloodGroup',
    'phone',
    'status',
    'lastDonationDate',
    'actions',
  ];

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    void this.loadDonors();
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadDonors(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const result = await this.donorService.list(
        this.currentPage(),
        this.pageSize(),
        this.searchQuery() || undefined,
      );
      this.donors.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load donors. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  // ── Event handlers ─────────────────────────────────────────────

  onSearch(query: string): void {
    this.searchQuery.set(query);
    this.currentPage.set(0);
    void this.loadDonors();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadDonors();
  }

  viewDonor(donor: Donor): void {
    void this.router.navigate(['/staff/donors', donor.id]);
  }

  editDonor(donor: Donor): void {
    void this.router.navigate(['/staff/donors', donor.id, 'edit']);
  }

  registerDonor(): void {
    void this.router.navigate(['/staff/donors/new']);
  }

  getFullName(donor: Donor): string {
    return getDonorFullName(donor);
  }
}
