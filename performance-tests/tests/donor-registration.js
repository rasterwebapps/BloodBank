/**
 * Load Test — Donor Registration
 *
 * Target: 100 concurrent donor registrations per second sustained for 2 minutes.
 *
 * Endpoints exercised:
 *   POST /api/v1/donors          — register new donor
 *   GET  /api/v1/donors/{id}     — fetch created donor
 *   GET  /api/v1/donors          — list donors (paginated)
 *
 * Run:
 *   k6 run tests/donor-registration.js
 *   k6 run -e BASE_URL=http://api-gateway:8080 -e AUTH_TOKEN=<token> tests/donor-registration.js
 */

import http  from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, AUTH_TOKEN, BASE_THRESHOLDS, defaultHeaders, randomInt } from '../k6.config.js';
import { generateDonor } from '../generators/donors.js';

// ---------------------------------------------------------------------------
// k6 options
// ---------------------------------------------------------------------------
export const options = {
  scenarios: {
    donor_registration: {
      executor:        'constant-arrival-rate',
      rate:            100,          // 100 iterations/sec
      timeUnit:        '1s',
      duration:        '2m',
      preAllocatedVUs: 150,
      maxVUs:          300,
    },
  },
  thresholds: {
    ...BASE_THRESHOLDS,
    // Registration-specific: creation must be fast
    'http_req_duration{scenario:donor_registration}': ['p(95)<200', 'p(99)<500'],
    // Track per-endpoint latency
    'http_req_duration{name:POST /api/v1/donors}':       ['p(95)<250'],
    'http_req_duration{name:GET  /api/v1/donors/{id}}':  ['p(95)<150'],
    'http_req_duration{name:GET  /api/v1/donors}':       ['p(95)<200'],
  },
};

const HEADERS = defaultHeaders();

// ---------------------------------------------------------------------------
// Scenario: one VU iteration = one user registering and then reading their donor record
// ---------------------------------------------------------------------------
export default function () {
  // ── 1. Register donor ────────────────────────────────────────────────────
  const newDonor  = generateDonor();
  const createRes = http.post(
    `${BASE_URL}/api/v1/donors`,
    JSON.stringify(newDonor),
    { headers: HEADERS, tags: { name: 'POST /api/v1/donors' } },
  );

  const created = check(createRes, {
    'POST /api/v1/donors — status 201': (r) => r.status === 201,
    'POST /api/v1/donors — has id':     (r) => {
      try { return !!JSON.parse(r.body).id; } catch (_) { return false; }
    },
  });

  if (!created) {
    console.warn(`[donor-registration] create failed: ${createRes.status} — ${createRes.body}`);
    sleep(1);
    return;
  }

  // ── 2. Read back the created donor ───────────────────────────────────────
  let donorId;
  try {
    donorId = JSON.parse(createRes.body).id;
  } catch (_) {
    sleep(1);
    return;
  }

  sleep(0.5);

  const getRes = http.get(
    `${BASE_URL}/api/v1/donors/${donorId}`,
    { headers: HEADERS, tags: { name: 'GET  /api/v1/donors/{id}' } },
  );

  check(getRes, {
    'GET /api/v1/donors/{id} — status 200': (r) => r.status === 200,
    'GET /api/v1/donors/{id} — correct id': (r) => {
      try { return JSON.parse(r.body).id === donorId; } catch (_) { return false; }
    },
  });

  // ── 3. List donors (paginated) ────────────────────────────────────────────
  sleep(0.3);
  const page    = randomInt(0, 9);
  const listRes = http.get(
    `${BASE_URL}/api/v1/donors?page=${page}&size=20`,
    { headers: HEADERS, tags: { name: 'GET  /api/v1/donors' } },
  );

  check(listRes, {
    'GET /api/v1/donors — status 200':      (r) => r.status === 200,
    'GET /api/v1/donors — has content':     (r) => {
      try {
        const body = JSON.parse(r.body);
        return Array.isArray(body.content) || Array.isArray(body);
      } catch (_) { return false; }
    },
  });

  // Simulate brief user think-time
  sleep(randomInt(1, 3));
}
