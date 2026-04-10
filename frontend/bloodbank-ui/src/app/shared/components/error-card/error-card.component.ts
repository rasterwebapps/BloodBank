import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

/**
 * Error display card with retry button.
 *
 * Usage:
 * ```html
 * <app-error-card
 *   [message]="error()"
 *   (retry)="loadData()" />
 * ```
 */
@Component({
  selector: 'app-error-card',
  standalone: true,
  imports: [MatCardModule, MatIconModule, MatButtonModule],
  templateUrl: './error-card.component.html',
  styleUrl: './error-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ErrorCardComponent {
  /** Error message to display */
  readonly message = input('An unexpected error occurred. Please try again.');

  /** Emitted when the retry button is clicked */
  readonly retry = output<void>();

  onRetry(): void {
    this.retry.emit();
  }
}
