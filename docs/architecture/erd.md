# Entity Relationship Description

This document describes the ~87 tables in the BloodBank single shared PostgreSQL 17 database (`bloodbank_db`), organized by domain. All tables are created via centralized Flyway migrations in `shared-libs/db-migration/`.

## Base Entity Columns

All tables inherit audit columns from base entity classes:

### BaseEntity (global scope)

| Column | Type | Description |
|---|---|---|
| `id` | `UUID` | Primary key (generated) |
| `created_at` | `TIMESTAMP` | Row creation time |
| `updated_at` | `TIMESTAMP` | Last update time |
| `created_by` | `VARCHAR(255)` | User who created the record |
| `updated_by` | `VARCHAR(255)` | User who last updated the record |
| `version` | `BIGINT` | Optimistic locking version |

### BranchScopedEntity (extends BaseEntity)

| Column | Type | Description |
|---|---|---|
| *(all BaseEntity columns)* | | |
| `branch_id` | `UUID NOT NULL` | FK → `branches(id)`, composite-indexed |

---

## Domain: Master Data (8 tables)

**Service:** branch-service
**Scope:** Global (BaseEntity)

| Table | Description | Key Columns |
|---|---|---|
| `blood_groups` | ABO/Rh blood group definitions | `code`, `name`, `description`, `is_active` |
| `component_types` | Blood component type definitions (Packed RBC, FFP, Platelets, etc.) | `code`, `name`, `shelf_life_days`, `storage_temp_min`, `storage_temp_max`, `is_active` |
| `deferral_reasons` | Reasons for donor deferral | `code`, `name`, `deferral_type` (TEMPORARY/PERMANENT), `deferral_days`, `is_active` |
| `reaction_types` | Transfusion reaction type definitions | `code`, `name`, `severity` (MILD/MODERATE/SEVERE/FATAL), `description`, `is_active` |
| `countries` | Country reference data | `iso_code`, `name`, `phone_code`, `currency_code`, `is_active` |
| `regions` | Geographic region/state/province | `name`, `country_id` (FK → countries), `is_active` |
| `cities` | City reference data | `name`, `region_id` (FK → regions), `is_active` |
| `icd_codes` | ICD-10 diagnosis codes (for transfusion indications) | `code`, `description`, `category`, `is_active` |

---

## Domain: Branch (4 tables)

**Service:** branch-service
**Scope:** Global (BaseEntity)

| Table | Description | Key Columns |
|---|---|---|
| `branches` | Blood bank branch locations | `name`, `code`, `branch_type`, `address`, `city_id` (FK → cities), `region_id` (FK → regions), `country_id` (FK → countries), `phone`, `email`, `latitude`, `longitude`, `is_active` |
| `branch_operating_hours` | Weekly operating schedule | `branch_id` (FK → branches), `day_of_week`, `open_time`, `close_time`, `is_closed` |
| `branch_equipment` | Equipment registry per branch | `branch_id` (FK → branches), `name`, `equipment_type`, `serial_number`, `last_maintenance_date`, `next_maintenance_date`, `status` |
| `branch_regions` | Branch-to-region assignment mapping | `branch_id` (FK → branches), `region_id` (FK → regions) |

---

## Domain: Donor (5 tables)

**Service:** donor-service
**Scope:** Branch-scoped (BranchScopedEntity)

| Table | Description | Key Columns |
|---|---|---|
| `donors` | Donor registration records | `first_name`, `last_name`, `date_of_birth`, `gender`, `blood_group_id` (FK → blood_groups), `email`, `phone`, `national_id`, `address`, `city_id`, `status` (ACTIVE/DEFERRED/INACTIVE), `registration_date` |
| `donor_health_records` | Health screening records per donation | `donor_id` (FK → donors), `screening_date`, `weight_kg`, `hemoglobin_gdl`, `blood_pressure_systolic`, `blood_pressure_diastolic`, `pulse_bpm`, `temperature_c`, `is_eligible`, `notes` |
| `donor_deferrals` | Donor deferral history | `donor_id` (FK → donors), `deferral_reason_id` (FK → deferral_reasons), `deferral_type`, `start_date`, `end_date`, `notes`, `deferred_by` |
| `donor_consents` | GDPR/regulatory consent records | `donor_id` (FK → donors), `consent_type`, `consent_given`, `consent_date`, `expiry_date`, `ip_address`, `document_id` |
| `donor_loyalty` | Loyalty/reward points tracking | `donor_id` (FK → donors), `total_donations`, `total_points`, `tier` (BRONZE/SILVER/GOLD/PLATINUM), `last_donation_date`, `next_eligible_date` |

