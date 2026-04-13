export enum LoyaltyTier {
  BRONZE = 'BRONZE',
  SILVER = 'SILVER',
  GOLD = 'GOLD',
  PLATINUM = 'PLATINUM',
}

export interface DonorProfile {
  id: string;
  firstName: string;
  lastName: string;
  bloodGroup: string;
  email: string;
  phone: string;
  donorId: string;
  loyaltyTier: LoyaltyTier;
  totalDonations: number;
  nextEligibleDate: string | null;
  profileImageUrl?: string;
}

export interface DonationRecord {
  id: string;
  date: string;
  location: string;
  volume: number;
  bloodGroup: string;
  status: string;
  certificateUrl?: string;
}

export interface Appointment {
  id: string;
  branchId: string;
  branchName: string;
  date: string;
  timeSlot: string;
  status: string;
  notes?: string;
}

export interface AppointmentRequest {
  branchId: string;
  date: string;
  timeSlot: string;
}

export interface EligibilityCheck {
  weight: number;
  hemoglobin: number;
  recentTravel: boolean;
  medications: boolean;
  lastDonationDate: string | null;
}

export interface EligibilityResult {
  eligible: boolean;
  reasons: string[];
  nextEligibleDate?: string;
}

export interface Camp {
  id: string;
  name: string;
  location: string;
  address: string;
  date: string;
  startTime: string;
  endTime: string;
  availableSlots: number;
  lat: number;
  lng: number;
}

export interface ReferralInfo {
  code: string;
  url: string;
  totalReferrals: number;
  successfulReferrals: number;
}

export interface Referral {
  id: string;
  name: string;
  email: string;
  status: string;
  date: string;
}

export interface Branch {
  id: string;
  name: string;
  city: string;
}
