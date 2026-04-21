/**
 * Hospital Data Generator — BloodBank Performance Tests
 *
 * Generates realistic hospital records for seeding and load testing.
 */

import { randomItem, randomInt } from '../k6.config.js';

// ---------------------------------------------------------------------------
// Data pools
// ---------------------------------------------------------------------------
const HOSPITAL_PREFIXES = [
  'St.', 'Mount', 'General', 'Regional', 'Memorial', 'University',
  'City', 'Community', 'Central', 'National', 'Sacred Heart', 'Holy Cross',
  'Mercy', 'Hope', 'Grace', 'Victory', 'Sunrise', 'Lakeside', 'Riverside',
  'Westside', 'Eastside', 'Northside', 'Southside',
];

const HOSPITAL_SUFFIXES = [
  'Hospital',
  'Medical Center',
  'Health System',
  'Healthcare',
  'Medical Institute',
  'Clinic',
  'Health Center',
  'Medical Group',
];

const SPECIALIZATIONS = [
  'GENERAL',
  'TRAUMA_CENTER',
  'PEDIATRIC',
  'CARDIAC',
  'ONCOLOGY',
  'NEUROLOGY',
  'ORTHOPEDIC',
  'MATERNITY',
  'BURN_CENTER',
  'TRANSPLANT_CENTER',
];

const HOSPITAL_TYPES = ['PUBLIC', 'PRIVATE', 'NON_PROFIT', 'GOVERNMENT', 'MILITARY'];

const CITY_STATE_PAIRS = [
  { city: 'New York',       state: 'NY', zip: '10001' },
  { city: 'Los Angeles',    state: 'CA', zip: '90001' },
  { city: 'Chicago',        state: 'IL', zip: '60601' },
  { city: 'Houston',        state: 'TX', zip: '77001' },
  { city: 'Phoenix',        state: 'AZ', zip: '85001' },
  { city: 'Philadelphia',   state: 'PA', zip: '19101' },
  { city: 'San Antonio',    state: 'TX', zip: '78201' },
  { city: 'San Diego',      state: 'CA', zip: '92101' },
  { city: 'Dallas',         state: 'TX', zip: '75201' },
  { city: 'San Jose',       state: 'CA', zip: '95101' },
  { city: 'Austin',         state: 'TX', zip: '73301' },
  { city: 'Jacksonville',   state: 'FL', zip: '32099' },
  { city: 'Fort Worth',     state: 'TX', zip: '76101' },
  { city: 'Columbus',       state: 'OH', zip: '43085' },
  { city: 'Charlotte',      state: 'NC', zip: '28201' },
  { city: 'Indianapolis',   state: 'IN', zip: '46201' },
  { city: 'San Francisco',  state: 'CA', zip: '94102' },
  { city: 'Seattle',        state: 'WA', zip: '98101' },
  { city: 'Denver',         state: 'CO', zip: '80201' },
  { city: 'Nashville',      state: 'TN', zip: '37201' },
  { city: 'Oklahoma City',  state: 'OK', zip: '73101' },
  { city: 'El Paso',        state: 'TX', zip: '79901' },
  { city: 'Washington',     state: 'DC', zip: '20001' },
  { city: 'Las Vegas',      state: 'NV', zip: '89101' },
  { city: 'Portland',       state: 'OR', zip: '97201' },
  { city: 'Memphis',        state: 'TN', zip: '37501' },
  { city: 'Louisville',     state: 'KY', zip: '40201' },
  { city: 'Baltimore',      state: 'MD', zip: '21201' },
  { city: 'Milwaukee',      state: 'WI', zip: '53201' },
  { city: 'Albuquerque',    state: 'NM', zip: '87101' },
  { city: 'Tucson',         state: 'AZ', zip: '85701' },
  { city: 'Fresno',         state: 'CA', zip: '93650' },
  { city: 'Sacramento',     state: 'CA', zip: '94203' },
  { city: 'Mesa',           state: 'AZ', zip: '85201' },
  { city: 'Kansas City',    state: 'MO', zip: '64101' },
  { city: 'Atlanta',        state: 'GA', zip: '30301' },
  { city: 'Omaha',          state: 'NE', zip: '68101' },
  { city: 'Colorado Springs', state: 'CO', zip: '80901' },
  { city: 'Raleigh',        state: 'NC', zip: '27601' },
  { city: 'Long Beach',     state: 'CA', zip: '90801' },
  { city: 'Virginia Beach', state: 'VA', zip: '23450' },
  { city: 'Minneapolis',    state: 'MN', zip: '55401' },
  { city: 'Tampa',          state: 'FL', zip: '33601' },
  { city: 'New Orleans',    state: 'LA', zip: '70112' },
  { city: 'Arlington',      state: 'TX', zip: '76001' },
  { city: 'Wichita',        state: 'KS', zip: '67201' },
  { city: 'Bakersfield',    state: 'CA', zip: '93301' },
  { city: 'Aurora',         state: 'CO', zip: '80010' },
  { city: 'Anaheim',        state: 'CA', zip: '92801' },
  { city: 'Santa Ana',      state: 'CA', zip: '92701' },
];