---

## Domain: Collection (3 tables)

**Service:** donor-service
**Scope:** Branch-scoped (BranchScopedEntity)

| Table | Description | Key Columns |
|---|---|---|
| `collections` | Blood collection/donation records | `donor_id` (FK → donors), `collection_date`, `collection_type` (WHOLE_BLOOD/APHERESIS), `bag_number`, `volume_ml`, `phlebotomist_id`, `status` (IN_PROGRESS/COMPLETED/FAILED), `site` (BRANCH/CAMP), `camp_id` (FK → blood_camps, nullable) |
| `collection_adverse_reactions` | Adverse reactions during collection | `collection_id` (FK → collections), `reaction_type`, `severity`, `description`, `treatment_given`, `reported_by`, `reported_at` |
| `collection_samples` | Sample tubes separated from collection | `collection_id` (FK → collections), `sample_type`, `tube_barcode`, `status` |

---

## Domain: Blood Camp (4 tables)

**Service:** donor-service
**Scope:** Branch-scoped (BranchScopedEntity)

| Table | Description | Key Columns |
|---|---|---|
| `blood_camps` | Mobile blood drive events | `name`, `location`, `address`, `camp_date`, `start_time`, `end_time`, `coordinator_id`, `status` (PLANNED/ACTIVE/COMPLETED/CANCELLED), `target_units`, `collected_units` |
| `camp_resources` | Resources allocated to camps | `camp_id` (FK → blood_camps), `resource_type` (EQUIPMENT/STAFF/SUPPLIES), `name`, `quantity` |
| `camp_donors` | Donors registered for camps | `camp_id` (FK → blood_camps), `donor_id` (FK → donors), `registration_date`, `status` (REGISTERED/CHECKED_IN/DONATED/NO_SHOW) |
| `camp_collections` | Collections made at camps | `camp_id` (FK → blood_camps), `collection_id` (FK → collections), `sequence_number` |

---

## Domain: Testing (5 tables)

**Service:** lab-service
**Scope:** Branch-scoped (BranchScopedEntity)

| Table | Description | Key Columns |
|---|---|---|
| `test_orders` | Lab test order requests | `collection_id` (FK → collections), `sample_barcode`, `ordered_by`, `ordered_at`, `status` (PENDING/IN_PROGRESS/COMPLETED/CANCELLED), `priority` (ROUTINE/URGENT) |
| `test_results` | Individual test result records | `test_order_id` (FK → test_orders), `test_panel_id` (FK → test_panels), `result_value`, `result_status` (REACTIVE/NON_REACTIVE/INDETERMINATE), `instrument_id` (FK → lab_instruments), `performed_by`, `performed_at`, `verified_by`, `verified_at` |
| `test_panels` | Test panel definitions (HIV, HBV, HCV, Syphilis, Malaria, Blood Grouping) | `code`, `name`, `description`, `test_method`, `is_mandatory`, `is_active` |
| `lab_instruments` | Laboratory instrument registry | `name`, `model`, `serial_number`, `instrument_type`, `calibration_date`, `next_calibration_date`, `status` (ACTIVE/MAINTENANCE/DECOMMISSIONED) |
| `quality_control_records` | QC records for lab instruments | `instrument_id` (FK → lab_instruments), `qc_date`, `qc_type`, `result`, `status` (PASS/FAIL), `performed_by`, `notes` |

---

## Domain: Inventory (9 tables)

**Service:** inventory-service
**Scope:** Branch-scoped (BranchScopedEntity)

