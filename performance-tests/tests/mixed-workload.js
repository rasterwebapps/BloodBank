/**
 * Load Test — Mixed Workload
 *
 * Target: 1 000 concurrent users exercising ALL endpoints simultaneously.
 * Applies THROUGHPUT_THRESHOLDS (including the > 500 req/sec floor).
 *
 * User persona distribution:
 *   30 % — Donor registration staff     (create / view donors)
 *   25 % — Hospital coordinators        (blood requests)
 *   25 % — Inventory / lab staff        (search blood units, stock levels)
 *   10 % — Managers / report viewers    (dashboard + reports)
 *    5 % — Compliance officers          (audit / compliance endpoints)
 *    5 % — Admin / system               (config lookups, healthchecks)
 *
 * Run:
 *   k6 run tests/mixed-workload.js
 *   k6 run -e BASE_URL=http://api-gateway:8080 -e AUTH_TOKEN=<token> tests/mixed-workload.js
 */

import http  from 'k6/http';
import { check, sleep, group } from 'k6';
import {
  BASE_URL,
  THROUGHPUT_THRESHOLDS,
  defaultHeaders,
  randomItem,
  randomInt,
  BLOOD_TYPES,
} from '../k6.config.js';
import { generateDonor }          from '../generators/donors.js';
import { generateSearchFilter }   from '../generators/blood-units.js';
import { generateBloodRequest }   from '../generators/hospitals.js';

// ---------------------------------------------------------------------------
// k6 options — 1 000 VUs with a brief warm-up ramp
// ---------------------------------------------------------------------------
export const options = {
  scenarios: {
    mixed_workload: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 250  },  // warm-up: ramp to 25 %
        { duration: '30s', target: 750  },  // ramp to 75 %
        { duration: '30s', target: 1000 },  // ramp to peak
        { duration: '3m',  target: 1000 },  // sustain peak for 3 minutes
        { duration: '30s', target: 0    },  // ramp down
      ],
    },
  },
  thresholds: {
    ...THROUGHPUT_THRESHOLDS,
    // Per-persona group latency
    'http_req_duration{group:::Donor registration}':   ['p(95)<250'],
    'http_req_duration{group:::Blood requests}':       ['p(95)<300'],
    'http_req_duration{group:::Inventory search}':     ['p(95)<150'],
    'http_req_duration{group:::Dashboard & reports}':  ['p(95)<300'],
    'http_req_duration{group:::Compliance}':           ['p(95)<300'],
    'http_req_duration{group:::Admin}':                ['p(95)<100'],
  },
};

const HEADERS = defaultHeaders();

const HOSPITAL_IDS   = Array.from({ length: 50 }, (_, i) => `hospital-${String(i + 1).padStart(3, '0')}`);
const BRANCH_IDS     = Array.from({ length: 10 }, (_, i) => `BRANCH-${String(i + 1).padStart(3, '0')}`);
const COMPONENT_TYPES = ['WHOLE_BLOOD', 'RED_BLOOD_CELLS', 'PLATELETS', 'FRESH_FROZEN_PLASMA'];

// ---------------------------------------------------------------------------
// Pick a persona based on weighted probability
// ---------------------------------------------------------------------------
function pickPersona() {
  const r = Math.random();
  if (r < 0.30) return 'DONOR_STAFF';
  if (r < 0.55) return 'HOSPITAL_COORDINATOR';
  if (r < 0.80) return 'INVENTORY_STAFF';
  if (r < 0.90) return 'MANAGER';
  if (r < 0.95) return 'COMPLIANCE';
  return 'ADMIN';
}

// ---------------------------------------------------------------------------
// Persona behaviours
// ---------------------------------------------------------------------------
function donorStaff() {
  group('Donor registration', () => {
    // Register a donor
    const donor     = generateDonor();
    const createRes = http.post(
      `${BASE_URL}/api/v1/donors`,
      JSON.stringify(donor),
      { headers: HEADERS },
    );
    const ok = check(createRes, {
      'donor create — 201': (r) => r.status === 201,
    });

    // Read back if successful
    if (ok) {
      let id;
      try { id = JSON.parse(createRes.body).id; } catch (_) {}
      if (id) {
        const getRes = http.get(`${BASE_URL}/api/v1/donors/${id}`, { headers: HEADERS });
        check(getRes, { 'donor get — 200': (r) => r.status === 200 });
      }
    }

    // Browse donor list
    const listRes = http.get(
      `${BASE_URL}/api/v1/donors?page=${randomInt(0, 9)}&size=20`,
      { headers: HEADERS },
    );
    check(listRes, { 'donors list — 200': (r) => r.status === 200 });
  });
}

