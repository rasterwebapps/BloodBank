/**
 * Blood Unit Data Generator — BloodBank Performance Tests
 *
 * Generates realistic blood unit records for seeding and load testing.
 */

import { randomItem, randomInt, BLOOD_TYPES } from '../k6.config.js';

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------
const COMPONENT_TYPES = [
  'WHOLE_BLOOD',
  'RED_BLOOD_CELLS',
  'PLATELETS',
  'FRESH_FROZEN_PLASMA',
  'CRYOPRECIPITATE',
  'GRANULOCYTES',
];

const STATUSES = [
  'AVAILABLE',
  'RESERVED',
  'QUARANTINE',
  'EXPIRED',
  'TRANSFUSED',
  'DISCARDED',
];

// Realistic status distribution weights (index aligns with STATUSES)
const STATUS_WEIGHTS = [60, 15, 10, 5, 7, 3]; // percentages

const STORAGE_LOCATIONS = [
  'REFRIGERATOR_A1', 'REFRIGERATOR_A2', 'REFRIGERATOR_A3',
  'REFRIGERATOR_B1', 'REFRIGERATOR_B2',
  'FREEZER_C1',      'FREEZER_C2',
  'PLATELET_AGITATOR_1', 'PLATELET_AGITATOR_2',
];

const COLLECTION_METHODS = ['VOLUNTARY', 'AUTOLOGOUS', 'DIRECTED', 'APHERESIS'];

const BRANCH_IDS = Array.from({ length: 10 }, (_, i) => `BRANCH-${String(i + 1).padStart(3, '0')}`);

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function pad(n, length = 2) {
  return String(n).padStart(length, '0');
}

/**
 * Returns a date string offset by `daysOffset` from today.
 * Negative offset = past date; positive = future date.
 */
function dateOffset(daysOffset) {
  const d = new Date();
  d.setDate(d.getDate() + daysOffset);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

/**
 * Weighted random status selection.
 * Matches realistic blood-bank inventory distributions.
 */
function weightedStatus() {
  const total  = STATUS_WEIGHTS.reduce((a, b) => a + b, 0);
  let rand     = randomInt(0, total - 1);
  for (let i = 0; i < STATUSES.length; i++) {
    rand -= STATUS_WEIGHTS[i];
    if (rand < 0) return STATUSES[i];
  }
  return STATUSES[0];
}

/**
 * Generates a unit barcode like "BB-2024-0000001".
 */
function generateBarcode(index) {
  const year = new Date().getFullYear();
  return `BB-${year}-${String(index).padStart(7, '0')}`;
}

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Generates a single blood unit object.
 *
 * @param {number} index  Sequential index for unique barcode generation.
 * @param {string|null} donorId  Optional donor UUID to link the unit.
 * @returns {Object}
 */
export function generateBloodUnit(index, donorId = null) {
  const componentType = randomItem(COMPONENT_TYPES);
  const collectedDaysAgo = randomInt(1, 30);
  const collectionDate    = dateOffset(-collectedDaysAgo);

  // Expiry depends on component type
  const expiryDays = {
    WHOLE_BLOOD:           35,
    RED_BLOOD_CELLS:       42,
    PLATELETS:              5,
    FRESH_FROZEN_PLASMA: 365,
    CRYOPRECIPITATE:     365,
    GRANULOCYTES:           1,
  }[componentType] || 42;

  const expiryDate = dateOffset(expiryDays - collectedDaysAgo);

  // Volume in mL by component type
  const volumeMap = {
    WHOLE_BLOOD:           randomInt(430, 490),
    RED_BLOOD_CELLS:       randomInt(250, 350),
    PLATELETS:             randomInt(50, 60),
    FRESH_FROZEN_PLASMA:   randomInt(200, 300),
    CRYOPRECIPITATE:       randomInt(10, 15),
    GRANULOCYTES:          randomInt(200, 400),
  };

  return {
    barcode:          generateBarcode(index),
    bloodGroup:       randomItem(BLOOD_TYPES),
    componentType,
    status:           weightedStatus(),
    collectionDate,
    expiryDate,
    volumeMl:         volumeMap[componentType],
    storageLocation:  randomItem(STORAGE_LOCATIONS),
    collectionMethod: randomItem(COLLECTION_METHODS),
    branchId:         randomItem(BRANCH_IDS),
    donorId:          donorId || `donor-placeholder-${randomInt(1, 100000)}`,
    testingResults: {
      hiv:        'NEGATIVE',
      hepatitisB: 'NEGATIVE',
      hepatitisC: 'NEGATIVE',
      syphilis:   'NEGATIVE',
      malaria:    'NEGATIVE',
    },
    notes: '',
  };
}

/**
 * Generates an array of `count` blood unit objects.
 *
 * @param {number} count
 * @param {number} startIndex   Base offset to ensure unique barcodes.
 * @param {string[]|null} donorIds  Optional pool of donor IDs to sample from.
 * @returns {Object[]}
 */
export function generateBloodUnits(count, startIndex = 0, donorIds = null) {
  const units = [];
  for (let i = 0; i < count; i++) {
    const donorId = donorIds ? randomItem(donorIds) : null;
    units.push(generateBloodUnit(startIndex + i, donorId));
  }
  return units;
}

/**
 * Generates a blood unit search filter object for query parameters.
 * Useful in inventory-search load tests.
 *
 * @returns {Object}
 */
export function generateSearchFilter() {
  const filters = {};

  // Randomly include a subset of filter criteria
  if (Math.random() > 0.3) filters.bloodGroup     = randomItem(BLOOD_TYPES);
  if (Math.random() > 0.5) filters.componentType  = randomItem(COMPONENT_TYPES);
  if (Math.random() > 0.6) filters.status         = 'AVAILABLE';
  if (Math.random() > 0.7) filters.branchId       = randomItem(BRANCH_IDS);

  return filters;
}
