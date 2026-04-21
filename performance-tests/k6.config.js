/**
 * k6 Base Configuration — BloodBank Performance Tests
 *
 * Shared thresholds, options, and helpers imported by every test.
 * Performance targets (from README / architecture docs):
 *   • P95 response time  < 200 ms
 *   • P99 response time  < 500 ms
 *   • Error rate         < 1 %
 *   • Throughput         > 500 req/sec  (mixed-workload & stress tests only)
 */

// ---------------------------------------------------------------------------
// Environment
// ---------------------------------------------------------------------------
export const BASE_URL   = __ENV.BASE_URL   || 'http://localhost:8080';
export const AUTH_TOKEN = __ENV.AUTH_TOKEN || '';

// ---------------------------------------------------------------------------
// Shared thresholds — import and spread into your test's `options` object
// ---------------------------------------------------------------------------
export const BASE_THRESHOLDS = {
  // Latency
  http_req_duration: ['p(95)<200', 'p(99)<500'],
  // Availability — fewer than 1 % of requests may fail
  http_req_failed: ['rate<0.01'],
};

/**
 * Thresholds for high-concurrency / mixed tests that also assert throughput.
 * Use this instead of BASE_THRESHOLDS when you want the 500 req/s floor.
 */
export const THROUGHPUT_THRESHOLDS = {
  ...BASE_THRESHOLDS,
  http_reqs: ['rate>500'],
};

// ---------------------------------------------------------------------------
// Common HTTP headers
// ---------------------------------------------------------------------------
export function defaultHeaders() {
  return {
    'Content-Type':  'application/json',
    'Authorization': `Bearer ${AUTH_TOKEN}`,
  };
}

export function authHeader() {
  return { Authorization: `Bearer ${AUTH_TOKEN}` };
}

// ---------------------------------------------------------------------------
// Pre-built k6 scenario helpers
// ---------------------------------------------------------------------------

/**
 * Returns a constant-arrival-rate scenario object.
 *
 * @param {number} rate          - target requests per second
 * @param {number} duration      - test duration in seconds
 * @param {number} preAllocated  - pre-allocated VUs
 * @param {number} maxVUs        - max VUs k6 may spawn
 */
export function constantRateScenario(rate, duration = 60, preAllocated = 20, maxVUs = 200) {
  return {
    executor:          'constant-arrival-rate',
    rate,
    timeUnit:          '1s',
    duration:          `${duration}s`,
    preAllocatedVUs:   preAllocated,
    maxVUs,
  };
}

/**
 * Returns a constant VU scenario object.
 *
 * @param {number} vus       - number of virtual users
 * @param {number} duration  - duration in seconds
 */
export function constantVUsScenario(vus, duration = 60) {
  return {
    executor: 'constant-vus',
    vus,
    duration: `${duration}s`,
  };
}

/**
 * Returns a ramping-VU scenario with the provided stages array.
 *
 * @param {Array<{duration: string, target: number}>} stages
 * @param {number} startVUs
 */
export function rampingVUsScenario(stages, startVUs = 0) {
  return {
    executor:  'ramping-vus',
    startVUs,
    stages,
  };
}

/**
 * Returns a ramping-arrival-rate scenario.
 *
 * @param {Array<{duration: string, target: number}>} stages
 * @param {number} preAllocated
 * @param {number} maxVUs
 */
export function rampingRateScenario(stages, preAllocated = 50, maxVUs = 500) {
  return {
    executor:        'ramping-arrival-rate',
    startRate:       0,
    timeUnit:        '1s',
    preAllocatedVUs: preAllocated,
    maxVUs,
    stages,
  };
}

// ---------------------------------------------------------------------------
// Utility: random item from an array
// ---------------------------------------------------------------------------
export function randomItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

// ---------------------------------------------------------------------------
// Utility: random integer in [min, max]
// ---------------------------------------------------------------------------
export function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

// ---------------------------------------------------------------------------
// Blood types constant — used across tests and generators
// ---------------------------------------------------------------------------
export const BLOOD_TYPES = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];
