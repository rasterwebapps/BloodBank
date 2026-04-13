import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import { BloodUnit, BloodStock, ComponentType } from '../models/inventory.model';

/**
 * Service for blood unit and stock level operations.
 */
@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1`;

  /** List blood units with pagination and optional filters. */
  async listBloodUnits(
    page = 0,
    size = 10,
    filters: Record<string, string> = {},
  ): Promise<PagedResponse<BloodUnit>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    for (const [key, value] of Object.entries(filters)) {
      if (value) {
        params = params.set(key, value);
      }
    }
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<BloodUnit>>>(
        `${this.baseUrl}/blood-units`,
        { params },
      ),
    );
    return response.data;
  }

  /** Get a single blood unit by ID. */
  async getBloodUnit(id: string): Promise<BloodUnit> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<BloodUnit>>(
        `${this.baseUrl}/blood-units/${id}`,
      ),
    );
    return response.data;
  }

  /** Get current stock levels across all blood groups and component types. */
  async getStockLevels(): Promise<BloodStock[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<BloodStock[]>>(
        `${this.baseUrl}/inventory/stock-levels`,
      ),
    );
    return response.data;
  }

  /** Get available component types. */
  async getComponentTypes(): Promise<ComponentType[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<ComponentType[]>>(
        `${this.baseUrl}/inventory/component-types`,
      ),
    );
    return response.data;
  }
}
