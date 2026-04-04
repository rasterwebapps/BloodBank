-- =============================================
-- V19: Composite Indexes
-- branch_id + status, branch_id + created_at, search columns, FK indexes
-- =============================================

-- =========================================
-- V1: Master Tables Indexes
-- =========================================
CREATE INDEX idx_regions_country_id ON regions(country_id);
CREATE INDEX idx_cities_region_id ON cities(region_id);
CREATE INDEX idx_deferral_reasons_type ON deferral_reasons(deferral_type);
CREATE INDEX idx_reaction_types_severity ON reaction_types(severity);

-- =========================================
-- V2: Branch Tables Indexes
-- =========================================
CREATE INDEX idx_branches_status ON branches(status);
CREATE INDEX idx_branches_city_id ON branches(city_id);
CREATE INDEX idx_branches_branch_type ON branches(branch_type);
CREATE INDEX idx_branches_parent_branch_id ON branches(parent_branch_id);
CREATE INDEX idx_branch_operating_hours_branch_id ON branch_operating_hours(branch_id);
CREATE INDEX idx_branch_equipment_branch_id_status ON branch_equipment(branch_id, status);
CREATE INDEX idx_branch_regions_branch_id ON branch_regions(branch_id);
CREATE INDEX idx_branch_regions_region_id ON branch_regions(region_id);

-- =========================================
-- V3: Donor Tables Indexes
-- =========================================
CREATE INDEX idx_donors_branch_id_status ON donors(branch_id, status);
CREATE INDEX idx_donors_branch_id_created_at ON donors(branch_id, created_at);
CREATE INDEX idx_donors_blood_group_id ON donors(blood_group_id);
CREATE INDEX idx_donors_email ON donors(email);
CREATE INDEX idx_donors_phone ON donors(phone);
CREATE INDEX idx_donors_national_id ON donors(national_id);
CREATE INDEX idx_donors_first_name_last_name ON donors(first_name, last_name);
CREATE INDEX idx_donors_city_id ON donors(city_id);
CREATE INDEX idx_donors_last_donation_date ON donors(last_donation_date);
CREATE INDEX idx_donor_health_records_branch_id ON donor_health_records(branch_id);
CREATE INDEX idx_donor_health_records_donor_id ON donor_health_records(donor_id);
CREATE INDEX idx_donor_health_records_screening_date ON donor_health_records(screening_date);
CREATE INDEX idx_donor_deferrals_branch_id_status ON donor_deferrals(branch_id, status);
CREATE INDEX idx_donor_deferrals_donor_id ON donor_deferrals(donor_id);
CREATE INDEX idx_donor_deferrals_deferral_reason_id ON donor_deferrals(deferral_reason_id);
CREATE INDEX idx_donor_consents_branch_id ON donor_consents(branch_id);
CREATE INDEX idx_donor_consents_donor_id ON donor_consents(donor_id);
CREATE INDEX idx_donor_consents_consent_type ON donor_consents(consent_type);
CREATE INDEX idx_donor_loyalty_branch_id ON donor_loyalty(branch_id);
CREATE INDEX idx_donor_loyalty_donor_id ON donor_loyalty(donor_id);
CREATE INDEX idx_donor_loyalty_tier ON donor_loyalty(tier);

-- =========================================
-- V4: Collection Tables Indexes
-- =========================================
CREATE INDEX idx_collections_branch_id_status ON collections(branch_id, status);
CREATE INDEX idx_collections_branch_id_created_at ON collections(branch_id, created_at);
CREATE INDEX idx_collections_donor_id ON collections(donor_id);
CREATE INDEX idx_collections_collection_date ON collections(collection_date);
CREATE INDEX idx_collections_collection_type ON collections(collection_type);
CREATE INDEX idx_collection_adverse_reactions_branch_id ON collection_adverse_reactions(branch_id);
CREATE INDEX idx_collection_adverse_reactions_collection_id ON collection_adverse_reactions(collection_id);
CREATE INDEX idx_collection_samples_branch_id ON collection_samples(branch_id);
CREATE INDEX idx_collection_samples_collection_id ON collection_samples(collection_id);
CREATE INDEX idx_collection_samples_status ON collection_samples(status);

