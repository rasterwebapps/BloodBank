import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import {
  Branch,
  BranchCreateRequest,
  BranchUpdateRequest,
  OperatingHours,
  Equipment,
  Region,
  BloodGroup,
  ComponentType,
  DeferralReason,
  ReactionType,
  BloodGroupCreateRequest,
  ComponentTypeCreateRequest,
  DeferralReasonCreateRequest,
  ReactionTypeCreateRequest,
} from '../models/branch.model';

/**
 * Service for branch management and master data operations.
 */
@Injectable({ providedIn: 'root' })
export class BranchService {
  private readonly http = inject(HttpClient);
  private readonly branchesUrl = `${environment.apiUrl}/api/v1/branches`;
  private readonly masterDataUrl = `${environment.apiUrl}/api/v1/master-data`;

  // ── Branches ───────────────────────────────────────────────────

  async list(page = 0, size = 10, search?: string): Promise<PagedResponse<Branch>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (search) {
      params = params.set('search', search);
    }
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Branch>>>(this.branchesUrl, { params }),
    );
    return response.data;
  }

  async getById(id: string): Promise<Branch> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<Branch>>(`${this.branchesUrl}/${id}`),
    );
    return response.data;
  }

  async create(request: BranchCreateRequest): Promise<Branch> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<Branch>>(this.branchesUrl, request),
    );
    return response.data;
  }

  async update(id: string, request: BranchUpdateRequest): Promise<Branch> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<Branch>>(`${this.branchesUrl}/${id}`, request),
    );
    return response.data;
  }

  async getOperatingHours(id: string): Promise<OperatingHours[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<OperatingHours[]>>(`${this.branchesUrl}/${id}/operating-hours`),
    );
    return response.data;
  }

  async getEquipment(id: string): Promise<Equipment[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<Equipment[]>>(`${this.branchesUrl}/${id}/equipment`),
    );
    return response.data;
  }

  async getRegions(id: string): Promise<Region[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<Region[]>>(`${this.branchesUrl}/${id}/regions`),
    );
    return response.data;
  }

  // ── Blood Groups ───────────────────────────────────────────────

  async getBloodGroups(): Promise<BloodGroup[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<BloodGroup[]>>(`${this.masterDataUrl}/blood-groups`),
    );
    return response.data;
  }

  async createBloodGroup(request: BloodGroupCreateRequest): Promise<BloodGroup> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<BloodGroup>>(`${this.masterDataUrl}/blood-groups`, request),
    );
    return response.data;
  }

  async updateBloodGroup(id: string, request: BloodGroupCreateRequest): Promise<BloodGroup> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<BloodGroup>>(`${this.masterDataUrl}/blood-groups/${id}`, request),
    );
    return response.data;
  }

  async deleteBloodGroup(id: string): Promise<void> {
    await firstValueFrom(
      this.http.delete<ApiResponse<void>>(`${this.masterDataUrl}/blood-groups/${id}`),
    );
  }

  // ── Component Types ────────────────────────────────────────────

  async getComponentTypes(): Promise<ComponentType[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<ComponentType[]>>(`${this.masterDataUrl}/component-types`),
    );
    return response.data;
  }

  async createComponentType(request: ComponentTypeCreateRequest): Promise<ComponentType> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<ComponentType>>(`${this.masterDataUrl}/component-types`, request),
    );
    return response.data;
  }

  async updateComponentType(id: string, request: ComponentTypeCreateRequest): Promise<ComponentType> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<ComponentType>>(`${this.masterDataUrl}/component-types/${id}`, request),
    );
    return response.data;
  }

  async deleteComponentType(id: string): Promise<void> {
    await firstValueFrom(
      this.http.delete<ApiResponse<void>>(`${this.masterDataUrl}/component-types/${id}`),
    );
  }

  // ── Deferral Reasons ───────────────────────────────────────────

  async getDeferralReasons(): Promise<DeferralReason[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<DeferralReason[]>>(`${this.masterDataUrl}/deferral-reasons`),
    );
    return response.data;
  }

  async createDeferralReason(request: DeferralReasonCreateRequest): Promise<DeferralReason> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<DeferralReason>>(`${this.masterDataUrl}/deferral-reasons`, request),
    );
    return response.data;
  }

  async updateDeferralReason(id: string, request: DeferralReasonCreateRequest): Promise<DeferralReason> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<DeferralReason>>(`${this.masterDataUrl}/deferral-reasons/${id}`, request),
    );
    return response.data;
  }

  async deleteDeferralReason(id: string): Promise<void> {
    await firstValueFrom(
      this.http.delete<ApiResponse<void>>(`${this.masterDataUrl}/deferral-reasons/${id}`),
    );
  }

  // ── Reaction Types ─────────────────────────────────────────────

  async getReactionTypes(): Promise<ReactionType[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<ReactionType[]>>(`${this.masterDataUrl}/reaction-types`),
    );
    return response.data;
  }

  async createReactionType(request: ReactionTypeCreateRequest): Promise<ReactionType> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<ReactionType>>(`${this.masterDataUrl}/reaction-types`, request),
    );
    return response.data;
  }

  async updateReactionType(id: string, request: ReactionTypeCreateRequest): Promise<ReactionType> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<ReactionType>>(`${this.masterDataUrl}/reaction-types/${id}`, request),
    );
    return response.data;
  }

  async deleteReactionType(id: string): Promise<void> {
    await firstValueFrom(
      this.http.delete<ApiResponse<void>>(`${this.masterDataUrl}/reaction-types/${id}`),
    );
  }
}
