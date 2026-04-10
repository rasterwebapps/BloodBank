import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import {
  KpiData,
  StockLevel,
  CollectionStats,
  DonationTrend,
} from '../models/dashboard.model';

/**
 * Service for fetching dashboard analytics data.
 */
@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/dashboard`;

  /** Fetch KPI summary for the current branch. */
  async getKpis(): Promise<KpiData> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<KpiData>>(`${this.baseUrl}/kpis`),
    );
    return response.data;
  }

  /** Fetch blood stock levels grouped by blood type. */
  async getStockLevels(): Promise<StockLevel[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<StockLevel[]>>(`${this.baseUrl}/stock-levels`),
    );
    return response.data;
  }

  /** Fetch collection workflow status breakdown. */
  async getCollectionStats(): Promise<CollectionStats> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<CollectionStats>>(
        `${this.baseUrl}/collection-stats`,
      ),
    );
    return response.data;
  }

  /** Fetch donation trends for the last 30 days. */
  async getDonationTrends(): Promise<DonationTrend[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<DonationTrend[]>>(
        `${this.baseUrl}/donation-trends`,
      ),
    );
    return response.data;
  }
}