-- =========================================
-- V5: Blood Camp Tables Indexes
-- =========================================
CREATE INDEX idx_blood_camps_branch_id_status ON blood_camps(branch_id, status);
CREATE INDEX idx_blood_camps_scheduled_date ON blood_camps(scheduled_date);
CREATE INDEX idx_blood_camps_city_id ON blood_camps(city_id);
CREATE INDEX idx_camp_resources_branch_id ON camp_resources(branch_id);
CREATE INDEX idx_camp_resources_camp_id ON camp_resources(camp_id);
CREATE INDEX idx_camp_donors_branch_id ON camp_donors(branch_id);
CREATE INDEX idx_camp_donors_camp_id ON camp_donors(camp_id);
CREATE INDEX idx_camp_donors_donor_id ON camp_donors(donor_id);
CREATE INDEX idx_camp_collections_branch_id ON camp_collections(branch_id);
CREATE INDEX idx_camp_collections_camp_id ON camp_collections(camp_id);

-- =========================================
-- V6: Lab Tables Indexes
-- =========================================
CREATE INDEX idx_lab_instruments_branch_id_status ON lab_instruments(branch_id, status);
CREATE INDEX idx_test_orders_branch_id_status ON test_orders(branch_id, status);
CREATE INDEX idx_test_orders_branch_id_created_at ON test_orders(branch_id, created_at);
CREATE INDEX idx_test_orders_sample_id ON test_orders(sample_id);
CREATE INDEX idx_test_orders_collection_id ON test_orders(collection_id);
CREATE INDEX idx_test_orders_donor_id ON test_orders(donor_id);
CREATE INDEX idx_test_orders_panel_id ON test_orders(panel_id);
CREATE INDEX idx_test_orders_order_date ON test_orders(order_date);
CREATE INDEX idx_test_results_branch_id ON test_results(branch_id);
CREATE INDEX idx_test_results_test_order_id ON test_results(test_order_id);
CREATE INDEX idx_test_results_test_name ON test_results(test_name);
CREATE INDEX idx_test_results_result_status ON test_results(result_status);
CREATE INDEX idx_test_results_instrument_id ON test_results(instrument_id);
CREATE INDEX idx_quality_control_records_branch_id ON quality_control_records(branch_id);
CREATE INDEX idx_quality_control_records_instrument_id ON quality_control_records(instrument_id);
CREATE INDEX idx_quality_control_records_qc_date ON quality_control_records(qc_date);

