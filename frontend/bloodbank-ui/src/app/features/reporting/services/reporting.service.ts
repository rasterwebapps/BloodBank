import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import {
  AuditLog,
  Report,
  ChainOfCustody,
  ScheduledReport,
  ReportGenerateRequest,
  AuditLogFilterRequest,
  ScheduledReportCreateRequest,
} from '../models/reporting.model';

/**
 * Service for reporting — audit logs, report builder, chain of custody, scheduled reports.
 */
@Injectable({ providedIn: 'root' })
export class ReportingService {
  private readonly http = inject(HttpClient);
  private readonly auditUrl = `${environment.apiUrl}/api/v1/audit-logs`;
  private readonly reportUrl = `${environment.apiUrl}/api/v1/reports`;
  private readonly custodyUrl = `${environment.apiUrl}/api/v1/chain-of-custody`;

  // ── Audit Logs ─────────────────────────────────────────────────

  async listAuditLogs(
    page = 0,
    size = 20,
    filters?: AuditLogFilterRequest,
  ): Promise<PagedResponse<AuditLog>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (filters) {
      for (const [key, value] of Object.entries(filters)) {
        if (value) params = params.set(key, value as string);
      }
    }
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<AuditLog>>>(this.auditUrl, { params }),
    );
    return response.data;
  }

  // ── Reports ────────────────────────────────────────────────────

  async listReports(page = 0, size = 10): Promise<PagedResponse<Report>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Report>>>(this.reportUrl, { params }),
    );
    return response.data;
  }

  async generateReport(request: ReportGenerateRequest): Promise<Report> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<Report>>(this.reportUrl, request),
    );
    return response.data;
  }

  async downloadReport(id: string): Promise<Blob> {
    return firstValueFrom(
      this.http.get(`${this.reportUrl}/${id}/download`, { responseType: 'blob' }),
    );
  }

  // ── Chain of Custody ───────────────────────────────────────────

  async getChainOfCustody(bloodUnitId: string): Promise<ChainOfCustody> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<ChainOfCustody>>(`${this.custodyUrl}/${bloodUnitId}`),
    );
    return response.data;
  }

  async searchChainOfCustody(
    unitNumber: string,
    page = 0,
    size = 10,
  ): Promise<PagedResponse<ChainOfCustody>> {
    const params = new HttpParams()
      .set('unitNumber', unitNumber)
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<ChainOfCustody>>>(this.custodyUrl, { params }),
    );
    return response.data;
  }

  // ── Scheduled Reports ──────────────────────────────────────────

  async listScheduledReports(page = 0, size = 10): Promise<PagedResponse<ScheduledReport>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<ScheduledReport>>>(
        `${this.reportUrl}/scheduled`,
        { params },
      ),
    );
    return response.data;
  }

  async createScheduledReport(request: ScheduledReportCreateRequest): Promise<ScheduledReport> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<ScheduledReport>>(`${this.reportUrl}/scheduled`, request),
    );
    return response.data;
  }

  async toggleScheduledReport(id: string, active: boolean): Promise<ScheduledReport> {
    const response = await firstValueFrom(
      this.http.patch<ApiResponse<ScheduledReport>>(
        `${this.reportUrl}/scheduled/${id}`,
        { isActive: active },
      ),
    );
    return response.data;
  }

  async deleteScheduledReport(id: string): Promise<void> {
    await firstValueFrom(
      this.http.delete<ApiResponse<void>>(`${this.reportUrl}/scheduled/${id}`),
    );
  }
}