const EMAIL_DOMAINS = [
  'hospital.org', 'health.org', 'medical.org', 'healthcare.com',
  'medcenter.org', 'clinic.org', 'healthsystem.org',
];

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------
let hospitalCounter = 1;

function pad(n, length = 2) {
  return String(n).padStart(length, '0');
}

function generateLicenseNumber() {
  const state = ['NY', 'CA', 'TX', 'FL', 'IL'][randomInt(0, 4)];
  const year  = randomInt(1980, 2020);
  const seq   = pad(randomInt(1, 9999), 4);
  return `${state}-HOSP-${year}-${seq}`;
}

function generateNpiNumber() {
  // NPI: 10-digit number starting with 1 or 2
  return String(randomInt(1000000000, 1999999999));
}

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Generates a single hospital object.
 *
 * @param {number|null} index  Optional sequential index for unique names.
 * @returns {Object}
 */
export function generateHospital(index = null) {
  const location   = randomItem(CITY_STATE_PAIRS);
  const prefix     = randomItem(HOSPITAL_PREFIXES);
  const suffix     = randomItem(HOSPITAL_SUFFIXES);
  const counter    = index !== null ? index : hospitalCounter++;
  const nameSuffix = counter > 1 ? ` ${counter}` : '';
  const name       = `${prefix} ${location.city} ${suffix}${nameSuffix}`;
  const slug       = name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '');

  return {
    name,
    registrationNumber: generateLicenseNumber(),
    npiNumber:          generateNpiNumber(),
    type:               randomItem(HOSPITAL_TYPES),
    specialization:     randomItem(SPECIALIZATIONS),
    email:              `admin@${slug.substring(0, 30)}.${randomItem(EMAIL_DOMAINS)}`,
    phone:              `+1${randomInt(200, 999)}${randomInt(2000000, 9999999)}`,
    fax:                `+1${randomInt(200, 999)}${randomInt(2000000, 9999999)}`,
    address: {
      street:  `${randomInt(100, 9999)} Healthcare Blvd`,
      city:    location.city,
      state:   location.state,
      zipCode: location.zip,
      country: 'US',
    },
    bedCapacity:         randomInt(50, 2000),
    icuBedCapacity:      randomInt(10, 200),
    emergencyCapacity:   randomInt(20, 150),
    bloodBankCapacity:   randomInt(100, 5000),  // units
    isActive:            true,
    accreditationLevel:  randomItem(['LEVEL_1', 'LEVEL_2', 'LEVEL_3']),
    contactPerson: {
      name:  `Dr. ${randomItem(['Smith', 'Johnson', 'Williams', 'Brown', 'Jones'])}`,
      title: randomItem(['Chief Medical Officer', 'Blood Bank Director', 'Laboratory Director']),
      phone: `+1${randomInt(200, 999)}${randomInt(2000000, 9999999)}`,
      email: `director@${slug.substring(0, 20)}.${randomItem(EMAIL_DOMAINS)}`,
    },
  };
}

/**
 * Generates an array of `count` hospital objects.
 *
 * @param {number} count
 * @param {number} startIndex  Base offset for naming uniqueness.
 * @returns {Object[]}
 */
export function generateHospitals(count, startIndex = 0) {
  const hospitals = [];
  for (let i = 0; i < count; i++) {
    hospitals.push(generateHospital(startIndex + i + 1));
  }
  return hospitals;
}

/**
 * Generates a blood request payload from a hospital.
 * Used in blood-request load tests.
 *
 * @param {string} hospitalId  UUID of the requesting hospital.
 * @returns {Object}
 */
export function generateBloodRequest(hospitalId) {
  const BLOOD_TYPES = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];
  const URGENCY_LEVELS = ['ROUTINE', 'URGENT', 'EMERGENCY', 'CRITICAL'];
  const COMPONENT_TYPES = [
    'WHOLE_BLOOD', 'RED_BLOOD_CELLS', 'PLATELETS', 'FRESH_FROZEN_PLASMA',
  ];

  return {
    hospitalId,
    bloodGroup:    randomItem(BLOOD_TYPES),
    componentType: randomItem(COMPONENT_TYPES),
    unitsRequired: randomInt(1, 10),
    urgencyLevel:  randomItem(URGENCY_LEVELS),
    patientAge:    randomInt(1, 90),
    patientGender: randomItem(['MALE', 'FEMALE']),
    diagnosis:     randomItem([
      'Trauma', 'Surgical procedure', 'Anaemia', 'Haematological malignancy',
      'Burns', 'Obstetric haemorrhage', 'Cardiac surgery', 'Organ transplant',
    ]),
    requiredBy: new Date(Date.now() + randomInt(1, 72) * 3600000).toISOString(),
    notes: '',
  };
}
