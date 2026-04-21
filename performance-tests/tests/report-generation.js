/**
 * Load Test — Report Generation
 *
 * Target: 20 concurrent large report requests processed simultaneously.
 * Reports are CPU/DB-intensive; this test validates that heavy background
 * workloads do not degrade latency or exhaust connection pools.
 *
 * Endpoints exercised:
 *   POST /api/v1/reports/generate    — trigger async report generation
 *   GET  /api/v1/reports/{id}        — poll report status
 *   GET  /api/v1/reports             — list recent reports
 *   GET  /api/v1/reports/{id}/download — download completed report (if available)
 *
 * Run:
 *   k6 run tests/report-generation.js
 */

import http  from 'k6/http';
import { check, sleep, group } from 'k6';
import { BASE_URL, BASE_THRESHOLDS, defaultHeaders, randomItem, randomInt } from '../k6.config.js';

// ---------------------------------------------------------------------------
// Report types and parameters
// ---------------------------------------------------------------------------
const REPORT_TYPES = [
  {
    type: 'DONOR_SUMMARY',
    params: {
      startDate: '2024-01-01',
      endDate:   '2024-12-31',
      format:    'PDF',
    },
  },
  {
    type: 'BLOOD_INVENTORY',
    params: {
      includeExpired:   false,
      includeReserved:  true,
      groupBy:          'BLOOD_GROUP',
      format:           'PDF',
    },
  },
  {
    type: 'TRANSFUSION_OUTCOMES',
    params: {
      startDate: '2024-01-01',
      endDate:   '2024-12-31',
      includeAdverseEvents: true,
      format:    'PDF',
    },
  },
  {
    type: 'COMPLIANCE_AUDIT',
    params: {
      startDate: '2024-01-01',
      endDate:   '2024-12-31',
      standard:  'AABB',
      format:    'PDF',
    },
  },
  {
    type: 'FINANCIAL_SUMMARY',
    params: {
      startDate: '2024-01-01',
      endDate:   '2024-12-31',
      currency:  'USD',
      format:    'EXCEL',
    },
  },
  {
    type: 'EXPIRY_WASTE',
    params: {
      startDate:  '2024-01-01',
      endDate:    '2024-12-31',
      bloodGroup: null,
      format:     'PDF',
    },
  },
];

// ---------------------------------------------------------------------------
// k6 options
// ---------------------------------------------------------------------------
export const options = {
  scenarios: {
    report_generation: {
      executor:  'constant-vus',
      vus:       20,       // 20 concurrent users generating reports
      duration:  '3m',
    },
  },
  thresholds: {
    ...BASE_THRESHOLDS,
    // Report generation can be slower (async)
    'http_req_duration{name:POST /api/v1/reports/generate}':     ['p(95)<500'],
    'http_req_duration{name:GET  /api/v1/reports/{id}}':         ['p(95)<200'],
    'http_req_duration{name:GET  /api/v1/reports}':              ['p(95)<300'],
    'http_req_duration{name:GET  /api/v1/reports/{id}/download}': ['p(95)<2000'],
    // Generous error threshold — report queuing may return 202
    http_req_failed: ['rate<0.02'],
  },
};

const HEADERS = defaultHeaders();
const MAX_POLL_ATTEMPTS = 12; // poll up to 12 times (12 × 5s = 60s)

// ---------------------------------------------------------------------------
// Scenario
// ---------------------------------------------------------------------------
export default function () {
  const template = randomItem(REPORT_TYPES);

  // ── 1. Trigger report generation ──────────────────────────────────────────
  group('Trigger report', () => {
    const payload = {
      reportType: template.type,
      parameters: {
        ...template.params,
        requestedAt: new Date().toISOString(),
      },
    };

    const createRes = http.post(
      `${BASE_URL}/api/v1/reports/generate`,
      JSON.stringify(payload),
      { headers: HEADERS, tags: { name: 'POST /api/v1/reports/generate' } },
    );

    const accepted = check(createRes, {
      'POST /api/v1/reports/generate — accepted': (r) => r.status === 202 || r.status === 201,
      'POST /api/v1/reports/generate — has id':   (r) => {
        try { return !!JSON.parse(r.body).id; } catch (_) { return false; }
      },
    });

    if (!accepted) {
      console.warn(`[report-generation] trigger failed: ${createRes.status} — ${createRes.body}`);
      sleep(5);
      return;
    }

    let reportId;
    try {
      reportId = JSON.parse(createRes.body).id;
    } catch (_) {
      sleep(5);
      return;
    }

    // ── 2. Poll for completion ───────────────────────────────────────────────
    let completed = false;
    for (let attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {
      sleep(5); // poll every 5 seconds

      const pollRes = http.get(
        `${BASE_URL}/api/v1/reports/${reportId}`,
        { headers: HEADERS, tags: { name: 'GET  /api/v1/reports/{id}' } },
      );

      check(pollRes, {
        'GET /api/v1/reports/{id} — status 200': (r) => r.status === 200,
      });

      let status;
      try {
        status = JSON.parse(pollRes.body).status;
      } catch (_) {
        break;
      }

      if (status === 'COMPLETED' || status === 'FAILED') {
        completed = true;

        check({ status }, {
          'Report completed successfully': (s) => s.status === 'COMPLETED',
        });

        // ── 3. Download completed report ─────────────────────────────────────
        if (status === 'COMPLETED') {
          const dlRes = http.get(
            `${BASE_URL}/api/v1/reports/${reportId}/download`,
            { headers: HEADERS, tags: { name: 'GET  /api/v1/reports/{id}/download' } },
          );
          check(dlRes, {
            'report download — status 200': (r) => r.status === 200,
            'report download — has content': (r) => r.body.length > 0,
          });
        }
        break;
      }
    }

    if (!completed) {
      console.warn(`[report-generation] report ${reportId} did not complete within poll window`);
    }
  });

  // ── 4. List recent reports ───────────────────────────────────────────────
  const listRes = http.get(
    `${BASE_URL}/api/v1/reports?page=0&size=10&sort=createdAt,desc`,
    { headers: HEADERS, tags: { name: 'GET  /api/v1/reports' } },
  );
  check(listRes, {
    'GET /api/v1/reports — status 200':    (r) => r.status === 200,
    'GET /api/v1/reports — body is JSON':  (r) => {
      try { JSON.parse(r.body); return true; } catch (_) { return false; }
    },
  });

  // Short pause before next report request
  sleep(randomInt(2, 5));
}
