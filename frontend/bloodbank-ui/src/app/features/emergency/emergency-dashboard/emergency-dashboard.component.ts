import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
  OnDestroy,
} from '@angular/core';
import { Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatBadgeModule } from '@angular/material/badge';

import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { EmergencyService } from '../services/emergency.service';
import {
  EmergencyRequest,
  EmergencyStatusEnum,
  PriorityLevelEnum,
  getPriorityClass,
  getPriorityLabel,
} from '../models/emergency.model';

/** Auto-refresh interval in milliseconds. */
const REFRESH_INTERVAL_MS = 30_000;

/**
 * Emergency dashboard showing summary statistics and active emergency requests.
 * Refreshes automatically every 30 seconds.
 */
@Component({
  selector: 'app-emergency-dashboard',
  standalone: true,
  imports: [
    DatePipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatTooltipModule,
    MatBadgeModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
    StatusBadgeComponent,
  ],
  templateUrl: './emergency-dashboard.component.html',
  styleUrl: './emergency-dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmergencyDashboardComponent implements OnInit, OnDestroy {
  private readonly emergencyService = inject(EmergencyService);
  private readonly router = inject(Router);

  private refreshTimer: ReturnType<typeof setInterval> | null = null;

  // ── State signals ──────────────────────────────────────────────
  readonly allRequests = signal<EmergencyRequest[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  // ── Computed statistics ────────────────────────────────────────
  readonly activeCount = computed(
    () =>
      this.allRequests().filter(
        (r) => r.status === EmergencyStatusEnum.ACTIVE,
      ).length,
  );

  readonly matchedCount = computed(
    () =>
      this.allRequests().filter(
        (r) => r.status === EmergencyStatusEnum.MATCHED,
      ).length,
  );

  readonly dispatchedCount = computed(
    () =>
      this.allRequests().filter(
        (r) => r.status === EmergencyStatusEnum.DISPATCHED,
      ).length,
  );

  readonly activeRequests = computed(() =>
    this.allRequests().filter(
      (r) =>
        r.status === EmergencyStatusEnum.ACTIVE ||
        r.status === EmergencyStatusEnum.MATCHED,
    ),
  );

  readonly isEmpty = computed(
    () =>
      this.activeRequests().length === 0 &&
      !this.loading() &&
      !this.error(),
  );

  readonly displayedColumns: string[] = [
    'requestNumber',
    'patientName',
    'bloodGroup',
    'priorityLevel',
    'status',
    'requestedAt',
    'actions',
  ];

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    void this.loadEmergencies();
    this.refreshTimer = setInterval(() => {
      void this.loadEmergencies();
    }, REFRESH_INTERVAL_MS);
  }

  ngOnDestroy(): void {
    if (this.refreshTimer !== null) {
      clearInterval(this.refreshTimer);
      this.refreshTimer = null;
    }
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadEmergencies(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.emergencyService.listEmergencies(0, 50);
      this.allRequests.set(result.content);
    } catch {
      this.error.set('Failed to load emergency data. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  // ── Navigation ─────────────────────────────────────────────────

  createEmergency(): void {
    void this.router.navigate(['/staff/emergency/request']);
  }

  viewDisasters(): void {
    void this.router.navigate(['/staff/emergency/disasters']);
  }

  // ── Helpers (exposed to template) ─────────────────────────────

  getPriorityClass(priority: PriorityLevelEnum): string {
    return getPriorityClass(priority);
  }

  getPriorityLabel(priority: PriorityLevelEnum): string {
    return getPriorityLabel(priority);
  }
}
