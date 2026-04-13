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
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';

import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { BranchService } from '../services/branch.service';
import { Branch, OperatingHours, Equipment, Region } from '../models/branch.model';

/**
 * Branch detail view with overview, operating hours, equipment and regions tabs.
 */
@Component({
  selector: 'app-branch-detail',
  standalone: true,
  imports: [
    DatePipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatTableModule,
    MatDividerModule,
    MatTooltipModule,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './branch-detail.component.html',
  styleUrl: './branch-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BranchDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly branchService = inject(BranchService);

  readonly branch = signal<Branch | null>(null);
  readonly operatingHours = signal<OperatingHours[]>([]);
  readonly equipment = signal<Equipment[]>([]);
  readonly regions = signal<Region[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly activeTab = signal(0);

  readonly branchId = computed(() => this.branch()?.id ?? '');

  readonly equipmentColumns: string[] = [
    'name',
    'type',
    'serialNumber',
    'lastMaintenanceDate',
    'nextMaintenanceDate',
    'status',
  ];

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      void this.loadBranch(id);
    }
  }

  async loadBranch(id: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const branch = await this.branchService.getById(id);
      this.branch.set(branch);

      const [hours, equip, regs] = await Promise.all([
        this.branchService.getOperatingHours(id),
        this.branchService.getEquipment(id),
        this.branchService.getRegions(id),
      ]);
      this.operatingHours.set(hours);
      this.equipment.set(equip);
      this.regions.set(regs);
    } catch {
      this.error.set('Failed to load branch details. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  editBranch(): void {
    const b = this.branch();
    if (b) {
      void this.router.navigate(['/staff/branches', b.id, 'edit']);
    }
  }

  goBack(): void {
    void this.router.navigate(['/staff/branches']);
  }

  onTabChange(index: number): void {
    this.activeTab.set(index);
  }
}
