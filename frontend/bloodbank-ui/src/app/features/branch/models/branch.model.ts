export type BranchStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';

export interface Branch {
  id: string;
  name: string;
  code: string;
  status: BranchStatus;
  address: string;
  city: string;
  country: string;
  phone: string;
  email: string;
  branchManagerName?: string;
  capacity: number;
  branchId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface OperatingHours {
  day: string;
  openTime: string;
  closeTime: string;
  isClosed: boolean;
}

export interface Equipment {
  id: string;
  name: string;
  type: string;
  serialNumber: string;
  lastMaintenanceDate: string;
  nextMaintenanceDate: string;
  status: string;
}

export interface Region {
  id: string;
  name: string;
  country: string;
  cities: string[];
}

export interface BloodGroup {
  id: string;
  name: string;
  aboGroup: string;
  rhFactor: string;
  isActive: boolean;
}

export interface ComponentType {
  id: string;
  name: string;
  code: string;
  storageTemperature: number;
  shelfLifeDays: number;
  isActive: boolean;
}

export interface DeferralReason {
  id: string;
  code: string;
  description: string;
  durationDays: number;
  isPermanent: boolean;
  isActive: boolean;
}

export interface ReactionType {
  id: string;
  code: string;
  name: string;
  severity: string;
  isActive: boolean;
}

export interface BranchCreateRequest {
  name: string;
  code: string;
  address: string;
  city: string;
  country: string;
  phone: string;
  email: string;
  capacity: number;
}

export interface BranchUpdateRequest {
  name: string;
  address: string;
  city: string;
  country: string;
  phone: string;
  email: string;
  capacity: number;
}

export interface BloodGroupCreateRequest {
  name: string;
  aboGroup: string;
  rhFactor: string;
  isActive: boolean;
}

export interface ComponentTypeCreateRequest {
  name: string;
  code: string;
  storageTemperature: number;
  shelfLifeDays: number;
  isActive: boolean;
}

export interface DeferralReasonCreateRequest {
  code: string;
  description: string;
  durationDays: number;
  isPermanent: boolean;
  isActive: boolean;
}

export interface ReactionTypeCreateRequest {
  code: string;
  name: string;
  severity: string;
  isActive: boolean;
}
