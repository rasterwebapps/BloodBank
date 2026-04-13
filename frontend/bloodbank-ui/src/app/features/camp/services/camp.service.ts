import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import {
  Camp,
  CampCreateRequest,
  CampDonorRegistration,
  CampDonorRegistrationRequest,
  CampResource,
} from '../models/camp.model';

/**
 * Service for blood camp CRUD and related operations.
 */
@Injectable({ providedIn: 'root' })
export class CampService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/camps`;

  /** List camps with pagination and optional status filter. */
  async list(
    page = 0,
    size = 10,
    status?: string,
  ): Promise<PagedResponse<Camp>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) {
      params = params.set('status', status);
    }
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Camp>>>(this.baseUrl, {
        params,
      }),
    );
    return response.data;
  }

  /** Get a single camp by ID. */
  async getById(id: string): Promise<Camp> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<Camp>>(`${this.baseUrl}/${id}`),
    );
    return response.data;
  }

  /** Create a new camp. */
  async create(request: CampCreateRequest): Promise<Camp> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<Camp>>(this.baseUrl, request),
    );
    return response.data;
  }

  /** Update an existing camp. */
  async update(id: string, request: CampCreateRequest): Promise<Camp> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<Camp>>(`${this.baseUrl}/${id}`, request),
    );
    return response.data;
  }

  /** Update camp status. */
  async updateStatus(id: string, status: string): Promise<Camp> {
    const response = await firstValueFrom(
      this.http.patch<ApiResponse<Camp>>(`${this.baseUrl}/${id}/status`, {
        status,
      }),
    );
    return response.data;
  }

  /** Get donor registrations for a camp. */
  async getDonorRegistrations(
    campId: string,
  ): Promise<CampDonorRegistration[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<CampDonorRegistration[]>>(
        `${this.baseUrl}/${campId}/donors`,
      ),
    );
    return response.data;
  }

  /** Get resources for a camp. */
  async getResources(campId: string): Promise<CampResource[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<CampResource[]>>(
        `${this.baseUrl}/${campId}/resources`,
      ),
    );
    return response.data;
  }

  /** Register a donor at a camp. */
  async registerDonor(
    campId: string,
    request: CampDonorRegistrationRequest,
  ): Promise<CampDonorRegistration> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<CampDonorRegistration>>(
        `${this.baseUrl}/${campId}/donors`,
        request,
      ),
    );
    return response.data;
  }
}
