export enum CrossMatchStatusEnum {
  PENDING = 'PENDING',
  COMPATIBLE = 'COMPATIBLE',
  INCOMPATIBLE = 'INCOMPATIBLE',
  DEFERRED = 'DEFERRED',
}

export enum CrossMatchTypeEnum {
  IMMEDIATE_SPIN = 'IMMEDIATE_SPIN',
  AHG = 'AHG',
  ELECTRONIC = 'ELECTRONIC',
  EMERGENCY_RELEASE = 'EMERGENCY_RELEASE',
}

export enum BloodIssueStatusEnum {
  ISSUED = 'ISSUED',
  RETURNED = 'RETURNED',
  DISCARDED = 'DISCARDED',
  TRANSFUSED = 'TRANSFUSED',
}

export enum TransfusionStatusEnum {
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  STOPPED = 'STOPPED',
  ADVERSE_REACTION = 'ADVERSE_REACTION',
}

export enum TransfusionOutcomeEnum {
  SUCCESSFUL = 'SUCCESSFUL',
  PARTIAL = 'PARTIAL',
  UNSUCCESSFUL = 'UNSUCCESSFUL',
  ADVERSE_REACTION = 'ADVERSE_REACTION',
}

export enum ReactionTypeEnum {
  FEBRILE = 'FEBRILE',
  ALLERGIC = 'ALLERGIC',
  HEMOLYTIC = 'HEMOLYTIC',
  ANAPHYLACTIC = 'ANAPHYLACTIC',
  TACO = 'TACO',
  TRALI = 'TRALI',
  DELAYED = 'DELAYED',
  OTHER = 'OTHER',
}

export enum ReactionSeverityEnum {
  MILD = 'MILD',
  MODERATE = 'MODERATE',
  SEVERE = 'SEVERE',
  LIFE_THREATENING = 'LIFE_THREATENING',
}

export enum HemovigilanceStatusEnum {
  REPORTED = 'REPORTED',
  UNDER_INVESTIGATION = 'UNDER_INVESTIGATION',
  CLOSED = 'CLOSED',
  ESCALATED = 'ESCALATED',
}

