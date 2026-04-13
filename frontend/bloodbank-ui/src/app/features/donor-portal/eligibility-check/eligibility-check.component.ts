import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
} from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { DonorPortalService } from '../services/donor-portal.service';
import { EligibilityResult } from '../models/donor-portal.models';

@Component({
  selector: 'app-eligibility-check',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatRadioModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
  ],
  templateUrl: './eligibility-check.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EligibilityCheckComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(DonorPortalService);

  readonly loading = signal(false);
  readonly result = signal<EligibilityResult | null>(null);
  readonly error = signal<string | null>(null);
  readonly maxDate = new Date();

  readonly form = this.fb.nonNullable.group({
    weight: [null as number | null, [Validators.required, Validators.min(50)]],
    hemoglobin: [null as number | null, [Validators.required, Validators.min(7)]],
    recentTravel: [false, Validators.required],
    medications: [false, Validators.required],
    lastDonationDate: [null as Date | null],
  });

  async check(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.result.set(null);
    this.error.set(null);
    try {
      const v = this.form.getRawValue();
      const result = await this.service.checkEligibility({
        weight: v.weight ?? 0,
        hemoglobin: v.hemoglobin ?? 0,
        recentTravel: v.recentTravel,
        medications: v.medications,
        lastDonationDate: v.lastDonationDate
          ? (v.lastDonationDate as Date).toISOString().split('T')[0]
          : null,
      });
      this.result.set(result);
    } catch {
      this.error.set('Failed to check eligibility. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  reset(): void {
    this.result.set(null);
    this.form.reset();
  }
}