| Table | Description | Key Columns |
|---|---|---|
| `blood_units` | Whole blood unit records (post-collection) | `collection_id` (FK → collections), `donor_id` (FK → donors), `blood_group_id` (FK → blood_groups), `bag_number`, `volume_ml`, `collection_date`, `status` (QUARANTINE/AVAILABLE/RESERVED/ISSUED/EXPIRED/DISPOSED), `expiry_date` |
| `blood_components` | Processed components derived from blood units | `blood_unit_id` (FK → blood_units), `component_type_id` (FK → component_types), `component_number`, `volume_ml`, `status`, `expiry_date`, `storage_location_id` (FK → storage_locations), `isbt_code` |
| `component_processing` | Processing step records | `blood_component_id` (FK → blood_components), `processing_type` (SEPARATION/IRRADIATION/LEUKOREDUCTION/POOLING), `processed_by`, `processed_at`, `notes` |
| `component_labels` | ISBT 128 label records | `blood_component_id` (FK → blood_components), `label_type`, `label_data`, `printed_at`, `printed_by` |
| `pooled_components` | Pooled platelet/cryoprecipitate records | `pool_number`, `component_type_id`, `pool_date`, `volume_ml`, `status`, `expiry_date` |
| `storage_locations` | Refrigerator/freezer/shelf locations | `name`, `location_type` (REFRIGERATOR/FREEZER/PLATELET_AGITATOR/ROOM_TEMP), `temperature_min`, `temperature_max`, `capacity`, `current_count`, `status` |
| `stock_transfers` | Inter-branch stock transfer records | `from_branch_id` (FK → branches), `to_branch_id` (FK → branches), `blood_component_id` (FK → blood_components), `transfer_date`, `status` (REQUESTED/IN_TRANSIT/DELIVERED/CANCELLED), `requested_by`, `approved_by` |
| `unit_disposals` | Disposal/wastage records | `blood_component_id` (FK → blood_components), `disposal_reason` (EXPIRED/DAMAGED/REACTIVE/RECALLED), `disposal_date`, `disposed_by`, `approved_by`, `notes` |
| `unit_reservations` | Temporary hold/reservation on units | `blood_component_id` (FK → blood_components), `reserved_for_patient`, `reserved_by`, `reserved_at`, `expires_at`, `status` (ACTIVE/RELEASED/EXPIRED) |

---

## Domain: Transfusion (8 tables)

**Service:** transfusion-service
**Scope:** Branch-scoped (BranchScopedEntity)

| Table | Description | Key Columns |
|---|---|---|
| `crossmatch_requests` | Cross-match compatibility test requests | `patient_name`, `patient_id`, `blood_group_id`, `requested_units`, `diagnosis_icd_code`, `requesting_doctor_id`, `hospital_id`, `status` (PENDING/IN_PROGRESS/COMPATIBLE/INCOMPATIBLE), `urgency` (ROUTINE/URGENT/EMERGENCY) |
| `crossmatch_results` | Cross-match test results | `crossmatch_request_id` (FK → crossmatch_requests), `blood_component_id` (FK → blood_components), `result` (COMPATIBLE/INCOMPATIBLE), `method`, `tested_by`, `tested_at` |
| `blood_issues` | Blood unit issuing records | `crossmatch_request_id` (FK → crossmatch_requests), `blood_component_id` (FK → blood_components), `issued_to_hospital_id`, `issued_by`, `issued_at`, `received_by`, `received_at`, `status` |
| `emergency_issues` | Emergency O-negative issues (bypass cross-match) | `patient_name`, `patient_id`, `blood_component_id` (FK → blood_components), `issued_by`, `authorized_by`, `issued_at`, `reason`, `status` |
| `transfusions` | Transfusion administration records | `blood_issue_id` (FK → blood_issues), `patient_name`, `patient_id`, `start_time`, `end_time`, `administered_by`, `status` (IN_PROGRESS/COMPLETED/STOPPED), `notes` |
| `transfusion_reactions` | Adverse transfusion reaction records | `transfusion_id` (FK → transfusions), `reaction_type_id` (FK → reaction_types), `severity`, `onset_time`, `symptoms`, `treatment`, `outcome`, `reported_by`, `reported_at` |
| `hemovigilance_reports` | Formal hemovigilance reporting | `transfusion_reaction_id` (FK → transfusion_reactions), `report_number`, `report_date`, `investigation_findings`, `root_cause`, `corrective_action`, `status` (OPEN/UNDER_INVESTIGATION/CLOSED), `reported_to_authority` |
| `lookback_investigations` | Look-back investigation records (donor tested positive after donation) | `donor_id` (FK → donors), `trigger_test_result_id` (FK → test_results), `investigation_date`, `affected_units`, `affected_recipients`, `status`, `findings`, `actions_taken` |

