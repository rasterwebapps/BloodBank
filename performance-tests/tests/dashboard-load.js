/**
 * Load Test — Dashboard Load
 *
 * Target: 500 concurrent users browsing the BloodBank dashboard simultaneously.
 * Simulates staff and managers checking KPI stats, inventory levels, and recent alerts.
 *
 * Endpoints exercised:
 *   GET /api/v1/dashboard/stats               — summary KPIs
 *   GET /api/v1/inventory/stock               — current stock levels (all blood types)
 *   GET /api/v1/donors?page=0&size=10         — recent donors widget
 *   GET /api/v1/hospital-requests?page=0&size=10 — recent requests widget
 *   GET /api/v1/blood-units?status=EXPIRING_SOON — expiring blood units alert
 *
 * Run:
 *   k6 run tests/dashboard-load.js
 */

import http  from 'k6/http';
import { check, sleep, group } from 'k6';
import { BASE_URL, BASE_THRESHOLDS, defaultHeaders, randomInt } from '../k6.config.js';

// ---------------------------------------------------------------------------
// k6 options
// ---------------------------------------------------------------------------
export const options = {
  scenarios: {
    dashboard_load: {
      executor:  'constant-vus',
      vus:       500,
      duration:  '3m',
    },
  },
  thresholds: {
    ...BASE_THRESHOLDS,
    // Dashboard reads hit cached/indexed data — should be very fast
    'http_req_duration{name:GET /api/v1/dashboard/stats}':            ['p(95)<100'],
    'http_req_duration{name:GET /api/v1/inventory/stock (all)}':      ['p(95)<150'],
    'http_req_duration{name:GET /api/v1/donors (widget)}':            ['p(95)<150'],
    'http_req_duration{name:GET /api/v1/hospital-requests (widget)}': ['p(95)<150'],
    'http_req_duration{name:GET /api/v1/blood-units (expiring)}':     ['p(95)<200'],
    // 500 concurrent users should not push error rate above 1 %
    'http_req_failed{scenario:dashboard_load}': ['rate<0.01'],
  },
};

const HEADERS = defaultHeaders();

// ---------------------------------------------------------------------------
// Scenario: simulate a single user's dashboard page-load sequence
// ---------------------------------------------------------------------------
export default function () {
  // Randomise whether this VU represents a branch manager or sys admin
  // (different data scope, but same endpoints)
  const branchParam = Math.random() > 0.5
    ? `&branchId=BRANCH-${String(randomInt(1, 10)).padStart(3, '0')}`
    : '';

  // ── Group: Dashboard initial load ─────────────────────────────────────────
  group('Dashboard initial load', () => {
    // 1a. Summary KPIs
    const statsRes = http.get(
      `${BASE_URL}/api/v1/dashboard/stats${branchParam ? `?${branchParam.slice(1)}` : ''}`,
      { headers: HEADERS, tags: { name: 'GET /api/v1/dashboard/stats' } },
    );
    check(statsRes, {
      'dashboard/stats — status 200':    (r) => r.status === 200,
      'dashboard/stats — has totalDonors': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.totalDonors !== undefined || body.stats !== undefined || body.kpis !== undefined;
        } catch (_) { return false; }
      },
    });

    // 1b. Stock levels for all blood types (parallel with KPIs in real browser)
    const stockRes = http.get(
      `${BASE_URL}/api/v1/inventory/stock`,
      { headers: HEADERS, tags: { name: 'GET /api/v1/inventory/stock (all)' } },
    );
    check(stockRes, {
      'inventory/stock (all) — status 200':  (r) => r.status === 200,
      'inventory/stock (all) — body is JSON': (r) => {
        try { JSON.parse(r.body); return true; } catch (_) { return false; }
      },
    });
  });

  sleep(0.5);

  // ── Group: Widgets / data panels ──────────────────────────────────────────
  group('Dashboard widgets', () => {
    // 2a. Recent donors widget
    const donorsRes = http.get(
      `${BASE_URL}/api/v1/donors?page=0&size=10&sort=createdAt,desc`,
      { headers: HEADERS, tags: { name: 'GET /api/v1/donors (widget)' } },
    );
    check(donorsRes, {
      'donors widget — status 200': (r) => r.status === 200,
    });

    // 2b. Recent blood requests widget
    const requestsRes = http.get(
      `${BASE_URL}/api/v1/hospital-requests?page=0&size=10&sort=createdAt,desc`,
      { headers: HEADERS, tags: { name: 'GET /api/v1/hospital-requests (widget)' } },
    );
    check(requestsRes, {
      'hospital-requests widget — status 200': (r) => r.status === 200,
    });

    // 2c. Expiring blood units alert panel
    const expiringRes = http.get(
      `${BASE_URL}/api/v1/blood-units?status=EXPIRING_SOON&page=0&size=10`,
      { headers: HEADERS, tags: { name: 'GET /api/v1/blood-units (expiring)' } },
    );
    check(expiringRes, {
      'blood-units (expiring) — status 200': (r) => r.status === 200,
    });
  });

  // Simulate user reading the dashboard before refreshing (30–90 s think-time)
  sleep(randomInt(30, 90));
}
