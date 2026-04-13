import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import {
  HospitalRequest,
  HospitalContract,
  HospitalFeedback,
  HospitalDashboardStats,
  BloodRequestCreateRequest,
  FeedbackCreateRequest,
} from '../models/hospital-portal.model';

@Injectable({ providedIn: 'root' })
export class HospitalPortalService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1`;

  async getDashboardStats(): Promise<HospitalDashboardStats> {
    return firstValueFrom(
      this.http.get<ApiResponse<HospitalDashboardStats>>(
        `${this.baseUrl}/hospital-requests/dashboard-stats`,
      ),
    ).then((r) => r.data);
  }

  async getRequests(page = 0, size = 10): Promise<PagedResponse<HospitalRequest>> {
    return firstValueFrom(
      this.http.get<PagedResponse<HospitalRequest>>(
        `${this.baseUrl}/hospital-requests`,
        { params: { page: page.toString(), size: size.toString() } },
      ),
    );
  }

  async createRequest(request: BloodRequestCreateRequest): Promise<HospitalRequest> {
    return firstValueFrom(
      this.http.post<ApiResponse<HospitalRequest>>(
        `${this.baseUrl}/hospital-requests`,
        request,
      ),
    ).then((r) => r.data);
  }

  async getContract(hospitalId: string): Promise<HospitalContract> {
    return firstValueFrom(
      this.http.get<ApiResponse<HospitalContract>>(
        `${this.baseUrl}/hospitals/${hospitalId}/contracts`,
      ),
    ).then((r) => r.data);
  }

  async submitFeedback(feedback: FeedbackCreateRequest): Promise<HospitalFeedback> {
    return firstValueFrom(
      this.http.post<ApiResponse<HospitalFeedback>>(
        `${this.baseUrl}/hospital-feedback`,
        feedback,
      ),
    ).then((r) => r.data);
  }

  async getMyFeedback(page = 0, size = 10): Promise<PagedResponse<HospitalFeedback>> {
    return firstValueFrom(
      this.http.get<PagedResponse<HospitalFeedback>>(
        `${this.baseUrl}/hospital-feedback/my`,
        { params: { page: page.toString(), size: size.toString() } },
      ),
    );
  }

  async getRequestById(id: string): Promise<HospitalRequest> {
    return firstValueFrom(
      this.http.get<ApiResponse<HospitalRequest>>(
        `${this.baseUrl}/hospital-requests/${id}`,
      ),
    ).then((r) => r.data);
  }
}
