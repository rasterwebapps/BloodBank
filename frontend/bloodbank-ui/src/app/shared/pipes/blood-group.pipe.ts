import { Pipe, PipeTransform } from '@angular/core';

/**
 * Transforms blood group enum values to human-readable display format.
 * Example: A_POSITIVE → "A+", O_NEGATIVE → "O−"
 */
@Pipe({
  name: 'bloodGroup',
  standalone: true,
})
export class BloodGroupPipe implements PipeTransform {
  private static readonly displayMap: Record<string, string> = {
    A_POSITIVE: 'A+',
    A_NEGATIVE: 'A−',
    B_POSITIVE: 'B+',
    B_NEGATIVE: 'B−',
    AB_POSITIVE: 'AB+',
    AB_NEGATIVE: 'AB−',
    O_POSITIVE: 'O+',
    O_NEGATIVE: 'O−',
  };

  transform(value: string | null | undefined): string {
    if (!value) {
      return '';
    }
    return BloodGroupPipe.displayMap[value] ?? value;
  }
}
