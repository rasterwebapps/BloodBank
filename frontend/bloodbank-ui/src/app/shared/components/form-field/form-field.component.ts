import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

/**
 * Standard mat-form-field wrapper with validation error display.
 *
 * Usage:
 * ```html
 * <app-form-field
 *   label="First Name"
 *   [control]="form.controls.firstName"
 *   [errorMessages]="{ required: 'First name is required', minlength: 'Min 2 chars' }" />
 * ```
 */
@Component({
  selector: 'app-form-field',
  standalone: true,
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule],
  templateUrl: './form-field.component.html',
  styleUrl: './form-field.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FormFieldComponent {
  /** Form control to bind */
  readonly control = input.required<FormControl>();

  /** Label text */
  readonly label = input.required<string>();

  /** Input type */
  readonly type = input('text');

  /** Placeholder text */
  readonly placeholder = input('');

  /** Hint text shown below the field */
  readonly hint = input('');

  /** Map of error keys to messages */
  readonly errorMessages = input<Record<string, string>>({});

  /** Whether the field is required */
  readonly required = input(false);

  get errorMessage(): string {
    const ctrl = this.control();
    const messages = this.errorMessages();
    if (ctrl && ctrl.errors && (ctrl.touched || ctrl.dirty)) {
      for (const key of Object.keys(ctrl.errors)) {
        if (messages[key]) {
          return messages[key];
        }
      }
      // Default messages
      if (ctrl.errors['required']) {
        return `${this.label()} is required`;
      }
      if (ctrl.errors['email']) {
        return 'Please enter a valid email';
      }
      if (ctrl.errors['minlength']) {
        const min = ctrl.errors['minlength'].requiredLength;
        return `Minimum ${min} characters`;
      }
      if (ctrl.errors['maxlength']) {
        const max = ctrl.errors['maxlength'].requiredLength;
        return `Maximum ${max} characters`;
      }
    }
    return '';
  }
}