---

## Domain: Hospital (4 tables)

**Service:** hospital-service
**Scope:** Branch-scoped (BranchScopedEntity)

| Table | Description | Key Columns |
|---|---|---|
| `hospitals` | Hospital/client registration | `name`, `code`, `address`, `city_id`, `contact_person`, `phone`, `email`, `hospital_type`, `bed_count`, `is_active` |
| `hospital_contracts` | Service contracts/SLAs | `hospital_id` (FK → hospitals), `contract_number`, `start_date`, `end_date`, `terms`, `discount_percentage`, `credit_limit`, `status` |
| `hospital_requests` | Blood request orders from hospitals | `hospital_id` (FK → hospitals), `patient_name`, `patient_id`, `blood_group_id`, `component_type_id`, `requested_units`, `urgency`, `status` (PENDING/MATCHED/ISSUED/CANCELLED), `requested_by`, `requested_at` |
| `hospital_feedback` | Hospital satisfaction/feedback records | `hospital_id` (FK → hospitals), `hospital_request_id` (FK → hospital_requests), `rating`, `feedback_text`, `submitted_by`, `submitted_at` |

---

## Domain: Billing (5 tables)

**Service:** billing-service
**Scope:** Branch-scoped (BranchScopedEntity)

| Table | Description | Key Columns |
|---|---|---|
| `rate_master` | Component pricing configuration | `component_type_id` (FK → component_types), `rate_amount`, `currency`, `effective_from`, `effective_to`, `rate_type` (STANDARD/EMERGENCY/DISCOUNTED), `is_active` |
| `invoices` | Invoice records | `hospital_id` (FK → hospitals), `invoice_number`, `invoice_date`, `due_date`, `subtotal`, `tax_amount`, `total_amount`, `currency`, `status` (DRAFT/SENT/PAID/OVERDUE/CANCELLED) |
| `invoice_line_items` | Invoice line item details | `invoice_id` (FK → invoices), `blood_component_id` (FK → blood_components), `component_type_id`, `quantity`, `unit_price`, `line_total`, `description` |
| `payments` | Payment records | `invoice_id` (FK → invoices), `payment_date`, `payment_amount`, `payment_method`, `reference_number`, `status` (PENDING/COMPLETED/FAILED/REFUNDED) |
| `credit_notes` | Credit note records for returns/adjustments | `invoice_id` (FK → invoices), `credit_note_number`, `amount`, `reason`, `issued_by`, `issued_at`, `status` |

---

## Domain: Notification (4 tables)

**Service:** notification-service
**Scope:** Global (BaseEntity)

| Table | Description | Key Columns |
|---|---|---|
| `notifications` | Notification delivery records | `recipient_id`, `recipient_type` (USER/DONOR/HOSPITAL), `channel` (EMAIL/SMS/PUSH/WHATSAPP), `subject`, `body`, `status` (PENDING/SENT/FAILED/READ), `sent_at`, `read_at` |
| `notification_templates` | Multi-language notification templates | `code`, `name`, `channel`, `subject_template`, `body_template`, `language` (en/es/fr), `is_active` |
| `notification_preferences` | User notification preference settings | `user_id`, `channel`, `category`, `is_enabled`, `quiet_hours_start`, `quiet_hours_end` |
| `campaigns` | Broadcast campaign records (donor drives, emergency appeals) | `name`, `campaign_type`, `target_audience`, `channel`, `message_template_id`, `scheduled_at`, `sent_at`, `total_recipients`, `total_sent`, `total_failed`, `status` |

---

## Domain: Compliance (5 tables)

**Service:** compliance-service
**Scope:** Global (BaseEntity)

