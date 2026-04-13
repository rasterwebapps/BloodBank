export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';

export type UserRole =
  | 'SUPER_ADMIN'
  | 'REGIONAL_ADMIN'
  | 'SYSTEM_ADMIN'
  | 'AUDITOR'
  | 'BRANCH_ADMIN'
  | 'BRANCH_MANAGER'
  | 'DOCTOR'
  | 'LAB_TECHNICIAN'
  | 'PHLEBOTOMIST'
  | 'NURSE'
  | 'INVENTORY_MANAGER'
  | 'BILLING_CLERK'
  | 'CAMP_COORDINATOR'
  | 'RECEPTIONIST'
  | 'HOSPITAL_USER'
  | 'DONOR';

export interface User {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  status: UserStatus;
  roles: UserRole[];
  branchId?: string;
  branchName?: string;
  createdAt: string;
  lastLoginAt?: string;
}

export interface UserActivity {
  id: string;
  userId: string;
  action: string;
  resourceType: string;
  resourceId?: string;
  description: string;
  ipAddress?: string;
  occurredAt: string;
}

export interface UserCreateRequest {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  roles: UserRole[];
  branchId?: string;
  temporaryPassword: string;
}

export interface UserUpdateRequest {
  firstName: string;
  lastName: string;
  email: string;
  roles: UserRole[];
  branchId?: string;
  status: UserStatus;
}

export const ASSIGNABLE_ROLES: UserRole[] = [
  'BRANCH_ADMIN',
  'BRANCH_MANAGER',
  'DOCTOR',
  'LAB_TECHNICIAN',
  'PHLEBOTOMIST',
  'NURSE',
  'INVENTORY_MANAGER',
  'BILLING_CLERK',
  'CAMP_COORDINATOR',
  'RECEPTIONIST',
  'HOSPITAL_USER',
  'DONOR',
];
