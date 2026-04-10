import { Component, ChangeDetectionStrategy, input, computed } from '@angular/core';
import { BloodGroupPipe } from '@shared/pipes/blood-group.pipe';

/**
 * Circular badge displaying blood group with distinct color.
 *
 * Usage:
 * ```html
 * <app-blood-group-badge [bloodGroup]="'A_POSITIVE'" />
 * ```
 */
@Component({
  selector: 'app-blood-group-badge',
  standalone: true,
  imports: [BloodGroupPipe],
  templateUrl: './blood-group-badge.component.html',
  styleUrl: './blood-group-badge.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BloodGroupBadgeComponent {
  /** Blood group enum value (e.g., A_POSITIVE) */
  readonly bloodGroup = input.required<string>();

  /** Size variant */
  readonly size = input<'sm' | 'md' | 'lg'>('md');

  /** CSS class for the blood group color */
  readonly colorClass = computed(() => {
    const bg = this.bloodGroup().toLowerCase().replace(/_/g, '-');
    return `blood-badge blood-badge-${bg} blood-badge-${this.size()}`;
  });
}