| Table | Description | Key Columns |
|---|---|---|
| `regulatory_frameworks` | Regulatory framework definitions | `code`, `name`, `jurisdiction`, `description`, `version`, `effective_date`, `is_active` |
| `sop_documents` | Standard Operating Procedure documents | `title`, `sop_number`, `version`, `department`, `effective_date`, `review_date`, `document_id` (FK → documents), `status` (DRAFT/ACTIVE/RETIRED), `approved_by` |
| `licenses` | Branch/organizational license records | `license_type`, `license_number`, `issuing_authority`, `issue_date`, `expiry_date`, `status` (ACTIVE/EXPIRED/SUSPENDED), `branch_id` (FK → branches) |
| `deviations` | Quality deviation/incident records | `deviation_number`, `deviation_type`, `description`, `detected_date`, `detected_by`, `severity`, `root_cause`, `corrective_action`, `status` (OPEN/INVESTIGATING/RESOLVED/CLOSED), `branch_id` (FK → branches) |
| `recall_records` | Product recall records | `recall_number`, `recall_type`, `reason`, `affected_components`, `initiated_date`, `initiated_by`, `status` (INITIATED/IN_PROGRESS/COMPLETED), `branch_id` (FK → branches) |

---

## Domain: Reporting & Audit (6 tables)

**Service:** reporting-service
**Scope:** Global (BaseEntity)

| Table | Description | Key Columns |
|---|---|---|
| `audit_logs` | Immutable audit trail (INSERT only — DB trigger prevents UPDATE/DELETE) | `entity_type`, `entity_id`, `action` (CREATE/UPDATE/DELETE/ACCESS), `old_value` (JSONB), `new_value` (JSONB), `performed_by`, `performed_at`, `ip_address`, `branch_id` |
| `digital_signatures` | FDA 21 CFR Part 11 compliant electronic signatures | `entity_type`, `entity_id`, `signer_id`, `signature_hash`, `reason`, `signed_at`, `certificate_id` |
| `chain_of_custody` | Vein-to-vein traceability chain | `blood_unit_id`, `event_type`, `event_description`, `location`, `performed_by`, `performed_at`, `previous_custodian`, `next_custodian` |
| `report_metadata` | Report definition and generation metadata | `name`, `report_type`, `description`, `query_definition`, `output_format` (PDF/CSV/XLSX), `last_generated_at` |
| `report_schedules` | Scheduled report generation configuration | `report_metadata_id` (FK → report_metadata), `cron_expression`, `recipients`, `is_active`, `last_run_at`, `next_run_at` |
| `dashboard_widgets` | Dashboard widget configuration | `name`, `widget_type`, `data_source`, `configuration` (JSONB), `position`, `size`, `dashboard_type`, `role_visibility` |

---

## Domain: Document (2 tables)

**Service:** document-service
**Scope:** Branch-scoped (BranchScopedEntity)

| Table | Description | Key Columns |
|---|---|---|
| `documents` | Document metadata (files stored in MinIO/S3) | `title`, `document_type`, `mime_type`, `file_size`, `storage_path`, `entity_type`, `entity_id`, `uploaded_by`, `uploaded_at`, `is_active` |
| `document_versions` | Document version history | `document_id` (FK → documents), `version_number`, `storage_path`, `file_size`, `uploaded_by`, `uploaded_at`, `change_notes` |

---

## Domain: Logistics (4 tables)

**Service:** inventory-service
**Scope:** Branch-scoped (BranchScopedEntity)

| Table | Description | Key Columns |
|---|---|---|
| `transport_requests` | Blood transport scheduling | `from_branch_id`, `to_branch_id`, `requested_by`, `requested_at`, `pickup_time`, `delivery_time`, `status` (REQUESTED/SCHEDULED/IN_TRANSIT/DELIVERED), `priority` |
| `cold_chain_logs` | IoT temperature monitoring during transport | `transport_request_id` (FK → transport_requests), `temperature_c`, `recorded_at`, `gps_latitude`, `gps_longitude`, `is_within_range` |
| `transport_boxes` | Transport container registry | `box_number`, `box_type`, `capacity`, `status` (AVAILABLE/IN_USE/MAINTENANCE), `last_sanitized_at` |
| `delivery_confirmations` | Delivery receipt records | `transport_request_id` (FK → transport_requests), `received_by`, `received_at`, `condition_on_arrival`, `temperature_on_arrival`, `notes`, `signature_id` |

---

