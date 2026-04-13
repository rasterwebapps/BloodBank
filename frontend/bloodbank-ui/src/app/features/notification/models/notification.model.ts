/**
 * Notification feature models matching backend DTOs.
 */

// ── Enums ────────────────────────────────────────────────────────

export enum NotificationTypeEnum {
  EMAIL = 'EMAIL',
  SMS = 'SMS',
  PUSH = 'PUSH',
  IN_APP = 'IN_APP',
}

export enum NotificationStatusEnum {
  PENDING = 'PENDING',
  SENT = 'SENT',
  DELIVERED = 'DELIVERED',
  FAILED = 'FAILED',
  BOUNCED = 'BOUNCED',
}

export enum TemplateTypeEnum {
  DONATION_REMINDER = 'DONATION_REMINDER',
  APPOINTMENT_CONFIRMATION = 'APPOINTMENT_CONFIRMATION',
  DEFERRAL_NOTICE = 'DEFERRAL_NOTICE',
  BLOOD_REQUEST_ALERT = 'BLOOD_REQUEST_ALERT',
  CAMP_INVITATION = 'CAMP_INVITATION',
  RESULT_NOTIFICATION = 'RESULT_NOTIFICATION',
  SYSTEM_ALERT = 'SYSTEM_ALERT',
  CUSTOM = 'CUSTOM',
}

export enum CampaignStatusEnum {
  DRAFT = 'DRAFT',
  SCHEDULED = 'SCHEDULED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

// ── Response Models ──────────────────────────────────────────────

export interface Notification {
  id: string;
  recipientId: string;
  recipientName: string;
  recipientContact: string;
  type: NotificationTypeEnum;
  subject: string | null;
  body: string;
  status: NotificationStatusEnum;
  sentAt: string | null;
  deliveredAt: string | null;
  failureReason: string | null;
  templateId: string | null;
  campaignId: string | null;
  branchId: string;
  createdAt: string;
}

export interface NotificationTemplate {
  id: string;
  code: string;
  name: string;
  type: TemplateTypeEnum;
  channel: NotificationTypeEnum;
  subject: string | null;
  body: string;
  variables: string[];
  isActive: boolean;
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

export interface Campaign {
  id: string;
  name: string;
  description: string | null;
  templateId: string;
  templateName: string;
  channel: NotificationTypeEnum;
  status: CampaignStatusEnum;
  scheduledAt: string | null;
  targetCriteria: string | null;
  totalRecipients: number;
  sentCount: number;
  deliveredCount: number;
  failedCount: number;
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

export interface Preference {
  id: string;
  userId: string;
  emailEnabled: boolean;
  smsEnabled: boolean;
  pushEnabled: boolean;
  inAppEnabled: boolean;
  donationReminders: boolean;
  appointmentReminders: boolean;
  systemAlerts: boolean;
  campaignMessages: boolean;
  updatedAt: string;
}

// ── Request Models ───────────────────────────────────────────────

export interface NotificationTemplateCreateRequest {
  code: string;
  name: string;
  type: TemplateTypeEnum;
  channel: NotificationTypeEnum;
  subject?: string;
  body: string;
}

export interface CampaignCreateRequest {
  name: string;
  description?: string;
  templateId: string;
  scheduledAt?: string;
  targetCriteria?: string;
}

export interface PreferenceUpdateRequest {
  emailEnabled: boolean;
  smsEnabled: boolean;
  pushEnabled: boolean;
  inAppEnabled: boolean;
  donationReminders: boolean;
  appointmentReminders: boolean;
  systemAlerts: boolean;
  campaignMessages: boolean;
}
