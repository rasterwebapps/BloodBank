import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import {
  CrossMatchRequest,
  CrossMatchResult,
  BloodIssue,
  Transfusion,
  HemovigilanceReport,
  CrossMatchStatusEnum,
  HemovigilanceStatusEnum,
  TransfusionStatusEnum,
  CrossMatchRequestCreate,
  BloodIssueCreateRequest,
  TransfusionCreateRequest,
  TransfusionCompleteRequest,
  ReactionReportRequest,
  TransfusionReaction,
} from '../models/transfusion.model';

/**
 * Service for transfusion management: cross-match, blood issue,
 * transfusion records, reaction reporting, and hemovigilance.
 */
@Injectable({ providedIn: 'root' })
export class TransfusionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}`;

  /** List cross-match requests with optional status filter. */
  async listCrossMatchRequests(
    page = 0,
    size = 10,
    status?: CrossMatchStatusEnum,
  ): Promise<PagedResponse<CrossMatchRequest>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) {
      params = params.set('status', status);
    }
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<CrossMatchRequest>>>(
        `${this.baseUrl}/api/v1/crossmatch`,
        { params },
      ),
    );
    return response.data;
  }

  /** Submit a new cross-match request. */
  async createCrossMatchRequest(
    request: CrossMatchRequestCreate,
  ): Promise<CrossMatchRequest> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<CrossMatchRequest>>(
        `${this.baseUrl}/api/v1/crossmatch`,
        request,
      ),
    );
    return response.data;
  }

  /** Retrieve cross-match results for a specific request. */
  async getCrossMatchResults(
    crossMatchRequestId: string,
  ): Promise<CrossMatchResult[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<CrossMatchResult[]>>(
        `${this.baseUrl}/api/v1/crossmatch/${crossMatchRequestId}/results`,
      ),
    );
    return response.data;
  }

  /** List blood issue records with pagination. */
  async listBloodIssues(
    page = 0,
    size = 10,
  ): Promise<PagedResponse<BloodIssue>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<BloodIssue>>>(
        `${this.baseUrl}/api/v1/blood-issues`,
        { params },
      ),
    );
    return response.data;
  }

  /** Issue a blood unit to a patient. */
  async issueBlood(request: BloodIssueCreateRequest): Promise<BloodIssue> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<BloodIssue>>(
        `${this.baseUrl}/api/v1/blood-issues`,
        request,
      ),
    );
    return response.data;
  }

  /** List transfusion records with optional status filter. */
  async listTransfusions(
    page = 0,
    size = 10,
    status?: TransfusionStatusEnum,
  ): Promise<PagedResponse<Transfusion>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) {
      params = params.set('status', status);
    }
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Transfusion>>>(
        `${this.baseUrl}/api/v1/transfusions`,
        { params },
      ),
    );
    return response.data;
  }

  /** Start a new transfusion. */
  async startTransfusion(
    request: TransfusionCreateRequest,
  ): Promise<Transfusion> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<Transfusion>>(
        `${this.baseUrl}/api/v1/transfusions`,
        request,
      ),
    );
    return response.data;
  }

  /** Complete an in-progress transfusion. */
  async completeTransfusion(
    transfusionId: string,
    request: TransfusionCompleteRequest,
  ): Promise<Transfusion> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<Transfusion>>(
        `${this.baseUrl}/api/v1/transfusions/${transfusionId}/complete`,
        request,
      ),
    );
    return response.data;
  }

  /** Report an adverse transfusion reaction. */
  async reportReaction(
    request: ReactionReportRequest,
  ): Promise<TransfusionReaction> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<TransfusionReaction>>(
        `${this.baseUrl}/api/v1/transfusion-reactions`,
        request,
      ),
    );
    return response.data;
  }

  /** List hemovigilance reports with optional status filter. */
  async listHemovigilanceReports(
    page = 0,
    size = 10,
    status?: HemovigilanceStatusEnum,
  ): Promise<PagedResponse<HemovigilanceReport>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) {
      params = params.set('status', status);
    }
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<HemovigilanceReport>>>(
        `${this.baseUrl}/api/v1/hemovigilance`,
        { params },
      ),
    );
    return response.data;
  }
}
