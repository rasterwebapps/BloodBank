import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import {
  EmergencyRequest,
  EmergencyRequestCreate,
  EmergencyMatch,
  DisasterEvent,
  DisasterEventCreate,
  EmergencyStatusEnum,
} from '../models/emergency.model';

/**
 * Service for emergency blood requests, unit matching, and disaster management.
 */
@Injectable({ providedIn: 'root' })
export class EmergencyService {
  private readonly http = inject(HttpClient);
  private readonly emergencyUrl = `${environment.apiUrl}/api/v1/emergencies`;
  private readonly matchingUrl  = `${environment.apiUrl}/api/v1/matching`;
  private readonly disasterUrl  = `${environment.apiUrl}/api/v1/disasters`;

  // ── Emergency Requests ─────────────────────────────────────────

  /** List emergency requests with pagination and optional status filter. */
  async listEmergencies(
    page = 0,
    size = 10,
    status?: EmergencyStatusEnum,
  ): Promise<PagedResponse<EmergencyRequest>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) {
      params = params.set('status', status);
    }
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<EmergencyRequest>>>(
        this.emergencyUrl,
        { params },
      ),
    );
    return response.data;
  }

  /** Get a single emergency request by ID. */
  async getEmergency(id: string): Promise<EmergencyRequest> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<EmergencyRequest>>(
        `${this.emergencyUrl}/${id}`,
      ),
    );
    return response.data;
  }

  /** Create a new emergency blood request. */
  async createEmergency(
    req: EmergencyRequestCreate,
  ): Promise<EmergencyRequest> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<EmergencyRequest>>(this.emergencyUrl, req),
    );
    return response.data;
  }

  // ── Unit Matching ──────────────────────────────────────────────

  /** Get matched blood units for an emergency request. */
  async getMatches(emergencyId: string): Promise<EmergencyMatch[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<EmergencyMatch[]>>(
        `${this.matchingUrl}/${emergencyId}`,
      ),
    );
    return response.data;
  }

  // ── Disaster Events ────────────────────────────────────────────

  /** List disaster events with pagination. */
  async listDisasters(
    page = 0,
    size = 10,
  ): Promise<PagedResponse<DisasterEvent>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<DisasterEvent>>>(
        this.disasterUrl,
        { params },
      ),
    );
    return response.data;
  }

  /** Get a single disaster event by ID. */
  async getDisaster(id: string): Promise<DisasterEvent> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<DisasterEvent>>(
        `${this.disasterUrl}/${id}`,
      ),
    );
    return response.data;
  }

  /** Create a new disaster event. */
  async createDisaster(req: DisasterEventCreate): Promise<DisasterEvent> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<DisasterEvent>>(this.disasterUrl, req),
    );
    return response.data;
  }
}
