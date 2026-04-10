import { Component, ChangeDetectionStrategy, input, computed } from '@angular/core';

/**
 * Colored badge for clinical/inventory statuses.
 *
 * Usage:
 * ```html
 * <app-status-badge [status]="unit.status" />
 * ```
 */
@Component({
  selector: 'app-status-badge',
  standalone: true,
  templateUrl: './status-badge.component.html',
  styleUrl: './status-badge.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatusBadgeComponent {
  /** Status value to display */
  readonly status = input.required<string>();

  /** CSS class based on status */
  readonly statusClass = computed(() => {
    const s = this.status().toUpperCase();
    return `status-badge status-${s.toLowerCase().replace(/_/g, '-')}`;
  });

  /** Human-readable label */
  readonly label = computed(() => {
    return this.status().replace(/_/g, ' ');
  });
}
