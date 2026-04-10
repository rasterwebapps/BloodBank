import {
  Component,
  ChangeDetectionStrategy,
  input,
  computed,
} from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

/**
 * Reusable KPI metric card used on the dashboard.
 *
 * Usage:
 * ```html
 * <app-kpi-card
 *   label="Donors Today"
 *   [value]="kpis().totalDonorsToday"
 *   trend="+5%"
 *   icon="people"
 *   colorClass="bg-blue-50 text-blue-700" />
 * ```
 */
@Component({
  selector: 'app-kpi-card',
  standalone: true,
  imports: [MatCardModule, MatIconModule],
  templateUrl: './kpi-card.component.html',
  styleUrl: './kpi-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KpiCardComponent {
  /** Label describing the metric */
  readonly label = input.required<string>();

  /** Numeric or string value to display */
  readonly value = input.required<string | number>();

  /** Trend indicator text, e.g. "+5%" or "-2%" */
  readonly trend = input<string>('');

  /** Material icon name */
  readonly icon = input<string>('analytics');

  /** Tailwind background + text color classes for the icon circle */
  readonly colorClass = input<string>('bg-blue-50 text-blue-700');

  /** Whether the trend is positive (starts with '+') */
  readonly isPositiveTrend = computed(() => {
    const t = this.trend();
    if (!t) return false;
    return t.startsWith('+');
  });

  /** Whether the trend is negative (starts with '-') */
  readonly isNegativeTrend = computed(() => {
    const t = this.trend();
    if (!t) return false;
    return t.startsWith('-');
  });
}
