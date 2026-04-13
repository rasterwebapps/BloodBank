import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import {
  Invoice,
  Payment,
  CreditNote,
  RateMaster,
  PaymentCreateRequest,
  RateMasterCreateRequest,
  RateMasterUpdateRequest,
  InvoiceStatusEnum,
} from '../models/billing.model';

/**
 * Service for billing — invoices, payments, credit notes, rate management.
 */
@Injectable({ providedIn: 'root' })
export class BillingService {
  private readonly http = inject(HttpClient);
  private readonly invoiceUrl = `${environment.apiUrl}/api/v1/invoices`;
  private readonly paymentUrl = `${environment.apiUrl}/api/v1/payments`;
  private readonly rateUrl = `${environment.apiUrl}/api/v1/rates`;

  // ── Invoices ───────────────────────────────────────────────────

  async listInvoices(
    page = 0,
    size = 10,
    status?: InvoiceStatusEnum,
    hospitalId?: string,
  ): Promise<PagedResponse<Invoice>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) params = params.set('status', status);
    if (hospitalId) params = params.set('hospitalId', hospitalId);
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Invoice>>>(this.invoiceUrl, { params }),
    );
    return response.data;
  }

  async getInvoiceById(id: string): Promise<Invoice> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<Invoice>>(`${this.invoiceUrl}/${id}`),
    );
    return response.data;
  }

  async getInvoicePayments(invoiceId: string): Promise<Payment[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<Payment[]>>(`${this.invoiceUrl}/${invoiceId}/payments`),
    );
    return response.data;
  }

  async getInvoiceCreditNotes(invoiceId: string): Promise<CreditNote[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<CreditNote[]>>(`${this.invoiceUrl}/${invoiceId}/credit-notes`),
    );
    return response.data;
  }

  // ── Payments ───────────────────────────────────────────────────

  async recordPayment(request: PaymentCreateRequest): Promise<Payment> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<Payment>>(this.paymentUrl, request),
    );
    return response.data;
  }

  async listPayments(
    page = 0,
    size = 10,
    invoiceId?: string,
  ): Promise<PagedResponse<Payment>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (invoiceId) params = params.set('invoiceId', invoiceId);
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Payment>>>(this.paymentUrl, { params }),
    );
    return response.data;
  }

  // ── Rates ──────────────────────────────────────────────────────

  async listRates(
    page = 0,
    size = 20,
    activeOnly = false,
  ): Promise<PagedResponse<RateMaster>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (activeOnly) params = params.set('active', 'true');
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<RateMaster>>>(this.rateUrl, { params }),
    );
    return response.data;
  }

  async getRateById(id: string): Promise<RateMaster> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<RateMaster>>(`${this.rateUrl}/${id}`),
    );
    return response.data;
  }

  async createRate(request: RateMasterCreateRequest): Promise<RateMaster> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<RateMaster>>(this.rateUrl, request),
    );
    return response.data;
  }

  async updateRate(id: string, request: RateMasterUpdateRequest): Promise<RateMaster> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<RateMaster>>(`${this.rateUrl}/${id}`, request),
    );
    return response.data;
  }

  async deactivateRate(id: string): Promise<void> {
    await firstValueFrom(
      this.http.delete<ApiResponse<void>>(`${this.rateUrl}/${id}`),
    );
  }
}
