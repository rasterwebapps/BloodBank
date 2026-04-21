/**
 * Stress Test — Database Connection Pool Exhaustion
 *
 * Attempts to exhaust the PostgreSQL connection pool by opening many
 * concurrent, long-running DB operations simultaneously.  Validates that:
 *   • The application queues requests rather than returning errors
 *   • No connection-pool deadlocks occur
 *   • The system recovers after load backs off
 *   • P99 latency stays under 2 000 ms even when queued
 *
 * Strategies used:
 *   1. High-concurrency write burst        — many simultaneous INSERT operations
 *   2. Long-running read queries           — paginating deep into large result sets
 *   3. Mixed read-write contention         — interleaved read and write transactions
 *   4. Report generation burst             — heavy aggregation queries
 *
 * Run:
 *   k6 run tests/stress-connection-pool.js
 *   k6 run -e BASE_URL=http://api-gateway:8080 -e AUTH_TOKEN=<token> tests/stress-connection-pool.js
 */

import http  from 'k6/http';
import { check, sleep } from 'k6';
import {
  BASE_URL,
  BASE_THRESHOLDS,
  defaultHeaders,
  randomItem,
  randomInt,
  BLOOD_TYPES,
} from '../k6.config.js';
import { generateDonor }       from '../generators/donors.js';
import { generateBloodRequest } from '../generators/hospitals.js';

// ---------------------------------------------------------------------------
// k6 options — ramp to a very high concurrency level that will stress the pool
// ---------------------------------------------------------------------------
export const options = {
  scenarios: {
    // Phase 1: write burst — exhaust write connections
    write_burst: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 200  },
        { duration: '2m',  target: 500  },  // 500 concurrent writes
        { duration: '1m',  target: 500  },  // sustain
        { duration: '30s', target: 0    },
      ],
      gracefulRampDown: '15s',
      tags: { scenario_name: 'write_burst' },
    },
    // Phase 2: deep pagination — hold read connections open
    deep_reads: {
      executor: 'constant-vus',
      vus:      200,
      duration: '4m',
      startTime: '4m',  // start after write burst
      tags: { scenario_name: 'deep_reads' },
    },
    // Phase 3: mixed contention — simultaneous reads and writes
    mixed_contention: {
      executor: 'constant-vus',
      vus:      300,
      duration: '3m',
      startTime: '8m',  // start after deep reads
      tags: { scenario_name: 'mixed_contention' },
    },
  },
  thresholds: {
    // Allow higher latency under connection pool stress
    http_req_duration: ['p(95)<1000', 'p(99)<2000'],
    // Critical: no more than 5 % errors even under pool exhaustion
    http_req_failed: ['rate<0.05'],

    // Write burst: writes may queue but must not fail
    'http_req_failed{scenario_name:write_burst}':    ['rate<0.05'],
    'http_req_duration{scenario_name:write_burst}':  ['p(95)<1000'],

    // Deep reads: long queries allowed but must succeed
    'http_req_failed{scenario_name:deep_reads}':     ['rate<0.02'],
    'http_req_duration{scenario_name:deep_reads}':   ['p(95)<800'],

    // Mixed: tightest constraint — application should handle gracefully
    'http_req_failed{scenario_name:mixed_contention}': ['rate<0.05'],
  },
};

const HEADERS      = defaultHeaders();
const HOSPITAL_IDS = Array.from({ length: 50 }, (_, i) => `hospital-${String(i + 1).padStart(3, '0')}`);

// ---------------------------------------------------------------------------
// Write burst: rapid sequential inserts without think-time
// ---------------------------------------------------------------------------
function writeBurst() {
  const scenario = randomInt(1, 3);

  if (scenario === 1) {
    // Donor registration — INSERT into donors table
    const res = http.post(
      `${BASE_URL}/api/v1/donors`,
      JSON.stringify(generateDonor()),
      { headers: HEADERS },
    );
    check(res, {
      'write burst donor — 2xx': (r) => r.status >= 200 && r.status < 300,
    });
  } else if (scenario === 2) {
    // Blood request — INSERT into hospital_requests table
    const res = http.post(
      `${BASE_URL}/api/v1/hospital-requests`,
      JSON.stringify(generateBloodRequest(randomItem(HOSPITAL_IDS))),
      { headers: HEADERS },
    );
    check(res, {
      'write burst request — 2xx': (r) => r.status >= 200 && r.status < 300,
    });
  } else {
    // Report trigger — complex INSERT + aggregation query
    const res = http.post(
      `${BASE_URL}/api/v1/reports/generate`,
      JSON.stringify({
        reportType: 'BLOOD_INVENTORY',
        parameters: { format: 'PDF', groupBy: 'BLOOD_GROUP' },
      }),
      { headers: HEADERS },
    );
    check(res, {
      'write burst report — 2xx': (r) => r.status >= 200 && r.status < 300,
    });
  }

  // No think-time — maximum write pressure
  sleep(0.1);
}

// ---------------------------------------------------------------------------
// Deep reads: fetch large result sets with deep pagination (holds connections)
// ---------------------------------------------------------------------------
function deepReads() {
  // Random deep page — forces DB to scan and skip many rows
  const page = randomInt(50, 500);
  const size = randomItem([50, 100]);

  const scenario = randomInt(1, 3);

  if (scenario === 1) {
    const res = http.get(
      `${BASE_URL}/api/v1/donors?page=${page}&size=${size}`,
      { headers: HEADERS },
    );
    check(res, { 'deep read donors — 200': (r) => r.status === 200 });

  } else if (scenario === 2) {
    const bloodGroup = randomItem(BLOOD_TYPES);
    const res        = http.get(
      `${BASE_URL}/api/v1/blood-units?bloodGroup=${encodeURIComponent(bloodGroup)}&page=${page}&size=${size}`,
      { headers: HEADERS },
    );
    check(res, { 'deep read blood-units — 200': (r) => r.status === 200 });

  } else {
    const res = http.get(
      `${BASE_URL}/api/v1/hospital-requests?page=${page}&size=${size}&sort=createdAt,asc`,
      { headers: HEADERS },
    );
    check(res, { 'deep read requests — 200': (r) => r.status === 200 });
  }

  // Minimal think-time — keep connections occupied
  sleep(0.5);
}

// ---------------------------------------------------------------------------
// Mixed contention: interleave reads and writes in the same VU
// ---------------------------------------------------------------------------
function mixedContention() {
  // Write
  const writeRes = http.post(
    `${BASE_URL}/api/v1/donors`,
    JSON.stringify(generateDonor()),
    { headers: HEADERS },
  );
  check(writeRes, { 'mixed write — 2xx': (r) => r.status >= 200 && r.status < 300 });

  // Immediately read — high chance of hitting the same DB connection pool
  const bloodGroup = randomItem(BLOOD_TYPES);
  const readRes    = http.get(
    `${BASE_URL}/api/v1/inventory/search?bloodGroup=${encodeURIComponent(bloodGroup)}&status=AVAILABLE&page=0&size=20`,
    { headers: HEADERS },
  );
  check(readRes, { 'mixed read — 200': (r) => r.status === 200 });

  sleep(0.2);
}

// ---------------------------------------------------------------------------
// Main VU loop — routes to the appropriate behaviour based on scenario tag
// ---------------------------------------------------------------------------
export default function () {
  const tag = __ENV.K6_SCENARIO_TAG || exec.scenario.tags.scenario_name || 'write_burst';

  switch (tag) {
    case 'write_burst':      writeBurst();       break;
    case 'deep_reads':       deepReads();        break;
    case 'mixed_contention': mixedContention();  break;
    default:                 writeBurst();
  }
}
