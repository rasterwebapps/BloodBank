import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import {
  StorageLocation,
  BloodUnit,
  DisposalRequest,
  ComponentProcessingRequest,
} from '../models/inventory.model';

/**
 * Service for storage management, disposal, and component processing operations.
 */
@Injectable({ providedIn: 'root' })
export class LogisticsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1`;

  /** List all storage locations for the current branch. */
  async listStorageLocations(): Promise<StorageLocation[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<StorageLocation[]>>(
        `${this.baseUrl}/storage-locations`,
      ),
    );
    return response.data;
  }

  /** Create a new storage location. */
  async createStorageLocation(
    data: Partial<StorageLocation>,
  ): Promise<StorageLocation> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<StorageLocation>>(
        `${this.baseUrl}/storage-locations`,
        data,
      ),
    );
    return response.data;
  }

  /** Dispose blood units with a reason. */
  async disposeUnits(request: DisposalRequest): Promise<void> {
    await firstValueFrom(
      this.http.post<ApiResponse<void>>(
        `${this.baseUrl}/blood-units/dispose`,
        request,
      ),
    );
  }

  /** Process a whole blood unit into component types. */
  async processComponents(
    request: ComponentProcessingRequest,
  ): Promise<BloodUnit[]> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<BloodUnit[]>>(
        `${this.baseUrl}/blood-units/process-components`,
        request,
      ),
    );
    return response.data;
  }
}
