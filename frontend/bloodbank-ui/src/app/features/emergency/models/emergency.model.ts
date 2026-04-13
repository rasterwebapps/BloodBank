/**
 * Domain models for the Emergency feature.
 * Covers emergency blood requests, unit matching,
 * disaster events, and donor mobilization.
 */

// ── Enums ──────────────────────────────────────────────────────────────────

export enum EmergencyStatusEnum {
  ACTIVE = 'ACTIVE',
  MATCHED = 'MATCHED',
  DISPATCHED = 'DISPATCHED',
  RESOLVED = 'RESOLVED',
  CANCELLED = 'CANCELLED',
}

export enum PriorityLevelEnum {
  P1_CRITICAL = 'P1_CRITICAL',
  P2_URGENT = 'P2_URGENT',
  P3_HIGH = 'P3_HIGH',
  P4_ROUTINE = 'P4_ROUTINE',
}

export enum DisasterStatusEnum {
  ACTIVE = 'ACTIVE',
  CONTAINED = 'CONTAINED',
  RESOLVED = 'RESOLVED',
}

export enum MatchStatusEnum {
  PENDING = 'PENDING',
  MATCHED = 'MATCHED',
  DISPATCHED = 'DISPATCHED',
  DELIVERED = 'DELIVERED',
}

// ── Domain interfaces ──────────────────────────────────────────────────────

export interface EmergencyRequest {
  id: string;
  requestNumber: string;
  patientName: string;
  patientId: string;
  bloodGroup: string;
  rhFactor: string;
  componentType: string;
  unitsRequired: number;
  priorityLevel: PriorityLevelEnum;
  status: EmergencyStatusEnum;
  requestedBy: string;
  requestedAt: string;
  hospitalName: string;
  matchedUnitIds: string[];
  notes: string | null;
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

export interface EmergencyMatch {
  id: string;
  emergencyRequestId: string;
  bloodUnitId: string;
  unitNumber: string;
  matchStatus: MatchStatusEnum;
  matchedAt: string;
  dispatchedAt: string | null;
  deliveredAt: string | null;
  notes: string | null;
}

export interface DisasterEvent {
  id: string;
  disasterCode: string;
  name: string;
  description: string;
  location: string;
  status: DisasterStatusEnum;
  severity: string;
  startedAt: string;
  expectedDuration: string | null;
  coordinatedBy: string;
  branchIds: string[];
  totalBloodRequired: number;
  totalBloodDispatched: number;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface DonorMobilization {
  id: string;
  disasterEventId: string;
  donorId: string;
  donorName: string;
  bloodGroup: string;
  mobilizationStatus: string;
  contactedAt: string;
  respondedAt: string | null;
  arrivalTime: string | null;
  notes: string | null;
}

// ── Request payloads ───────────────────────────────────────────────────────

export interface EmergencyRequestCreate {
  patientName: string;
  patientId: string;
  bloodGroup: string;
  rhFactor: string;
  componentType: string;
  unitsRequired: number;
  priorityLevel: PriorityLevelEnum;
  hospitalName: string;
  notes: string;
}

export interface DisasterEventCreate {
  name: string;
  description: string;
  location: string;
  severity: string;
  startedAt: string;
  expectedDuration: string;
  totalBloodRequired: number;
  notes: string;
}

// ── Select option constants ────────────────────────────────────────────────

export const PRIORITY_LEVEL_OPTIONS = [
  { value: PriorityLevelEnum.P1_CRITICAL, label: 'P1 - Critical', cssClass: 'priority-p1' },
  { value: PriorityLevelEnum.P2_URGENT,   label: 'P2 - Urgent',   cssClass: 'priority-p2' },
  { value: PriorityLevelEnum.P3_HIGH,     label: 'P3 - High',     cssClass: 'priority-p3' },
  { value: PriorityLevelEnum.P4_ROUTINE,  label: 'P4 - Routine',  cssClass: 'priority-p4' },
] as const;

export const EMERGENCY_STATUS_OPTIONS = [
  { value: EmergencyStatusEnum.ACTIVE,     label: 'Active' },
  { value: EmergencyStatusEnum.MATCHED,    label: 'Matched' },
  { value: EmergencyStatusEnum.DISPATCHED, label: 'Dispatched' },
  { value: EmergencyStatusEnum.RESOLVED,   label: 'Resolved' },
  { value: EmergencyStatusEnum.CANCELLED,  label: 'Cancelled' },
] as const;

export const DISASTER_STATUS_OPTIONS = [
  { value: DisasterStatusEnum.ACTIVE,     label: 'Active' },
  { value: DisasterStatusEnum.CONTAINED,  label: 'Contained' },
  { value: DisasterStatusEnum.RESOLVED,   label: 'Resolved' },
] as const;

export const BLOOD_GROUP_OPTIONS = [
  { value: 'A',  label: 'A' },
  { value: 'B',  label: 'B' },
  { value: 'AB', label: 'AB' },
  { value: 'O',  label: 'O' },
] as const;

export const RH_FACTOR_OPTIONS = [
  { value: '+', label: 'Positive (+)' },
  { value: '-', label: 'Negative (−)' },
] as const;

export const COMPONENT_TYPE_OPTIONS = [
  { value: 'WHOLE_BLOOD',         label: 'Whole Blood' },
  { value: 'PACKED_RED_CELLS',    label: 'Packed Red Cells' },
  { value: 'PLATELETS',           label: 'Platelets' },
  { value: 'FRESH_FROZEN_PLASMA', label: 'Fresh Frozen Plasma' },
  { value: 'CRYOPRECIPITATE',     label: 'Cryoprecipitate' },
] as const;

export const SEVERITY_OPTIONS = [
  { value: 'LOW',      label: 'Low' },
  { value: 'MEDIUM',   label: 'Medium' },
  { value: 'HIGH',     label: 'High' },
  { value: 'CRITICAL', label: 'Critical' },
] as const;

// ── Utility helpers ────────────────────────────────────────────────────────

/** Returns the CSS class for the given priority level badge. */
export function getPriorityClass(priority: PriorityLevelEnum): string {
  switch (priority) {
    case PriorityLevelEnum.P1_CRITICAL: return 'priority-p1';
    case PriorityLevelEnum.P2_URGENT:   return 'priority-p2';
    case PriorityLevelEnum.P3_HIGH:     return 'priority-p3';
    case PriorityLevelEnum.P4_ROUTINE:  return 'priority-p4';
    default:                             return '';
  }
}

/** Returns the human-readable label for the given priority level. */
export function getPriorityLabel(priority: PriorityLevelEnum): string {
  return PRIORITY_LEVEL_OPTIONS.find((o) => o.value === priority)?.label ?? priority;
}