function hospitalCoordinator() {
  group('Blood requests', () => {
    const hospitalId = randomItem(HOSPITAL_IDS);
    const payload    = generateBloodRequest(hospitalId);

    // Submit request
    const createRes = http.post(
      `${BASE_URL}/api/v1/hospital-requests`,
      JSON.stringify(payload),
      { headers: HEADERS },
    );
    const ok = check(createRes, {
      'request create — 201': (r) => r.status === 201,
    });

    if (ok) {
      let id;
      try { id = JSON.parse(createRes.body).id; } catch (_) {}
      if (id) {
        const getRes = http.get(`${BASE_URL}/api/v1/hospital-requests/${id}`, { headers: HEADERS });
        check(getRes, { 'request get — 200': (r) => r.status === 200 });
      }
    }

    // List hospital's requests
    const listRes = http.get(
      `${BASE_URL}/api/v1/hospital-requests?hospitalId=${hospitalId}&page=0&size=10`,
      { headers: HEADERS },
    );
    check(listRes, { 'requests list — 200': (r) => r.status === 200 });
  });
}

function inventoryStaff() {
  group('Inventory search', () => {
    const bloodGroup    = randomItem(BLOOD_TYPES);
    const componentType = randomItem(COMPONENT_TYPES);
    const branchId      = randomItem(BRANCH_IDS);

    // Inventory search
    const searchRes = http.get(
      `${BASE_URL}/api/v1/inventory/search?bloodGroup=${encodeURIComponent(bloodGroup)}&componentType=${componentType}&status=AVAILABLE&page=0&size=20`,
      { headers: HEADERS },
    );
    check(searchRes, { 'inventory search — 200': (r) => r.status === 200 });

    // Stock levels
    const stockRes = http.get(
      `${BASE_URL}/api/v1/inventory/stock?bloodGroup=${encodeURIComponent(bloodGroup)}&branchId=${branchId}`,
      { headers: HEADERS },
    );
    check(stockRes, { 'inventory stock — 200': (r) => r.status === 200 });

    // Blood unit search
    const unitsRes = http.get(
      `${BASE_URL}/api/v1/blood-units/search?bloodGroup=${encodeURIComponent(bloodGroup)}&componentType=${componentType}&status=AVAILABLE&page=0&size=10`,
      { headers: HEADERS },
    );
    check(unitsRes, { 'blood-units search — 200': (r) => r.status === 200 });
  });
}

function manager() {
  group('Dashboard & reports', () => {
    // Dashboard stats
    const statsRes = http.get(
      `${BASE_URL}/api/v1/dashboard/stats`,
      { headers: HEADERS },
    );
    check(statsRes, { 'dashboard stats — 200': (r) => r.status === 200 });

    // List recent reports
    const reportsRes = http.get(
      `${BASE_URL}/api/v1/reports?page=0&size=10&sort=createdAt,desc`,
      { headers: HEADERS },
    );
    check(reportsRes, { 'reports list — 200': (r) => r.status === 200 });

    // Full stock overview
    const stockRes = http.get(`${BASE_URL}/api/v1/inventory/stock`, { headers: HEADERS });
    check(stockRes, { 'full stock — 200': (r) => r.status === 200 });
  });
}

function complianceOfficer() {
  group('Compliance', () => {
    const startDate = '2024-01-01';
    const endDate   = '2024-12-31';

    // Audit log / compliance report list
    const auditRes = http.get(
      `${BASE_URL}/api/v1/reports?reportType=COMPLIANCE_AUDIT&page=0&size=10`,
      { headers: HEADERS },
    );
    check(auditRes, { 'compliance audit list — 200': (r) => r.status === 200 });

    // Blood unit audit trail
    const unitsRes = http.get(
      `${BASE_URL}/api/v1/blood-units?page=${randomInt(0, 4)}&size=20`,
      { headers: HEADERS },
    );
    check(unitsRes, { 'blood-units audit — 200': (r) => r.status === 200 });
  });
}

function admin() {
  group('Admin', () => {
    // Health check
    const healthRes = http.get(`${BASE_URL}/actuator/health`, { headers: HEADERS });
    check(healthRes, { 'health — 200': (r) => r.status === 200 });

    // Donor count
    const donorRes = http.get(
      `${BASE_URL}/api/v1/donors?page=0&size=1`,
      { headers: HEADERS },
    );
    check(donorRes, { 'donor count check — 200': (r) => r.status === 200 });
  });
}

// ---------------------------------------------------------------------------
// Main VU loop
// ---------------------------------------------------------------------------
export default function () {
  const persona = pickPersona();

  switch (persona) {
    case 'DONOR_STAFF':          donorStaff();          break;
    case 'HOSPITAL_COORDINATOR': hospitalCoordinator(); break;
    case 'INVENTORY_STAFF':      inventoryStaff();      break;
    case 'MANAGER':              manager();             break;
    case 'COMPLIANCE':           complianceOfficer();   break;
    case 'ADMIN':                admin();               break;
    default:                     inventoryStaff();
  }

  // Simulate user think-time: 1–3 seconds between page interactions
  sleep(randomInt(1, 3));
}
