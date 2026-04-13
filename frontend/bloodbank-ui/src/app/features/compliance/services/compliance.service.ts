import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import {
  RegulatoryFramework,
  SopDocument,
  License,
  Deviation,
  RecallRecord,
  DeviationCreateRequest,
  DeviationUpdateRequest,
} from '../models/compliance.model';

/**
 * Service for compliance — frameworks, SOPs, licenses, deviations, recalls.
 */
@Injectable({ providedIn: 'root' })
export class ComplianceService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/compliance`;

  // ── Frameworks ─────────────────────────────────────────────────

  async listFrameworks(page = 0, size = 10): Promise<PagedResponse<RegulatoryFramework>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<RegulatoryFramework>>>(
        `${this.baseUrl}/frameworks`,
        { params },
      ),
    );
    return response.data;
  }

  async getFrameworkById(id: string): Promise<RegulatoryFramework> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<RegulatoryFramework>>(`${this.baseUrl}/frameworks/${id}`),
    );
    return response.data;
  }

  // ── SOPs ───────────────────────────────────────────────────────

  async listSops(page = 0, size = 10): Promise<PagedResponse<SopDocument>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<SopDocument>>>(
        `${this.baseUrl}/sops`,
        { params },
      ),
    );
    return response.data;
  }

  async getSopById(id: string): Promise<SopDocument> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<SopDocument>>(`${this.baseUrl}/sops/${id}`),
    );
    return response.data;
  }

  // ── Licenses ───────────────────────────────────────────────────

  async listLicenses(page = 0, size = 10): Promise<PagedResponse<License>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<License>>>(
        `${this.baseUrl}/licenses`,
        { params },
      ),
    );
    return response.data;
  }

  async getLicenseById(id: string): Promise<License> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<License>>(`${this.baseUrl}/licenses/${id}`),
    );
    return response.data;
  }

  // ── Deviations ─────────────────────────────────────────────────

  async listDeviations(page = 0, size = 10): Promise<PagedResponse<Deviation>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Deviation>>>(
        `${this.baseUrl}/deviations`,
        { params },
      ),
    );
    return response.data;
  }

  async createDeviation(request: DeviationCreateRequest): Promise<Deviation> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<Deviation>>(`${this.baseUrl}/deviations`, request),
    );
    return response.data;
  }

  async updateDeviation(id: string, request: DeviationUpdateRequest): Promise<Deviation> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<Deviation>>(`${this.baseUrl}/deviations/${id}`, request),
    );
    return response.data;
  }

  // ── Recalls ────────────────────────────────────────────────────

  async listRecalls(page = 0, size = 10): Promise<PagedResponse<RecallRecord>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<RecallRecord>>>(
        `${this.baseUrl}/recalls`,
        { params },
      ),
    );
    return response.data;
  }

  async getRecallById(id: string): Promise<RecallRecord> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<RecallRecord>>(`${this.baseUrl}/recalls/${id}`),
    );
    return response.data;
  }
}
