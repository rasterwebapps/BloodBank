import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import {
  User,
  UserActivity,
  UserCreateRequest,
  UserUpdateRequest,
} from '../models/user-management.model';

/**
 * Service for user management operations.
 */
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/users`;

  async list(
    page = 0,
    size = 10,
    search?: string,
    branchId?: string,
  ): Promise<PagedResponse<User>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (search) {
      params = params.set('search', search);
    }
    if (branchId) {
      params = params.set('branchId', branchId);
    }
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<User>>>(this.baseUrl, { params }),
    );
    return response.data;
  }

  async getById(id: string): Promise<User> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<User>>(`${this.baseUrl}/${id}`),
    );
    return response.data;
  }

  async create(request: UserCreateRequest): Promise<User> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<User>>(this.baseUrl, request),
    );
    return response.data;
  }

  async update(id: string, request: UserUpdateRequest): Promise<User> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<User>>(`${this.baseUrl}/${id}`, request),
    );
    return response.data;
  }

  async delete(id: string): Promise<void> {
    await firstValueFrom(
      this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`),
    );
  }

  async getActivity(userId: string, page = 0, size = 20): Promise<PagedResponse<UserActivity>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<UserActivity>>>(
        `${this.baseUrl}/${userId}/activity`,
        { params },
      ),
    );
    return response.data;
  }

  async resetPassword(userId: string): Promise<void> {
    await firstValueFrom(
      this.http.post<ApiResponse<void>>(`${this.baseUrl}/${userId}/reset-password`, {}),
    );
  }
}
