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
import { PageEvent } from '@angular/material/paginator';
import { DatePipe } from '@angular/common';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { LabService } from '../services/lab.service';
import {
  LabInstrument,
  InstrumentStatusEnum,
  INSTRUMENT_STATUS_OPTIONS,
} from '../models/lab.model';

/**
 * Lab instrument list with calibration status and management actions.
 */
@Component({
  selector: 'app-instrument-list',
  standalone: true,
  imports: [
    DatePipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatChipsModule,
    DataTableComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './instrument-list.component.html',
  styleUrl: './instrument-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InstrumentListComponent implements OnInit {
  private readonly labService = inject(LabService);
  private readonly router = inject(Router);

  // ── State signals ──────────────────────────────────────────────
  readonly instruments = signal<LabInstrument[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);

  // ── Computed ───────────────────────────────────────────────────
  readonly isEmpty = computed(
    () =>
      this.instruments().length === 0 && !this.loading() && !this.error(),
  );

  readonly calibrationDueCount = computed(
    () =>
      this.instruments().filter(
        (i) => i.status === InstrumentStatusEnum.CALIBRATION_DUE,
      ).length,
  );

  readonly displayedColumns: string[] = [
    'name',
    'manufacturer',
    'serialNumber',
    'status',
    'lastCalibration',
    'nextCalibration',
    'location',
    'actions',
  ];

  readonly statusOptions = INSTRUMENT_STATUS_OPTIONS;

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    void this.loadInstruments();
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadInstruments(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const result = await this.labService.listInstruments(
        this.currentPage(),
        this.pageSize(),
      );
      this.instruments.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load instruments. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  // ── Event handlers ─────────────────────────────────────────────

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadInstruments();
  }

  viewInstrument(instrument: LabInstrument): void {
    void this.router.navigate(['/staff/lab/instruments', instrument.id]);
  }

  getStatusClass(status: InstrumentStatusEnum): string {
    switch (status) {
      case InstrumentStatusEnum.ACTIVE:
        return 'instrument-status instrument-active';
      case InstrumentStatusEnum.INACTIVE:
        return 'instrument-status instrument-inactive';
      case InstrumentStatusEnum.MAINTENANCE:
        return 'instrument-status instrument-maintenance';
      case InstrumentStatusEnum.CALIBRATION_DUE:
        return 'instrument-status instrument-calibration-due';
      case InstrumentStatusEnum.OUT_OF_SERVICE:
        return 'instrument-status instrument-out-of-service';
      default:
        return 'instrument-status';
    }
  }

  getStatusLabel(status: InstrumentStatusEnum): string {
    const option = this.statusOptions.find((o) => o.value === status);
    return option ? option.label : status;
  }

  isCalibrationOverdue(instrument: LabInstrument): boolean {
    if (!instrument.nextCalibrationDate) return false;
    return new Date(instrument.nextCalibrationDate) < new Date();
  }

  getDaysUntilCalibration(instrument: LabInstrument): number | null {
    if (!instrument.nextCalibrationDate) return null;
    const next = new Date(instrument.nextCalibrationDate);
    const now = new Date();
    return Math.ceil((next.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
  }
}