export interface CrossMatchRequest {
  id: string;
  requestNumber: string;
  patientName: string;
  patientId: string;
  bloodGroup: string;
  rhFactor: string;
  componentType: string;
  unitsRequested: number;
  urgency: string;
  requestedBy: string;
  requestedAt: string;
  status: CrossMatchStatusEnum;
  compatibleUnitIds: string[];
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

export interface CrossMatchResult {
  id: string;
  crossMatchRequestId: string;
  bloodUnitId: string;
  unitNumber: string;
  crossMatchType: CrossMatchTypeEnum;
  isCompatible: boolean;
  performedBy: string;
  performedAt: string;
  notes: string | null;
  branchId: string;
}

export interface BloodIssue {
  id: string;
  issueNumber: string;
  crossMatchRequestId: string;
  patientName: string;
  patientId: string;
  bloodUnitId: string;
  unitNumber: string;
  componentType: string;
  issuedBy: string;
  issuedAt: string;
  status: BloodIssueStatusEnum;
  returnedAt: string | null;
  notes: string | null;
  branchId: string;
  createdAt: string;
}

export interface Transfusion {
  id: string;
  transfusionNumber: string;
  patientName: string;
  patientId: string;
  bloodUnitId: string;
  unitNumber: string;
  componentType: string;
  volumeMl: number;
  startTime: string;
  endTime: string | null;
  administeredBy: string;
  supervisedBy: string | null;
  status: TransfusionStatusEnum;
  outcome: TransfusionOutcomeEnum | null;
  preTransfusionVitals: string | null;
  postTransfusionVitals: string | null;
  notes: string | null;
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

export interface TransfusionReaction {
  id: string;
  transfusionId: string;
  reactionType: ReactionTypeEnum;
  severity: ReactionSeverityEnum;
  onsetTime: string;
  actionsTaken: string;
  reportedBy: string;
  reportedAt: string;
  outcome: string | null;
  notes: string | null;
  branchId: string;
  createdAt: string;
}

export interface HemovigilanceReport {
  id: string;
  reportNumber: string;
  transfusionId: string;
  transfusionNumber: string;
  patientName: string;
  reactionType: ReactionTypeEnum;
  severity: ReactionSeverityEnum;
  reportedBy: string;
  reportedAt: string;
  status: HemovigilanceStatusEnum;
  investigationNotes: string | null;
  closedAt: string | null;
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

export interface CrossMatchRequestCreate {
  patientName: string;
  patientId: string;
  bloodGroup: string;
  rhFactor: string;
  componentType: string;
  unitsRequested: number;
  urgency: string;
}

export interface BloodIssueCreateRequest {
  crossMatchRequestId: string;
  bloodUnitId: string;
  notes: string;
}

export interface TransfusionCreateRequest {
  bloodUnitId: string;
  patientName: string;
  patientId: string;
  volumeMl: number;
  startTime: string;
  preTransfusionVitals: string;
  notes: string;
}

export interface TransfusionCompleteRequest {
  endTime: string;
  volumeMl: number;
  outcome: TransfusionOutcomeEnum;
  postTransfusionVitals: string;
  notes: string;
}

export interface ReactionReportRequest {
  transfusionId: string;
  reactionType: ReactionTypeEnum;
  severity: ReactionSeverityEnum;
  onsetTime: string;
  actionsTaken: string;
  notes: string;
}

export const CROSS_MATCH_STATUS_OPTIONS = [
  { value: CrossMatchStatusEnum.PENDING, label: 'Pending' },
  { value: CrossMatchStatusEnum.COMPATIBLE, label: 'Compatible' },
  { value: CrossMatchStatusEnum.INCOMPATIBLE, label: 'Incompatible' },
  { value: CrossMatchStatusEnum.DEFERRED, label: 'Deferred' },
] as const;

export const REACTION_TYPE_OPTIONS = [
  { value: ReactionTypeEnum.FEBRILE, label: 'Febrile' },
  { value: ReactionTypeEnum.ALLERGIC, label: 'Allergic' },
  { value: ReactionTypeEnum.HEMOLYTIC, label: 'Hemolytic' },
  { value: ReactionTypeEnum.ANAPHYLACTIC, label: 'Anaphylactic' },
  { value: ReactionTypeEnum.TACO, label: 'TACO' },
  { value: ReactionTypeEnum.TRALI, label: 'TRALI' },
  { value: ReactionTypeEnum.DELAYED, label: 'Delayed' },
  { value: ReactionTypeEnum.OTHER, label: 'Other' },
] as const;

export const REACTION_SEVERITY_OPTIONS = [
  { value: ReactionSeverityEnum.MILD, label: 'Mild' },
  { value: ReactionSeverityEnum.MODERATE, label: 'Moderate' },
  { value: ReactionSeverityEnum.SEVERE, label: 'Severe' },
  { value: ReactionSeverityEnum.LIFE_THREATENING, label: 'Life Threatening' },
] as const;

export const HEMOVIGILANCE_STATUS_OPTIONS = [
  { value: HemovigilanceStatusEnum.REPORTED, label: 'Reported' },
  { value: HemovigilanceStatusEnum.UNDER_INVESTIGATION, label: 'Under Investigation' },
  { value: HemovigilanceStatusEnum.CLOSED, label: 'Closed' },
  { value: HemovigilanceStatusEnum.ESCALATED, label: 'Escalated' },
] as const;

export const TRANSFUSION_OUTCOME_OPTIONS = [
  { value: TransfusionOutcomeEnum.SUCCESSFUL, label: 'Successful' },
  { value: TransfusionOutcomeEnum.PARTIAL, label: 'Partial' },
  { value: TransfusionOutcomeEnum.UNSUCCESSFUL, label: 'Unsuccessful' },
  { value: TransfusionOutcomeEnum.ADVERSE_REACTION, label: 'Adverse Reaction' },
] as const;
