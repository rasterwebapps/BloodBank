import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { DatePipe } from '@angular/common';

import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { BloodGroupBadgeComponent } from '@shared/components/blood-group-badge/blood-group-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { NotificationService } from '@core/services/notification.service';
import { CampService } from '../services/camp.service';
import {
  Camp,
  CampStatus,
  CampDonorRegistration,
  CampResource,
} from '../models/camp.model';

/**
 * Camp detail view with stats, donor registrations, and resources.
 */
@Component({
  selector: 'app-camp-detail',
  standalone: true,
  imports: [
    DatePipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatTableModule,
    MatChipsModule,
    StatusBadgeComponent,
    BloodGroupBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './camp-detail.component.html',
  styleUrl: './camp-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CampDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly campService = inject(CampService);
  private readonly notification = inject(NotificationService);

  // ── State ──────────────────────────────────────────────────────
  readonly camp = signal<Camp | null>(null);
  readonly donors = signal<CampDonorRegistration[]>([]);
  readonly resources = signal<CampResource[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  // ── Computed ───────────────────────────────────────────────────
  readonly canActivate = computed(
    () => this.camp()?.status === CampStatus.PLANNED,
  );
  readonly canComplete = computed(
    () => this.camp()?.status === CampStatus.ACTIVE,
  );

  readonly donorColumns: string[] = [
    'donorName',
    'bloodGroup',
    'phone',
    'attended',
  ];
  readonly resourceColumns: string[] = [
    'resourceType',
    'quantity',
    'notes',
  ];

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      void this.loadCamp(id);
    }
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadCamp(id: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const [camp, donors, resources] = await Promise.all([
        this.campService.getById(id),
        this.campService.getDonorRegistrations(id),
        this.campService.getResources(id),
      ]);
      this.camp.set(camp);
      this.donors.set(donors);
      this.resources.set(resources);
    } catch {
      this.error.set('Failed to load camp details. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  // ── Actions ────────────────────────────────────────────────────

  async activateCamp(): Promise<void> {
    const camp = this.camp();
    if (!camp) return;

    try {
      const updated = await this.campService.updateStatus(
        camp.id,
        CampStatus.ACTIVE,
      );
      this.camp.set(updated);
      this.notification.success('Camp activated successfully.');
    } catch {
      this.notification.error('Failed to activate camp.');
    }
  }

  async completeCamp(): Promise<void> {
    const camp = this.camp();
    if (!camp) return;

    try {
      const updated = await this.campService.updateStatus(
        camp.id,
        CampStatus.COMPLETED,
      );
      this.camp.set(updated);
      this.notification.success('Camp completed successfully.');
    } catch {
      this.notification.error('Failed to complete camp.');
    }
  }

  registerDonor(): void {
    const camp = this.camp();
    if (camp) {
      void this.router.navigate(['/staff/camps', camp.id, 'register-donor']);
    }
  }

  editCamp(): void {
    const camp = this.camp();
    if (camp) {
      void this.router.navigate(['/staff/camps', camp.id, 'edit']);
    }
  }

  goBack(): void {
    void this.router.navigate(['/staff/camps']);
  }
}
