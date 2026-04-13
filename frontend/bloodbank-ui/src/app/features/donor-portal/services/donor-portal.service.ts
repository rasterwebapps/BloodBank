import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import {
  Appointment,
  AppointmentRequest,
  Branch,
  Camp,
  DonationRecord,
  DonorProfile,
  EligibilityCheck,
  EligibilityResult,
  Referral,
  ReferralInfo,
} from '../models/donor-portal.models';

@Injectable({ providedIn: 'root' })
export class DonorPortalService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1`;

  async getProfile(): Promise<DonorProfile> {
    return firstValueFrom(
      this.http.get<ApiResponse<DonorProfile>>(`${this.baseUrl}/donors/me`),
    ).then((r) => r.data);
  }

  async getDonationHistory(page = 0, size = 10): Promise<PagedResponse<DonationRecord>> {
    return firstValueFrom(
      this.http.get<PagedResponse<DonationRecord>>(
        `${this.baseUrl}/donors/me/donations`,
        { params: { page: page.toString(), size: size.toString() } },
      ),
    );
  }

  async getAppointments(): Promise<Appointment[]> {
    return firstValueFrom(
      this.http.get<ApiResponse<Appointment[]>>(`${this.baseUrl}/donors/me/appointments`),
    ).then((r) => r.data);
  }

  async bookAppointment(request: AppointmentRequest): Promise<Appointment> {
    return firstValueFrom(
      this.http.post<ApiResponse<Appointment>>(
        `${this.baseUrl}/donors/me/appointments`,
        request,
      ),
    ).then((r) => r.data);
  }

  async cancelAppointment(id: string): Promise<void> {
    await firstValueFrom(
      this.http.delete(`${this.baseUrl}/donors/me/appointments/${id}`),
    );
  }

  async checkEligibility(request: EligibilityCheck): Promise<EligibilityResult> {
    return firstValueFrom(
      this.http.post<ApiResponse<EligibilityResult>>(
        `${this.baseUrl}/donors/me/eligibility`,
        request,
      ),
    ).then((r) => r.data);
  }

  async getNearbyCamps(lat: number, lng: number): Promise<Camp[]> {
    return firstValueFrom(
      this.http.get<ApiResponse<Camp[]>>(`${this.baseUrl}/camps/nearby`, {
        params: { lat: lat.toString(), lng: lng.toString() },
      }),
    ).then((r) => r.data);
  }

  async getCampsByCity(city: string): Promise<Camp[]> {
    return firstValueFrom(
      this.http.get<ApiResponse<Camp[]>>(`${this.baseUrl}/camps/nearby`, {
        params: { city },
      }),
    ).then((r) => r.data);
  }

  async getReferralCode(): Promise<ReferralInfo> {
    return firstValueFrom(
      this.http.get<ApiResponse<ReferralInfo>>(`${this.baseUrl}/donors/me/referral`),
    ).then((r) => r.data);
  }

  async getReferrals(): Promise<Referral[]> {
    return firstValueFrom(
      this.http.get<ApiResponse<Referral[]>>(`${this.baseUrl}/donors/me/referrals`),
    ).then((r) => r.data);
  }

  async getBranches(): Promise<Branch[]> {
    return firstValueFrom(
      this.http.get<ApiResponse<Branch[]>>(`${this.baseUrl}/branches`),
    ).then((r) => r.data);
  }

  async register(data: Record<string, unknown>): Promise<DonorProfile> {
    return firstValueFrom(
      this.http.post<ApiResponse<DonorProfile>>(`${this.baseUrl}/donors/register`, data),
    ).then((r) => r.data);
  }
}
