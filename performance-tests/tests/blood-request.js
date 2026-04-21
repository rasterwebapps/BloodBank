/**
 * Load Test — Blood Request
 *
 * Target: 50 concurrent hospital blood requests per second sustained for 2 minutes.
 *
 * Endpoints exercised:
 *   POST /api/v1/hospital-requests        — submit blood request
 *   GET  /api/v1/hospital-requests        — list requests (paginated)
 *   GET  /api/v1/hospital-requests/{id}   — fetch specific request
 *
 * Run:
 *   k6 run tests/blood-request.js
 *   k6 run -e BASE_URL=http://api-gateway:8080 -e AUTH_TOKEN=<token> tests/blood-request.js
 *   k6 run -e HOSPITAL_IDS=id1,id2,id3 tests/blood-request.js
 */

import http  from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, BASE_THRESHOLDS, defaultHeaders, authHeader, randomItem, randomInt, BLOOD_TYPES } from '../k6.config.js';
import { generateBloodRequest } from '../generators/hospitals.js';

// ---------------------------------------------------------------------------
// Preload hospital IDs from env or use placeholder IDs
// ---------------------------------------------------------------------------
const RAW_IDS      = __ENV.HOSPITAL_IDS ? __ENV.HOSPITAL_IDS.split(',') : [];
const HOSPITAL_IDS = RAW_IDS.length > 0
  ? RAW_IDS
  : Array.from({ length: 50 }, (_, i) => `hospital-${String(i + 1).padStart(3, '0')}`);

// ---------------------------------------------------------------------------
// k6 options
// ---------------------------------------------------------------------------
export const options = {
  scenarios: {
    blood_request: {
      executor:        'constant-arrival-rate',
      rate:            50,           // 50 iterations/sec
      timeUnit:        '1s',
      duration:        '2m',
      preAllocatedVUs: 80,
      maxVUs:          200,
    },
  },
  thresholds: {
    ...BASE_THRESHOLDS,
    'http_req_duration{name:POST /api/v1/hospital-requests}':      ['p(95)<300'],
    'http_req_duration{name:GET  /api/v1/hospital-requests}':      ['p(95)<200'],
    'http_req_duration{name:GET  /api/v1/hospital-requests/{id}}': ['p(95)<150'],
  },
};

const HEADERS = defaultHeaders();

// ---------------------------------------------------------------------------
// Scenario
// ---------------------------------------------------------------------------
export default function () {
  const hospitalId = randomItem(HOSPITAL_IDS);

  // ── 1. Submit blood request ───────────────────────────────────────────────
  const requestPayload = generateBloodRequest(hospitalId);
  const createRes = http.post(
    `${BASE_URL}/api/v1/hospital-requests`,
    JSON.stringify(requestPayload),
    { headers: HEADERS, tags: { name: 'POST /api/v1/hospital-requests' } },
  );

  const created = check(createRes, {
    'POST /api/v1/hospital-requests — status 201': (r) => r.status === 201,
    'POST /api/v1/hospital-requests — has id':     (r) => {
      try { return !!JSON.parse(r.body).id; } catch (_) { return false; }
    },
  });

  if (!created) {
    console.warn(`[blood-request] create failed: ${createRes.status} — ${createRes.body}`);
    sleep(1);
    return;
  }

  let requestId;
  try {
    requestId = JSON.parse(createRes.body).id;
  } catch (_) {
    sleep(1);
    return;
  }

  sleep(0.4);

  // ── 2. Fetch request by ID ────────────────────────────────────────────────
  const getRes = http.get(
    `${BASE_URL}/api/v1/hospital-requests/${requestId}`,
    { headers: HEADERS, tags: { name: 'GET  /api/v1/hospital-requests/{id}' } },
  );

  check(getRes, {
    'GET /api/v1/hospital-requests/{id} — status 200': (r) => r.status === 200,
    'GET /api/v1/hospital-requests/{id} — correct id': (r) => {
      try { return JSON.parse(r.body).id === requestId; } catch (_) { return false; }
    },
  });

  sleep(0.3);

  // ── 3. List requests (filtered by hospital) ───────────────────────────────
  const page    = randomInt(0, 4);
  const listRes = http.get(
    `${BASE_URL}/api/v1/hospital-requests?hospitalId=${hospitalId}&page=${page}&size=20`,
    { headers: HEADERS, tags: { name: 'GET  /api/v1/hospital-requests' } },
  );

  check(listRes, {
    'GET /api/v1/hospital-requests — status 200':  (r) => r.status === 200,
    'GET /api/v1/hospital-requests — body is JSON': (r) => {
      try { JSON.parse(r.body); return true; } catch (_) { return false; }
    },
  });

  sleep(randomInt(1, 3));
}
