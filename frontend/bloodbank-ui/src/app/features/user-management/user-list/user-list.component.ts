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
import { FormsModule } from '@angular/forms';
import { PageEvent } from '@angular/material/paginator';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { SearchBarComponent } from '@shared/components/search-bar/search-bar.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { BranchService } from '@features/branch/services/branch.service';
import { UserService } from '../services/user.service';
import { User } from '../models/user-management.model';
import { Branch } from '@features/branch/models/branch.model';

/**
 * User list with search, branch filter, pagination, and management actions.
 */
@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatChipsModule,
    MatSelectModule,
    MatFormFieldModule,
    DataTableComponent,
    SearchBarComponent,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserListComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly branchService = inject(BranchService);
  private readonly router = inject(Router);

  readonly users = signal<User[]>([]);
  readonly branches = signal<Branch[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);
  readonly searchQuery = signal('');
  readonly selectedBranchId = signal('');

  readonly isEmpty = computed(
    () => this.users().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns: string[] = [
    'username',
    'name',
    'email',
    'roles',
    'status',
    'branch',
    'actions',
  ];

  ngOnInit(): void {
    void this.loadBranches();
    void this.loadUsers();
  }

  async loadBranches(): Promise<void> {
    try {
      const result = await this.branchService.list(0, 100);
      this.branches.set(result.content);
    } catch {
      // Non-critical — branch filter will just be empty
    }
  }

  async loadUsers(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.userService.list(
        this.currentPage(),
        this.pageSize(),
        this.searchQuery() || undefined,
        this.selectedBranchId() || undefined,
      );
      this.users.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load users. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  onSearch(query: string): void {
    this.searchQuery.set(query);
    this.currentPage.set(0);
    void this.loadUsers();
  }

  onBranchFilter(branchId: string): void {
    this.selectedBranchId.set(branchId);
    this.currentPage.set(0);
    void this.loadUsers();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadUsers();
  }

  addUser(): void {
    void this.router.navigate(['/staff/users/new']);
  }

  editUser(user: User): void {
    void this.router.navigate(['/staff/users', user.id, 'edit']);
  }

  viewActivity(user: User): void {
    void this.router.navigate(['/staff/users', user.id, 'activity']);
  }

  getFullName(user: User): string {
    return `${user.firstName} ${user.lastName}`;
  }
}
