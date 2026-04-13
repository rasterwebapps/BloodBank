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
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { PageEvent } from '@angular/material/paginator';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { BloodGroupBadgeComponent } from '@shared/components/blood-group-badge/blood-group-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { InventoryService } from '../services/inventory.service';
import {
  BloodUnit,
  BLOOD_GROUP_OPTIONS,
  COMPONENT_TYPE_OPTIONS,
  BLOOD_UNIT_STATUS_OPTIONS,
  EXPIRY_WARNING_DAYS,
  getComponentTypeLabel,
} from '../models/inventory.model';

/**
 * Blood unit list with blood group, component type, and status filters.
 */
@Component({
  selector: 'app-blood-unit-list',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatTableModule,
    MatSelectModule,
    MatFormFieldModule,
    MatCardModule,
    DataTableComponent,
    StatusBadgeComponent,
    BloodGroupBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './blood-unit-list.component.html',
  styleUrl: './blood-unit-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BloodUnitListComponent implements OnInit {
  private readonly inventoryService = inject(InventoryService);

  // ── State signals ──────────────────────────────────────────────
  readonly units = signal<BloodUnit[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);
  readonly filterBloodGroup = signal('');
  readonly filterComponentType = signal('');
  readonly filterStatus = signal('');

  // ── Computed ───────────────────────────────────────────────────
  readonly isEmpty = computed(
    () => this.units().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns: string[] = [
    'unitNumber',
    'bloodGroup',
    'componentType',
    'status',
    'collectionDate',
    'expiryDate',
    'actions',
  ];

  // ── Filter options ─────────────────────────────────────────────
  readonly bloodGroupOptions = BLOOD_GROUP_OPTIONS;
  readonly componentTypeOptions = COMPONENT_TYPE_OPTIONS;
  readonly statusOptions = BLOOD_UNIT_STATUS_OPTIONS;

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    void this.loadUnits();
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadUnits(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const filters: Record<string, string> = {};
      if (this.filterBloodGroup()) filters['bloodGroupId'] = this.filterBloodGroup();
      if (this.filterComponentType()) filters['componentType'] = this.filterComponentType();
      if (this.filterStatus()) filters['status'] = this.filterStatus();

      const result = await this.inventoryService.listBloodUnits(
        this.currentPage(),
        this.pageSize(),
        filters,
      );
      this.units.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load blood units. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  // ── Event handlers ─────────────────────────────────────────────

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadUnits();
  }

  onBloodGroupFilter(value: string): void {
    this.filterBloodGroup.set(value);
    this.currentPage.set(0);
    void this.loadUnits();
  }

  onComponentTypeFilter(value: string): void {
    this.filterComponentType.set(value);
    this.currentPage.set(0);
    void this.loadUnits();
  }

  onStatusFilter(value: string): void {
    this.filterStatus.set(value);
    this.currentPage.set(0);
    void this.loadUnits();
  }

  clearFilters(): void {
    this.filterBloodGroup.set('');
    this.filterComponentType.set('');
    this.filterStatus.set('');
    this.currentPage.set(0);
    void this.loadUnits();
  }

  // ── Helpers ────────────────────────────────────────────────────

  isExpiringSoon(unit: BloodUnit): boolean {
    const expiry = new Date(unit.expiryDate);
    const warningDate = new Date();
    warningDate.setDate(warningDate.getDate() + EXPIRY_WARNING_DAYS);
    return expiry <= warningDate && expiry > new Date();
  }

  getComponentTypeLabel(value: string): string {
    return getComponentTypeLabel(value);
  }
}
