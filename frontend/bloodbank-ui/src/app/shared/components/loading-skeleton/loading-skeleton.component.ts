import { Component, ChangeDetectionStrategy, input, computed } from '@angular/core';

/**
 * Animated pulse placeholder rows for loading states.
 *
 * Usage:
 * ```html
 * <app-loading-skeleton [rows]="5" [columns]="3" />
 * ```
 */
@Component({
  selector: 'app-loading-skeleton',
  standalone: true,
  templateUrl: './loading-skeleton.component.html',
  styleUrl: './loading-skeleton.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoadingSkeletonComponent {
  /** Number of skeleton rows */
  readonly rows = input(5);

  /** Number of columns per row */
  readonly columns = input(3);

  /** Height of each row in pixels */
  readonly rowHeight = input(20);

  readonly rowArray = computed(() =>
    Array.from({ length: this.rows() }, (_, i) => i),
  );

  readonly columnArray = computed(() =>
    Array.from({ length: this.columns() }, (_, i) => i),
  );
}
