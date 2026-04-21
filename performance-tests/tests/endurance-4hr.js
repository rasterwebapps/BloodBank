/**
 * Endurance Test — 4-Hour Soak
 *
 * Sustains 500 virtual users for 4 hours to detect:
 *   • Memory leaks (growing heap, off-heap allocations)
 *   • Connection pool exhaustion (leak of JDBC / HTTP connections)
 *   • Thread pool exhaustion (Tomcat thread starvation)
 *   • Cache poisoning / size unboundedness
 *   • Log file growth / disk exhaustion
 *   • Gradual latency drift (response times increasing over time)
 *
 * Monitoring:
 *   • Prometheus metrics are scraped every 15 s during the test
 *   • Grafana dashboard panels to watch:
 *       - JVM heap used     (jvm_memory_used_bytes{area="heap"})
 *       - DB connection pool active  (hikaricp_connections_active)
 *       - Tomcat threads active      (tomcat_threads_busy_threads)
 *       - GC pause time             (jvm_gc_pause_seconds)
 *       - HTTP error rate           (http_server_requests{status=~"5.."})
 *
 * Run:
 *   k6 run tests/endurance-4hr.js
 *   k6 run -e BASE_URL=http://api-gateway:8080 -e AUTH_TOKEN=<token> tests/endurance-4hr.js
 *
 * Expected duration: ~4 hours 10 minutes (including ramp)
 */

import http  from 'k6/http';
import { check, sleep, group } from 'k6';
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
// k6 options — 4-hour sustained load
// ---------------------------------------------------------------------------
export const options = {
  scenarios: {
    endurance: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '5m',   target: 500 },   // gradual ramp to avoid JVM warm-up skew
        { duration: '4h',   target: 500 },   // 4-hour soak at 500 VUs
        { duration: '5m',   target: 0   },   // graceful ramp-down
      ],
      gracefulRampDown: '60s',
    },
  },
  thresholds: {
    ...BASE_THRESHOLDS,
    // Endurance-specific: latency must not drift over 4 hours
    'http_req_duration': ['p(95)<200', 'p(99)<500'],
    // Error rate must stay below 1 % for the entire duration
    'http_req_failed': ['rate<0.01'],
    // Track per-endpoint to detect individual service degradation
    'http_req_duration{name:POST /api/v1/donors}':              ['p(95)<250'],
    'http_req_duration{name:POST /api/v1/hospital-requests}':   ['p(95)<300'],
    'http_req_duration{name:GET  /api/v1/inventory/search}':    ['p(95)<150'],
    'http_req_duration{name:GET  /api/v1/dashboard/stats}':     ['p(95)<100'],
    'http_req_duration{name:GET  /api/v1/donors}':              ['p(95)<200'],
    'http_req_duration{name:GET  /api/v1/inventory/stock}':     ['p(95)<100'],
    'http_req_duration{name:GET  /api/v1/blood-units/search}':  ['p(95)<200'],
  },
};

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------
const HEADERS      = defaultHeaders();
const HOSPITAL_IDS = Array.from({ length: 50 }, (_, i) => `hospital-${String(i + 1).padStart(3, '0')}`);
const BRANCH_IDS   = Array.from({ length: 10 }, (_, i) => `BRANCH-${String(i + 1).padStart(3, '0')}`);
const COMPONENT_TYPES = [
  'WHOLE_BLOOD', 'RED_BLOOD_CELLS', 'PLATELETS',
  'FRESH_FROZEN_PLASMA', 'CRYOPRECIPITATE',
];

// ---------------------------------------------------------------------------
// Prometheus metric snapshot helper
//
// Periodically fetches JVM / HikariCP / Tomcat metrics via the actuator
// endpoint and emits custom k6 trend metrics for Grafana.
// ---------------------------------------------------------------------------
import { Trend, Counter, Gauge } from 'k6/metrics';

const heapUsedGauge     = new Gauge('jvm_heap_used_bytes');
const dbActiveGauge     = new Gauge('hikari_connections_active');
const threadsBusyGauge  = new Gauge('tomcat_threads_busy');
const gcPauseTrend      = new Trend('jvm_gc_pause_ms');
const memLeakCounter    = new Counter('memory_leak_alerts');

/**
 * Scrapes Prometheus-format metrics from the Spring Boot actuator.
 * Only one VU in every 500 performs this scrape per iteration to avoid overhead.
 */
