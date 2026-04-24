/**
 * E2E: Transfusion Reaction Report — happy path
 *
 * Covers:
 *   1. Navigate to the reaction report page
 *   2. Select an existing transfusion
 *   3. Fill the reaction details
 *   4. Submit and assert the success notification
 *   5. Assert the form resets to defaults after submission
 *   6. Assert critical-severity reactions are visually flagged
 *
 * Assumes:
 *   - Authenticated staff user with NURSE or DOCTOR role
 *   - At least one in-progress transfusion exists in the system
 *   - Backend accepts the reaction payload
 */
import { test, expect } from '@playwright/test';

test.describe('Transfusion reaction report — happy path', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/staff/transfusion/reactions/new');
    await expect(
      page.getByRole('heading', { name: /reaction report/i }),
    ).toBeVisible({ timeout: 10_000 });
    // Wait for the transfusion list to load
    await page.waitForLoadState('networkidle');
  });

  test('should report a mild febrile reaction successfully', async ({ page }) => {
    // ── Select transfusion ──────────────────────────────────────────────────
    const transfusionSelect = page.getByLabel(/transfusion/i);
    await transfusionSelect.click();
    // Select the first available option (assumes ≥1 transfusion)
    await page.getByRole('option').first().click();

    // ── Fill reaction type (default is FEBRILE — verify it is pre-selected)
    await expect(page.getByLabel(/reaction type/i)).toContainText(/febrile/i);

    // ── Severity (default is MILD) ──────────────────────────────────────────
    await expect(page.getByLabel(/severity/i)).toContainText(/mild/i);

    // ── Onset time ─────────────────────────────────────────────────────────
    await page.getByLabel(/onset time/i).fill('2026-04-24T10:30');

    // ── Actions taken ───────────────────────────────────────────────────────
    await page.getByLabel(/actions taken/i).fill(
      'Stopped transfusion. Administered antihistamine. Notified physician.',
    );

    // ── Notes ───────────────────────────────────────────────────────────────
    await page.getByLabel(/notes/i).fill('Patient responded well within 15 minutes.');

    // ── Submit ──────────────────────────────────────────────────────────────
    await page.getByRole('button', { name: /report reaction/i }).click();

    // ── Success notification ────────────────────────────────────────────────
    await expect(
      page.locator('.snackbar-success, mat-snack-bar-container'),
    ).toContainText('Reaction reported successfully', { timeout: 10_000 });

    // ── Form resets to defaults after submission ─────────────────────────────
    await expect(page.getByLabel(/reaction type/i)).toContainText(/febrile/i);
    await expect(page.getByLabel(/severity/i)).toContainText(/mild/i);
    await expect(page.getByLabel(/actions taken/i)).toHaveValue('');
  });

  test('should indicate a life-threatening reaction is critical', async ({ page }) => {
    // Select transfusion
    await page.getByLabel(/transfusion/i).click();
    await page.getByRole('option').first().click();

    // Change severity to LIFE_THREATENING
    await page.getByLabel(/severity/i).click();
    await page.getByRole('option', { name: /life threatening/i }).click();

    // The component exposes isCriticalReaction — the template should display a warning
    await expect(
      page.locator('[data-testid="critical-reaction-warning"], .critical-reaction-alert, .mat-warn'),
    ).toBeVisible();
  });

  test('should show validation errors when required fields are empty', async ({ page }) => {
    await page.getByRole('button', { name: /report reaction/i }).click();

    // Mat-error elements should appear for untouched required fields
    await expect(page.locator('mat-error').first()).toBeVisible();

    // Snackbar should not show success
    await expect(page.locator('.snackbar-success')).not.toBeVisible();
  });
});
