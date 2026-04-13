import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import { Transfer, TransferRequest } from '../models/inventory.model';

/**
 * Service for inter-branch blood unit transfer operations.
 */
@Injectable({ providedIn: 'root' })
export class TransferService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/transfers`;

  /** Create a new inter-branch transfer. */
  async createTransfer(request: TransferRequest): Promise<Transfer> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<Transfer>>(this.baseUrl, request),
    );
    return response.data;
  }

  /** List transfers with pagination. */
  async listTransfers(page = 0, size = 10): Promise<PagedResponse<Transfer>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Transfer>>>(this.baseUrl, {
        params,
      }),
    );
    return response.data;
  }

  /** Get a single transfer by ID. */
  async getTransfer(id: string): Promise<Transfer> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<Transfer>>(`${this.baseUrl}/${id}`),
    );
    return response.data;
  }
}
