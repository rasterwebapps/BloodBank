import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import {
  Collection,
  CollectionCreateRequest,
  AdverseReaction,
  AdverseReactionCreateRequest,
  CollectionSample,
  SampleRegistrationRequest,
} from '../models/collection.model';

/**
 * Service for blood collection CRUD and related operations.
 */
@Injectable({ providedIn: 'root' })
export class CollectionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/collections`;

  /** List collections with pagination. */
  async list(page = 0, size = 10): Promise<PagedResponse<Collection>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Collection>>>(this.baseUrl, {
        params,
      }),
    );
    return response.data;
  }

  /** Get today's collections. */
  async getTodayCollections(
    page = 0,
    size = 20,
    query?: string,
  ): Promise<PagedResponse<Collection>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('date', 'today');
    if (query) {
      params = params.set('query', query);
    }
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Collection>>>(this.baseUrl, {
        params,
      }),
    );
    return response.data;
  }

  /** Get a single collection by ID. */
  async getById(id: string): Promise<Collection> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<Collection>>(`${this.baseUrl}/${id}`),
    );
    return response.data;
  }

  /** Record a new blood collection. */
  async create(request: CollectionCreateRequest): Promise<Collection> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<Collection>>(this.baseUrl, request),
    );
    return response.data;
  }

  /** Update collection status. */
  async updateStatus(id: string, status: string): Promise<Collection> {
    const response = await firstValueFrom(
      this.http.patch<ApiResponse<Collection>>(
        `${this.baseUrl}/${id}/status`,
        { status },
      ),
    );
    return response.data;
  }

  /** Report an adverse reaction. */
  async reportAdverseReaction(
    request: AdverseReactionCreateRequest,
  ): Promise<AdverseReaction> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<AdverseReaction>>(
        `${this.baseUrl}/adverse-reactions`,
        request,
      ),
    );
    return response.data;
  }

  /** Register a sample from a collection. */
  async registerSample(
    request: SampleRegistrationRequest,
  ): Promise<CollectionSample> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<CollectionSample>>(
        `${this.baseUrl}/samples`,
        request,
      ),
    );
    return response.data;
  }
}
