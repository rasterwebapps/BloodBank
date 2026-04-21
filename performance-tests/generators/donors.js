/**
 * Donor Data Generator — BloodBank Performance Tests
 *
 * Generates realistic donor records for seeding and load testing.
 * All data is synthetic / randomised; no real personal information is used.
 */

import { randomItem, randomInt, BLOOD_TYPES } from '../k6.config.js';

// ---------------------------------------------------------------------------
// Name pools
// ---------------------------------------------------------------------------
const FIRST_NAMES = [
  'James', 'Mary', 'John', 'Patricia', 'Robert', 'Jennifer', 'Michael', 'Linda',
  'William', 'Barbara', 'David', 'Elizabeth', 'Richard', 'Susan', 'Joseph', 'Jessica',
  'Thomas', 'Sarah', 'Charles', 'Karen', 'Christopher', 'Nancy', 'Daniel', 'Margaret',
  'Matthew', 'Lisa', 'Anthony', 'Betty', 'Mark', 'Dorothy', 'Donald', 'Sandra',
  'Steven', 'Ashley', 'Paul', 'Dorothy', 'Andrew', 'Kimberly', 'Kenneth', 'Emily',
  'Joshua', 'Donna', 'Kevin', 'Michelle', 'Brian', 'Carol', 'George', 'Amanda',
  'Timothy', 'Melissa', 'Ronald', 'Deborah', 'Edward', 'Stephanie', 'Jason', 'Rebecca',
  'Jeffrey', 'Sharon', 'Ryan', 'Laura', 'Jacob', 'Cynthia', 'Gary', 'Kathleen',
  'Nicholas', 'Amy', 'Eric', 'Angela', 'Jonathan', 'Shirley', 'Stephen', 'Anna',
  'Larry', 'Brenda', 'Justin', 'Pamela', 'Scott', 'Emma', 'Brandon', 'Nicole',
  'Benjamin', 'Helen', 'Samuel', 'Samantha', 'Raymond', 'Katherine', 'Gregory', 'Christine',
  'Frank', 'Debra', 'Alexander', 'Rachel', 'Patrick', 'Carolyn', 'Jack', 'Janet',
];

const LAST_NAMES = [
  'Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis',
  'Rodriguez', 'Martinez', 'Hernandez', 'Lopez', 'Gonzalez', 'Wilson', 'Anderson',
  'Thomas', 'Taylor', 'Moore', 'Jackson', 'Martin', 'Lee', 'Perez', 'Thompson',
  'White', 'Harris', 'Sanchez', 'Clark', 'Ramirez', 'Lewis', 'Robinson', 'Walker',
  'Young', 'Allen', 'King', 'Wright', 'Scott', 'Torres', 'Nguyen', 'Hill', 'Flores',
  'Green', 'Adams', 'Nelson', 'Baker', 'Hall', 'Rivera', 'Campbell', 'Mitchell',
  'Carter', 'Roberts', 'Turner', 'Phillips', 'Evans', 'Collins', 'Parker', 'Edwards',
  'Stewart', 'Flores', 'Morris', 'Nguyen', 'Murphy', 'Rivera', 'Cook', 'Rogers',
  'Morgan', 'Peterson', 'Cooper', 'Reed', 'Bailey', 'Bell', 'Gomez', 'Kelly',
  'Howard', 'Ward', 'Cox', 'Diaz', 'Richardson', 'Wood', 'Watson', 'Brooks',
  'Bennett', 'Gray', 'James', 'Reyes', 'Cruz', 'Hughes', 'Price', 'Myers', 'Long',
  'Foster', 'Sanders', 'Ross', 'Morales', 'Powell', 'Sullivan', 'Russell', 'Ortiz',
];

const EMAIL_DOMAINS = [
  'gmail.com', 'yahoo.com', 'outlook.com', 'hotmail.com', 'icloud.com',
  'protonmail.com', 'mail.com', 'live.com', 'aol.com', 'zoho.com',
];

