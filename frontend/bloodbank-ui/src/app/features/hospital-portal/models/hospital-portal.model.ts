export enum BloodGroupEnum {
  A_POS = 'A_POS',
  A_NEG = 'A_NEG',
  B_POS = 'B_POS',
  B_NEG = 'B_NEG',
  AB_POS = 'AB_POS',
  AB_NEG = 'AB_NEG',
  O_POS = 'O_POS',
  O_NEG = 'O_NEG',
}

export enum ComponentTypeEnum {
  WHOLE_BLOOD = 'WHOLE_BLOOD',
  PACKED_RBC = 'PACKED_RBC',
  PLATELETS = 'PLATELETS',
  FFP = 'FFP',
  CRYOPRECIPITATE = 'CRYOPRECIPITATE',
}

export enum RequestPriorityEnum {
  ROUTINE = 'ROUTINE',
  URGENT = 'URGENT',
  EMERGENCY = 'EMERGENCY',
}

export enum RequestStatusEnum {
  SUBMITTED = 'SUBMITTED',
  MATCHED = 'MATCHED',
  ISSUED = 'ISSUED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
}

export interface HospitalRequest {
  id: string;
  requestNumber: string;
  hospitalId: string;
  bloodGroup: BloodGroupEnum;
  componentType: ComponentTypeEnum;
  quantity: number;
  priority: RequestPriorityEnum;
  status: RequestStatusEnum;
  requiredDate: string;
  clinicalNotes: string;
  requestedAt: string;
  updatedAt: string;
}

export interface HospitalContract {
  id: string;
  hospitalId: string;
  hospitalName: string;
  contractNumber: string;
  startDate: string;
  endDate: string;
  slaResponseHours: number;
  slaFulfillmentRate: number;
  isActive: boolean;
  contractedUnits: number;
  usedUnits: number;
}

export interface HospitalFeedback {
  id: string;
  requestId: string;
  hospitalId: string;
  rating: number;
  comments: string;
  submittedAt: string;
}

export interface HospitalDashboardStats {
  activeRequests: number;
  pendingRequests: number;
  matchedRequests: number;
  deliveredThisMonth: number;
  contractStatus: string;
  contractExpiryDate: string;
  recentDeliveries: HospitalRequest[];
  pendingFeedback: HospitalRequest[];
}

export interface BloodRequestCreateRequest {
  bloodGroup: BloodGroupEnum;
  componentType: ComponentTypeEnum;
  quantity: number;
  priority: RequestPriorityEnum;
  requiredDate: string;
  clinicalNotes: string;
}

export interface FeedbackCreateRequest {
  requestId: string;
  rating: number;
  comments: string;
}

export const BLOOD_GROUP_OPTIONS = [
  { label: 'A+', value: BloodGroupEnum.A_POS },
  { label: 'A-', value: BloodGroupEnum.A_NEG },
  { label: 'B+', value: BloodGroupEnum.B_POS },
  { label: 'B-', value: BloodGroupEnum.B_NEG },
  { label: 'AB+', value: BloodGroupEnum.AB_POS },
  { label: 'AB-', value: BloodGroupEnum.AB_NEG },
  { label: 'O+', value: BloodGroupEnum.O_POS },
  { label: 'O-', value: BloodGroupEnum.O_NEG },
];

export const COMPONENT_TYPE_OPTIONS = [
  { label: 'Whole Blood', value: ComponentTypeEnum.WHOLE_BLOOD },
  { label: 'Packed RBC', value: ComponentTypeEnum.PACKED_RBC },
  { label: 'Platelets', value: ComponentTypeEnum.PLATELETS },
  { label: 'FFP', value: ComponentTypeEnum.FFP },
  { label: 'Cryoprecipitate', value: ComponentTypeEnum.CRYOPRECIPITATE },
];

export const PRIORITY_OPTIONS = [
  { label: 'Routine', value: RequestPriorityEnum.ROUTINE },
  { label: 'Urgent', value: RequestPriorityEnum.URGENT },
  { label: 'Emergency', value: RequestPriorityEnum.EMERGENCY },
];
