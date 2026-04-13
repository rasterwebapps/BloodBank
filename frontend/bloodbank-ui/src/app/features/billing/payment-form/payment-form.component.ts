import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar } from '@angular/material/snack-bar';

import { BillingService } from '../services/billing.service';
import { Invoice, PaymentMethodEnum, PAYMENT_METHOD_OPTIONS } from '../models/billing.model';

/**
 * Payment form — record a payment against an invoice.
 */
@Component({
  selector: 'app-payment-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
  ],
  templateUrl: './payment-form.component.html',
  styleUrl: './payment-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaymentFormComponent implements OnInit {
  private readonly billingService = inject(BillingService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly invoice = signal<Invoice | null>(null);
  readonly submitting = signal(false);
  readonly invoiceId = signal('');

  readonly paymentMethods = PAYMENT_METHOD_OPTIONS;

  readonly form = this.fb.nonNullable.group({
    paymentDate: [new Date().toISOString().split('T')[0], Validators.required],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    method: [PaymentMethodEnum.CASH as string, Validators.required],
    referenceNumber: [''],
    notes: [''],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.invoiceId.set(id);
      void this.loadInvoice(id);
    }
  }

  async loadInvoice(id: string): Promise<void> {
    try {
      const invoice = await this.billingService.getInvoiceById(id);
      this.invoice.set(invoice);
      this.form.patchValue({ amount: invoice.balanceAmount });
    } catch {
      this.snackBar.open('Failed to load invoice', 'Dismiss', { duration: 3000 });
    }
  }

  async submit(): Promise<void> {
    if (this.form.invalid) return;
    this.submitting.set(true);
    try {
      const value = this.form.getRawValue();
      await this.billingService.recordPayment({
        invoiceId: this.invoiceId(),
        paymentDate: value.paymentDate,
        amount: value.amount,
        method: value.method as PaymentMethodEnum,
        referenceNumber: value.referenceNumber || undefined,
        notes: value.notes || undefined,
      });
      this.snackBar.open('Payment recorded successfully', 'Dismiss', { duration: 3000 });
      void this.router.navigate(['/staff/billing', this.invoiceId()]);
    } catch {
      this.snackBar.open('Failed to record payment', 'Dismiss', { duration: 3000 });
    } finally {
      this.submitting.set(false);
    }
  }

  cancel(): void {
    void this.router.navigate(['/staff/billing', this.invoiceId()]);
  }
}