-- =========================================
-- V7: Inventory Tables Indexes
-- =========================================
CREATE INDEX idx_storage_locations_branch_id_status ON storage_locations(branch_id, status);
CREATE INDEX idx_blood_units_branch_id_status ON blood_units(branch_id, status);
CREATE INDEX idx_blood_units_branch_id_created_at ON blood_units(branch_id, created_at);
CREATE INDEX idx_blood_units_collection_id ON blood_units(collection_id);
CREATE INDEX idx_blood_units_donor_id ON blood_units(donor_id);
CREATE INDEX idx_blood_units_blood_group_id ON blood_units(blood_group_id);
CREATE INDEX idx_blood_units_expiry_date ON blood_units(expiry_date);
CREATE INDEX idx_blood_units_tti_status ON blood_units(tti_status);
CREATE INDEX idx_blood_units_storage_location_id ON blood_units(storage_location_id);
CREATE INDEX idx_blood_components_branch_id_status ON blood_components(branch_id, status);
CREATE INDEX idx_blood_components_branch_id_created_at ON blood_components(branch_id, created_at);
CREATE INDEX idx_blood_components_blood_unit_id ON blood_components(blood_unit_id);
CREATE INDEX idx_blood_components_component_type_id ON blood_components(component_type_id);
CREATE INDEX idx_blood_components_blood_group_id ON blood_components(blood_group_id);
CREATE INDEX idx_blood_components_expiry_date ON blood_components(expiry_date);
CREATE INDEX idx_blood_components_storage_location_id ON blood_components(storage_location_id);
CREATE INDEX idx_component_processing_branch_id ON component_processing(branch_id);
CREATE INDEX idx_component_processing_component_id ON component_processing(component_id);
CREATE INDEX idx_component_labels_branch_id ON component_labels(branch_id);
CREATE INDEX idx_component_labels_component_id ON component_labels(component_id);
CREATE INDEX idx_pooled_components_branch_id_status ON pooled_components(branch_id, status);
CREATE INDEX idx_pooled_components_component_type_id ON pooled_components(component_type_id);
CREATE INDEX idx_pooled_components_blood_group_id ON pooled_components(blood_group_id);
CREATE INDEX idx_pooled_components_expiry_date ON pooled_components(expiry_date);
CREATE INDEX idx_stock_transfers_branch_id_status ON stock_transfers(branch_id, status);
CREATE INDEX idx_stock_transfers_source_branch_id ON stock_transfers(source_branch_id);
CREATE INDEX idx_stock_transfers_destination_branch_id ON stock_transfers(destination_branch_id);
CREATE INDEX idx_stock_transfers_component_id ON stock_transfers(component_id);
CREATE INDEX idx_unit_disposals_branch_id ON unit_disposals(branch_id);
CREATE INDEX idx_unit_disposals_blood_unit_id ON unit_disposals(blood_unit_id);
CREATE INDEX idx_unit_disposals_component_id ON unit_disposals(component_id);
CREATE INDEX idx_unit_disposals_disposal_date ON unit_disposals(disposal_date);
CREATE INDEX idx_unit_reservations_branch_id_status ON unit_reservations(branch_id, status);
CREATE INDEX idx_unit_reservations_component_id ON unit_reservations(component_id);
CREATE INDEX idx_unit_reservations_expiry_date ON unit_reservations(expiry_date);

-- =========================================
-- V8: Transfusion Tables Indexes
-- =========================================
CREATE INDEX idx_crossmatch_requests_branch_id_status ON crossmatch_requests(branch_id, status);
CREATE INDEX idx_crossmatch_requests_hospital_id ON crossmatch_requests(hospital_id);
CREATE INDEX idx_crossmatch_requests_patient_blood_group ON crossmatch_requests(patient_blood_group_id);
CREATE INDEX idx_crossmatch_requests_component_type_id ON crossmatch_requests(component_type_id);
CREATE INDEX idx_crossmatch_results_branch_id ON crossmatch_results(branch_id);
CREATE INDEX idx_crossmatch_results_crossmatch_request_id ON crossmatch_results(crossmatch_request_id);
CREATE INDEX idx_crossmatch_results_component_id ON crossmatch_results(component_id);
CREATE INDEX idx_blood_issues_branch_id_status ON blood_issues(branch_id, status);
CREATE INDEX idx_blood_issues_branch_id_created_at ON blood_issues(branch_id, created_at);
CREATE INDEX idx_blood_issues_crossmatch_request_id ON blood_issues(crossmatch_request_id);
CREATE INDEX idx_blood_issues_component_id ON blood_issues(component_id);
CREATE INDEX idx_blood_issues_hospital_id ON blood_issues(hospital_id);
CREATE INDEX idx_blood_issues_issue_date ON blood_issues(issue_date);
CREATE INDEX idx_emergency_issues_branch_id ON emergency_issues(branch_id);
CREATE INDEX idx_emergency_issues_blood_issue_id ON emergency_issues(blood_issue_id);
CREATE INDEX idx_transfusions_branch_id_status ON transfusions(branch_id, status);
CREATE INDEX idx_transfusions_blood_issue_id ON transfusions(blood_issue_id);
CREATE INDEX idx_transfusions_hospital_id ON transfusions(hospital_id);
CREATE INDEX idx_transfusion_reactions_branch_id ON transfusion_reactions(branch_id);
CREATE INDEX idx_transfusion_reactions_transfusion_id ON transfusion_reactions(transfusion_id);
CREATE INDEX idx_transfusion_reactions_reaction_type_id ON transfusion_reactions(reaction_type_id);
CREATE INDEX idx_transfusion_reactions_severity ON transfusion_reactions(severity);
CREATE INDEX idx_hemovigilance_reports_branch_id_status ON hemovigilance_reports(branch_id, status);
CREATE INDEX idx_hemovigilance_reports_transfusion_reaction_id ON hemovigilance_reports(transfusion_reaction_id);
CREATE INDEX idx_lookback_investigations_branch_id_status ON lookback_investigations(branch_id, status);
CREATE INDEX idx_lookback_investigations_donor_id ON lookback_investigations(donor_id);

