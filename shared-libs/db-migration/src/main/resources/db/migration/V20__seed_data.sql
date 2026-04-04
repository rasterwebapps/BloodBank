-- =============================================
-- V20: Seed / Reference Data
-- Blood groups, component types, WHO test panels, countries,
-- notification templates
-- =============================================

-- =========================================
-- Blood Groups (ABO + Rh)
-- =========================================
INSERT INTO blood_groups (group_name, description) VALUES
    ('A+',  'Blood Group A, Rh Positive'),
    ('A-',  'Blood Group A, Rh Negative'),
    ('B+',  'Blood Group B, Rh Positive'),
    ('B-',  'Blood Group B, Rh Negative'),
    ('AB+', 'Blood Group AB, Rh Positive'),
    ('AB-', 'Blood Group AB, Rh Negative'),
    ('O+',  'Blood Group O, Rh Positive'),
    ('O-',  'Blood Group O, Rh Negative');

-- =========================================
-- Component Types
-- =========================================
INSERT INTO component_types (type_code, type_name, description, shelf_life_days, storage_temp_min, storage_temp_max) VALUES
    ('WB',    'Whole Blood',                'Unprocessed whole blood',                             35, 2.00, 6.00),
    ('PRBC',  'Packed Red Blood Cells',     'Red blood cells separated from whole blood',          42, 2.00, 6.00),
    ('FFP',   'Fresh Frozen Plasma',        'Plasma frozen within 8 hours of collection',         365, -30.00, -18.00),
    ('PC',    'Platelet Concentrate',       'Platelets from whole blood',                           5, 20.00, 24.00),
    ('SDP',   'Single Donor Platelets',     'Platelets collected by apheresis',                     5, 20.00, 24.00),
    ('CRYO',  'Cryoprecipitate',            'Fibrinogen-rich fraction from FFP',                  365, -30.00, -18.00),
    ('FP24',  'Frozen Plasma 24hr',         'Plasma frozen within 24 hours of collection',        365, -30.00, -18.00),
    ('LRBC',  'Leukoreduced RBC',           'RBC with leukocyte reduction',                        42, 2.00, 6.00),
    ('WRBC',  'Washed Red Blood Cells',     'RBC washed to remove plasma proteins',                24, 2.00, 6.00),
    ('IRBC',  'Irradiated RBC',             'RBC treated with gamma irradiation',                  28, 2.00, 6.00),
    ('GP',    'Granulocyte Concentrate',    'Granulocytes for neutropenic patients',                1, 20.00, 24.00);

-- =========================================
-- WHO Mandatory Test Panels
-- =========================================
INSERT INTO test_panels (panel_code, panel_name, description, test_names, is_mandatory) VALUES
    ('TTI_MANDATORY', 'Mandatory TTI Panel',
     'WHO-recommended mandatory transfusion-transmitted infection screening',
     'HIV_1_2,HBsAg,HCV,SYPHILIS',
     TRUE),
    ('TTI_EXTENDED', 'Extended TTI Panel',
     'Extended TTI panel including additional regional tests',
     'HIV_1_2,HBsAg,HCV,SYPHILIS,MALARIA,CHAGAS,HTLV_I_II',
     FALSE),
    ('BLOOD_GROUPING', 'Blood Grouping Panel',
     'ABO and Rh blood group determination',
     'ABO_GROUPING,RH_TYPING,ANTIBODY_SCREEN',
     TRUE),
    ('CROSSMATCH', 'Crossmatch Panel',
     'Compatibility testing panel',
     'IMMEDIATE_SPIN,ANTIGLOBULIN,AUTOCONTROL',
     TRUE),
    ('ANTIBODY_ID', 'Antibody Identification Panel',
     'Panel for identifying irregular antibodies',
     'ANTIBODY_IDENTIFICATION,DAT,ELUTION',
     FALSE),
    ('COMPONENT_QC', 'Component QC Panel',
     'Quality control testing for blood components',
     'VOLUME,HEMATOCRIT,WBC_COUNT,PLATELET_COUNT,PH,SWIRL,STERILITY',
     FALSE);

