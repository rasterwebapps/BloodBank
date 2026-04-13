import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '@shared/components/confirm-dialog/confirm-dialog.component';
import { NotificationService } from '@core/services/notification.service';
import { BranchService } from '../services/branch.service';
import {
  BloodGroup,
  ComponentType,
  DeferralReason,
  ReactionType,
} from '../models/branch.model';
import { firstValueFrom } from 'rxjs';

/**
 * Master data management with tabs for Blood Groups, Component Types,
 * Deferral Reasons, and Reaction Types.
 */
@Component({
  selector: 'app-master-data',
  standalone: true,
  imports: [
    MatTabsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './master-data.component.html',
  styleUrl: './master-data.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MasterDataComponent implements OnInit {
  private readonly branchService = inject(BranchService);
  private readonly notification = inject(NotificationService);
  private readonly dialog = inject(MatDialog);

  readonly bloodGroups = signal<BloodGroup[]>([]);
  readonly componentTypes = signal<ComponentType[]>([]);
  readonly deferralReasons = signal<DeferralReason[]>([]);
  readonly reactionTypes = signal<ReactionType[]>([]);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly bloodGroupColumns: string[] = ['name', 'aboGroup', 'rhFactor', 'isActive', 'actions'];
  readonly componentTypeColumns: string[] = ['name', 'code', 'storageTemperature', 'shelfLifeDays', 'isActive', 'actions'];
  readonly deferralReasonColumns: string[] = ['code', 'description', 'durationDays', 'isPermanent', 'isActive', 'actions'];
  readonly reactionTypeColumns: string[] = ['code', 'name', 'severity', 'isActive', 'actions'];

  ngOnInit(): void {
    void this.loadAll();
  }

  async loadAll(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const [bg, ct, dr, rt] = await Promise.all([
        this.branchService.getBloodGroups(),
        this.branchService.getComponentTypes(),
        this.branchService.getDeferralReasons(),
        this.branchService.getReactionTypes(),
      ]);
      this.bloodGroups.set(bg);
      this.componentTypes.set(ct);
      this.deferralReasons.set(dr);
      this.reactionTypes.set(rt);
    } catch {
      this.error.set('Failed to load master data. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  async deleteBloodGroup(id: string): Promise<void> {
    const confirmed = await this.confirmDelete('blood group');
    if (!confirmed) return;
    try {
      await this.branchService.deleteBloodGroup(id);
      this.bloodGroups.update(items => items.filter(i => i.id !== id));
      this.notification.success('Blood group deleted.');
    } catch {
      this.notification.error('Failed to delete blood group.');
    }
  }

  async deleteComponentType(id: string): Promise<void> {
    const confirmed = await this.confirmDelete('component type');
    if (!confirmed) return;
    try {
      await this.branchService.deleteComponentType(id);
      this.componentTypes.update(items => items.filter(i => i.id !== id));
      this.notification.success('Component type deleted.');
    } catch {
      this.notification.error('Failed to delete component type.');
    }
  }

  async deleteDeferralReason(id: string): Promise<void> {
    const confirmed = await this.confirmDelete('deferral reason');
    if (!confirmed) return;
    try {
      await this.branchService.deleteDeferralReason(id);
      this.deferralReasons.update(items => items.filter(i => i.id !== id));
      this.notification.success('Deferral reason deleted.');
    } catch {
      this.notification.error('Failed to delete deferral reason.');
    }
  }

  async deleteReactionType(id: string): Promise<void> {
    const confirmed = await this.confirmDelete('reaction type');
    if (!confirmed) return;
    try {
      await this.branchService.deleteReactionType(id);
      this.reactionTypes.update(items => items.filter(i => i.id !== id));
      this.notification.success('Reaction type deleted.');
    } catch {
      this.notification.error('Failed to delete reaction type.');
    }
  }

  private async confirmDelete(itemName: string): Promise<boolean> {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `Delete ${itemName}`,
        message: `Are you sure you want to delete this ${itemName}? This action cannot be undone.`,
        confirmText: 'Delete',
        color: 'warn',
      },
    });
    return firstValueFrom(ref.afterClosed()) as Promise<boolean>;
  }
}
