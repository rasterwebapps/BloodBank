/**
 * Compliance feature models matching backend DTOs.
 */

// ── Enums ────────────────────────────────────────────────────────

export enum FrameworkStatusEnum {
  ACTIVE = 'ACTIVE',
  DRAFT = 'DRAFT',
  RETIRED = 'RETIRED',
}

export enum SopStatusEnum {
  DRAFT = 'DRAFT',
  UNDER_REVIEW = 'UNDER_REVIEW',
  APPROVED = 'APPROVED',
  SUPERSEDED = 'SUPERSEDED',
  ARCHIVED = 'ARCHIVED',
}

export enum LicenseStatusEnum {
  ACTIVE = 'ACTIVE',
  EXPIRED = 'EXPIRED',
  SUSPENDED = 'SUSPENDED',
  REVOKED = 'REVOKED',
  PENDING_RENEWAL = 'PENDING_RENEWAL',
}

export enum DeviationSeverityEnum {
  CRITICAL = 'CRITICAL',
  MAJOR = 'MAJOR',
  MINOR = 'MINOR',
  OBSERVATION = 'OBSERVATION',
}

export enum DeviationStatusEnum {
  OPEN = 'OPEN',
  IN_INVESTIGATION = 'IN_INVESTIGATION',
  CAPA_IN_PROGRESS = 'CAPA_IN_PROGRESS',
  CLOSED = 'CLOSED',
  VERIFIED = 'VERIFIED',
}

export enum RecallClassEnum {
  CLASS_I = 'CLASS_I',
  CLASS_II = 'CLASS_II',
  CLASS_III = 'CLASS_III',
}

export enum RecallStatusEnum {
  INITIATED = 'INITIATED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

// ── Response Models ──────────────────────────────────────────────

export interface RegulatoryFramework {
  id: string;
  code: string;
  name: string;
  issuingAuthority: string;
  version: string;
  effectiveDate: string;
  status: FrameworkStatusEnum;
  description: string | null;
  documentUrl: string | null;
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

export interface SopDocument {
  id: string;
  sopCode: string;
  title: string;
  version: string;
  effectiveDate: string;
  reviewDate: string;
  status: SopStatusEnum;
  category: string;
  description: string | null;
  documentUrl: string | null;
  approvedBy: string | null;
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

export interface License {
  id: string;
  licenseNumber: string;
  licenseType: string;
  issuingAuthority: string;
  issueDate: string;
  expiryDate: string;
  status: LicenseStatusEnum;
  conditions: string | null;
  documentUrl: string | null;
  renewalReminderDays: number;
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

export interface Deviation {
  id: string;
  deviationNumber: string;
  title: string;
  description: string;
  severity: DeviationSeverityEnum;
  status: DeviationStatusEnum;
  detectedDate: string;
  detectedBy: string;
  department: string;
  rootCause: string | null;
  correctiveAction: string | null;
  preventiveAction: string | null;
  closureDate: string | null;
  closedBy: string | null;
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

export interface RecallRecord {
  id: string;
  recallNumber: string;
  title: string;
  description: string;
  recallClass: RecallClassEnum;
  status: RecallStatusEnum;
  initiatedDate: string;
  initiatedBy: string;
  unitsAffected: number;
  unitsRecovered: number;
  reason: string;
  completionDate: string | null;
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

// ── Request Models ───────────────────────────────────────────────

export interface DeviationCreateRequest {
  title: string;
  description: string;
  severity: DeviationSeverityEnum;
  detectedDate: string;
  department: string;
}

export interface DeviationUpdateRequest {
  status: DeviationStatusEnum;
  rootCause?: string;
  correctiveAction?: string;
  preventiveAction?: string;
}