-- =========================================
-- V9: Hospital Tables Indexes
-- =========================================
CREATE INDEX idx_hospitals_branch_id_status ON hospitals(branch_id, status);
CREATE INDEX idx_hospitals_city_id ON hospitals(city_id);
CREATE INDEX idx_hospitals_hospital_type ON hospitals(hospital_type);
CREATE INDEX idx_hospital_contracts_branch_id_status ON hospital_contracts(branch_id, status);
CREATE INDEX idx_hospital_contracts_hospital_id ON hospital_contracts(hospital_id);
CREATE INDEX idx_hospital_contracts_end_date ON hospital_contracts(end_date);
CREATE INDEX idx_hospital_requests_branch_id_status ON hospital_requests(branch_id, status);
CREATE INDEX idx_hospital_requests_branch_id_created_at ON hospital_requests(branch_id, created_at);
CREATE INDEX idx_hospital_requests_hospital_id ON hospital_requests(hospital_id);
CREATE INDEX idx_hospital_requests_patient_blood_group ON hospital_requests(patient_blood_group_id);
CREATE INDEX idx_hospital_requests_component_type_id ON hospital_requests(component_type_id);
CREATE INDEX idx_hospital_requests_priority ON hospital_requests(priority);
CREATE INDEX idx_hospital_feedback_branch_id ON hospital_feedback(branch_id);
CREATE INDEX idx_hospital_feedback_hospital_id ON hospital_feedback(hospital_id);
CREATE INDEX idx_hospital_feedback_request_id ON hospital_feedback(request_id);

-- =========================================
-- V10: Billing Tables Indexes
-- =========================================
CREATE INDEX idx_rate_master_branch_id ON rate_master(branch_id);
CREATE INDEX idx_rate_master_component_type_id ON rate_master(component_type_id);
CREATE INDEX idx_rate_master_service_code ON rate_master(service_code);
CREATE INDEX idx_invoices_branch_id_status ON invoices(branch_id, status);
CREATE INDEX idx_invoices_branch_id_created_at ON invoices(branch_id, created_at);
CREATE INDEX idx_invoices_hospital_id ON invoices(hospital_id);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);
CREATE INDEX idx_invoice_line_items_branch_id ON invoice_line_items(branch_id);
CREATE INDEX idx_invoice_line_items_invoice_id ON invoice_line_items(invoice_id);
CREATE INDEX idx_payments_branch_id ON payments(branch_id);
CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
CREATE INDEX idx_payments_payment_date ON payments(payment_date);
CREATE INDEX idx_credit_notes_branch_id ON credit_notes(branch_id);
CREATE INDEX idx_credit_notes_invoice_id ON credit_notes(invoice_id);

-- =========================================
-- V11: Notification Tables Indexes
-- =========================================
CREATE INDEX idx_notifications_branch_id_status ON notifications(branch_id, status);
CREATE INDEX idx_notifications_branch_id_created_at ON notifications(branch_id, created_at);
CREATE INDEX idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX idx_notifications_channel ON notifications(channel);
CREATE INDEX idx_notifications_template_id ON notifications(template_id);
CREATE INDEX idx_notification_preferences_user_id ON notification_preferences(user_id);
CREATE INDEX idx_campaigns_branch_id_status ON campaigns(branch_id, status);
CREATE INDEX idx_campaigns_scheduled_at ON campaigns(scheduled_at);