-- =========================================
-- Deferral Reasons
-- =========================================
INSERT INTO deferral_reasons (reason_code, reason_description, deferral_type, default_days) VALUES
    ('LOW_HB',         'Low hemoglobin level',                        'TEMPORARY', 90),
    ('LOW_WEIGHT',     'Below minimum weight requirement',            'TEMPORARY', 180),
    ('RECENT_SURGERY', 'Recent surgery or dental procedure',          'TEMPORARY', 180),
    ('MEDICATION',     'Currently on deferral medication',            'TEMPORARY', 30),
    ('TRAVEL_MALARIA', 'Travel to malaria-endemic area',              'TEMPORARY', 365),
    ('PREGNANCY',      'Pregnant or recently delivered',              'TEMPORARY', 365),
    ('TATTOO_PIERCING','Recent tattoo or body piercing',              'TEMPORARY', 365),
    ('COLD_FLU',       'Cold, flu, or infection symptoms',            'TEMPORARY', 14),
    ('VACCINATION',    'Recent vaccination',                          'TEMPORARY', 28),
    ('TRANSFUSION_RX', 'Previous blood transfusion',                  'TEMPORARY', 365),
    ('HIV_POSITIVE',   'HIV positive test result',                    'PERMANENT', NULL),
    ('HBV_CARRIER',    'Hepatitis B carrier',                         'PERMANENT', NULL),
    ('HCV_POSITIVE',   'Hepatitis C positive',                        'PERMANENT', NULL),
    ('IV_DRUG_USE',    'History of intravenous drug use',             'PERMANENT', NULL),
    ('CJD_RISK',       'Creutzfeldt-Jakob disease risk factors',      'PERMANENT', NULL);

-- =========================================
-- Reaction Types
-- =========================================
INSERT INTO reaction_types (reaction_code, reaction_name, severity, description) VALUES
    ('FNHTR',   'Febrile Non-Hemolytic',            'MILD',     'Temperature rise ≥1°C during or after transfusion'),
    ('ALLERGIC_MILD', 'Mild Allergic Reaction',      'MILD',     'Urticaria, itching, or rash'),
    ('ALLERGIC_SEV',  'Severe Allergic/Anaphylactic', 'SEVERE',  'Anaphylaxis with hypotension, bronchospasm'),
    ('AHTR',    'Acute Hemolytic Reaction',          'SEVERE',   'ABO incompatible transfusion with hemolysis'),
    ('DHTR',    'Delayed Hemolytic Reaction',        'MODERATE', 'Hemolysis occurring 1-28 days post-transfusion'),
    ('TRALI',   'Transfusion-Related Acute Lung Injury', 'SEVERE', 'Non-cardiogenic pulmonary edema'),
    ('TACO',    'Transfusion-Associated Circulatory Overload', 'MODERATE', 'Volume overload with respiratory distress'),
    ('TAD',     'Transfusion-Associated Dyspnea',    'MILD',     'Respiratory distress not meeting TRALI/TACO criteria'),
    ('SEPTIC',  'Septic Transfusion Reaction',       'SEVERE',   'Bacterial contamination of blood product'),
    ('PTP',     'Post-Transfusion Purpura',          'SEVERE',   'Thrombocytopenia 5-12 days post-transfusion'),
    ('TA_GVHD', 'Transfusion-Associated GVHD',      'FATAL',    'Graft-versus-host disease from viable donor lymphocytes'),
    ('IRON_OVR','Transfusional Iron Overload',        'MODERATE', 'Iron accumulation from chronic transfusions');

-- =========================================
-- Countries (key countries for a global blood bank system)
-- =========================================
INSERT INTO countries (country_code, country_name, phone_code) VALUES
    ('USA', 'United States of America', '+1'),
    ('GBR', 'United Kingdom',           '+44'),
    ('CAN', 'Canada',                   '+1'),
    ('AUS', 'Australia',                '+61'),
    ('IND', 'India',                    '+91'),
    ('BRA', 'Brazil',                   '+55'),
    ('DEU', 'Germany',                  '+49'),
    ('FRA', 'France',                   '+33'),
    ('JPN', 'Japan',                    '+81'),
    ('ZAF', 'South Africa',            '+27'),
    ('NGA', 'Nigeria',                  '+234'),
    ('KEN', 'Kenya',                    '+254'),
    ('EGY', 'Egypt',                    '+20'),
    ('SAU', 'Saudi Arabia',            '+966'),
    ('ARE', 'United Arab Emirates',     '+971'),
    ('SGP', 'Singapore',               '+65'),
    ('MYS', 'Malaysia',                '+60'),
    ('THA', 'Thailand',                '+66'),
    ('MEX', 'Mexico',                   '+52'),
    ('COL', 'Colombia',                '+57');

