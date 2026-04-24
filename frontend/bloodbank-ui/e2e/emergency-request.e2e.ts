/**
 * E2E: Emergency Blood Request — happy path
 *
 * Covers:
 *   1. Navigate to the emergency request form
 *   2. Submit with P1 (critical) priority — assert button colour is red (warn)
 *   3. Fill all required fields and submit
 *   4. Assert a success notification appears
 *   5. Assert the new request appears in the "recent requests" table
 *   6. Validate required-field errors when submitting an empty form
 *
 * Assumes:
 *   - Authenticated staff user with DOCTOR, NURSE, or BRANCH_MANAGER role
 *   - Backend emergency endpoint is available
 */
import { test, expect } from '@playwright/test';

test.describe('Emergency blood request — happy path', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/staff/emergency/new');
    await expect(
      page.getByRole('heading', { name: /emergency blood request/i }),
    ).toBeVisible({ timeout: 10_000 });
  });

  test('should create a P1-critical emergency request successfully', async ({ page }) => {
    // ── Set priority to P1_CRITICAL ─────────────────────────────────────────
    await page.getByLabel(/priority/i).click();
    await page.getByRole('option', { name: /p1.*critical/i }).click();

    // The submit button should now have warn (red) styling
    const submitBtn = page.getByRole('button', { name: /create emergency request/i });
    await expect(submitBtn).toHaveClass(/mat-warn|warn/, { timeout: 3_000 });

    // ── Fill required patient details ───────────────────────────────────────
    await page.getByLabel('Patient Name').fill('Sophie Williams');
    await page.getByLabel('Patient ID').fill(`PAT-EMR-${Date.now()}`);

    await page.getByLabel(/blood group/i).click();
    await page.getByRole('option', { name: /^O$/ }).click();

    await page.getByLabel(/rh factor/i).click();
    await page.getByRole('option', { name: /negative/i }).click();

    await page.getByLabel(/component type/i).click();
    await page.getByRole('option', { name: /packed red cells/i }).click();

    await page.getByLabel(/units required/i).fill('4');
    await page.getByLabel(/hospital/i).fill('City General Hospital');
    await page.getByLabel(/notes/i).fill('Trauma surgery — immediate requirement.');

    // ── Submit ──────────────────────────────────────────────────────────────
    await submitBtn.click();

    // ── Success notification ────────────────────────────────────────────────
    await expect(
      page.locator('.snackbar-success, mat-snack-bar-container'),
    ).toContainText('Emergency request created successfully', { timeout: 15_000 });

    // ── New request appears in recent requests table ─────────────────────────
    await expect(
      page.locator('mat-table, [data-testid="recent-requests-table"]')
          .locator('text=Sophie Williams'),
    ).toBeVisible({ timeout: 10_000 });

    // ── Form resets after successful submission ─────────────────────────────
    await expect(page.getByLabel('Patient Name')).toHaveValue('');
    await expect(page.getByLabel(/units required/i)).toHaveValue('1');
  });

  test('should show warn button colour for P2_URGENT priority', async ({ page }) => {
    await page.getByLabel(/priority/i).click();
    await page.getByRole('option', { name: /p2.*urgent/i }).click();

    const submitBtn = page.getByRole('button', { name: /create emergency request/i });
    await expect(submitBtn).toHaveClass(/mat-warn|warn/);
  });

  test('should show primary button colour for P3_HIGH priority (default)', async ({ page }) => {
    // P3_HIGH is the default value — no interaction needed
    const submitBtn = page.getByRole('button', { name: /create emergency request/i });
    await expect(submitBtn).toHaveClass(/mat-primary|primary/);
  });

  test('should show validation errors when required fields are empty', async ({ page }) => {
    // Clear any pre-filled values
    await page.getByLabel('Patient Name').clear();

    await page.getByRole('button', { name: /create emergency request/i }).click();

    // At least one mat-error must appear
    await expect(page.locator('mat-error').first()).toBeVisible();

    // No success snackbar
    await expect(page.locator('.snackbar-success')).not.toBeVisible();
  });

  test('should navigate to emergency dashboard from the link', async ({ page }) => {
    await page.getByRole('button', { name: /view dashboard|emergency dashboard/i }).click();
    await expect(page).toHaveURL(/\/staff\/emergency(?!\/new)/);
  });
});