-- =========================================
-- V12: Compliance Tables Indexes
-- =========================================
CREATE INDEX idx_regulatory_frameworks_country_id ON regulatory_frameworks(country_id);
CREATE INDEX idx_sop_documents_branch_id_status ON sop_documents(branch_id, status);
CREATE INDEX idx_sop_documents_category ON sop_documents(category);
CREATE INDEX idx_sop_documents_framework_id ON sop_documents(framework_id);
CREATE INDEX idx_licenses_branch_id_status ON licenses(branch_id, status);
CREATE INDEX idx_licenses_expiry_date ON licenses(expiry_date);
CREATE INDEX idx_deviations_branch_id_status ON deviations(branch_id, status);
CREATE INDEX idx_deviations_branch_id_created_at ON deviations(branch_id, created_at);
CREATE INDEX idx_deviations_severity ON deviations(severity);
CREATE INDEX idx_deviations_category ON deviations(category);
CREATE INDEX idx_recall_records_branch_id_status ON recall_records(branch_id, status);
CREATE INDEX idx_recall_records_recall_type ON recall_records(recall_type);
CREATE INDEX idx_recall_records_lookback_investigation_id ON recall_records(lookback_investigation_id);

-- =========================================
-- V13: Reporting Tables Indexes
-- =========================================
CREATE INDEX idx_audit_logs_branch_id ON audit_logs(branch_id);
CREATE INDEX idx_audit_logs_entity_type_entity_id ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_actor_id ON audit_logs(actor_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_digital_signatures_branch_id ON digital_signatures(branch_id);
CREATE INDEX idx_digital_signatures_entity_type_entity_id ON digital_signatures(entity_type, entity_id);
CREATE INDEX idx_digital_signatures_signer_id ON digital_signatures(signer_id);
CREATE INDEX idx_chain_of_custody_branch_id ON chain_of_custody(branch_id);
CREATE INDEX idx_chain_of_custody_entity_type_entity_id ON chain_of_custody(entity_type, entity_id);
CREATE INDEX idx_chain_of_custody_event_time ON chain_of_custody(event_time);
CREATE INDEX idx_report_metadata_branch_id ON report_metadata(branch_id);
CREATE INDEX idx_report_metadata_report_type ON report_metadata(report_type);
CREATE INDEX idx_report_schedules_branch_id ON report_schedules(branch_id);
CREATE INDEX idx_report_schedules_report_id ON report_schedules(report_id);
CREATE INDEX idx_dashboard_widgets_branch_id ON dashboard_widgets(branch_id);

-- =========================================
-- V14: Document Tables Indexes
-- =========================================
CREATE INDEX idx_documents_branch_id ON documents(branch_id);
CREATE INDEX idx_documents_entity_type_entity_id ON documents(entity_type, entity_id);
CREATE INDEX idx_documents_document_type ON documents(document_type);
CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_document_versions_branch_id ON document_versions(branch_id);
CREATE INDEX idx_document_versions_document_id ON document_versions(document_id);

-- =========================================
-- V15: Logistics Tables Indexes
-- =========================================
CREATE INDEX idx_transport_boxes_branch_id_status ON transport_boxes(branch_id, status);
CREATE INDEX idx_transport_requests_branch_id_status ON transport_requests(branch_id, status);
CREATE INDEX idx_transport_requests_branch_id_created_at ON transport_requests(branch_id, created_at);
CREATE INDEX idx_transport_requests_source_branch_id ON transport_requests(source_branch_id);
CREATE INDEX idx_transport_requests_destination_branch_id ON transport_requests(destination_branch_id);
CREATE INDEX idx_transport_requests_destination_hospital_id ON transport_requests(destination_hospital_id);
CREATE INDEX idx_transport_requests_transport_box_id ON transport_requests(transport_box_id);
CREATE INDEX idx_cold_chain_logs_branch_id ON cold_chain_logs(branch_id);
CREATE INDEX idx_cold_chain_logs_transport_request_id ON cold_chain_logs(transport_request_id);
CREATE INDEX idx_cold_chain_logs_storage_location_id ON cold_chain_logs(storage_location_id);
CREATE INDEX idx_cold_chain_logs_recorded_at ON cold_chain_logs(recorded_at);
CREATE INDEX idx_cold_chain_logs_is_within_range ON cold_chain_logs(is_within_range);
CREATE INDEX idx_delivery_confirmations_branch_id ON delivery_confirmations(branch_id);
CREATE INDEX idx_delivery_confirmations_transport_request_id ON delivery_confirmations(transport_request_id);

-- =========================================
-- V16: Emergency Tables Indexes
-- =========================================
CREATE INDEX idx_emergency_requests_branch_id_status ON emergency_requests(branch_id, status);
CREATE INDEX idx_emergency_requests_blood_group_id ON emergency_requests(blood_group_id);
CREATE INDEX idx_emergency_requests_component_type_id ON emergency_requests(component_type_id);
CREATE INDEX idx_emergency_requests_hospital_id ON emergency_requests(hospital_id);
CREATE INDEX idx_emergency_requests_priority ON emergency_requests(priority);
CREATE INDEX idx_emergency_requests_required_by ON emergency_requests(required_by);
CREATE INDEX idx_emergency_requests_disaster_event_id ON emergency_requests(disaster_event_id);
CREATE INDEX idx_disaster_events_branch_id_status ON disaster_events(branch_id, status);
CREATE INDEX idx_disaster_events_city_id ON disaster_events(city_id);
CREATE INDEX idx_disaster_events_start_date ON disaster_events(start_date);
CREATE INDEX idx_donor_mobilizations_branch_id ON donor_mobilizations(branch_id);
CREATE INDEX idx_donor_mobilizations_disaster_event_id ON donor_mobilizations(disaster_event_id);
CREATE INDEX idx_donor_mobilizations_emergency_request_id ON donor_mobilizations(emergency_request_id);
CREATE INDEX idx_donor_mobilizations_donor_id ON donor_mobilizations(donor_id);
CREATE INDEX idx_donor_mobilizations_response ON donor_mobilizations(response);

-- =========================================
-- V17: User Management Tables Indexes
-- =========================================
CREATE INDEX idx_user_profiles_email ON user_profiles(email);
CREATE INDEX idx_user_profiles_is_active ON user_profiles(is_active);
CREATE INDEX idx_user_branch_assignments_user_profile_id ON user_branch_assignments(user_profile_id);
CREATE INDEX idx_user_branch_assignments_branch_id ON user_branch_assignments(branch_id);
CREATE INDEX idx_user_branch_assignments_role_name ON user_branch_assignments(role_name);
CREATE INDEX idx_user_activity_logs_user_profile_id ON user_activity_logs(user_profile_id);
CREATE INDEX idx_user_activity_logs_branch_id ON user_activity_logs(branch_id);
CREATE INDEX idx_user_activity_logs_timestamp ON user_activity_logs(timestamp);
CREATE INDEX idx_user_activity_logs_activity_type ON user_activity_logs(activity_type);
CREATE INDEX idx_user_sessions_user_profile_id ON user_sessions(user_profile_id);
CREATE INDEX idx_user_sessions_branch_id ON user_sessions(branch_id);
CREATE INDEX idx_user_sessions_is_active ON user_sessions(is_active);
CREATE INDEX idx_role_change_audit_user_profile_id ON role_change_audit(user_profile_id);
CREATE INDEX idx_role_change_audit_branch_id ON role_change_audit(branch_id);
CREATE INDEX idx_role_change_audit_changed_at ON role_change_audit(changed_at);

-- =========================================
-- V18: System Tables Indexes
-- =========================================
CREATE INDEX idx_system_settings_category ON system_settings(category);
CREATE INDEX idx_feature_flags_is_enabled ON feature_flags(is_enabled);
CREATE INDEX idx_scheduled_jobs_is_active ON scheduled_jobs(is_active);
CREATE INDEX idx_scheduled_jobs_next_run_at ON scheduled_jobs(next_run_at);
CREATE INDEX idx_tenant_configs_branch_id ON tenant_configs(branch_id);
