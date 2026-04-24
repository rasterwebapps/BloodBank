/**
 * E2E: Cross-Match Request — happy path
 *
 * Covers:
 *   1. Navigate to the cross-match request page
 *   2. Fill the request form with valid patient data
 *   3. Submit the request
 *   4. Assert a success notification appears
 *   5. Assert the new request appears in the pending requests table
 *
 * Assumes:
 *   - Authenticated staff user with DOCTOR or LAB_TECHNICIAN role
 *   - Backend returns the created request with status PENDING
 */
import { test, expect } from '@playwright/test';

test.describe('Cross-match request — happy path', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/staff/transfusion/crossmatch');
    await expect(
      page.getByRole('heading', { name: /cross.?match/i }),
    ).toBeVisible({ timeout: 10_000 });
  });

  test('should submit a valid cross-match request and show it in the list', async ({ page }) => {
    // ── Fill the form ───────────────────────────────────────────────────────
    await page.getByLabel('Patient Name').fill('Michael Torres');
    await page.getByLabel('Patient ID').fill(`PAT-${Date.now()}`);

    await page.getByLabel('Blood Group').click();
    await page.getByRole('option', { name: 'A+' }).click();

    await page.getByLabel(/rh factor/i).click();
    await page.getByRole('option', { name: /positive/i }).click();

    await page.getByLabel(/component type/i).click();
    await page.getByRole('option', { name: /packed red cells/i }).click();

    await page.getByLabel(/units requested/i).fill('2');

    await page.getByLabel(/urgency/i).click();
    await page.getByRole('option', { name: /routine/i }).click();

    // ── Submit ──────────────────────────────────────────────────────────────
    await page.getByRole('button', { name: /submit/i }).click();

    // ── Success notification ────────────────────────────────────────────────
    await expect(
      page.locator('.snackbar-success, mat-snack-bar-container'),
    ).toContainText('Cross-match request submitted successfully', { timeout: 10_000 });

    // ── New request appears in the table ────────────────────────────────────
    await expect(
      page.locator('mat-table, table').locator('text=Michael Torres'),
    ).toBeVisible({ timeout: 10_000 });

    // Form is reset — patient name field should be empty
    await expect(page.getByLabel('Patient Name')).toHaveValue('');
  });

  test('should show validation errors when required fields are empty', async ({ page }) => {
    await page.getByRole('button', { name: /submit/i }).click();

    // The form is invalid — an Angular Material error or mat-error appears
    // The component calls markAllAsTouched, which shows required field indicators
    await expect(page.locator('mat-error').first()).toBeVisible();

    // Snackbar should NOT show a success message
    await expect(
      page.locator('.snackbar-success'),
    ).not.toBeVisible();
  });
});
