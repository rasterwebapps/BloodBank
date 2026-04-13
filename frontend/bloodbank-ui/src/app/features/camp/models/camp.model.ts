/**
 * Camp feature models matching backend DTOs.
 */

// ── Enums ────────────────────────────────────────────────────────

export enum CampStatus {
  PLANNED = 'PLANNED',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

// ── Response Models ──────────────────────────────────────────────

/** Matches CampResponse from backend. */
export interface Camp {
  id: string;
  name: string;
  location: string;
  address: string;
  campDate: string;
  startTime: string;
  endTime: string;
  targetDonors: number;
  registeredDonors: number;
  actualDonors: number;
  status: CampStatus;
  organizerName: string;
  contactPhone: string;
  notes: string | null;
  branchId: string;
  createdAt: string;
}

/** Matches CampResourceResponse from backend. */
export interface CampResource {
  id: string;
  campId: string;
  resourceType: string;
  quantity: number;
  notes: string | null;
}

/** Matches CampDonorRegistrationResponse from backend. */
export interface CampDonorRegistration {
  id: string;
  campId: string;
  donorId: string | null;
  donorName: string;
  bloodGroup: string;
  phone: string;
  email: string;
  registeredAt: string;
  attended: boolean;
  collectionId: string | null;
}

// ── Request Models ───────────────────────────────────────────────

/** Matches CampCreateRequest from backend. */
export interface CampCreateRequest {
  name: string;
  location: string;
  address: string;
  campDate: string;
  startTime: string;
  endTime: string;
  targetDonors: number;
  organizerName: string;
  contactPhone: string;
  notes: string;
}

/** Matches CampDonorRegistrationRequest from backend. */
export interface CampDonorRegistrationRequest {
  donorId: string;
  firstName: string;
  lastName: string;
  bloodGroup: string;
  phone: string;
  email: string;
}

// ── Display helpers ──────────────────────────────────────────────

/** Camp status options for filters. */
export const CAMP_STATUS_OPTIONS = [
  { value: CampStatus.PLANNED, label: 'Planned' },
  { value: CampStatus.ACTIVE, label: 'Active' },
  { value: CampStatus.COMPLETED, label: 'Completed' },
  { value: CampStatus.CANCELLED, label: 'Cancelled' },
] as const;
