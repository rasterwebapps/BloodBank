import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import {
  TestOrder,
  TestOrderStatusEnum,
  TestResult,
  TestResultCreateRequest,
  TestResultReviewRequest,
  QcRecord,
  QcRecordCreateRequest,
  LabInstrument,
  InstrumentCreateRequest,
} from '../models/lab.model';

/**
 * Service for lab test orders, results, QC, and instrument operations.
 */
@Injectable({ providedIn: 'root' })
export class LabService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  // ── Test Orders ─────────────────────────────────────────────────

  /** List test orders with pagination and optional status filter. */
  async listTestOrders(
    page = 0,
    size = 10,
    status?: TestOrderStatusEnum,
  ): Promise<PagedResponse<TestOrder>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) {
      params = params.set('status', status);
    }
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<TestOrder>>>(
        `${this.apiUrl}/api/v1/test-orders`,
        { params },
      ),
    );
    return response.data;
  }

  /** Get a single test order by ID. */
  async getTestOrder(id: string): Promise<TestOrder> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<TestOrder>>(
        `${this.apiUrl}/api/v1/test-orders/${id}`,
      ),
    );
    return response.data;
  }

  // ── Test Results ────────────────────────────────────────────────

  /** Get test results for a test order. */
  async getTestResults(testOrderId: string): Promise<TestResult[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<TestResult[]>>(
        `${this.apiUrl}/api/v1/test-results/order/${testOrderId}`,
      ),
    );
    return response.data;
  }

  /** Submit a new test result. */
  async createTestResult(request: TestResultCreateRequest): Promise<TestResult> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<TestResult>>(
        `${this.apiUrl}/api/v1/test-results`,
        request,
      ),
    );
    return response.data;
  }

  /** Review (confirm/reject) a test result — dual-review workflow. */
  async reviewTestResult(
    resultId: string,
    request: TestResultReviewRequest,
  ): Promise<TestResult> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<TestResult>>(
        `${this.apiUrl}/api/v1/test-results/${resultId}/review`,
        request,
      ),
    );
    return response.data;
  }

  /** List test results pending review. */
  async listPendingReviews(
    page = 0,
    size = 10,
  ): Promise<PagedResponse<TestResult>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<TestResult>>>(
        `${this.apiUrl}/api/v1/test-results/pending-review`,
        { params },
      ),
    );
    return response.data;
  }

  // ── Quality Control ─────────────────────────────────────────────

  /** List QC records with pagination. */
  async listQcRecords(
    page = 0,
    size = 10,
    instrumentId?: string,
  ): Promise<PagedResponse<QcRecord>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (instrumentId) {
      params = params.set('instrumentId', instrumentId);
    }
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<QcRecord>>>(
        `${this.apiUrl}/api/v1/qc`,
        { params },
      ),
    );
    return response.data;
  }

  /** Create a QC record. */
  async createQcRecord(request: QcRecordCreateRequest): Promise<QcRecord> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<QcRecord>>(
        `${this.apiUrl}/api/v1/qc`,
        request,
      ),
    );
    return response.data;
  }

  // ── Instruments ─────────────────────────────────────────────────

  /** List lab instruments with pagination. */
  async listInstruments(
    page = 0,
    size = 10,
  ): Promise<PagedResponse<LabInstrument>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<LabInstrument>>>(
        `${this.apiUrl}/api/v1/instruments`,
        { params },
      ),
    );
    return response.data;
  }

  /** Get a single instrument by ID. */
  async getInstrument(id: string): Promise<LabInstrument> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<LabInstrument>>(
        `${this.apiUrl}/api/v1/instruments/${id}`,
      ),
    );
    return response.data;
  }

  /** Create a new instrument. */
  async createInstrument(request: InstrumentCreateRequest): Promise<LabInstrument> {
    const response = await firstValueFrom(
      this.http.post<ApiResponse<LabInstrument>>(
        `${this.apiUrl}/api/v1/instruments`,
        request,
      ),
    );
    return response.data;
  }

  /** Update an instrument. */
  async updateInstrument(
    id: string,
    request: InstrumentCreateRequest,
  ): Promise<LabInstrument> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<LabInstrument>>(
        `${this.apiUrl}/api/v1/instruments/${id}`,
        request,
      ),
    );
    return response.data;
  }
}
