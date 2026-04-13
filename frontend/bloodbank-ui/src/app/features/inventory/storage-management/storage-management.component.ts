import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTableModule } from '@angular/material/table';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { firstValueFrom } from 'rxjs';

import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { NotificationService } from '@core/services/notification.service';
import { BranchContextService } from '@core/services/branch-context.service';
import { LogisticsService } from '../services/logistics.service';
import {
  StorageLocation,
  STORAGE_TYPE_OPTIONS,
} from '../models/inventory.model';
import { AddStorageDialogComponent } from './add-storage-dialog.component';

/**
 * Storage location management — lists locations and allows adding new ones.
 */
@Component({
  selector: 'app-storage-management',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatTableModule,
    MatProgressBarModule,
    MatDialogModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './storage-management.component.html',
  styleUrl: './storage-management.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StorageManagementComponent implements OnInit {
  private readonly logisticsService = inject(LogisticsService);
  private readonly notification = inject(NotificationService);
  private readonly branchContext = inject(BranchContextService);
  private readonly dialog = inject(MatDialog);

  // ── State ──────────────────────────────────────────────────────
  readonly locations = signal<StorageLocation[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  // ── Computed ───────────────────────────────────────────────────
  readonly isEmpty = computed(
    () => this.locations().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns: string[] = [
    'name',
    'type',
    'utilization',
    'temperature',
    'actions',
  ];

  readonly storageTypeOptions = STORAGE_TYPE_OPTIONS;

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    void this.loadLocations();
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadLocations(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const data = await this.logisticsService.listStorageLocations();
      this.locations.set(data);
    } catch {
      this.error.set('Failed to load storage locations. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  // ── Actions ────────────────────────────────────────────────────

  openAddDialog(): void {
    const dialogRef = this.dialog.open(AddStorageDialogComponent, {
      width: '520px',
      data: { branchId: this.branchContext.branchId() },
    });

    void firstValueFrom(dialogRef.afterClosed()).then((result: Partial<StorageLocation> | undefined) => {
      if (result) {
        void this.createLocation(result);
      }
    });
  }

  private async createLocation(data: Partial<StorageLocation>): Promise<void> {
    try {
      const created = await this.logisticsService.createStorageLocation(data);
      this.locations.set([...this.locations(), created]);
      this.notification.success('Storage location added successfully.');
    } catch {
      this.notification.error('Failed to add storage location.');
    }
  }

  // ── Helpers ────────────────────────────────────────────────────

  getUtilization(location: StorageLocation): number {
    if (location.capacity === 0) return 0;
    return Math.round((location.currentUnits / location.capacity) * 100);
  }

  getUtilizationClass(location: StorageLocation): string {
    const pct = this.getUtilization(location);
    if (pct >= 90) return 'util-critical';
    if (pct >= 70) return 'util-warning';
    return 'util-ok';
  }

  getStorageTypeLabel(value: string): string {
    const opt = STORAGE_TYPE_OPTIONS.find((o) => o.value === value);
    return opt ? opt.label : value;
  }
}
