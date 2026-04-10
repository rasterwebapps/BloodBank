/**
 * Donor feature models matching backend DTOs.
 */

// ── Enums ────────────────────────────────────────────────────────

export enum GenderEnum {
  MALE = 'MALE',
  FEMALE = 'FEMALE',
  OTHER = 'OTHER',
}

export enum DonorTypeEnum {
  VOLUNTARY = 'VOLUNTARY',
  REPLACEMENT = 'REPLACEMENT',
  AUTOLOGOUS = 'AUTOLOGOUS',
  DIRECTED = 'DIRECTED',
}

export enum DonorStatusEnum {
  ACTIVE = 'ACTIVE',
  DEFERRED = 'DEFERRED',
  PERMANENTLY_DEFERRED = 'PERMANENTLY_DEFERRED',
  INACTIVE = 'INACTIVE',
}

export enum DeferralTypeEnum {
  TEMPORARY = 'TEMPORARY',
  PERMANENT = 'PERMANENT',
}

export enum DeferralStatusEnum {
  ACTIVE = 'ACTIVE',
  REINSTATED = 'REINSTATED',
  EXPIRED = 'EXPIRED',
}

export enum LoyaltyTierEnum {
  BRONZE = 'BRONZE',
  SILVER = 'SILVER',
  GOLD = 'GOLD',
  PLATINUM = 'PLATINUM',
}

// ── Response Models ──────────────────────────────────────────────

/** Matches DonorResponse from backend. */
export interface Donor {
  id: string;
  donorNumber: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: GenderEnum;
  bloodGroupId: string;
  rhFactor: string;
  email: string;
  phone: string;
  addressLine1: string;
  addressLine2: string;
  cityId: string;
  postalCode: string;
  nationalId: string;
  nationality: string;
  occupation: string;
  donorType: DonorTypeEnum;
  status: DonorStatusEnum;
  lastDonationDate: string | null;
  totalDonations: number;
  registrationDate: string;
  photoUrl: string | null;
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

/** Matches DonorHealthRecordResponse from backend. */
export interface DonorHealthRecord {
  id: string;
  donorId: string;
  screeningDate: string;
  weightKg: number;
  heightCm: number;
  bloodPressureSystolic: number;
  bloodPressureDiastolic: number;
  pulseRate: number;
  temperatureCelsius: number;
  hemoglobinGdl: number;
  isEligible: boolean;
  notes: string | null;
  screenedBy: string;
  branchId: string;
  createdAt: string;
}

/** Matches DonorConsentResponse from backend. */
export interface DonorConsent {
  id: string;
  donorId: string;
  consentType: string;
  consentGiven: boolean;
  consentDate: string;
  expiryDate: string | null;
  consentText: string;
  signatureReference: string | null;
  ipAddress: string | null;
  revokedAt: string | null;
  branchId: string;
  createdAt: string;
}

/** Matches DonorDeferralResponse from backend. */
export interface DonorDeferral {
  id: string;
  donorId: string;
  deferralReasonId: string;
  deferralType: DeferralTypeEnum;
  deferralDate: string;
  reinstatementDate: string | null;
  notes: string | null;
  deferredBy: string;
  status: DeferralStatusEnum;
  branchId: string;
  createdAt: string;
}

/** Matches DonorLoyaltyResponse from backend. */
export interface DonorLoyalty {
  id: string;
  donorId: string;
  pointsEarned: number;
  pointsRedeemed: number;
  pointsBalance: number;
  tier: LoyaltyTierEnum;
  lastActivityDate: string | null;
  branchId: string;
  createdAt: string;
}

// ── Request Models ───────────────────────────────────────────────

/** Matches DonorCreateRequest from backend. */
export interface DonorCreateRequest {
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: GenderEnum;
  bloodGroupId: string;
  rhFactor: string;
  email: string;
  phone: string;
  addressLine1: string;
  addressLine2: string;
  cityId: string;
  postalCode: string;
  nationalId: string;
  nationality: string;
  occupation: string;
  donorType: DonorTypeEnum;
  branchId: string;
}

/** Matches DonorUpdateRequest from backend. */
export interface DonorUpdateRequest {
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: GenderEnum;
  bloodGroupId: string;
  rhFactor: string;
  email: string;
  phone: string;
  addressLine1: string;
  addressLine2: string;
  cityId: string;
  postalCode: string;
  nationalId: string;
  nationality: string;
  occupation: string;
}

/** Matches DonorSearchRequest from backend. */
export interface DonorSearchRequest {
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  nationalId?: string;
  bloodGroupId?: string;
  status?: DonorStatusEnum;
}

// ── Display helpers ──────────────────────────────────────────────

export function getDonorFullName(donor: Pick<Donor, 'firstName' | 'lastName'>): string {
  return `${donor.firstName} ${donor.lastName}`;
}

/** Blood group options for select dropdowns. */
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

/** Gender display options. */
export const GENDER_OPTIONS = [
  { value: GenderEnum.MALE, label: 'Male' },
  { value: GenderEnum.FEMALE, label: 'Female' },
  { value: GenderEnum.OTHER, label: 'Other' },
] as const;

/** Donor type display options. */
export const DONOR_TYPE_OPTIONS = [
  { value: DonorTypeEnum.VOLUNTARY, label: 'Voluntary' },
  { value: DonorTypeEnum.REPLACEMENT, label: 'Replacement' },
  { value: DonorTypeEnum.AUTOLOGOUS, label: 'Autologous' },
  { value: DonorTypeEnum.DIRECTED, label: 'Directed' },
] as const;

/** Donor status display options. */
export const DONOR_STATUS_OPTIONS = [
  { value: DonorStatusEnum.ACTIVE, label: 'Active' },
  { value: DonorStatusEnum.DEFERRED, label: 'Deferred' },
  { value: DonorStatusEnum.PERMANENTLY_DEFERRED, label: 'Permanently Deferred' },
  { value: DonorStatusEnum.INACTIVE, label: 'Inactive' },
] as const;
