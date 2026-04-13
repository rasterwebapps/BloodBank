/**
 * Billing feature models matching backend DTOs.
 */

// ── Enums ────────────────────────────────────────────────────────

export enum InvoiceStatusEnum {
  DRAFT = 'DRAFT',
  ISSUED = 'ISSUED',
  PARTIALLY_PAID = 'PARTIALLY_PAID',
  PAID = 'PAID',
  CANCELLED = 'CANCELLED',
  OVERDUE = 'OVERDUE',
}

export enum PaymentMethodEnum {
  CASH = 'CASH',
  CHEQUE = 'CHEQUE',
  BANK_TRANSFER = 'BANK_TRANSFER',
  INSURANCE = 'INSURANCE',
  ONLINE = 'ONLINE',
}

export enum PaymentStatusEnum {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED',
}

export enum RateTypeEnum {
  PER_UNIT = 'PER_UNIT',
  FLAT_FEE = 'FLAT_FEE',
  PROCESSING = 'PROCESSING',
  CROSSMATCH = 'CROSSMATCH',
  SCREENING = 'SCREENING',
}

// ── Response Models ──────────────────────────────────────────────

export interface InvoiceLineItem {
  id: string;
  description: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  bloodUnitId: string | null;
  rateId: string | null;
}

export interface Invoice {
  id: string;
  invoiceNumber: string;
  hospitalId: string;
  hospitalName: string;
  issuedDate: string;
  dueDate: string;
  status: InvoiceStatusEnum;
  subtotal: number;
  taxAmount: number;
  discountAmount: number;
  totalAmount: number;
  paidAmount: number;
  balanceAmount: number;
  currency: string;
  notes: string | null;
  lineItems: InvoiceLineItem[];
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

export interface Payment {
  id: string;
  invoiceId: string;
  invoiceNumber: string;
  paymentDate: string;
  amount: number;
  method: PaymentMethodEnum;
  status: PaymentStatusEnum;
  referenceNumber: string | null;
  notes: string | null;
  processedBy: string;
  branchId: string;
  createdAt: string;
}

export interface CreditNote {
  id: string;
  creditNoteNumber: string;
  invoiceId: string;
  invoiceNumber: string;
  issuedDate: string;
  amount: number;
  reason: string;
  appliedAmount: number;
  branchId: string;
  createdAt: string;
}

export interface RateMaster {
  id: string;
  serviceCode: string;
  serviceName: string;
  rateType: RateTypeEnum;
  unitPrice: number;
  currency: string;
  effectiveFrom: string;
  effectiveTo: string | null;
  isActive: boolean;
  bloodGroupId: string | null;
  componentType: string | null;
  branchId: string;
  createdAt: string;
  updatedAt: string;
}

// ── Request Models ───────────────────────────────────────────────

export interface PaymentCreateRequest {
  invoiceId: string;
  paymentDate: string;
  amount: number;
  method: PaymentMethodEnum;
  referenceNumber?: string;
  notes?: string;
}

export interface RateMasterCreateRequest {
  serviceCode: string;
  serviceName: string;
  rateType: RateTypeEnum;
  unitPrice: number;
  currency: string;
  effectiveFrom: string;
  effectiveTo?: string;
  bloodGroupId?: string;
  componentType?: string;
}

export interface RateMasterUpdateRequest {
  serviceName: string;
  unitPrice: number;
  effectiveTo?: string;
  isActive: boolean;
}

// ── Display helpers ──────────────────────────────────────────────

export const INVOICE_STATUS_OPTIONS = [
  { value: InvoiceStatusEnum.DRAFT, label: 'Draft' },
  { value: InvoiceStatusEnum.ISSUED, label: 'Issued' },
  { value: InvoiceStatusEnum.PARTIALLY_PAID, label: 'Partially Paid' },
  { value: InvoiceStatusEnum.PAID, label: 'Paid' },
  { value: InvoiceStatusEnum.CANCELLED, label: 'Cancelled' },
  { value: InvoiceStatusEnum.OVERDUE, label: 'Overdue' },
] as const;

export const PAYMENT_METHOD_OPTIONS = [
  { value: PaymentMethodEnum.CASH, label: 'Cash' },
  { value: PaymentMethodEnum.CHEQUE, label: 'Cheque' },
  { value: PaymentMethodEnum.BANK_TRANSFER, label: 'Bank Transfer' },
  { value: PaymentMethodEnum.INSURANCE, label: 'Insurance' },
  { value: PaymentMethodEnum.ONLINE, label: 'Online' },
] as const;
