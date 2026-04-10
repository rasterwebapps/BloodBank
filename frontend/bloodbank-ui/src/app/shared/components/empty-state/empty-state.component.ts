import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

/**
 * Empty state display with icon, message, and optional action button.
 *
 * Usage:
 * ```html
 * <app-empty-state
 *   icon="people"
 *   message="No donors found"
 *   [actionLabel]="'Register Donor'"
 *   (action)="onRegister()" />
 * ```
 */
@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [MatIconModule, MatButtonModule],
  templateUrl: './empty-state.component.html',
  styleUrl: './empty-state.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmptyStateComponent {
  /** Material icon name */
  readonly icon = input('info');

  /** Main message to display */
  readonly message = input.required<string>();

  /** Optional action button label */
  readonly actionLabel = input<string | null>(null);

  /** Emitted when the action button is clicked */
  readonly action = output<void>();

  onAction(): void {
    this.action.emit();
  }
}
