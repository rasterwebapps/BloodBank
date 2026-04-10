import { Pipe, PipeTransform } from '@angular/core';
import { formatDistanceToNow } from 'date-fns';

/**
 * Transforms a date or ISO string into a relative time string.
 * Example: "2024-01-01T00:00:00Z" → "3 months ago"
 */
@Pipe({
  name: 'dateAgo',
  standalone: true,
})
export class DateAgoPipe implements PipeTransform {
  transform(value: string | Date | null | undefined): string {
    if (!value) {
      return '';
    }
    const date = typeof value === 'string' ? new Date(value) : value;
    if (isNaN(date.getTime())) {
      return '';
    }
    return formatDistanceToNow(date, { addSuffix: true });
  }
}
