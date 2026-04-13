import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import {
  SystemSetting,
  FeatureFlag,
  UpdateSettingRequest,
  UpdateFeatureFlagRequest,
  SettingCategory,
} from '../models/settings.model';

/**
 * Service for system settings and feature flag management.
 */
@Injectable({ providedIn: 'root' })
export class SettingsService {
  private readonly http = inject(HttpClient);
  private readonly settingsUrl = `${environment.apiUrl}/api/v1/settings`;
  private readonly featureFlagsUrl = `${environment.apiUrl}/api/v1/feature-flags`;

  async getSettings(category?: SettingCategory): Promise<SystemSetting[]> {
    let params = new HttpParams();
    if (category) {
      params = params.set('category', category);
    }
    const response = await firstValueFrom(
      this.http.get<ApiResponse<SystemSetting[]>>(this.settingsUrl, { params }),
    );
    return response.data;
  }

  async updateSetting(id: string, request: UpdateSettingRequest): Promise<SystemSetting> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<SystemSetting>>(`${this.settingsUrl}/${id}`, request),
    );
    return response.data;
  }

  async getFeatureFlags(): Promise<FeatureFlag[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<FeatureFlag[]>>(this.featureFlagsUrl),
    );
    return response.data;
  }

  async updateFeatureFlag(id: string, request: UpdateFeatureFlagRequest): Promise<FeatureFlag> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<FeatureFlag>>(`${this.featureFlagsUrl}/${id}`, request),
    );
    return response.data;
  }
}
