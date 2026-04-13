import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { PageEvent } from '@angular/material/paginator';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { BillingService } from '../services/billing.service';
import { Invoice, InvoiceStatusEnum, INVOICE_STATUS_OPTIONS } from '../models/billing.model';

/**
 * Invoice list — displays all invoices with status filter and pagination.
 */
@Component({
  selector: 'app-invoice-list',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    CurrencyPipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatChipsModule,
    MatSelectModule,
    MatFormFieldModule,
    DataTableComponent,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './invoice-list.component.html',
  styleUrl: './invoice-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InvoiceListComponent implements OnInit {
  private readonly billingService = inject(BillingService);
  private readonly router = inject(Router);

  readonly invoices = signal<Invoice[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);
  readonly statusFilter = signal<InvoiceStatusEnum | undefined>(undefined);

  readonly isEmpty = computed(
    () => this.invoices().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns = [
    'invoiceNumber',
    'hospitalName',
    'issuedDate',
    'dueDate',
    'totalAmount',
    'balanceAmount',
    'status',
    'actions',
  ];

  readonly statusOptions = INVOICE_STATUS_OPTIONS;

  ngOnInit(): void {
    void this.loadInvoices();
  }

  async loadInvoices(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.billingService.listInvoices(
        this.currentPage(),
        this.pageSize(),
        this.statusFilter(),
      );
      this.invoices.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load invoices. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  onStatusChange(status: InvoiceStatusEnum | ''): void {
    this.statusFilter.set(status === '' ? undefined : status);
    this.currentPage.set(0);
    void this.loadInvoices();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadInvoices();
  }

  viewInvoice(invoice: Invoice): void {
    void this.router.navigate(['/staff/billing', invoice.id]);
  }

  recordPayment(invoice: Invoice): void {
    void this.router.navigate(['/staff/billing', invoice.id, 'payment']);
  }
}
