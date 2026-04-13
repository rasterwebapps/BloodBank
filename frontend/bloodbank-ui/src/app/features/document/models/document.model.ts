/**
 * Document feature models matching backend DTOs.
 */

// ── Enums ────────────────────────────────────────────────────────

export enum DocumentCategoryEnum {
  SOP = 'SOP',
  POLICY = 'POLICY',
  FORM = 'FORM',
  REPORT = 'REPORT',
  CERTIFICATE = 'CERTIFICATE',
  OTHER = 'OTHER',
}

export enum DocumentStatusEnum {
  ACTIVE = 'ACTIVE',
  ARCHIVED = 'ARCHIVED',
  DRAFT = 'DRAFT',
}

// ── Response Models ──────────────────────────────────────────────

export interface DocumentVersion {
  id: string;
  documentId: string;
  versionNumber: string;
  fileUrl: string;
  fileSize: number;
  mimeType: string;
  uploadedBy: string;
  changeNotes: string | null;
  createdAt: string;
}

export interface Document {
  id: string;
  name: string;
  description: string | null;
  category: DocumentCategoryEnum;
  status: DocumentStatusEnum;
  folder: string | null;
  tags: string[];
  currentVersion: string;
  latestVersionId: string | null;
  fileUrl: string | null;
  fileSize: number | null;
  mimeType: string | null;
  uploadedBy: string;
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

// ── Request Models ───────────────────────────────────────────────

export interface DocumentUploadRequest {
  name: string;
  description?: string;
  category: DocumentCategoryEnum;
  folder?: string;
  tags?: string[];
}

export interface DocumentUpdateRequest {
  name: string;
  description?: string;
  category: DocumentCategoryEnum;
  folder?: string;
  tags?: string[];
  status: DocumentStatusEnum;
}

// ── Display helpers ──────────────────────────────────────────────

export const DOCUMENT_CATEGORY_OPTIONS = [
  { value: DocumentCategoryEnum.SOP, label: 'SOP' },
  { value: DocumentCategoryEnum.POLICY, label: 'Policy' },
  { value: DocumentCategoryEnum.FORM, label: 'Form' },
  { value: DocumentCategoryEnum.REPORT, label: 'Report' },
  { value: DocumentCategoryEnum.CERTIFICATE, label: 'Certificate' },
  { value: DocumentCategoryEnum.OTHER, label: 'Other' },
] as const;

export function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}
