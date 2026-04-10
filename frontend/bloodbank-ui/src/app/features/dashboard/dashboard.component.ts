import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { BaseChartDirective } from 'ng2-charts';
import {
  Chart,
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  DoughnutController,
  ArcElement,
  LineController,
  LineElement,
  PointElement,
  Filler,
  Tooltip,
  Legend,
} from 'chart.js';
import { ChartConfiguration } from 'chart.js';

import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { DashboardService } from './services/dashboard.service';
import {
  KpiData,
  StockLevel,
  CollectionStats,
  DonationTrend,
} from './models/dashboard.model';
import { KpiCardComponent } from './components/kpi-card/kpi-card.component';

// Register Chart.js components globally
Chart.register(
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  DoughnutController,
  ArcElement,
  LineController,
  LineElement,
  PointElement,
  Filler,
  Tooltip,
  Legend,
);

/**
 * Main dashboard view showing KPIs and analytics charts.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    BaseChartDirective,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    KpiCardComponent,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);

  // ── State signals ──────────────────────────────────────────────
  readonly kpis = signal<KpiData | null>(null);
  readonly stockLevels = signal<StockLevel[]>([]);
  readonly collectionStats = signal<CollectionStats | null>(null);
  readonly donationTrends = signal<DonationTrend[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  // ── Computed chart data ────────────────────────────────────────

  /** Bar chart: blood stock levels by group */
  readonly stockChartData = computed<ChartConfiguration<'bar'>['data']>(() => {
    const levels = this.stockLevels();
    return {
      labels: levels.map((s) => s.bloodGroup),
      datasets: [
        {
          label: 'Available',
          data: levels.map((s) => s.available),
          backgroundColor: '#3b82f6', // blue-500
        },
        {
          label: 'Reserved',
          data: levels.map((s) => s.reserved),
          backgroundColor: '#f59e0b', // amber-500
        },
      ],
    };
  });

  readonly stockChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom' },
    },
    scales: {
      y: { beginAtZero: true },
    },
  };

  /** Doughnut chart: collection status breakdown */
  readonly collectionChartData = computed<
    ChartConfiguration<'doughnut'>['data']
  >(() => {
    const stats = this.collectionStats();
    return {
      labels: ['Completed', 'In Progress', 'Scheduled', 'Cancelled'],
      datasets: [
        {
          data: [
            stats?.completed ?? 0,
            stats?.inProgress ?? 0,
            stats?.scheduled ?? 0,
            stats?.cancelled ?? 0,
          ],
          backgroundColor: [
            '#16a34a', // green-600
            '#3b82f6', // blue-500
            '#f59e0b', // amber-500
            '#dc2626', // red-600
          ],
        },
      ],
    };
  });

  readonly collectionChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom' },
    },
  };

  /** Line chart: donation trends over 30 days */
  readonly trendChartData = computed<ChartConfiguration<'line'>['data']>(
    () => {
      const trends = this.donationTrends();
      return {
        labels: trends.map((t) => t.date),
        datasets: [
          {
            label: 'Donations',
            data: trends.map((t) => t.count),
            borderColor: '#dc2626', // red-600
            backgroundColor: 'rgba(220, 38, 38, 0.1)',
            fill: true,
            tension: 0.3,
          },
        ],
      };
    },
  );

  readonly trendChartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom' },
    },
    scales: {
      y: { beginAtZero: true },
    },
  };

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    void this.loadDashboard();
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadDashboard(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const [kpis, stockLevels, collectionStats, donationTrends] =
        await Promise.all([
          this.dashboardService.getKpis(),
          this.dashboardService.getStockLevels(),
          this.dashboardService.getCollectionStats(),
          this.dashboardService.getDonationTrends(),
        ]);

      this.kpis.set(kpis);
      this.stockLevels.set(stockLevels);
      this.collectionStats.set(collectionStats);
      this.donationTrends.set(donationTrends);
    } catch {
      this.error.set(
        'Failed to load dashboard data. Please try again.',
      );
    } finally {
      this.loading.set(false);
    }
  }
}
