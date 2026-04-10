import { Pipe, PipeTransform } from '@angular/core';

/**
 * Truncates long text with an ellipsis.
 * Example: "Very long text here" | truncate:10 → "Very long…"
 */
@Pipe({
  name: 'truncate',
  standalone: true,
})
export class TruncatePipe implements PipeTransform {
  transform(value: string | null | undefined, limit = 50, trail = '…'): string {
    if (!value) {
      return '';
    }
    if (value.length <= limit) {
      return value;
    }
    return value.substring(0, limit).trimEnd() + trail;
  }
}
