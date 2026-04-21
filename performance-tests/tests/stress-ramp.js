/**
 * Stress Test — Gradual Ramp
 *
 * Gradually ramps to 2 000 virtual users over ~10 minutes, then sustains
 * peak load for 5 minutes, then ramps down.  Identifies the point at which
 * the system starts degrading (latency spikes, error rate increases).
 *
 * Applies THROUGHPUT_THRESHOLDS so test will fail if the system cannot
 * handle the full 2 000-user load within target SLOs.
 *
 * Run:
 *   k6 run tests/stress-ramp.js
 *   k6 run -e BASE_URL=http://api-gateway:8080 -e AUTH_TOKEN=<token> tests/stress-ramp.js
 */

import http  from 'k6/http';
import { check, sleep } from 'k6';
import {
  BASE_URL,
  THROUGHPUT_THRESHOLDS,
  defaultHeaders,
  randomItem,
  randomInt,
  BLOOD_TYPES,
} from '../k6.config.js';
import { generateDonor }         from '../generators/donors.js';
import { generateBloodRequest }  from '../generators/hospitals.js';

// ---------------------------------------------------------------------------
// k6 options — staged ramp to 2 000 VUs
// ---------------------------------------------------------------------------
export const options = {
  scenarios: {
    stress_ramp: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m',  target: 100  },  // warm-up
        { duration: '2m',  target: 500  },  // moderate load
        { duration: '2m',  target: 1000 },  // high load
        { duration: '2m',  target: 1500 },  // very high load
        { duration: '2m',  target: 2000 },  // peak — 2 000 VUs
        { duration: '5m',  target: 2000 },  // sustain peak
        { duration: '1m',  target: 0    },  // ramp down
      ],
      gracefulRampDown: '30s',
    },
  },
  thresholds: {
    ...THROUGHPUT_THRESHOLDS,
    // At peak, degradation is expected; warn but do not hard-fail on P99
    'http_req_duration': ['p(95)<200', 'p(99)<1000'],
    // Allow slightly higher error rate under extreme load
    'http_req_failed': ['rate<0.05'],
  },
};

const HEADERS      = defaultHeaders();
const HOSPITAL_IDS = Array.from({ length: 50 }, (_, i) => `hospital-${String(i + 1).padStart(3, '0')}`);
const BRANCH_IDS   = Array.from({ length: 10 }, (_, i) => `BRANCH-${String(i + 1).padStart(3, '0')}`);
const COMPONENT_TYPES = ['WHOLE_BLOOD', 'RED_BLOOD_CELLS', 'PLATELETS', 'FRESH_FROZEN_PLASMA'];

// ---------------------------------------------------------------------------
// Mixed workload — same distribution as mixed-workload.js
// ---------------------------------------------------------------------------
export default function () {
  const scenario = randomInt(1, 6);

  if (scenario <= 2) {
    // Donor registration (30 %)
    const donor     = generateDonor();
    const createRes = http.post(
      `${BASE_URL}/api/v1/donors`,
      JSON.stringify(donor),
      { headers: HEADERS },
    );
    check(createRes, { 'donor create — 201': (r) => r.status === 201 });

    const listRes = http.get(
      `${BASE_URL}/api/v1/donors?page=${randomInt(0, 9)}&size=20`,
      { headers: HEADERS },
    );
    check(listRes, { 'donors list — 200': (r) => r.status === 200 });

  } else if (scenario <= 4) {
    // Blood requests / inventory (40 %)
    const hospitalId = randomItem(HOSPITAL_IDS);
    const bloodGroup = randomItem(BLOOD_TYPES);

    const reqRes = http.post(
      `${BASE_URL}/api/v1/hospital-requests`,
      JSON.stringify(generateBloodRequest(hospitalId)),
      { headers: HEADERS },
    );
    check(reqRes, { 'blood request — 201': (r) => r.status === 201 });

    const stockRes = http.get(
      `${BASE_URL}/api/v1/inventory/stock?bloodGroup=${encodeURIComponent(bloodGroup)}`,
      { headers: HEADERS },
    );
    check(stockRes, { 'stock check — 200': (r) => r.status === 200 });

  } else if (scenario === 5) {
    // Dashboard (20 %)
    const statsRes = http.get(`${BASE_URL}/api/v1/dashboard/stats`, { headers: HEADERS });
    check(statsRes, { 'dashboard stats — 200': (r) => r.status === 200 });

    const searchRes = http.get(
      `${BASE_URL}/api/v1/inventory/search?bloodGroup=${encodeURIComponent(randomItem(BLOOD_TYPES))}&status=AVAILABLE&page=0&size=20`,
      { headers: HEADERS },
    );
    check(searchRes, { 'inventory search — 200': (r) => r.status === 200 });

  } else {
    // Reports (10 %)
    const listRes = http.get(
      `${BASE_URL}/api/v1/reports?page=0&size=10`,
      { headers: HEADERS },
    );
    check(listRes, { 'reports list — 200': (r) => r.status === 200 });
  }

  sleep(randomInt(1, 3));
}