function scrapeMetrics() {
  if (__VU % 500 !== 1) return;  // only first VU per cycle

  // Heap memory
  const heapRes = http.get(
    `${BASE_URL}/actuator/metrics/jvm.memory.used?tag=area:heap`,
    { headers: HEADERS, tags: { name: 'actuator:heap' } },
  );
  if (heapRes.status === 200) {
    try {
      const data  = JSON.parse(heapRes.body);
      const value = data.measurements && data.measurements[0] ? data.measurements[0].value : 0;
      heapUsedGauge.add(value);
      // Alert if heap > 1.5 GB (indicates potential leak)
      if (value > 1.5e9) {
        memLeakCounter.add(1);
        console.warn(`[endurance] Heap usage high: ${(value / 1e9).toFixed(2)} GB`);
      }
    } catch (_) {}
  }

  // HikariCP active connections
  const dbRes = http.get(
    `${BASE_URL}/actuator/metrics/hikaricp.connections.active`,
    { headers: HEADERS, tags: { name: 'actuator:db-connections' } },
  );
  if (dbRes.status === 200) {
    try {
      const data  = JSON.parse(dbRes.body);
      const value = data.measurements && data.measurements[0] ? data.measurements[0].value : 0;
      dbActiveGauge.add(value);
      if (value > 45) {
        console.warn(`[endurance] DB connections near limit: ${value}/50`);
      }
    } catch (_) {}
  }

  // Tomcat busy threads
  const threadsRes = http.get(
    `${BASE_URL}/actuator/metrics/tomcat.threads.busy`,
    { headers: HEADERS, tags: { name: 'actuator:threads' } },
  );
  if (threadsRes.status === 200) {
    try {
      const data  = JSON.parse(threadsRes.body);
      const value = data.measurements && data.measurements[0] ? data.measurements[0].value : 0;
      threadsBusyGauge.add(value);
      if (value > 180) {
        console.warn(`[endurance] Thread pool near exhaustion: ${value}/200`);
      }
    } catch (_) {}
  }
}

// ---------------------------------------------------------------------------
// Workload functions (same persona distribution as mixed-workload.js)
// ---------------------------------------------------------------------------
function runDonorStaff() {
  group('Donor operations', () => {
    const donor = generateDonor();
    const createRes = http.post(
      `${BASE_URL}/api/v1/donors`,
      JSON.stringify(donor),
      { headers: HEADERS, tags: { name: 'POST /api/v1/donors' } },
    );
    check(createRes, { 'donor create — 201': (r) => r.status === 201 });

    const listRes = http.get(
      `${BASE_URL}/api/v1/donors?page=${randomInt(0, 9)}&size=20`,
      { headers: HEADERS, tags: { name: 'GET  /api/v1/donors' } },
    );
    check(listRes, { 'donors list — 200': (r) => r.status === 200 });
  });
}

function runHospitalCoordinator() {
  group('Blood request operations', () => {
    const hospitalId = randomItem(HOSPITAL_IDS);
    const res = http.post(
      `${BASE_URL}/api/v1/hospital-requests`,
      JSON.stringify(generateBloodRequest(hospitalId)),
      { headers: HEADERS, tags: { name: 'POST /api/v1/hospital-requests' } },
    );
    check(res, { 'blood request — 201': (r) => r.status === 201 });

    const listRes = http.get(
      `${BASE_URL}/api/v1/hospital-requests?hospitalId=${hospitalId}&page=0&size=10`,
      { headers: HEADERS },
    );
    check(listRes, { 'requests list — 200': (r) => r.status === 200 });
  });
}

function runInventoryStaff() {
  group('Inventory search operations', () => {
    const bloodGroup    = randomItem(BLOOD_TYPES);
    const componentType = randomItem(COMPONENT_TYPES);
    const branchId      = randomItem(BRANCH_IDS);

    const searchRes = http.get(
      `${BASE_URL}/api/v1/inventory/search?bloodGroup=${encodeURIComponent(bloodGroup)}&componentType=${componentType}&status=AVAILABLE&page=0&size=20`,
      { headers: HEADERS, tags: { name: 'GET  /api/v1/inventory/search' } },
    );
    check(searchRes, { 'inventory search — 200': (r) => r.status === 200 });

    const stockRes = http.get(
      `${BASE_URL}/api/v1/inventory/stock?bloodGroup=${encodeURIComponent(bloodGroup)}`,
      { headers: HEADERS, tags: { name: 'GET  /api/v1/inventory/stock' } },
    );
    check(stockRes, { 'stock level — 200': (r) => r.status === 200 });

    const unitsRes = http.get(
      `${BASE_URL}/api/v1/blood-units/search?bloodGroup=${encodeURIComponent(bloodGroup)}&componentType=${componentType}&branchId=${branchId}&status=AVAILABLE&page=0&size=20`,
      { headers: HEADERS, tags: { name: 'GET  /api/v1/blood-units/search' } },
    );
    check(unitsRes, { 'blood units search — 200': (r) => r.status === 200 });
  });
}

function runManager() {
  group('Dashboard operations', () => {
    const statsRes = http.get(
      `${BASE_URL}/api/v1/dashboard/stats`,
      { headers: HEADERS, tags: { name: 'GET  /api/v1/dashboard/stats' } },
    );
    check(statsRes, { 'dashboard stats — 200': (r) => r.status === 200 });

    const stockRes = http.get(
      `${BASE_URL}/api/v1/inventory/stock`,
      { headers: HEADERS, tags: { name: 'GET  /api/v1/inventory/stock' } },
    );
    check(stockRes, { 'full stock overview — 200': (r) => r.status === 200 });
  });
}

// ---------------------------------------------------------------------------
// Main VU loop
// ---------------------------------------------------------------------------
export default function () {
  // Scrape Prometheus metrics (one VU only, low overhead)
  scrapeMetrics();

  // Pick a user persona
  const r = Math.random();
  if      (r < 0.30) runDonorStaff();
  else if (r < 0.55) runHospitalCoordinator();
  else if (r < 0.80) runInventoryStaff();
  else               runManager();

  // Realistic think-time: 1–3 seconds between interactions
  sleep(randomInt(1, 3));
}
