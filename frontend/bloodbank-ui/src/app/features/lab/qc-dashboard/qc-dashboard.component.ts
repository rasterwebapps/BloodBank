import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
  ElementRef,
  viewChild,
  AfterViewInit,
} from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PageEvent } from '@angular/material/paginator';
import { DatePipe } from '@angular/common';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { LabService } from '../services/lab.service';
import {
  QcRecord,
  QcStatusEnum,
  LabInstrument,
  getQcStatusClass,
  QC_STATUS_OPTIONS,
} from '../models/lab.model';

/**
 * Quality control dashboard with QC records, instrument status,
 * and Levey-Jennings chart visualization.
 */
@Component({
  selector: 'app-qc-dashboard',
  standalone: true,
  imports: [
    DatePipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatFormFieldModule,
    MatChipsModule,
    MatTableModule,
    MatTooltipModule,
    DataTableComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './qc-dashboard.component.html',
  styleUrl: './qc-dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QcDashboardComponent implements OnInit, AfterViewInit {
  private readonly labService = inject(LabService);

  readonly ljCanvas = viewChild<ElementRef<HTMLCanvasElement>>('ljCanvas');

  // ── State ──────────────────────────────────────────────────────
  readonly qcRecords = signal<QcRecord[]>([]);
  readonly instruments = signal<LabInstrument[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);
  readonly selectedInstrumentId = signal<string>('');

  // ── Computed ───────────────────────────────────────────────────
  readonly isEmpty = computed(
    () => this.qcRecords().length === 0 && !this.loading() && !this.error(),
  );

  readonly passCount = computed(
    () => this.qcRecords().filter((r) => r.status === QcStatusEnum.PASS).length,
  );
  readonly failCount = computed(
    () => this.qcRecords().filter((r) => r.status === QcStatusEnum.FAIL).length,
  );
  readonly warningCount = computed(
    () => this.qcRecords().filter((r) => r.status === QcStatusEnum.WARNING).length,
  );

  readonly displayedColumns: string[] = [
    'instrumentName',
    'testType',
    'controlLevel',
    'observedValue',
    'expectedValue',
    'status',
    'performedAt',
    'performedBy',
  ];

  readonly qcStatusOptions = QC_STATUS_OPTIONS;

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    void this.loadData();
  }

  ngAfterViewInit(): void {
    // Chart will render once data loads
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadData(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const [qcResult, instrumentResult] = await Promise.all([
        this.labService.listQcRecords(
          this.currentPage(),
          this.pageSize(),
          this.selectedInstrumentId() || undefined,
        ),
        this.labService.listInstruments(0, 100),
      ]);
      this.qcRecords.set(qcResult.content);
      this.totalElements.set(qcResult.totalElements);
      this.instruments.set(instrumentResult.content);
      this.renderLeveyJenningsChart();
    } catch {
      this.error.set('Failed to load QC data. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  async loadQcRecords(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const result = await this.labService.listQcRecords(
        this.currentPage(),
        this.pageSize(),
        this.selectedInstrumentId() || undefined,
      );
      this.qcRecords.set(result.content);
      this.totalElements.set(result.totalElements);
      this.renderLeveyJenningsChart();
    } catch {
      this.error.set('Failed to load QC records.');
    } finally {
      this.loading.set(false);
    }
  }

  // ── Event handlers ─────────────────────────────────────────────

  onInstrumentFilterChange(instrumentId: string): void {
    this.selectedInstrumentId.set(instrumentId);
    this.currentPage.set(0);
    void this.loadQcRecords();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadQcRecords();
  }

  getQcBadgeClass(status: QcStatusEnum): string {
    return getQcStatusClass(status);
  }

  getQcStatusLabel(status: QcStatusEnum): string {
    const option = this.qcStatusOptions.find((o) => o.value === status);
    return option ? option.label : status;
  }

  getDeviation(record: QcRecord): string {
    if (record.standardDeviation === 0) return '0.00';
    const sd = (record.observedValue - record.mean) / record.standardDeviation;
    return sd.toFixed(2);
  }

  // ── Levey-Jennings Chart (Canvas 2D) ──────────────────────────

  private renderLeveyJenningsChart(): void {
    const canvasRef = this.ljCanvas();
    if (!canvasRef) return;

    const canvas = canvasRef.nativeElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const records = this.qcRecords();
    if (records.length === 0) return;

    const width = canvas.width;
    const height = canvas.height;
    const padding = { top: 30, right: 20, bottom: 40, left: 60 };
    const chartWidth = width - padding.left - padding.right;
    const chartHeight = height - padding.top - padding.bottom;

    // Clear canvas
    ctx.clearRect(0, 0, width, height);

    // Use the first record's mean and SD for reference lines
    const mean = records[0].mean;
    const sd = records[0].standardDeviation;

    if (sd === 0) return;

    // Y-axis range: mean ± 4 SD
    const yMin = mean - 4 * sd;
    const yMax = mean + 4 * sd;

    const toCanvasX = (i: number) =>
      padding.left + (i / Math.max(records.length - 1, 1)) * chartWidth;
    const toCanvasY = (val: number) =>
      padding.top + ((yMax - val) / (yMax - yMin)) * chartHeight;

    // Draw ±1SD, ±2SD, ±3SD lines
    const sdLines = [
      { offset: 3, color: '#ef5350', label: '+3SD' },
      { offset: 2, color: '#ff9800', label: '+2SD' },
      { offset: 1, color: '#66bb6a', label: '+1SD' },
      { offset: 0, color: '#1565c0', label: 'Mean' },
      { offset: -1, color: '#66bb6a', label: '-1SD' },
      { offset: -2, color: '#ff9800', label: '-2SD' },
      { offset: -3, color: '#ef5350', label: '-3SD' },
    ];

    for (const line of sdLines) {
      const y = toCanvasY(mean + line.offset * sd);
      ctx.beginPath();
      ctx.setLineDash(line.offset === 0 ? [] : [5, 5]);
      ctx.strokeStyle = line.color;
      ctx.lineWidth = line.offset === 0 ? 2 : 1;
      ctx.moveTo(padding.left, y);
      ctx.lineTo(width - padding.right, y);
      ctx.stroke();
      ctx.setLineDash([]);

      // Labels
      ctx.fillStyle = line.color;
      ctx.font = '10px sans-serif';
      ctx.textAlign = 'right';
      ctx.fillText(line.label, padding.left - 5, y + 4);
    }

    // Plot data points and connecting lines
    if (records.length > 1) {
      ctx.beginPath();
      ctx.strokeStyle = '#1565c0';
      ctx.lineWidth = 1.5;
      for (let i = 0; i < records.length; i++) {
        const x = toCanvasX(i);
        const y = toCanvasY(records[i].observedValue);
        if (i === 0) {
          ctx.moveTo(x, y);
        } else {
          ctx.lineTo(x, y);
        }
      }
      ctx.stroke();
    }

    // Draw points
    for (let i = 0; i < records.length; i++) {
      const x = toCanvasX(i);
      const y = toCanvasY(records[i].observedValue);
      const deviation = Math.abs(records[i].observedValue - mean) / sd;

      ctx.beginPath();
      ctx.arc(x, y, 4, 0, Math.PI * 2);
      ctx.fillStyle =
        deviation > 3 ? '#c62828' : deviation > 2 ? '#e65100' : '#1565c0';
      ctx.fill();
      ctx.strokeStyle = '#fff';
      ctx.lineWidth = 1;
      ctx.stroke();
    }

    // Title
    ctx.fillStyle = '#333';
    ctx.font = 'bold 12px sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText('Levey-Jennings Control Chart', width / 2, 16);
  }
}
