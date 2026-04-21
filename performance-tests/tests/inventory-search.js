/**
 * Load Test — Inventory Search
 *
 * Target: 200 concurrent inventory queries per second sustained for 2 minutes.
 * This is the most read-heavy scenario, simulating hospital staff searching for
 * available blood units in real time.
 *
 * Endpoints exercised:
 *   GET /api/v1/inventory/search   — filtered search (blood type, component, branch)
 *   GET /api/v1/inventory/stock    — aggregate stock levels
 *   GET /api/v1/blood-units        — paginated blood unit list
 *   GET /api/v1/blood-units/search — advanced search with multiple filters
 *
 * Run:
 *   k6 run tests/inventory-search.js
 */

import http  from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, BASE_THRESHOLDS, defaultHeaders, randomItem, randomInt, BLOOD_TYPES } from '../k6.config.js';
import { generateSearchFilter } from '../generators/blood-units.js';

// ---------------------------------------------------------------------------
// k6 options
// ---------------------------------------------------------------------------
export const options = {
  scenarios: {
    inventory_search: {
      executor:        'constant-arrival-rate',
      rate:            200,          // 200 iterations/sec
      timeUnit:        '1s',
      duration:        '2m',
      preAllocatedVUs: 250,
      maxVUs:          500,
    },
  },
  thresholds: {
    ...BASE_THRESHOLDS,
    // Search should be fast (cached / indexed)
    'http_req_duration{name:GET /api/v1/inventory/search}':   ['p(95)<150'],
    'http_req_duration{name:GET /api/v1/inventory/stock}':    ['p(95)<100'],
    'http_req_duration{name:GET /api/v1/blood-units}':        ['p(95)<200'],
    'http_req_duration{name:GET /api/v1/blood-units/search}': ['p(95)<200'],
  },
};

const HEADERS      = defaultHeaders();
const COMPONENT_TYPES = [
  'WHOLE_BLOOD', 'RED_BLOOD_CELLS', 'PLATELETS',
  'FRESH_FROZEN_PLASMA', 'CRYOPRECIPITATE',
];
const BRANCH_IDS = Array.from({ length: 10 }, (_, i) => `BRANCH-${String(i + 1).padStart(3, '0')}`);

// ---------------------------------------------------------------------------
// Build query string from a filter object
// ---------------------------------------------------------------------------
function toQueryString(filters) {
  return Object.entries(filters)
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join('&');
}

// ---------------------------------------------------------------------------
// Scenario
// ---------------------------------------------------------------------------
export default function () {
  // Distribute requests across the four query patterns
  const scenario = randomInt(1, 4);

  // ── Pattern 1: Inventory search (most common) ─────────────────────────────
  if (scenario === 1) {
    const filter = generateSearchFilter();
    const qs     = toQueryString(filter);
    const res    = http.get(
      `${BASE_URL}/api/v1/inventory/search?${qs}&page=0&size=20`,
      { headers: HEADERS, tags: { name: 'GET /api/v1/inventory/search' } },
    );

    check(res, {
      'inventory/search — status 200':   (r) => r.status === 200,
      'inventory/search — has results':  (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.content !== undefined || Array.isArray(body);
        } catch (_) { return false; }
      },
    });
  }

  // ── Pattern 2: Stock levels by blood group ────────────────────────────────
  if (scenario === 2) {
    const bloodGroup = randomItem(BLOOD_TYPES);
    const branchId   = Math.random() > 0.5 ? `&branchId=${randomItem(BRANCH_IDS)}` : '';
    const res        = http.get(
      `${BASE_URL}/api/v1/inventory/stock?bloodGroup=${encodeURIComponent(bloodGroup)}${branchId}`,
      { headers: HEADERS, tags: { name: 'GET /api/v1/inventory/stock' } },
    );

    check(res, {
      'inventory/stock — status 200':     (r) => r.status === 200,
      'inventory/stock — numeric count':  (r) => {
        try {
          const body = JSON.parse(r.body);
          return typeof body.totalUnits === 'number' || typeof body.count === 'number';
        } catch (_) { return false; }
      },
    });
  }

  // ── Pattern 3: Paginated blood unit list ──────────────────────────────────
  if (scenario === 3) {
    const page    = randomInt(0, 49);
    const size    = randomItem([10, 20, 50]);
    const res     = http.get(
      `${BASE_URL}/api/v1/blood-units?page=${page}&size=${size}&status=AVAILABLE`,
      { headers: HEADERS, tags: { name: 'GET /api/v1/blood-units' } },
    );

    check(res, {
      'blood-units list — status 200':    (r) => r.status === 200,
      'blood-units list — has content':   (r) => {
        try {
          const body = JSON.parse(r.body);
          return Array.isArray(body.content) || Array.isArray(body);
        } catch (_) { return false; }
      },
    });
  }

  // ── Pattern 4: Advanced multi-filter search ───────────────────────────────
  if (scenario === 4) {
    const bloodGroup     = randomItem(BLOOD_TYPES);
    const componentType  = randomItem(COMPONENT_TYPES);
    const branchId       = randomItem(BRANCH_IDS);
    const res            = http.get(
      `${BASE_URL}/api/v1/blood-units/search?bloodGroup=${encodeURIComponent(bloodGroup)}&componentType=${componentType}&branchId=${branchId}&status=AVAILABLE&page=0&size=20`,
      { headers: HEADERS, tags: { name: 'GET /api/v1/blood-units/search' } },
    );

    check(res, {
      'blood-units/search — status 200':   (r) => r.status === 200,
      'blood-units/search — body is JSON': (r) => {
        try { JSON.parse(r.body); return true; } catch (_) { return false; }
      },
    });
  }

  // Minimal think-time between rapid searches
  sleep(randomInt(1, 2));
}