const GENDERS = ['MALE', 'FEMALE', 'OTHER'];

const CITY_STATE_PAIRS = [
  { city: 'New York',      state: 'NY' },
  { city: 'Los Angeles',   state: 'CA' },
  { city: 'Chicago',       state: 'IL' },
  { city: 'Houston',       state: 'TX' },
  { city: 'Phoenix',       state: 'AZ' },
  { city: 'Philadelphia',  state: 'PA' },
  { city: 'San Antonio',   state: 'TX' },
  { city: 'San Diego',     state: 'CA' },
  { city: 'Dallas',        state: 'TX' },
  { city: 'San Jose',      state: 'CA' },
  { city: 'Austin',        state: 'TX' },
  { city: 'Jacksonville',  state: 'FL' },
  { city: 'Fort Worth',    state: 'TX' },
  { city: 'Columbus',      state: 'OH' },
  { city: 'Charlotte',     state: 'NC' },
  { city: 'Indianapolis',  state: 'IN' },
  { city: 'San Francisco', state: 'CA' },
  { city: 'Seattle',       state: 'WA' },
  { city: 'Denver',        state: 'CO' },
  { city: 'Nashville',     state: 'TN' },
];

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/** Zero-pads a number to the given length. */
function pad(n, length = 2) {
  return String(n).padStart(length, '0');
}

/**
 * Returns a random date string (YYYY-MM-DD) for a donor aged between 18 and 65.
 */
function randomDob() {
  const now      = new Date();
  const minYear  = now.getFullYear() - 65;
  const maxYear  = now.getFullYear() - 18;
  const year     = randomInt(minYear, maxYear);
  const month    = randomInt(1, 12);
  const day      = randomInt(1, 28); // stay in safe range for all months
  return `${year}-${pad(month)}-${pad(day)}`;
}

/**
 * Returns a random US-style phone number string: "+1XXXXXXXXXX"
 */
function randomPhone() {
  const areaCode  = randomInt(200, 999);
  const exchange  = randomInt(200, 999);
  const subscriber = randomInt(1000, 9999);
  return `+1${areaCode}${exchange}${subscriber}`;
}

/**
 * Returns a random 5-digit US ZIP code string.
 */
function randomZip() {
  return pad(randomInt(10000, 99999), 5);
}

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Generates a single realistic donor object.
 *
 * @param {number|null} index  Optional sequential index used to guarantee
 *                             unique e-mail addresses during bulk seeding.
 * @returns {Object}
 */
export function generateDonor(index = null) {
  const firstName = randomItem(FIRST_NAMES);
  const lastName  = randomItem(LAST_NAMES);
  const domain    = randomItem(EMAIL_DOMAINS);
  const suffix    = index !== null ? index : randomInt(1000, 999999);
  const location  = randomItem(CITY_STATE_PAIRS);

  return {
    firstName,
    lastName,
    email:        `${firstName.toLowerCase()}.${lastName.toLowerCase()}.${suffix}@${domain}`,
    phone:        randomPhone(),
    dateOfBirth:  randomDob(),
    gender:       randomItem(GENDERS),
    bloodGroup:   randomItem(BLOOD_TYPES),
    address: {
      street:  `${randomInt(1, 9999)} ${randomItem(LAST_NAMES)} St`,
      city:    location.city,
      state:   location.state,
      zipCode: randomZip(),
      country: 'US',
    },
    weight:        randomInt(50, 120),   // kg
    isEligible:    true,
    medicalNotes:  '',
  };
}

/**
 * Generates an array of `count` donor objects.
 * Indices are used for unique e-mail addresses.
 *
 * @param {number} count
 * @param {number} startIndex  Base offset to avoid collisions across batches.
 * @returns {Object[]}
 */
export function generateDonors(count, startIndex = 0) {
  const donors = [];
  for (let i = 0; i < count; i++) {
    donors.push(generateDonor(startIndex + i));
  }
  return donors;
}
