import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright E2E configuration for BloodBank UI.
 *
 * Prerequisites:
 *   1. Run `npm install` to install @playwright/test.
 *   2. Run `npx playwright install chromium` to download the Chromium browser.
 *   3. Start the Angular dev server: `npm run start`
 *   4. The Keycloak + API gateway must be reachable at the configured base URL.
 *
 * Environment variables:
 *   BASE_URL  — base URL of the running app  (default: http://localhost:4200)
 *   TEST_USER — staff username for Keycloak  (default: teststaff)
 *   TEST_PASS — staff password               (default: Test@1234)
 */
export default defineConfig({
  testDir: './e2e',
  testMatch: '**/*.e2e.ts',

  /**
   * Maximum time one test can run.
   * 60 s accommodates the Keycloak redirect round-trip (~2–5 s), Angular
   * lazy-loading of feature modules (~1–3 s), and API response times in CI
   * where cold-start latency can reach 10–20 s per service.
   */
  timeout: 60_000,

  /* Retry failed tests once in CI. */
  retries: process.env['CI'] ? 1 : 0,

  /* Run tests in parallel — each worker gets its own browser context. */
  workers: process.env['CI'] ? 2 : undefined,

  reporter: [
    ['list'],
    ['html', { open: 'never', outputFolder: 'playwright-report' }],
  ],

  use: {
    /* Base URL for all page.goto() calls without a full URL. */
    baseURL: process.env['BASE_URL'] ?? 'http://localhost:4200',

    /* Collect traces on test failure for debugging. */
    trace: 'on-first-retry',

    /* Capture screenshots on failure. */
    screenshot: 'only-on-failure',

    /* Viewport matching the most common staff monitor resolution. */
    viewport: { width: 1440, height: 900 },
  },

  projects: [
    /* Set up: authenticate once and save the storage state. */
    {
      name: 'setup',
      testMatch: '**/auth.setup.ts',
    },

    /* Run all E2E tests in Chromium using the saved auth state. */
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        storageState: 'playwright/.auth/staff.json',
      },
      dependencies: ['setup'],
    },
  ],

  /* Start the Angular dev server before tests if it is not already running. */
  webServer: {
    command: 'npm run start',
    url: process.env['BASE_URL'] ?? 'http://localhost:4200',
    reuseExistingServer: true,
    timeout: 120_000,
  },
});
