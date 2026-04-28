import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';

import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { BillingService } from '../services/billing.service';
import { Invoice, Payment, CreditNote } from '../models/billing.model';

/**
 * Invoice detail — shows line items, payments, and credit notes.
 */
@Component({
  selector: 'app-invoice-detail',
  standalone: true,
  imports: [
    DatePipe,
    CurrencyPipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatDividerModule,
    MatChipsModule,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './invoice-detail.component.html',
  styleUrl: './invoice-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InvoiceDetailComponent implements OnInit {
  private readonly billingService = inject(BillingService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly invoice = signal<Invoice | null>(null);
  readonly payments = signal<Payment[]>([]);
  readonly creditNotes = signal<CreditNote[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly lineItemColumns = ['description', 'quantity', 'unitPrice', 'totalPrice'];
  readonly paymentColumns = ['paymentDate', 'method', 'referenceNumber', 'amount', 'status'];
  readonly creditNoteColumns = ['creditNoteNumber', 'issuedDate', 'amount', 'reason'];

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) void this.loadInvoice(id);
  }

  async loadInvoice(id: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const [invoice, payments, creditNotes] = await Promise.all([
        this.billingService.getInvoiceById(id),
        this.billingService.getInvoicePayments(id),
        this.billingService.getInvoiceCreditNotes(id),
      ]);
      this.invoice.set(invoice);
      this.payments.set(payments);
      this.creditNotes.set(creditNotes);
    } catch {
      this.error.set('Failed to load invoice details.');
    } finally {
      this.loading.set(false);
    }
  }

  recordPayment(): void {
    const id = this.invoice()?.id;
    if (id) void this.router.navigate(['/staff/billing', id, 'payment']);
  }

  goBack(): void {
    void this.router.navigate(['/staff/billing']);
  }
}
