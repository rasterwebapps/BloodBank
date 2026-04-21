/**
 * Stress Test — Service Failure & Degradation
 *
 * Simulates one downstream service becoming unavailable mid-test and observes
 * how the system degrades (circuit breakers, fallbacks, timeouts).
 *
 * Phases:
 *   1. Baseline   (0–2 min)  — 200 VUs, normal mixed traffic
 *   2. Failure    (2–5 min)  — 200 VUs, one service is "killed" (DNS unreachable
 *                              or returning 503); test observes circuit-breaker
 *                              behaviour and fallback responses
 *   3. Recovery   (5–7 min)  — service "comes back"; measure recovery time
 *
 * NOTE: The actual service kill is performed via an external mechanism (e.g.,
 * `docker stop` or Kubernetes pod deletion) timed to coincide with Phase 2.
 * This script observes the outcome and validates SLOs during each phase.
 *
 * Run:
 *   k6 run tests/stress-failure.js
 *
 * To simulate the failure manually:
 *   # Kill donor-service at t=120s
 *   docker stop bloodbank-donor-service-1
 *   sleep 180
 *   docker start bloodbank-donor-service-1
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
// k6 options
// ---------------------------------------------------------------------------
export const options = {
  scenarios: {
    service_failure: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 200 },  // warm-up ramp
        { duration: '90s', target: 200 },  // PHASE 1: baseline (normal)
        { duration: '3m',  target: 200 },  // PHASE 2: degraded (kill service externally)
        { duration: '2m',  target: 200 },  // PHASE 3: recovery
        { duration: '30s', target: 0   },  // ramp down
      ],
      gracefulRampDown: '30s',
    },
  },
  thresholds: {
    // Overall test — allow degraded SLOs since service is down
    http_req_duration: ['p(95)<1000'],
    http_req_failed:   ['rate<0.30'],   // up to 30 % failures expected when service is down

    // Baseline phase must meet normal SLOs
    'http_req_duration{phase:baseline}': ['p(95)<200', 'p(99)<500'],
    'http_req_failed{phase:baseline}':   ['rate<0.01'],

    // Recovery phase — service back up, SLOs should restore
    'http_req_duration{phase:recovery}': ['p(95)<300'],
    'http_req_failed{phase:recovery}':   ['rate<0.05'],

    // Circuit-breaker fallbacks must respond quickly (even if degraded)
    'http_req_duration{phase:failure}': ['p(95)<500'],
  },
};

const HEADERS      = defaultHeaders();
const HOSPITAL_IDS = Array.from({ length: 50 }, (_, i) => `hospital-${String(i + 1).padStart(3, '0')}`);
const START_TIME   = Date.now();

// Phase boundaries in seconds from test start
const PHASE_BASELINE_END = 120;
const PHASE_FAILURE_END  = 300;

function getPhase() {
  const elapsed = (Date.now() - START_TIME) / 1000;
  if (elapsed < PHASE_BASELINE_END) return 'baseline';
  if (elapsed < PHASE_FAILURE_END)  return 'failure';
  return 'recovery';
}

// ---------------------------------------------------------------------------
// Scenario
// ---------------------------------------------------------------------------
export default function () {
  const phase = getPhase();
  const tags  = { phase };

  const scenario = randomInt(1, 4);

  // ── 1. Donor operations (donor-service may be "killed" in phase 2) ────────
  if (scenario === 1) {
    const donor = generateDonor();
    const res   = http.post(
      `${BASE_URL}/api/v1/donors`,
      JSON.stringify(donor),
      { headers: HEADERS, tags },
    );

    if (phase === 'failure') {
      // During failure: accept 201, 503, or 500 — circuit breaker may short-circuit
      check(res, {
        'donor — accepted or circuit-broken': (r) => [201, 503, 500, 502].includes(r.status),
      });
    } else {
      check(res, {
        'donor create — 201': (r) => r.status === 201,
      });
    }
  }

  // ── 2. Blood request (hospital-service) ───────────────────────────────────
  if (scenario === 2) {
    const hospitalId = randomItem(HOSPITAL_IDS);
    const res        = http.post(
      `${BASE_URL}/api/v1/hospital-requests`,
      JSON.stringify(generateBloodRequest(hospitalId)),
      { headers: HEADERS, tags },
    );
    check(res, {
      'blood request — accepted': (r) => r.status === 201 || r.status === 202 || r.status === 503,
    });
  }

  // ── 3. Inventory search (inventory-service — expected to stay up) ─────────
  if (scenario === 3) {
    const bloodGroup = randomItem(BLOOD_TYPES);
    const res        = http.get(
      `${BASE_URL}/api/v1/inventory/search?bloodGroup=${encodeURIComponent(bloodGroup)}&status=AVAILABLE&page=0&size=20`,
      { headers: HEADERS, tags },
    );
    check(res, {
      'inventory search — 200': (r) => r.status === 200,
    });
  }

  // ── 4. Dashboard (should degrade gracefully with cached data) ─────────────
  if (scenario === 4) {
    const res = http.get(`${BASE_URL}/api/v1/dashboard/stats`, { headers: HEADERS, tags });
    check(res, {
      'dashboard — 200 or degraded': (r) => r.status === 200 || r.status === 206,
    });
  }

  sleep(randomInt(1, 3));
}
