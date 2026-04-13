/**
 * Collection feature models matching backend DTOs.
 */

// ── Enums ────────────────────────────────────────────────────────

export enum CollectionStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  ADVERSE_REACTION = 'ADVERSE_REACTION',
}

export enum ReactionSeverity {
  MILD = 'MILD',
  MODERATE = 'MODERATE',
  SEVERE = 'SEVERE',
}

export enum TubeType {
  EDTA = 'EDTA',
  SST = 'SST',
  CITRATE = 'CITRATE',
  HEPARIN = 'HEPARIN',
}

// ── Response Models ──────────────────────────────────────────────

/** Matches CollectionResponse from backend. */
export interface Collection {
  id: string;
  donorId: string;
  donorName: string;
  donorBloodGroup: string;
  bagNumber: string;
  volumeMl: number;
  startTime: string;
  endTime: string | null;
  status: CollectionStatus;
  notes: string | null;
  systolicBP: number | null;
  diastolicBP: number | null;
  pulse: number | null;
  weight: number | null;
  branchId: string;
  createdAt: string;
}

/** Matches AdverseReactionResponse from backend. */
export interface AdverseReaction {
  id: string;
  collectionId: string;
  reactionType: string;
  severity: ReactionSeverity;
  description: string;
  actionTaken: string;
  reportedAt: string;
}

/** Matches CollectionSampleResponse from backend. */
export interface CollectionSample {
  id: string;
  collectionId: string;
  sampleCode: string;
  tubeType: TubeType;
  volumeMl: number;
  collectedAt: string;
  status: string;
}

// ── Request Models ───────────────────────────────────────────────

/** Matches CollectionCreateRequest from backend. */
export interface CollectionCreateRequest {
  donorId: string;
  bagNumber: string;
  volumeMl: number;
  startTime: string;
  endTime: string;
  notes: string;
  systolicBP: number;
  diastolicBP: number;
  pulse: number;
  weight: number;
}

/** Matches AdverseReactionCreateRequest from backend. */
export interface AdverseReactionCreateRequest {
  collectionId: string;
  reactionType: string;
  severity: ReactionSeverity;
  description: string;
  actionTaken: string;
}

/** Matches SampleRegistrationRequest from backend. */
export interface SampleRegistrationRequest {
  collectionId: string;
  sampleCode: string;
  tubeType: TubeType;
  volumeMl: number;
}

// ── Display helpers ──────────────────────────────────────────────

/** Reaction type options for select dropdowns. */
export const REACTION_TYPE_OPTIONS = [
  { value: 'VASOVAGAL', label: 'Vasovagal' },
  { value: 'HEMATOMA', label: 'Hematoma' },
  { value: 'NAUSEA', label: 'Nausea' },
  { value: 'DIZZINESS', label: 'Dizziness' },
  { value: 'OTHER', label: 'Other' },
] as const;

/** Severity options for select dropdowns. */
export const SEVERITY_OPTIONS = [
  { value: ReactionSeverity.MILD, label: 'Mild' },
  { value: ReactionSeverity.MODERATE, label: 'Moderate' },
  { value: ReactionSeverity.SEVERE, label: 'Severe' },
] as const;

/** Tube type options for select dropdowns. */
export const TUBE_TYPE_OPTIONS = [
  { value: TubeType.EDTA, label: 'EDTA' },
  { value: TubeType.SST, label: 'SST' },
  { value: TubeType.CITRATE, label: 'Citrate' },
  { value: TubeType.HEPARIN, label: 'Heparin' },
] as const;

/** Collection status options. */
export const COLLECTION_STATUS_OPTIONS = [
  { value: CollectionStatus.PENDING, label: 'Pending' },
  { value: CollectionStatus.IN_PROGRESS, label: 'In Progress' },
  { value: CollectionStatus.COMPLETED, label: 'Completed' },
  { value: CollectionStatus.ADVERSE_REACTION, label: 'Adverse Reaction' },
] as const;
