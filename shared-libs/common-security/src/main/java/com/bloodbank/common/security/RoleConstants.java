package com.bloodbank.common.security;

public final class RoleConstants {

    private RoleConstants() {
        // Prevent instantiation
    }

    // Realm roles
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String REGIONAL_ADMIN = "REGIONAL_ADMIN";
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";
    public static final String AUDITOR = "AUDITOR";

    // Client roles
    public static final String BRANCH_ADMIN = "BRANCH_ADMIN";
    public static final String BRANCH_MANAGER = "BRANCH_MANAGER";
    public static final String DOCTOR = "DOCTOR";
    public static final String LAB_TECHNICIAN = "LAB_TECHNICIAN";
    public static final String PHLEBOTOMIST = "PHLEBOTOMIST";
    public static final String NURSE = "NURSE";
    public static final String INVENTORY_MANAGER = "INVENTORY_MANAGER";
    public static final String BILLING_CLERK = "BILLING_CLERK";
    public static final String CAMP_COORDINATOR = "CAMP_COORDINATOR";
    public static final String RECEPTIONIST = "RECEPTIONIST";
    public static final String HOSPITAL_USER = "HOSPITAL_USER";
    public static final String DONOR = "DONOR";
}
