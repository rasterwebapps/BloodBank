/**
 * Lab feature models matching backend DTOs.
 */

// ── Enums ────────────────────────────────────────────────────────

export enum TestOrderStatusEnum {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

export enum TestOrderPriorityEnum {
  ROUTINE = 'ROUTINE',
  URGENT = 'URGENT',
  STAT = 'STAT',
}

export enum TestTypeEnum {
  HIV = 'HIV',
  HBV = 'HBV',
  HCV = 'HCV',
  SYPHILIS = 'SYPHILIS',
  MALARIA = 'MALARIA',
  BLOOD_GROUPING = 'BLOOD_GROUPING',
  ANTIBODY_SCREENING = 'ANTIBODY_SCREENING',
}

export enum TestResultValueEnum {
  REACTIVE = 'REACTIVE',
  NON_REACTIVE = 'NON_REACTIVE',
  INDETERMINATE = 'INDETERMINATE',
  POSITIVE = 'POSITIVE',
  NEGATIVE = 'NEGATIVE',
  INVALID = 'INVALID',
}

export enum ReviewStatusEnum {
  PENDING_REVIEW = 'PENDING_REVIEW',
  REVIEWED = 'REVIEWED',
  REJECTED = 'REJECTED',
}

export enum InstrumentStatusEnum {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  MAINTENANCE = 'MAINTENANCE',
  CALIBRATION_DUE = 'CALIBRATION_DUE',
  OUT_OF_SERVICE = 'OUT_OF_SERVICE',
}

export enum QcStatusEnum {
  PASS = 'PASS',
  FAIL = 'FAIL',
  WARNING = 'WARNING',
}

// ── Response Models ──────────────────────────────────────────────

/** Matches TestOrderResponse from backend. */
export interface TestOrder {
  id: string;
  sampleId: string;
  sampleNumber: string;
  donorId: string;
  donorName: string;
  testPanelId: string;
  testPanelName: string;
  priority: TestOrderPriorityEnum;
  status: TestOrderStatusEnum;
  orderedBy: string;
  orderedAt: string;
  completedAt: string | null;
  notes: string | null;
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

/** Matches TestResultResponse from backend. */
export interface TestResult {
  id: string;
  testOrderId: string;
  testType: TestTypeEnum;
  result: TestResultValueEnum;
  value: string | null;
  unit: string | null;
  referenceRange: string | null;
  method: string | null;
  instrumentId: string | null;
  instrumentName: string | null;
  testedBy: string;
  testedAt: string;
  reviewedBy: string | null;
  reviewedAt: string | null;
  reviewStatus: ReviewStatusEnum;
  reviewNotes: string | null;
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

/** Matches LabInstrumentResponse from backend. */
export interface LabInstrument {
  id: string;
  name: string;
  manufacturer: string;
  model: string;
  serialNumber: string;
  status: InstrumentStatusEnum;
  lastCalibrationDate: string | null;
  nextCalibrationDate: string | null;
  calibrationIntervalDays: number;
  location: string;
  installedDate: string;
  notes: string | null;
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

/** Matches QualityControlRecordResponse from backend. */
export interface QcRecord {
  id: string;
  instrumentId: string;
  instrumentName: string;
  testType: TestTypeEnum;
  controlLevel: string;
  expectedValue: number;
  observedValue: number;
  standardDeviation: number;
  mean: number;
  status: QcStatusEnum;
  lotNumber: string;
  expiryDate: string;
  performedBy: string;
  performedAt: string;
  notes: string | null;
  branchId: string;
  createdAt: string;
}

// ── Request Models ───────────────────────────────────────────────

/** Request to enter a test result. */
export interface TestResultCreateRequest {
  testOrderId: string;
  testType: TestTypeEnum;
  result: TestResultValueEnum;
  value: string;
  unit: string;
  referenceRange: string;
  method: string;
  instrumentId: string;
}

/** Request for reviewing/confirming a test result. */
export interface TestResultReviewRequest {
  reviewStatus: ReviewStatusEnum;
  reviewNotes: string;
}

/** Request to create a QC record. */
export interface QcRecordCreateRequest {
  instrumentId: string;
  testType: TestTypeEnum;
  controlLevel: string;
  expectedValue: number;
  observedValue: number;
  standardDeviation: number;
  mean: number;
  lotNumber: string;
  expiryDate: string;
  notes: string;
}

/** Request to create or update a lab instrument. */
export interface InstrumentCreateRequest {
  name: string;
  manufacturer: string;
  model: string;
  serialNumber: string;
  calibrationIntervalDays: number;
  location: string;
  installedDate: string;
  notes: string;
}

// ── Display helpers ──────────────────────────────────────────────

/** Priority display options. */
export const PRIORITY_OPTIONS = [
  { value: TestOrderPriorityEnum.ROUTINE, label: 'Routine' },
  { value: TestOrderPriorityEnum.URGENT, label: 'Urgent' },
  { value: TestOrderPriorityEnum.STAT, label: 'STAT' },
] as const;

/** Test order status display options. */
export const TEST_ORDER_STATUS_OPTIONS = [
  { value: TestOrderStatusEnum.PENDING, label: 'Pending' },
  { value: TestOrderStatusEnum.IN_PROGRESS, label: 'In Progress' },
  { value: TestOrderStatusEnum.COMPLETED, label: 'Completed' },
  { value: TestOrderStatusEnum.CANCELLED, label: 'Cancelled' },
] as const;

/** Test type display options. */
export const TEST_TYPE_OPTIONS = [
  { value: TestTypeEnum.HIV, label: 'HIV' },
  { value: TestTypeEnum.HBV, label: 'HBV (Hepatitis B)' },
  { value: TestTypeEnum.HCV, label: 'HCV (Hepatitis C)' },
  { value: TestTypeEnum.SYPHILIS, label: 'Syphilis' },
  { value: TestTypeEnum.MALARIA, label: 'Malaria' },
  { value: TestTypeEnum.BLOOD_GROUPING, label: 'Blood Grouping' },
  { value: TestTypeEnum.ANTIBODY_SCREENING, label: 'Antibody Screening' },
] as const;

/** Test result value display options. */
export const RESULT_VALUE_OPTIONS = [
  { value: TestResultValueEnum.REACTIVE, label: 'Reactive' },
  { value: TestResultValueEnum.NON_REACTIVE, label: 'Non-Reactive' },
  { value: TestResultValueEnum.INDETERMINATE, label: 'Indeterminate' },
  { value: TestResultValueEnum.POSITIVE, label: 'Positive' },
  { value: TestResultValueEnum.NEGATIVE, label: 'Negative' },
  { value: TestResultValueEnum.INVALID, label: 'Invalid' },
] as const;

/** Instrument status display options. */
export const INSTRUMENT_STATUS_OPTIONS = [
  { value: InstrumentStatusEnum.ACTIVE, label: 'Active' },
  { value: InstrumentStatusEnum.INACTIVE, label: 'Inactive' },
  { value: InstrumentStatusEnum.MAINTENANCE, label: 'Maintenance' },
  { value: InstrumentStatusEnum.CALIBRATION_DUE, label: 'Calibration Due' },
  { value: InstrumentStatusEnum.OUT_OF_SERVICE, label: 'Out of Service' },
] as const;

/** Review status display options. */
export const REVIEW_STATUS_OPTIONS = [
  { value: ReviewStatusEnum.PENDING_REVIEW, label: 'Pending Review' },
  { value: ReviewStatusEnum.REVIEWED, label: 'Reviewed' },
  { value: ReviewStatusEnum.REJECTED, label: 'Rejected' },
] as const;

/** QC status display options. */
export const QC_STATUS_OPTIONS = [
  { value: QcStatusEnum.PASS, label: 'Pass' },
  { value: QcStatusEnum.FAIL, label: 'Fail' },
  { value: QcStatusEnum.WARNING, label: 'Warning' },
] as const;

/** Map priority to a CSS class for badge styling. */
export function getPriorityClass(priority: TestOrderPriorityEnum): string {
  switch (priority) {
    case TestOrderPriorityEnum.STAT:
      return 'priority-stat';
    case TestOrderPriorityEnum.URGENT:
      return 'priority-urgent';
    default:
      return 'priority-routine';
  }
}

/** Map QC status to a CSS class. */
export function getQcStatusClass(status: QcStatusEnum): string {
  switch (status) {
    case QcStatusEnum.PASS:
      return 'qc-pass';
    case QcStatusEnum.FAIL:
      return 'qc-fail';
    case QcStatusEnum.WARNING:
      return 'qc-warning';
  }
}
