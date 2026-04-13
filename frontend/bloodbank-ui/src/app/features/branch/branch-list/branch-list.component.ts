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
import { PageEvent } from '@angular/material/paginator';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { SearchBarComponent } from '@shared/components/search-bar/search-bar.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { AuthService } from '@core/auth/auth.service';
import { BranchService } from '../services/branch.service';
import { Branch } from '../models/branch.model';

/**
 * Branch list view with search, pagination, and management actions.
 */
@Component({
  selector: 'app-branch-list',
  standalone: true,
  imports: [
    RouterLink,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    DataTableComponent,
    SearchBarComponent,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './branch-list.component.html',
  styleUrl: './branch-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BranchListComponent implements OnInit {
  private readonly branchService = inject(BranchService);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  readonly branches = signal<Branch[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);
  readonly searchQuery = signal('');

  readonly isEmpty = computed(
    () => this.branches().length === 0 && !this.loading() && !this.error(),
  );

  readonly isSuperAdmin = computed(() => this.authService.hasAnyRole(['SUPER_ADMIN']));

  readonly displayedColumns: string[] = [
    'name',
    'code',
    'city',
    'status',
    'capacity',
    'actions',
  ];

  ngOnInit(): void {
    void this.loadBranches();
  }

  async loadBranches(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.branchService.list(
        this.currentPage(),
        this.pageSize(),
        this.searchQuery() || undefined,
      );
      this.branches.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load branches. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  onSearch(query: string): void {
    this.searchQuery.set(query);
    this.currentPage.set(0);
    void this.loadBranches();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadBranches();
  }

  viewBranch(branch: Branch): void {
    void this.router.navigate(['/staff/branches', branch.id]);
  }

  editBranch(branch: Branch): void {
    void this.router.navigate(['/staff/branches', branch.id, 'edit']);
  }

  addBranch(): void {
    void this.router.navigate(['/staff/branches/new']);
  }

  goToMasterData(): void {
    void this.router.navigate(['/staff/branches/master-data']);
  }
}