-- =========================================
-- Notification Templates
-- =========================================
INSERT INTO notification_templates (template_code, template_name, channel, subject, body_template, variables) VALUES
    ('DONATION_REMINDER', 'Donation Reminder',
     'EMAIL', 'Time to donate blood again!',
     'Dear {{donorName}}, it has been {{daysSinceLastDonation}} days since your last donation. You are now eligible to donate again. Please visit your nearest branch or schedule an appointment.',
     'donorName,daysSinceLastDonation'),

    ('DONATION_THANKYOU', 'Post-Donation Thank You',
     'EMAIL', 'Thank you for your blood donation!',
     'Dear {{donorName}}, thank you for your generous blood donation on {{donationDate}} at {{branchName}}. Your donation can save up to 3 lives!',
     'donorName,donationDate,branchName'),

    ('RESULT_READY', 'Test Results Ready',
     'EMAIL', 'Your blood test results are ready',
     'Dear {{donorName}}, the test results for your donation on {{donationDate}} are now available. Please log in to your donor portal to view them.',
     'donorName,donationDate'),

    ('EMERGENCY_APPEAL', 'Emergency Blood Appeal',
     'SMS', NULL,
     'URGENT: {{bloodGroup}} blood needed at {{branchName}}. If you can donate, please visit {{branchAddress}} or call {{branchPhone}}.',
     'bloodGroup,branchName,branchAddress,branchPhone'),

    ('APPOINTMENT_CONFIRMATION', 'Appointment Confirmation',
     'EMAIL', 'Your donation appointment is confirmed',
     'Dear {{donorName}}, your blood donation appointment is confirmed for {{appointmentDate}} at {{appointmentTime}} at {{branchName}}. Please remember to eat well and stay hydrated before your appointment.',
     'donorName,appointmentDate,appointmentTime,branchName'),

    ('STOCK_ALERT', 'Low Stock Alert',
     'IN_APP', 'Low blood stock alert',
     'ALERT: {{componentType}} {{bloodGroup}} stock at {{branchName}} is critically low. Current stock: {{currentStock}} units. Minimum required: {{minStock}} units.',
     'componentType,bloodGroup,branchName,currentStock,minStock'),

    ('RECALL_NOTIFICATION', 'Product Recall Notification',
     'EMAIL', 'Blood product recall notification',
     'A recall has been initiated for blood product(s) from donation {{donationNumber}}. Recall number: {{recallNumber}}. Reason: {{recallReason}}. Please take immediate action per SOP.',
     'donationNumber,recallNumber,recallReason'),

    ('DONOR_DEFERRED', 'Donor Deferral Notice',
     'EMAIL', 'Donation deferral notice',
     'Dear {{donorName}}, we regret to inform you that you have been temporarily deferred from donating blood. Reason: {{deferralReason}}. You will be eligible to donate again after {{reinstatementDate}}.',
     'donorName,deferralReason,reinstatementDate'),

    ('CAMP_INVITATION', 'Blood Camp Invitation',
     'SMS', NULL,
     'You are invited to a blood donation camp at {{campVenue}} on {{campDate}}. Organized by {{organizerName}}. Register now!',
     'campVenue,campDate,organizerName'),

    ('LOYALTY_MILESTONE', 'Loyalty Milestone Achievement',
     'IN_APP', 'Congratulations on your milestone!',
     'Dear {{donorName}}, congratulations! You have reached {{milestone}} blood donations. You have been upgraded to {{tierName}} tier. Thank you for being a life saver!',
     'donorName,milestone,tierName');

-- =========================================
-- ICD Codes (common codes for blood transfusion indications)
-- =========================================
INSERT INTO icd_codes (icd_code, description, category) VALUES
    ('D50.0', 'Iron deficiency anemia secondary to blood loss (chronic)', 'Anemia'),
    ('D50.9', 'Iron deficiency anemia, unspecified', 'Anemia'),
    ('D61.9', 'Aplastic anemia, unspecified', 'Anemia'),
    ('D62',   'Acute posthemorrhagic anemia', 'Anemia'),
    ('D64.9', 'Anemia, unspecified', 'Anemia'),
    ('D65',   'Disseminated intravascular coagulation', 'Coagulation'),
    ('D66',   'Hereditary factor VIII deficiency (Hemophilia A)', 'Coagulation'),
    ('D67',   'Hereditary factor IX deficiency (Hemophilia B)', 'Coagulation'),
    ('D68.9', 'Coagulation defect, unspecified', 'Coagulation'),
    ('D69.6', 'Thrombocytopenia, unspecified', 'Thrombocytopenia'),
    ('R58',   'Hemorrhage, not elsewhere classified', 'Hemorrhage'),
    ('T79.4', 'Traumatic shock', 'Trauma'),
    ('O72.1', 'Other immediate postpartum hemorrhage', 'Obstetric'),
    ('O72.3', 'Postpartum coagulation defects', 'Obstetric'),
    ('C91.0', 'Acute lymphoblastic leukemia', 'Oncology'),
    ('C92.0', 'Acute myeloblastic leukemia', 'Oncology'),
    ('D46.9', 'Myelodysplastic syndrome, unspecified', 'Oncology'),
    ('D57.1', 'Sickle-cell disease without crisis', 'Hemoglobinopathy'),
    ('D56.1', 'Beta thalassemia', 'Hemoglobinopathy'),
    ('T80.0', 'Air embolism following infusion, transfusion and therapeutic injection', 'Transfusion Complication');
