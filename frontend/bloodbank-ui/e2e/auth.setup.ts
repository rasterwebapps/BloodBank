/**
 * Playwright auth setup — runs once before all E2E tests.
 *
 * Logs in as a staff user through the Keycloak login page and saves
 * the browser storage state (cookies + localStorage) so that subsequent
 * test workers can start already authenticated.
 *
 * The saved state file is referenced in playwright.config.ts:
 *   storageState: 'playwright/.auth/staff.json'
 */
import { test as setup, expect } from '@playwright/test';
import * as path from 'path';

const AUTH_FILE = path.join(__dirname, '../playwright/.auth/staff.json');

setup('authenticate as staff user', async ({ page }) => {
  const username = process.env['TEST_USER'] ?? 'teststaff';
  const password = process.env['TEST_PASS'] ?? 'Test@1234';

  // Navigate to the app — the roleGuard redirects to Keycloak login
  await page.goto('/staff/dashboard');

  // Wait for the Keycloak login form
  await page.waitForURL(/\/auth\/realms\/bloodbank\//);

  // Fill credentials — use role-based selectors to avoid fragility from Keycloak UI text changes
  await page.getByRole('textbox', { name: /username|email/i }).fill(username);
  await page.getByRole('textbox', { name: /password/i }).fill(password);
  await page.getByRole('button', { name: /sign in/i }).click();

  // After login Keycloak redirects back to the app
  await page.waitForURL(/\/staff\//);
  await expect(page.locator('app-shell, app-sidenav, [data-testid="sidenav"]')).toBeVisible({
    timeout: 15_000,
  });

  // Persist the authenticated browser state
  await page.context().storageState({ path: AUTH_FILE });
});
