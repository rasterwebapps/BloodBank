import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { BaseChartDirective } from 'ng2-charts';
import {
  Chart,
  CategoryScale,
  LinearScale,
  BarElement,
  BarController,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import type { ChartData, ChartOptions } from 'chart.js';

import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { InventoryService } from '../services/inventory.service';
import {
  BloodStock,
  BLOOD_GROUP_OPTIONS,
  COMPONENT_TYPE_OPTIONS,
  ADEQUATE_THRESHOLD,
  LOW_THRESHOLD,
  getStockLevelClass,
  getBloodGroupLabel,
  getComponentTypeLabel,
} from '../models/inventory.model';

// Register Chart.js components once at module level
Chart.register(
  CategoryScale,
  LinearScale,
  BarElement,
  BarController,
  Title,
  Tooltip,
  Legend,
);

interface StockCell {
  key: string;
  bloodGroupId: string;
  componentType: string;
  bloodGroupLabel: string;
  componentLabel: string;
  count: number;
}

/**
 * Inventory stock dashboard showing blood group × component type matrix
 * and a bar chart of units by blood group.
 */
@Component({
  selector: 'app-stock-dashboard',
  standalone: true,
  imports: [
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    BaseChartDirective,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './stock-dashboard.component.html',
  styleUrl: './stock-dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StockDashboardComponent implements OnInit {
  private readonly inventoryService = inject(InventoryService);

  // ── State ──────────────────────────────────────────────────────
  readonly stockLevels = signal<BloodStock[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  // ── Static options ─────────────────────────────────────────────
  readonly bloodGroupOptions = BLOOD_GROUP_OPTIONS;
  readonly componentTypeOptions = COMPONENT_TYPE_OPTIONS;

  // ── Computed ───────────────────────────────────────────────────
  readonly isEmpty = computed(
    () => this.stockLevels().length === 0 && !this.loading() && !this.error(),
  );

  readonly stockCells = computed<StockCell[]>(() => {
    const stocks = this.stockLevels();
    const cells: StockCell[] = [];
    for (const bg of BLOOD_GROUP_OPTIONS) {
      for (const ct of COMPONENT_TYPE_OPTIONS) {
        const found = stocks.find(
          (s) => s.bloodGroupId === bg.value && s.componentType === ct.value,
        );
        cells.push({
          key: `${bg.value}-${ct.value}`,
          bloodGroupId: bg.value,
          componentType: ct.value,
          bloodGroupLabel: bg.label,
          componentLabel: ct.label,
          count: found?.count ?? 0,
        });
      }
    }
    return cells;
  });

  readonly stockByBloodGroup = computed<Record<string, number>>(() => {
    const result: Record<string, number> = {};
    for (const bg of BLOOD_GROUP_OPTIONS) {
      result[bg.label] = this.stockLevels()
        .filter((s) => s.bloodGroupId === bg.value)
        .reduce((sum, s) => sum + s.count, 0);
    }
    return result;
  });

  readonly chartData = computed<ChartData<'bar'>>(() => {
    const byGroup = this.stockByBloodGroup();
    const values = Object.values(byGroup);
    return {
      labels: Object.keys(byGroup),
      datasets: [
        {
          label: 'Units Available',
          data: values,
          backgroundColor: [
            '#ef9a9a',
            '#ef5350',
            '#f48fb1',
            '#f06292',
            '#90caf9',
            '#64b5f6',
            '#a5d6a7',
            '#66bb6a',
          ],
          borderRadius: 4,
        },
      ],
    };
  });

  readonly chartOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      title: {
        display: true,
        text: 'Blood Stock by Group',
        font: { size: 14, weight: 'bold' },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        title: { display: true, text: 'Units' },
        ticks: { stepSize: 1 },
      },
    },
  };

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    void this.loadStockLevels();
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadStockLevels(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const data = await this.inventoryService.getStockLevels();
      this.stockLevels.set(data);
    } catch {
      this.error.set('Failed to load stock levels. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  // ── Helpers ────────────────────────────────────────────────────

  getStockClass(count: number): string {
    return getStockLevelClass(count);
  }

  getBloodGroupLabel(value: string): string {
    return getBloodGroupLabel(value);
  }

  getComponentTypeLabel(value: string): string {
    return getComponentTypeLabel(value);
  }

  getCriticalCount(): number {
    return this.stockCells().filter((c) => c.count < LOW_THRESHOLD).length;
  }

  getLowCount(): number {
    return this.stockCells().filter(
      (c) => c.count >= LOW_THRESHOLD && c.count < ADEQUATE_THRESHOLD,
    ).length;
  }

  getAdequateCount(): number {
    return this.stockCells().filter((c) => c.count >= ADEQUATE_THRESHOLD).length;
  }

  getCellForBloodGroupAndComponent(
    bloodGroupId: string,
    componentType: string,
  ): StockCell | undefined {
    return this.stockCells().find(
      (c) =>
        c.bloodGroupId === bloodGroupId && c.componentType === componentType,
    );
  }
}
