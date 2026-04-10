import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PageEvent } from '@angular/material/paginator';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { BloodGroupBadgeComponent } from '@shared/components/blood-group-badge/blood-group-badge.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { DonorService } from '../services/donor.service';
import {
  Donor,
  BLOOD_GROUP_OPTIONS,
  DONOR_STATUS_OPTIONS,
  getDonorFullName,
} from '../models/donor.model';

/**
 * Advanced donor search with multiple filter criteria.
 */
@Component({
  selector: 'app-donor-search',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    MatTooltipModule,
    DataTableComponent,
    BloodGroupBadgeComponent,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './donor-search.component.html',
  styleUrl: './donor-search.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DonorSearchComponent {
  private readonly fb = inject(FormBuilder);
  private readonly donorService = inject(DonorService);
  private readonly router = inject(Router);

  // ── State ──────────────────────────────────────────────────────
  readonly results = signal<Donor[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly searched = signal(false);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);

  // ── Computed ───────────────────────────────────────────────────
  readonly isEmpty = computed(
    () =>
      this.searched() &&
      this.results().length === 0 &&
      !this.loading() &&
      !this.error(),
  );

  // ── Select options ─────────────────────────────────────────────
  readonly bloodGroupOptions = BLOOD_GROUP_OPTIONS;
  readonly statusOptions = DONOR_STATUS_OPTIONS;

  readonly displayedColumns: string[] = [
    'donorNumber',
    'name',
    'bloodGroup',
    'phone',
    'status',
    'actions',
  ];

  // ── Filter form ────────────────────────────────────────────────
  readonly filterForm = this.fb.group({
    firstName: [''],
    lastName: [''],
    email: [''],
    phone: [''],
    nationalId: [''],
    bloodGroupId: [''],
    status: [''],
  });

  // ── Actions ────────────────────────────────────────────────────

  async onSearch(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    this.searched.set(true);
    this.currentPage.set(0);

    try {
      await this.executeSearch();
    } catch {
      this.error.set('Search failed. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  async onPageChange(event: PageEvent): Promise<void> {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loading.set(true);

    try {
      await this.executeSearch();
    } catch {
      this.error.set('Failed to load page.');
    } finally {
      this.loading.set(false);
    }
  }

  private async executeSearch(): Promise<void> {
    const formValue = this.filterForm.getRawValue();
    const params: Record<string, string> = {};
    for (const [key, value] of Object.entries(formValue)) {
      if (value !== null && value !== undefined && value !== '') {
        params[key] = value;
      }
    }

    const result = await this.donorService.search(
      params,
      this.currentPage(),
      this.pageSize(),
    );
    this.results.set(result.content);
    this.totalElements.set(result.totalElements);
  }

  clearFilters(): void {
    this.filterForm.reset();
    this.results.set([]);
    this.totalElements.set(0);
    this.searched.set(false);
    this.error.set(null);
  }

  viewDonor(donor: Donor): void {
    void this.router.navigate(['/staff/donors', donor.id]);
  }

  getFullName(donor: Donor): string {
    return getDonorFullName(donor);
  }
}
