/**
 * Stress Test — Traffic Spike
 *
 * Simulates a sudden surge from idle to 5 000 virtual users for 60 seconds,
 * then immediately drops back to baseline.  Models a mass-casualty event where
 * all regional hospitals simultaneously request blood supplies.
 *
 * This test intentionally violates normal SLO thresholds — the system is not
 * expected to sustain < 200 ms P95 at 5 000 VUs.  Instead it validates:
 *   • The system does not crash (no 5xx errors > 10 %)
 *   • The system recovers to normal latency after the spike
 *   • Connection pools / thread pools do not deadlock
 *
 * Run:
 *   k6 run tests/stress-spike.js
 *   k6 run -e BASE_URL=http://api-gateway:8080 -e AUTH_TOKEN=<token> tests/stress-spike.js
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
import { generateDonor }         from '../generators/donors.js';
import { generateBloodRequest }  from '../generators/hospitals.js';

// ---------------------------------------------------------------------------
// k6 options — instant spike to 5 000 VUs
// ---------------------------------------------------------------------------
export const options = {
  scenarios: {
    spike: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 100  },   // baseline warm-up
        { duration: '10s', target: 5000 },   // SPIKE: instant ramp to 5 000
        { duration: '60s', target: 5000 },   // sustain spike for 1 minute
        { duration: '10s', target: 100  },   // instant drop back
        { duration: '90s', target: 100  },   // recovery observation
        { duration: '15s', target: 0    },   // graceful shutdown
      ],
      gracefulRampDown: '30s',
    },
  },
  thresholds: {
    // During spike: allow degraded latency but cap server errors
    http_req_duration: ['p(95)<2000', 'p(99)<5000'],
    // Critical: fewer than 10 % of requests should return 5xx
    http_req_failed: ['rate<0.10'],
    // After recovery, baseline thresholds apply (checked by tag)
    'http_req_duration{phase:recovery}': ['p(95)<200', 'p(99)<500'],
    'http_req_failed{phase:recovery}':   ['rate<0.01'],
  },
};

const HEADERS      = defaultHeaders();
const HOSPITAL_IDS = Array.from({ length: 50 }, (_, i) => `hospital-${String(i + 1).padStart(3, '0')}`);

// ---------------------------------------------------------------------------
// Phase detection — tracks elapsed seconds from the moment the first VU starts.
// Boundaries:
//   0–40 s  → warmup  (baseline ramp)
//   40–110 s → spike   (5 000 VUs)
//   >110 s  → recovery (back to 100 VUs + observation)
// ---------------------------------------------------------------------------

// Record the test start time once per VU (module-level, evaluated on first import).
const SPIKE_START = parseInt(__ENV._SPIKE_START_MS || String(Date.now()), 10);

// ---------------------------------------------------------------------------
// Scenario
// ---------------------------------------------------------------------------
export default function () {
  const elapsed = (Date.now() - SPIKE_START) / 1000;
  const phase   = elapsed > 110 ? 'recovery' : (elapsed > 40 ? 'spike' : 'warmup');
  const tags    = { phase };

  const scenario = randomInt(1, 5);

  if (scenario <= 2) {
    // Blood request (40 %) — most urgent during mass-casualty
    const hospitalId = randomItem(HOSPITAL_IDS);
    const res = http.post(
      `${BASE_URL}/api/v1/hospital-requests`,
      JSON.stringify(generateBloodRequest(hospitalId)),
      { headers: HEADERS, tags },
    );
    check(res, {
      'spike — blood request accepted': (r) => r.status === 201 || r.status === 202,
    });

  } else if (scenario <= 4) {
    // Inventory search (40 %) — all hospitals checking availability
    const bloodGroup = randomItem(BLOOD_TYPES);
    const res        = http.get(
      `${BASE_URL}/api/v1/inventory/search?bloodGroup=${encodeURIComponent(bloodGroup)}&status=AVAILABLE&page=0&size=10`,
      { headers: HEADERS, tags },
    );
    check(res, {
      'spike — inventory search 200': (r) => r.status === 200,
    });

  } else {
    // Dashboard (20 %)
    const res = http.get(
      `${BASE_URL}/api/v1/dashboard/stats`,
      { headers: HEADERS, tags },
    );
    check(res, {
      'spike — dashboard 200': (r) => r.status === 200,
    });
  }

  // Minimal think-time during spike — simulate panic traffic
  sleep(phase === 'spike' ? 0.5 : randomInt(1, 3));
}