## Domain: Emergency (3 tables)

**Service:** request-matching-service
**Scope:** Branch-scoped (BranchScopedEntity)

| Table | Description | Key Columns |
|---|---|---|
| `emergency_requests` | Emergency blood requests | `blood_group_id`, `component_type_id`, `units_needed`, `hospital_id`, `patient_name`, `urgency` (CRITICAL/IMMEDIATE), `status` (OPEN/MATCHING/FULFILLED/EXPIRED), `requested_by`, `requested_at` |
| `disaster_events` | Mass casualty/disaster event records | `event_name`, `event_type`, `location`, `severity`, `start_date`, `end_date`, `estimated_casualties`, `blood_units_needed`, `status` (ACTIVE/RESOLVED) |
| `donor_mobilizations` | Donor mobilization campaigns for emergencies | `disaster_event_id` (FK → disaster_events), `blood_group_id`, `target_donors`, `mobilized_donors`, `channel`, `message_sent_at`, `status` |

---

## Domain: User Management (5 tables)

**Service:** Managed via Keycloak + reporting-service (audit)
**Scope:** Global (BaseEntity)

| Table | Description | Key Columns |
|---|---|---|
| `user_profiles` | Extended user profile data (beyond Keycloak) | `keycloak_user_id`, `employee_id`, `department`, `designation`, `phone`, `profile_picture_path`, `is_active` |
| `user_branch_assignments` | User-to-branch assignment mapping | `user_id` (FK → user_profiles), `branch_id` (FK → branches), `assigned_role`, `assigned_at`, `assigned_by`, `is_primary` |
| `user_activity_logs` | User activity tracking | `user_id`, `activity_type`, `description`, `ip_address`, `user_agent`, `performed_at` |
| `user_sessions` | Active session tracking | `user_id`, `session_id`, `login_at`, `last_active_at`, `ip_address`, `device_info`, `is_active` |
| `role_change_audit` | Role assignment change history | `user_id`, `old_role`, `new_role`, `changed_by`, `changed_at`, `reason` |

---

## Domain: System (4 tables)

**Service:** config-server / reporting-service
**Scope:** Global (BaseEntity)

| Table | Description | Key Columns |
|---|---|---|
| `system_settings` | System-wide configuration key-value pairs | `setting_key`, `setting_value`, `description`, `data_type`, `is_encrypted`, `updated_by` |
| `feature_flags` | Feature toggle configuration | `flag_name`, `is_enabled`, `description`, `applicable_branches` (JSONB), `updated_by` |
| `scheduled_jobs` | Scheduled job registry and status | `job_name`, `cron_expression`, `job_class`, `last_run_at`, `next_run_at`, `status`, `last_result` |
| `tenant_configs` | Multi-tenant configuration (country-specific settings) | `tenant_code`, `country_id`, `config_key`, `config_value`, `description` |

---

## Relationship Summary

```
donors ──< collections ──< collection_samples ──> test_orders ──< test_results
  │              │                                                      │
  │              └──> blood_units ──< blood_components ──> storage_locations
  │                                        │
  │                                        ├──> crossmatch_results
  │                                        ├──> blood_issues ──> transfusions ──< transfusion_reactions
  │                                        ├──> invoice_line_items ──> invoices
  │                                        └──> unit_disposals
  │
  ├──< donor_health_records
  ├──< donor_deferrals
  ├──< donor_consents
  └──< donor_loyalty

branches ──< branch_operating_hours
         ──< branch_equipment
         ──< stock_transfers (from/to)

hospitals ──< hospital_contracts
          ──< hospital_requests ──> crossmatch_requests
          ──< hospital_feedback
```

## Table Count Summary

| Domain | Count |
|---|---|
| Master Data | 8 |
| Branch | 4 |
| Donor | 5 |
| Collection | 3 |
| Blood Camp | 4 |
| Testing | 5 |
| Inventory | 9 |
| Transfusion | 8 |
| Hospital | 4 |
| Billing | 5 |
| Notification | 4 |
| Compliance | 5 |
| Reporting & Audit | 6 |
| Document | 2 |
| Logistics | 4 |
| Emergency | 3 |
| User Management | 5 |
| System | 4 |
| **Total** | **~87** |
