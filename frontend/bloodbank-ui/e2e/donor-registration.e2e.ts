/**
 * E2E: Donor Registration — happy path
 *
 * Covers the full donor registration flow:
 *   1. Navigate to the new-donor form
 *   2. Fill in all required fields
 *   3. Submit the form
 *   4. Assert the user is redirected to the donor detail page
 *   5. Assert a success notification is shown
 *
 * Assumes:
 *   - Authenticated staff user with RECEPTIONIST or PHLEBOTOMIST role
 *   - Backend API is available and accepts the donor payload
 */
import { test, expect } from '@playwright/test';

test.describe('Donor registration — happy path', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/staff/donors/new');
    // Wait for the form to render
    await expect(page.locator('h1')).toContainText('Register Donor');
  });

  test('should successfully register a new donor', async ({ page }) => {
    // ── Personal information ────────────────────────────────────────────────
    await page.getByLabel('First Name').fill('Amara');
    await page.getByLabel('Last Name').fill('Johnson');

    // Date of birth — open datepicker and type the date
    await page.getByLabel('Date of Birth').fill('1985-03-20');

    await page.getByLabel('Gender').click();
    await page.getByRole('option', { name: 'Female' }).click();

    // ── Blood group ─────────────────────────────────────────────────────────
    await page.getByLabel(/blood group/i).click();
    await page.getByRole('option', { name: 'O+' }).click();

    // ── Contact ─────────────────────────────────────────────────────────────
    await page.getByLabel('Email').fill(`amara.johnson.${Date.now()}@example.com`);
    await page.getByLabel('Phone').fill('+12025551234');

    // ── Address ─────────────────────────────────────────────────────────────
    await page.getByLabel('Address Line 1').fill('456 Oak Avenue');
    await page.getByLabel('Postal Code').fill('20001');

    // ── Identity ────────────────────────────────────────────────────────────
    await page.getByLabel('National ID').fill(`NID-E2E-${Date.now()}`);
    await page.getByLabel('Nationality').fill('American');

    // ── Donor type ──────────────────────────────────────────────────────────
    await page.getByLabel(/donor type/i).click();
    await page.getByRole('option', { name: 'Voluntary' }).click();

    // ── Submit ──────────────────────────────────────────────────────────────
    await page.getByRole('button', { name: /register/i }).click();

    // ── Assertions ──────────────────────────────────────────────────────────
    // Redirected to donor detail page
    await expect(page).toHaveURL(/\/staff\/donors\/[a-f0-9-]{36}$/, { timeout: 15_000 });

    // Success snackbar visible
    await expect(
      page.locator('.snackbar-success, mat-snack-bar-container'),
    ).toContainText('Donor registered successfully', { timeout: 5_000 });
  });

  test('should show validation errors when required fields are missing', async ({ page }) => {
    // Click submit without filling anything
    await page.getByRole('button', { name: /register/i }).click();

    // Required-field error messages must appear
    await expect(page.getByText('First name is required')).toBeVisible();
    await expect(page.getByText('Last name is required')).toBeVisible();
    await expect(page.getByText('Email is required')).toBeVisible();

    // URL must NOT change (stayed on the form page)
    await expect(page).toHaveURL(/\/staff\/donors\/new/);
  });

  test('should navigate back when cancel is clicked', async ({ page }) => {
    await page.getByLabel('Go back').click();
    // Redirected away from the form
    await expect(page).toHaveURL(/\/staff\/donors(?!\/new)/);
  });
});
