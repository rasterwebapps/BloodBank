import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import {
  Notification,
  NotificationTemplate,
  Campaign,
  Preference,
  NotificationTemplateCreateRequest,
  CampaignCreateRequest,
  PreferenceUpdateRequest,
} from '../models/notification.model';

/**
 * Service for notifications — messages, templates, campaigns, preferences.
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly notificationUrl = `${environment.apiUrl}/api/v1/notifications`;
  private readonly templateUrl = `${environment.apiUrl}/api/v1/templates`;
  private readonly campaignUrl = `${environment.apiUrl}/api/v1/campaigns`;

  // ── Notifications ──────────────────────────────────────────────

  async listNotifications(page = 0, size = 10): Promise<PagedResponse<Notification>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Notification>>>(this.notificationUrl, { params }),
    );
    return response.data;
  }

  async getNotificationById(id: string): Promise<Notification> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<Notification>>(`${this.notificationUrl}/${id}`),
    );
    return response.data;
  }

  // ── Templates ──────────────────────────────────────────────────

  async listTemplates(page = 0, size = 20): Promise<PagedResponse<NotificationTemplate>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<NotificationTemplate>>>(this.templateUrl, { params }),
    );
    return response.data;
  }

  async getTemplateById(id: string): Promise<NotificationTemplate> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<NotificationTemplate>>(`${this.templateUrl}/${id}`),
    );
    return response.data;
  }

  async createTemplate(request: NotificationTemplateCreateRequest): Promise<NotificationTemplate> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<NotificationTemplate>>(this.templateUrl, request),
    );
    return response.data;
  }

  async updateTemplate(
    id: string,
    request: NotificationTemplateCreateRequest,
  ): Promise<NotificationTemplate> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<NotificationTemplate>>(`${this.templateUrl}/${id}`, request),
    );
    return response.data;
  }

  async deleteTemplate(id: string): Promise<void> {
    await firstValueFrom(
      this.http.delete<ApiResponse<void>>(`${this.templateUrl}/${id}`),
    );
  }

  // ── Campaigns ──────────────────────────────────────────────────

  async listCampaigns(page = 0, size = 10): Promise<PagedResponse<Campaign>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Campaign>>>(this.campaignUrl, { params }),
    );
    return response.data;
  }

  async createCampaign(request: CampaignCreateRequest): Promise<Campaign> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<Campaign>>(this.campaignUrl, request),
    );
    return response.data;
  }

  async launchCampaign(id: string): Promise<Campaign> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<Campaign>>(`${this.campaignUrl}/${id}/launch`, {}),
    );
    return response.data;
  }

  // ── Preferences ────────────────────────────────────────────────

  async getPreferences(): Promise<Preference> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<Preference>>(`${this.notificationUrl}/preferences`),
    );
    return response.data;
  }

  async updatePreferences(request: PreferenceUpdateRequest): Promise<Preference> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<Preference>>(`${this.notificationUrl}/preferences`, request),
    );
    return response.data;
  }
}
