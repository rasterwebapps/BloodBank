/**
 * Domain models and enums for the Inventory feature.
 */

export enum BloodUnitStatus {
  AVAILABLE = 'AVAILABLE',
  RESERVED = 'RESERVED',
  EXPIRED = 'EXPIRED',
  DISCARDED = 'DISCARDED',
  ISSUED = 'ISSUED',
}

export enum DisposalReason {
  EXPIRED = 'EXPIRED',
  CONTAMINATED = 'CONTAMINATED',
  DAMAGED = 'DAMAGED',
  QUALITY_FAILURE = 'QUALITY_FAILURE',
  OTHER = 'OTHER',
}

export enum StorageType {
  REFRIGERATOR = 'REFRIGERATOR',
  FREEZER = 'FREEZER',
  PLATELET_AGITATOR = 'PLATELET_AGITATOR',
  ROOM_TEMPERATURE = 'ROOM_TEMPERATURE',
}

export enum TransferStatus {
  PENDING = 'PENDING',
  IN_TRANSIT = 'IN_TRANSIT',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

export interface BloodUnit {
  id: string;
  unitNumber: string;
  bloodGroupId: string;
  componentType: string;
  collectionDate: string;
  expiryDate: string;
  status: BloodUnitStatus;
  volumeMl: number;
  donorId: string;
  branchId: string;
  storageLocationId: string | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface BloodStock {
  bloodGroupId: string;
  componentType: string;
  count: number;
  criticalThreshold: number;
  lowThreshold: number;
}

export interface ComponentType {
  id: string;
  name: string;
  code: string;
  shelfLifeDays: number;
  storageTemp: string;
}

export interface StorageLocation {
  id: string;
  name: string;
  type: StorageType;
  capacity: number;
  currentUnits: number;
  minTempCelsius: number;
  maxTempCelsius: number;
  branchId: string;
  createdAt: string;
}

export interface Transfer {
  id: string;
  sourceBranchId: string;
  destinationBranchId: string;
  units: BloodUnit[];
  status: TransferStatus;
  initiatedBy: string;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface DisposalRequest {
  unitIds: string[];
  reason: DisposalReason;
  notes?: string;
}

export interface ComponentProcessingRequest {
  sourceUnitId: string;
  componentTypes: string[];
  notes?: string;
}

export interface TransferRequest {
  destinationBranchId: string;
  unitIds: string[];
  notes?: string;
}

export const BLOOD_GROUP_OPTIONS = [
  { value: 'A_POSITIVE', label: 'A+' },
  { value: 'A_NEGATIVE', label: 'A−' },
  { value: 'B_POSITIVE', label: 'B+' },
  { value: 'B_NEGATIVE', label: 'B−' },
  { value: 'AB_POSITIVE', label: 'AB+' },
  { value: 'AB_NEGATIVE', label: 'AB−' },
  { value: 'O_POSITIVE', label: 'O+' },
  { value: 'O_NEGATIVE', label: 'O−' },
] as const;

export const COMPONENT_TYPE_OPTIONS = [
  { value: 'WHOLE_BLOOD', label: 'Whole Blood' },
  { value: 'PACKED_RED_CELLS', label: 'Packed Red Cells' },
  { value: 'FRESH_FROZEN_PLASMA', label: 'Fresh Frozen Plasma' },
  { value: 'PLATELETS', label: 'Platelets' },
  { value: 'CRYOPRECIPITATE', label: 'Cryoprecipitate' },
] as const;

export const DISPOSAL_REASON_OPTIONS = [
  { value: DisposalReason.EXPIRED, label: 'Expired' },
  { value: DisposalReason.CONTAMINATED, label: 'Contaminated' },
  { value: DisposalReason.DAMAGED, label: 'Damaged' },
  { value: DisposalReason.QUALITY_FAILURE, label: 'Quality Failure' },
  { value: DisposalReason.OTHER, label: 'Other' },
] as const;

export const BLOOD_UNIT_STATUS_OPTIONS = [
  { value: BloodUnitStatus.AVAILABLE, label: 'Available' },
  { value: BloodUnitStatus.RESERVED, label: 'Reserved' },
  { value: BloodUnitStatus.EXPIRED, label: 'Expired' },
  { value: BloodUnitStatus.DISCARDED, label: 'Discarded' },
  { value: BloodUnitStatus.ISSUED, label: 'Issued' },
] as const;

export const STORAGE_TYPE_OPTIONS = [
  { value: StorageType.REFRIGERATOR, label: 'Refrigerator' },
  { value: StorageType.FREEZER, label: 'Freezer' },
  { value: StorageType.PLATELET_AGITATOR, label: 'Platelet Agitator' },
  { value: StorageType.ROOM_TEMPERATURE, label: 'Room Temperature' },
] as const;

/** Returns the stock level category for a given count. */
export function getStockLevel(count: number): 'adequate' | 'low' | 'critical' {
  if (count >= 10) return 'adequate';
  if (count >= 3) return 'low';
  return 'critical';
}

/** Returns a CSS class name based on stock count. */
export function getStockLevelClass(count: number): string {
  const level = getStockLevel(count);
  if (level === 'adequate') return 'stock-adequate';
  if (level === 'low') return 'stock-low';
  return 'stock-critical';
}

/** Returns the human-readable label for a blood group value. */
export function getBloodGroupLabel(value: string): string {
  const option = BLOOD_GROUP_OPTIONS.find((o) => o.value === value);
  return option ? option.label : value;
}

/** Returns the human-readable label for a component type value. */
export function getComponentTypeLabel(value: string): string {
  const option = COMPONENT_TYPE_OPTIONS.find((o) => o.value === value);
  return option ? option.label : value;
}
