/**
 * Reporting feature models matching backend DTOs.
 */

// ── Enums ────────────────────────────────────────────────────────

export enum AuditActionEnum {
  CREATE = 'CREATE',
  READ = 'READ',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
  LOGIN = 'LOGIN',
  LOGOUT = 'LOGOUT',
  EXPORT = 'EXPORT',
  APPROVE = 'APPROVE',
  REJECT = 'REJECT',
}

export enum ReportTypeEnum {
  COLLECTION_SUMMARY = 'COLLECTION_SUMMARY',
  INVENTORY_STATUS = 'INVENTORY_STATUS',
  TRANSFUSION_SUMMARY = 'TRANSFUSION_SUMMARY',
  DONOR_STATISTICS = 'DONOR_STATISTICS',
  BILLING_SUMMARY = 'BILLING_SUMMARY',
  COMPLIANCE_REPORT = 'COMPLIANCE_REPORT',
  CAMP_PERFORMANCE = 'CAMP_PERFORMANCE',
  CUSTOM = 'CUSTOM',
}

export enum ReportFormatEnum {
  PDF = 'PDF',
  EXCEL = 'EXCEL',
  CSV = 'CSV',
}

export enum CustodyEventEnum {
  COLLECTED = 'COLLECTED',
  TESTED = 'TESTED',
  PROCESSED = 'PROCESSED',
  STORED = 'STORED',
  TRANSFERRED = 'TRANSFERRED',
  ISSUED = 'ISSUED',
  DISCARDED = 'DISCARDED',
}

export enum ScheduleFrequencyEnum {
  DAILY = 'DAILY',
  WEEKLY = 'WEEKLY',
  MONTHLY = 'MONTHLY',
  QUARTERLY = 'QUARTERLY',
}

// ── Response Models ──────────────────────────────────────────────

export interface AuditLog {
  id: string;
  userId: string;
  username: string;
  action: AuditActionEnum;
  entityType: string;
  entityId: string | null;
  ipAddress: string;
  userAgent: string | null;
  details: string | null;
  branchId: string | null;
  timestamp: string;
}

export interface Report {
  id: string;
  name: string;
  type: ReportTypeEnum;
  format: ReportFormatEnum;
  parameters: Record<string, string>;
  generatedBy: string;
  generatedAt: string;
  fileUrl: string | null;
  fileSize: number | null;
  branchId: string | null;
  createdAt: string;
}

export interface ChainOfCustodyEvent {
  id: string;
  bloodUnitId: string;
  unitNumber: string;
  event: CustodyEventEnum;
  performedBy: string;
  location: string;
  notes: string | null;
  timestamp: string;
  branchId: string;
}

export interface ChainOfCustody {
  bloodUnitId: string;
  unitNumber: string;
  donorId: string;
  donorName: string;
  bloodGroup: string;
  componentType: string;
  events: ChainOfCustodyEvent[];
}

export interface ScheduledReport {
  id: string;
  name: string;
  reportType: ReportTypeEnum;
  format: ReportFormatEnum;
  frequency: ScheduleFrequencyEnum;
  parameters: Record<string, string>;
  recipientEmails: string[];
  isActive: boolean;
  lastRunAt: string | null;
  nextRunAt: string | null;
  createdBy: string;
  branchId: string | null;
  createdAt: string;
  updatedAt: string;
}

// ── Request Models ───────────────────────────────────────────────

export interface ReportGenerateRequest {
  name: string;
  type: ReportTypeEnum;
  format: ReportFormatEnum;
  parameters: Record<string, string>;
}

export interface AuditLogFilterRequest {
  userId?: string;
  action?: AuditActionEnum;
  entityType?: string;
  fromDate?: string;
  toDate?: string;
}

export interface ScheduledReportCreateRequest {
  name: string;
  reportType: ReportTypeEnum;
  format: ReportFormatEnum;
  frequency: ScheduleFrequencyEnum;
  parameters: Record<string, string>;
  recipientEmails: string[];
}
