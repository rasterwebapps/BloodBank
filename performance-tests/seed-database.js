/**
 * Database Seeder — BloodBank Performance Tests
 *
 * Seeds the BloodBank platform with realistic test data via HTTP API:
 *   • 50    hospitals
 *   • 100 K donors
 *   • 500 K blood units  (linked to seeded donors)
 *
 * Run with:
 *   k6 run seed-database.js
 *
 * Tuning env vars:
 *   BASE_URL        API gateway URL   (default: http://localhost:8080)
 *   AUTH_TOKEN      Bearer token      (required)
 *   HOSPITAL_COUNT  override default 50
 *   DONOR_COUNT     override default 100000
 *   BLOOD_UNIT_COUNT override default 500000
 *   BATCH_SIZE      records per HTTP call (default: 50)
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, AUTH_TOKEN, defaultHeaders } from './k6.config.js';
import { generateHospitals } from './generators/hospitals.js';
import { generateDonors }    from './generators/donors.js';
import { generateBloodUnits } from './generators/blood-units.js';

// ---------------------------------------------------------------------------
// Configuration
// ---------------------------------------------------------------------------
const HOSPITAL_COUNT   = parseInt(__ENV.HOSPITAL_COUNT   || '50',     10);
const DONOR_COUNT      = parseInt(__ENV.DONOR_COUNT      || '100000', 10);
const BLOOD_UNIT_COUNT = parseInt(__ENV.BLOOD_UNIT_COUNT || '500000', 10);
const BATCH_SIZE       = parseInt(__ENV.BATCH_SIZE       || '50',     10);

// ---------------------------------------------------------------------------
// k6 options: single VU, sequential — seeding is not a concurrency test
// ---------------------------------------------------------------------------
export const options = {
  vus:      1,
  iterations: 1,
  thresholds: {
    // Seed requests may be slower; allow up to 5 s per batch
    http_req_duration: ['p(95)<5000'],
    http_req_failed:   ['rate<0.05'],
  },
};

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------
const HEADERS = defaultHeaders();

function postBatch(url, records, label) {
  const payload = JSON.stringify(records);
  const res = http.post(url, payload, { headers: HEADERS });

  const ok = check(res, {
    [`${label} batch status 2xx`]: (r) => r.status >= 200 && r.status < 300,
  });

  if (!ok) {
    console.error(`[SEED] ${label} batch failed — status ${res.status}: ${res.body}`);
  }
  return res;
}

/**
 * Seeds a resource in batches.
 *
 * @param {string}   url        Endpoint URL (must accept an array payload)
 * @param {Function} generator  Function(count, startIndex) => Object[]
 * @param {number}   total      Total records to seed
 * @param {string}   label      Human-readable label for logging
 * @returns {string[]} Array of IDs extracted from successful responses
 */
function seedInBatches(url, generator, total, label) {
  const ids      = [];
  let   seeded   = 0;

  console.log(`[SEED] Starting ${label}: ${total} records in batches of ${BATCH_SIZE}`);

  while (seeded < total) {
    const batchCount = Math.min(BATCH_SIZE, total - seeded);
    const records    = generator(batchCount, seeded);

    const res = postBatch(url, records, label);

    if (res.status >= 200 && res.status < 300) {
      try {
        const body = JSON.parse(res.body);
        // Support both an array of objects and a wrapper like { ids: [...] }
        const created = Array.isArray(body) ? body : (body.ids || body.data || []);
        created.forEach((item) => {
          if (item && item.id) ids.push(item.id);
        });
      } catch (_) {
        // Response may not be JSON — ignore
      }
    }

    seeded += batchCount;

    // Progress log every 10 %
    const pct = Math.round((seeded / total) * 100);
    if (pct % 10 === 0 || seeded === total) {
      console.log(`[SEED] ${label}: ${seeded}/${total} (${pct}%)`);
    }

    // Throttle to avoid overwhelming the server
    sleep(0.1);
  }

  console.log(`[SEED] ${label} complete — ${seeded} records submitted, ${ids.length} IDs captured`);
  return ids;
}

// ---------------------------------------------------------------------------
// Main seeder
// ---------------------------------------------------------------------------
export default function () {
  console.log('='.repeat(60));
  console.log('[SEED] BloodBank Database Seeder');
  console.log(`[SEED] Target: ${BASE_URL}`);
  console.log(`[SEED] Hospitals:   ${HOSPITAL_COUNT}`);
  console.log(`[SEED] Donors:      ${DONOR_COUNT}`);
  console.log(`[SEED] Blood Units: ${BLOOD_UNIT_COUNT}`);
  console.log('='.repeat(60));

  // ------------------------------------------------------------------
  // 1. Seed hospitals
  // ------------------------------------------------------------------
  const hospitalIds = seedInBatches(
    `${BASE_URL}/api/v1/hospitals/batch`,
    generateHospitals,
    HOSPITAL_COUNT,
    'Hospitals',
  );

  // ------------------------------------------------------------------
  // 2. Seed donors
  // ------------------------------------------------------------------
  const donorIds = seedInBatches(
    `${BASE_URL}/api/v1/donors/batch`,
    generateDonors,
    DONOR_COUNT,
    'Donors',
  );

  // ------------------------------------------------------------------
  // 3. Seed blood units (linked to donors where possible)
  // ------------------------------------------------------------------
  const resolvedDonorIds = donorIds.length > 0 ? donorIds : null;

  seedInBatches(
    `${BASE_URL}/api/v1/blood-units/batch`,
    (count, startIndex) => {
      // Pass captured donor IDs so blood units reference real donors
      const { generateBloodUnits: gen } = { generateBloodUnits };
      return generateBloodUnits(count, startIndex, resolvedDonorIds);
    },
    BLOOD_UNIT_COUNT,
    'Blood Units',
  );

  // ------------------------------------------------------------------
  // Summary
  // ------------------------------------------------------------------
  console.log('='.repeat(60));
  console.log('[SEED] Seeding complete');
  console.log(`[SEED]   Hospitals captured:   ${hospitalIds.length}`);
  console.log(`[SEED]   Donors    captured:   ${donorIds.length}`);
  console.log('[SEED]   Blood units: submitted (IDs not captured to save memory)');
  console.log('='.repeat(60));
}
