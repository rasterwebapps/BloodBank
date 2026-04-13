import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '@env/environment';
import { ApiResponse } from '@models/api-response.model';
import { PagedResponse } from '@models/paged-response.model';
import {
  Document,
  DocumentVersion,
  DocumentUploadRequest,
  DocumentUpdateRequest,
  DocumentCategoryEnum,
} from '../models/document.model';

/**
 * Service for document management — CRUD, versioning, file upload.
 */
@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/v1/documents`;

  // ── Documents ──────────────────────────────────────────────────

  async listDocuments(
    page = 0,
    size = 20,
    folder?: string,
    category?: DocumentCategoryEnum,
  ): Promise<PagedResponse<Document>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (folder) params = params.set('folder', folder);
    if (category) params = params.set('category', category);
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Document>>>(this.baseUrl, { params }),
    );
    return response.data;
  }

  async getDocumentById(id: string): Promise<Document> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<Document>>(`${this.baseUrl}/${id}`),
    );
    return response.data;
  }

  async uploadDocument(
    file: File,
    metadata: DocumentUploadRequest,
  ): Promise<Document> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('name', metadata.name);
    formData.append('category', metadata.category);
    if (metadata.description) formData.append('description', metadata.description);
    if (metadata.folder) formData.append('folder', metadata.folder);
    if (metadata.tags) formData.append('tags', metadata.tags.join(','));
    const response = await firstValueFrom(
      this.http.post<ApiResponse<Document>>(this.baseUrl, formData),
    );
    return response.data;
  }

  async updateDocument(id: string, request: DocumentUpdateRequest): Promise<Document> {
    const response = await firstValueFrom(
      this.http.put<ApiResponse<Document>>(`${this.baseUrl}/${id}`, request),
    );
    return response.data;
  }

  async deleteDocument(id: string): Promise<void> {
    await firstValueFrom(
      this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`),
    );
  }

  async searchDocuments(
    query: string,
    page = 0,
    size = 20,
  ): Promise<PagedResponse<Document>> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());
    const response = await firstValueFrom(
      this.http.get<ApiResponse<PagedResponse<Document>>>(`${this.baseUrl}/search`, { params }),
    );
    return response.data;
  }

  // ── Versions ───────────────────────────────────────────────────

  async listVersions(documentId: string): Promise<DocumentVersion[]> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<DocumentVersion[]>>(`${this.baseUrl}/${documentId}/versions`),
    );
    return response.data;
  }

  async uploadNewVersion(documentId: string, file: File, changeNotes?: string): Promise<DocumentVersion> {
    const formData = new FormData();
    formData.append('file', file);
    if (changeNotes) formData.append('changeNotes', changeNotes);
    const response = await firstValueFrom(
      this.http.post<ApiResponse<DocumentVersion>>(
        `${this.baseUrl}/${documentId}/versions`,
        formData,
      ),
    );
    return response.data;
  }

  async downloadDocument(id: string): Promise<Blob> {
    return firstValueFrom(
      this.http.get(`${this.baseUrl}/${id}/download`, { responseType: 'blob' }),
    );
  }
}
