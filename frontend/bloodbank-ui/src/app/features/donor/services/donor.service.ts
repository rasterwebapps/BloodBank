import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import {
  Donor,
  DonorCreateRequest,
  DonorUpdateRequest,
  DonorHealthRecord,
  DonorConsent,
  DonorDeferral,
  DonorLoyalty,
  DonorStatusEnum,
} from '../models/donor.model';

/**
 * Service for donor CRUD and related operations.
 */
@Injectable({ providedIn: 'root' })
export class DonorService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/donors`;

  /** List donors with pagination and optional search query. */
  async list(
    page = 0,
    size = 10,
    query?: string,
  ): Promise<PagedResponse<Donor>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (query) {
      params = params.set('query', query);
    }
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Donor>>>(
        `${this.baseUrl}/search`,
        { params },
      ),
    );
    return response.data;
  }

  /** Get a single donor by ID. */
  async getById(id: string): Promise<Donor> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<Donor>>(`${this.baseUrl}/${id}`),
    );
    return response.data;
  }

  /** Get a single donor by donor number. */
  async getByDonorNumber(donorNumber: string): Promise<Donor> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<Donor>>(
        `${this.baseUrl}/number/${donorNumber}`,
      ),
    );
    return response.data;
  }

  /** Register a new donor. */
  async create(request: DonorCreateRequest): Promise<Donor> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<Donor>>(this.baseUrl, request),
    );
    return response.data;
  }

  /** Update an existing donor. */
  async update(id: string, request: DonorUpdateRequest): Promise<Donor> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<Donor>>(`${this.baseUrl}/${id}`, request),
    );
    return response.data;
  }

  /** List donors by status with pagination. */
  async getByStatus(
    status: DonorStatusEnum,
    page = 0,
    size = 10,
  ): Promise<PagedResponse<Donor>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Donor>>>(
        `${this.baseUrl}/status/${status}`,
        { params },
      ),
    );
    return response.data;
  }

  /** Search donors with advanced filters. */
  async search(
    filters: Record<string, string>,
    page = 0,
    size = 10,
  ): Promise<PagedResponse<Donor>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    for (const [key, value] of Object.entries(filters)) {
      if (value) {
        params = params.set(key, value);
      }
    }
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Donor>>>(
        `${this.baseUrl}/search`,
        { params },
      ),
    );
    return response.data;
  }

  /** Get health records for a donor. */
  async getHealthRecords(donorId: string): Promise<DonorHealthRecord[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<DonorHealthRecord[]>>(
        `${this.baseUrl}/${donorId}/health-records`,
      ),
    );
    return response.data;
  }

  /** Get consents for a donor. */
  async getConsents(donorId: string): Promise<DonorConsent[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<DonorConsent[]>>(
        `${this.baseUrl}/${donorId}/consents`,
      ),
    );
    return response.data;
  }

  /** Check donor eligibility. */
  async checkEligibility(donorId: string): Promise<DonorHealthRecord> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<DonorHealthRecord>>(
        `${this.baseUrl}/${donorId}/eligibility`,
      ),
    );
    return response.data;
  }
}
