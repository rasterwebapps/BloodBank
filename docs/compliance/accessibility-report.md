# Accessibility Compliance Report

**Last Updated**: 2026-04-21
**Milestone Issues**: M9-033, M9-034, M9-035, M9-036
**Status**: 🟡 PENDING VALIDATION

---

## Overview

This document provides the accessibility compliance checklist for the BloodBank Angular 21 frontend (`frontend/bloodbank-ui/`). BloodBank must meet **WCAG 2.1 Level AA** as a minimum standard, supporting diverse clinical staff including those with visual, motor, auditory, and cognitive impairments.

**Tooling:**
- Automated: `axe-core` (via `@axe-core/playwright` in E2E tests), Angular ESLint `a11y` plugin
- Manual: NVDA + Chrome, VoiceOver + Safari, JAWS + Edge
- Color contrast: Colour Contrast Analyser, browser DevTools

---

## M9-033: WCAG 2.1 AA Checklist

### Principle 1 — Perceivable

#### 1.1 Text Alternatives

| Criterion | Level | Requirement | Implementation | Status |
|---|---|---|---|---|
| 1.1.1 Non-text Content | A | All images have descriptive `alt` attributes | `<img alt="...">` on all images | ☐ |
| 1.1.1 Non-text Content | A | Decorative images use `alt=""` or `aria-hidden="true"` | Icons used decoratively: `aria-hidden="true"` | ☐ |
| 1.1.1 Non-text Content | A | Icon buttons have `aria-label` | `<button mat-icon-button aria-label="Delete donor">` | ☐ |
| 1.1.1 Non-text Content | A | Charts have text alternatives | Chart.js: `aria-label` on canvas + data table fallback | ☐ |
| 1.1.1 Non-text Content | A | Status badges have text equivalent | Blood group badges: visible text + `aria-label` | ☐ |

**Angular template pattern:**

```html
<!-- ✅ Icon button with aria-label -->
<button mat-icon-button aria-label="Delete donor {{donor.firstName}}">
  <mat-icon aria-hidden="true">delete</mat-icon>
</button>

<!-- ✅ Decorative icon -->
<mat-icon aria-hidden="true">bloodtype</mat-icon>

<!-- ✅ Chart with text alternative -->
<canvas #bloodStockChart
  aria-label="Blood stock levels chart"
  role="img">
</canvas>
<div class="sr-only">
  <!-- Tabular data as fallback -->
  <table aria-label="Blood stock levels data">...</table>
</div>
```

---

#### 1.2 Time-based Media

| Criterion | Level | Requirement | Status |
|---|---|---|---|
| 1.2.1 Audio-only / Video-only | A | Training videos have text transcripts | ☐ |
| 1.2.2 Captions (pre-recorded) | A | Instructional videos have synchronized captions | ☐ |
| 1.2.3 Audio Description | A | Video content has audio description or text alternative | ☐ |

---

#### 1.3 Adaptable

| Criterion | Level | Requirement | Implementation | Status |
|---|---|---|---|---|
| 1.3.1 Info and Relationships | A | Semantic HTML5 — `<main>`, `<nav>`, `<header>`, `<section>`, `<aside>` | Angular shell layout | ☐ |
| 1.3.1 Info and Relationships | A | Form fields associated with labels via `for`/`id` or `aria-labelledby` | Angular Material form fields | ☐ |
| 1.3.1 Info and Relationships | A | Data tables have `<th>` with `scope` attributes | `mat-table` with header cells | ☐ |
| 1.3.2 Meaningful Sequence | A | Reading order matches visual order (DOM order = visual order) | Angular Material layout | ☐ |
| 1.3.3 Sensory Characteristics | A | Instructions don't rely solely on color, shape, size, or position | Blood group uses text + color badge | ☐ |
| 1.3.4 Orientation | AA | Content not locked to portrait or landscape | Responsive Angular Material | ☐ |
| 1.3.5 Identify Input Purpose | AA | Autocomplete attributes on identity inputs | `autocomplete="given-name"` etc. | ☐ |

---

#### 1.4 Distinguishable

| Criterion | Level | Requirement | Target | Status |
|---|---|---|---|---|
| 1.4.1 Use of Color | A | Color not sole means of conveying information | Error text + icon + color | ☐ |
| 1.4.2 Audio Control | A | Auto-playing audio can be paused | No auto-play audio | N/A |
| **1.4.3 Contrast (Minimum)** | **AA** | **Normal text ≥ 4.5:1 contrast ratio** | See Section M9-036 | ☐ |
| **1.4.4 Resize Text** | **AA** | **Text resizes to 200% without loss of content/functionality** | CSS relative units (rem/em) | ☐ |
| 1.4.5 Images of Text | AA | No images of text (except logos) | All text is real text | ☐ |
| **1.4.10 Reflow** | **AA** | **Content reflows at 320px without horizontal scrolling** | Responsive Material grid | ☐ |
| **1.4.11 Non-text Contrast** | **AA** | **UI components ≥ 3:1 contrast against adjacent colors** | Material M3 color tokens | ☐ |
| 1.4.12 Text Spacing | AA | No loss of content when letter/word/line spacing increased | CSS `overflow: visible` | ☐ |
| 1.4.13 Content on Hover/Focus | AA | Tooltip content dismissible, hoverable, persistent | Angular Material tooltips | ☐ |

---

### Principle 2 — Operable

#### 2.1 Keyboard Accessible

| Criterion | Level | Requirement | Status |
|---|---|---|---|
| 2.1.1 Keyboard | A | All functionality operable via keyboard | ☐ |
| 2.1.2 No Keyboard Trap | A | Focus never trapped in component | ☐ |
| 2.1.3 Keyboard (No Exception) | AAA | (Aspirational, not required for AA) | — |
| 2.1.4 Character Key Shortcuts | A | No single-character shortcuts (or they can be turned off) | ☐ |

---

#### 2.2 Enough Time

| Criterion | Level | Requirement | Status |
|---|---|---|---|
| 2.2.1 Timing Adjustable | A | Session timeout warns user 1 minute before expiry; extendable | ☐ |
| 2.2.2 Pause, Stop, Hide | A | Moving content (progress bars) can be paused | ☐ |

---

#### 2.3 Seizures and Physical Reactions

| Criterion | Level | Requirement | Status |
|---|---|---|---|
| 2.3.1 Three Flashes | A | No content flashes more than 3 times per second | ☐ |

---

#### 2.4 Navigable

| Criterion | Level | Requirement | Implementation | Status |
|---|---|---|---|---|
| 2.4.1 Bypass Blocks | A | Skip-to-content link at top of page | `<a class="skip-link" href="#main-content">Skip to main content</a>` | ☐ |
| 2.4.2 Page Titled | A | Every page has unique, descriptive `<title>` | Angular `Title` service per route | ☐ |
| 2.4.3 Focus Order | A | Logical focus order throughout all views | Tested with Tab key | ☐ |
| 2.4.4 Link Purpose | A | Link text meaningful out of context | No "click here" links | ☐ |
| 2.4.5 Multiple Ways | AA | Multiple ways to find content (search, nav, sitemap) | Global search + sidebar nav | ☐ |
| 2.4.6 Headings and Labels | AA | Descriptive headings and labels on all forms | `h1`–`h3` hierarchy + form labels | ☐ |
| 2.4.7 Focus Visible | AA | Keyboard focus indicator always visible | Material focus ring enforced | ☐ |
| 2.4.11 Focus Not Obscured | AA | Focused component not entirely hidden by sticky header | `scroll-margin-top` on anchors | ☐ |

---

#### 2.5 Input Modalities

| Criterion | Level | Requirement | Status |
|---|---|---|---|
| 2.5.1 Pointer Gestures | A | All multi-pointer gestures have single-pointer alternative | ☐ |
| 2.5.2 Pointer Cancellation | A | Click-down actions can be cancelled (mouseup triggers action) | ☐ |
| 2.5.3 Label in Name | A | Visible label text contained in accessible name | ☐ |
| 2.5.4 Motion Actuation | A | No functionality requires device motion/shake | ☐ |

---

### Principle 3 — Understandable

#### 3.1 Readable

| Criterion | Level | Requirement | Status |
|---|---|---|---|
| 3.1.1 Language of Page | A | `<html lang="en">` (or appropriate language) | ☐ |
| 3.1.2 Language of Parts | AA | Language of phrases in other languages marked with `lang` attribute | ☐ |

---

#### 3.2 Predictable

| Criterion | Level | Requirement | Status |
|---|---|---|---|
| 3.2.1 On Focus | A | Focus doesn't trigger unexpected context change | ☐ |
| 3.2.2 On Input | A | Form field change doesn't auto-submit | ☐ |
| 3.2.3 Consistent Navigation | AA | Navigation consistent across pages | ☐ |
| 3.2.4 Consistent Identification | AA | Components with same function identified consistently | ☐ |

---

#### 3.3 Input Assistance

| Criterion | Level | Requirement | Implementation | Status |
|---|---|---|---|---|
| 3.3.1 Error Identification | A | Errors identified with text description | Angular Material `mat-error` | ☐ |
| 3.3.2 Labels or Instructions | A | All form fields have visible labels | `mat-label` on all `mat-form-field` | ☐ |
| 3.3.3 Error Suggestion | AA | Error messages suggest corrections | Validation messages: "Enter valid email" | ☐ |
| 3.3.4 Error Prevention | AA | Reversible, checked, or confirmed for legal/financial actions | Confirmation dialogs for critical actions | ☐ |

---

### Principle 4 — Robust

#### 4.1 Compatible

| Criterion | Level | Requirement | Status |
|---|---|---|---|
| 4.1.1 Parsing | A | Valid HTML (Angular renders valid DOM) | ☐ |
| 4.1.2 Name, Role, Value | A | All UI components have accessible name, role, state | ☐ |
| 4.1.3 Status Messages | AA | Status messages programmatically determined without focus | `aria-live="polite"` for notifications | ☐ |

**Status message pattern:**

```html
<!-- ✅ Live region for status updates -->
<div aria-live="polite" aria-atomic="true" class="sr-only" id="status-announcer">
  {{ statusMessage() }}
</div>

<!-- ✅ Error alert -->
<div role="alert" *ngIf="error()">{{ error() }}</div>
```

---

## M9-034: Keyboard Navigation Checklist

### Global Navigation

| Feature | Expected Behavior | Status |
|---|---|---|
| Skip link | Tab from page top → "Skip to main content" link visible and functional | ☐ |
| Sidebar navigation | Tab through nav items, Enter to navigate, arrow keys within nav groups | ☐ |
| Top bar actions | Tab to notification bell, user menu, branch selector | ☐ |
| Breadcrumbs | Tab through breadcrumb links, Enter to navigate | ☐ |

### Data Tables (`mat-table`)

| Feature | Expected Behavior | Status |
|---|---|---|
| Table navigation | Tab into table, arrow keys navigate cells | ☐ |
| Row actions | Tab to action buttons, Enter/Space to activate | ☐ |
| Column sort | Tab to column header, Enter/Space to sort | ☐ |
| Pagination | Tab to paginator, Enter to navigate pages | ☐ |
| Row selection | Space to select row, Shift+Space for range select | ☐ |

### Forms

| Feature | Expected Behavior | Status |
|---|---|---|
| Form fields | Tab order matches visual top-to-bottom, left-to-right order | ☐ |
| Dropdowns (`mat-select`) | Tab to field, Enter/Space to open, arrow keys to navigate options, Enter to select | ☐ |
| Date pickers (`mat-datepicker`) | Tab to field, Enter to open calendar, arrow keys to navigate dates | ☐ |
| Checkboxes | Tab to checkbox, Space to toggle | ☐ |
| Radio groups | Tab to group, arrow keys within group | ☐ |
| Autocomplete | Tab to field, type to filter, arrow keys to navigate suggestions, Enter to select | ☐ |
| File upload | Tab to button, Enter/Space to open file dialog | ☐ |
| Submit button | Tab to submit, Enter to submit | ☐ |
| Cancel/Reset | Tab to cancel, Enter to activate | ☐ |

### Dialogs and Modals

| Feature | Expected Behavior | Status |
|---|---|---|
| Dialog open | Focus moves to dialog on open | ☐ |
| Focus trap | Tab cycles within dialog (does not leave dialog) | ☐ |
| Dialog close | Escape key closes dialog, focus returns to trigger | ☐ |
| Confirmation dialog | Tab between "Confirm" and "Cancel" buttons | ☐ |

### Key Feature Pages (Critical Clinical Workflows)

| Page | Key Interactions | Status |
|---|---|---|
| Donor Registration | Full form navigation, blood group select, consent checkbox | ☐ |
| Blood Collection | Vitals entry, adverse reaction reporting | ☐ |
| Lab Results Entry | Test panel entry, dual sign-off flow | ☐ |
| Inventory Management | Unit search, status update, transfer initiation | ☐ |
| Blood Request | Component type select, clinical notes, urgent toggle | ☐ |
| Crossmatch | Unit selection, compatibility result entry | ☐ |
| Transfusion Administration | Patient verification, bedside check checklist, vitals | ☐ |
| Adverse Reaction Report | Reaction type, severity, free-text notes | ☐ |

---

## M9-035: Screen Reader Compatibility Checklist

### Screen Reader / Browser Combinations Tested

| Screen Reader | Browser | Platform | Status |
|---|---|---|---|
| NVDA 2024.x | Chrome (latest) | Windows 11 | ☐ |
| NVDA 2024.x | Firefox (latest) | Windows 11 | ☐ |
| JAWS 2024 | Chrome (latest) | Windows 11 | ☐ |
| JAWS 2024 | Edge (latest) | Windows 11 | ☐ |
| VoiceOver | Safari (latest) | macOS Sonoma | ☐ |
| VoiceOver | Chrome (latest) | iOS 17 (iPad) | ☐ |
| TalkBack | Chrome | Android 14 | ☐ |

### Screen Reader Functional Tests

| Feature | Expected Announcement | Status |
|---|---|---|
| Page title | "BloodBank — Donor Management" (or current page) | ☐ |
| Navigation landmark | "Navigation" landmark identified | ☐ |
| Main content landmark | "Main" landmark identified | ☐ |
| Table headers | Column headers announced when navigating cells | ☐ |
| Form labels | Every form field announces its label | ☐ |
| Required fields | "Required" announced for mandatory fields | ☐ |
| Validation errors | Error messages announced immediately (aria-live) | ☐ |
| Blood group badge | "A Positive" not just "A+" (screen reader text) | ☐ |
| Status badge (unit) | "Available" / "Quarantine" announced | ☐ |
| Loading state | "Loading..." announced when data is fetching | ☐ |
| Success notification | "Donor saved successfully" announced (aria-live="polite") | ☐ |
| Error notification | "Error: Email already exists" announced (role="alert") | ☐ |
| Dialog open | "Confirm Delete — Dialog" announced on dialog open | ☐ |
| Progress indicator | "Loading, please wait" for spinner | ☐ |
| Pagination | "Page 2 of 15" announced | ☐ |
| Sort state | "Name column, sorted ascending" announced | ☐ |
| Expanded/collapsed | Accordion state announced: "Expanded" / "Collapsed" | ☐ |

### ARIA Landmark Structure

```html
<!-- Required landmark structure in app-shell -->
<a href="#main-content" class="skip-link">Skip to main content</a>

<header role="banner">
  <nav aria-label="Top navigation">...</nav>
</header>

<nav aria-label="Sidebar navigation" role="navigation">
  ...
</nav>

<main id="main-content" role="main">
  <nav aria-label="Breadcrumb" aria-current="page">...</nav>
  <!-- Page content -->
</main>

<footer role="contentinfo">...</footer>
```

---

## M9-036: Color Contrast Requirements

### WCAG 2.1 AA Contrast Ratios

- **Normal text** (< 18pt / < 14pt bold): minimum **4.5:1**
- **Large text** (≥ 18pt / ≥ 14pt bold): minimum **3:1**
- **UI components and graphical objects**: minimum **3:1** against adjacent colors

### BloodBank Color Palette — Contrast Verification

| Element | Foreground | Background | Ratio | Target | Status |
|---|---|---|---|---|---|
| Body text | `#212121` | `#FFFFFF` | 16.1:1 | ≥ 4.5:1 | ☐ |
| Body text (dark mode) | `#E0E0E0` | `#121212` | 11.6:1 | ≥ 4.5:1 | ☐ |
| Primary button text | `#FFFFFF` | `#1565C0` (primary) | 7.0:1 | ≥ 4.5:1 | ☐ |
| Secondary button text | `#1565C0` | `#FFFFFF` | 7.0:1 | ≥ 4.5:1 | ☐ |
| Danger/error text | `#B71C1C` | `#FFFFFF` | 7.5:1 | ≥ 4.5:1 | ☐ |
| Warning text | `#E65100` | `#FFFFFF` | 5.1:1 | ≥ 4.5:1 | ☐ |
| Success text | `#1B5E20` | `#FFFFFF` | 9.2:1 | ≥ 4.5:1 | ☐ |
| Placeholder text | `#757575` | `#FFFFFF` | 4.6:1 | ≥ 4.5:1 | ☐ |
| Disabled text | `#9E9E9E` | `#FFFFFF` | 2.8:1 | Note (1) | ☐ |
| Link text | `#1565C0` | `#FFFFFF` | 7.0:1 | ≥ 4.5:1 | ☐ |
| Table header text | `#FFFFFF` | `#1565C0` | 7.0:1 | ≥ 4.5:1 | ☐ |
| Input border (focused) | `#1565C0` | `#FFFFFF` | 7.0:1 | ≥ 3:1 | ☐ |
| Input border (unfocused) | `#757575` | `#FFFFFF` | 4.6:1 | ≥ 3:1 | ☐ |

> **(1)** Disabled UI components are exempt from contrast requirements under WCAG 2.1 SC 1.4.3.

### Blood Group Badge Contrast

| Blood Group | Text Color | Background | Ratio | Status |
|---|---|---|---|---|
| A+ / A- | `#FFFFFF` | `#C62828` | 5.4:1 | ☐ |
| B+ / B- | `#FFFFFF` | `#1565C0` | 7.0:1 | ☐ |
| AB+ / AB- | `#FFFFFF` | `#6A1B9A` | 6.8:1 | ☐ |
| O+ / O- | `#FFFFFF` | `#2E7D32` | 6.1:1 | ☐ |

### Status Badge Contrast

| Status | Text Color | Background | Ratio | Status |
|---|---|---|---|---|
| AVAILABLE | `#FFFFFF` | `#2E7D32` | 6.1:1 | ☐ |
| QUARANTINE | `#212121` | `#F9A825` | 5.2:1 | ☐ |
| ISSUED | `#FFFFFF` | `#1565C0` | 7.0:1 | ☐ |
| TRANSFUSED | `#FFFFFF` | `#4527A0` | 7.5:1 | ☐ |
| REACTIVE_DISCARD | `#FFFFFF` | `#B71C1C` | 7.5:1 | ☐ |
| EXPIRED | `#FFFFFF` | `#424242` | 9.3:1 | ☐ |

### Focus Indicator Contrast

| Component | Focus Ring Color | Adjacent Color | Ratio | Status |
|---|---|---|---|---|
| Button focus | `#1565C0` | `#FFFFFF` | 7.0:1 | ☐ |
| Input focus | `#1565C0` | `#FFFFFF` | 7.0:1 | ☐ |
| Link focus | `#1565C0` | `#FFFFFF` | 7.0:1 | ☐ |
| Table row focus | `rgba(21,101,192,0.12)` border | `#FFFFFF` | ~3.5:1 | ☐ |

### Automated Contrast Scan

```bash
# Run axe-core contrast scan via Playwright
npx playwright test --grep "accessibility" \
  frontend/bloodbank-ui/e2e/accessibility.spec.ts

# Run axe on every page
npx axe-cli https://bloodbank.example.com/staff/donors \
  --tags wcag2aa --reporter json > /tmp/axe-report.json

# Check for contrast failures
cat /tmp/axe-report.json | jq '.violations[] | select(.id == "color-contrast")'
```

---

## Accessibility Testing Summary

| Test Area | Total Items | Pass | Fail | Untested |
|---|---|---|---|---|
| WCAG 2.1 AA | 38 criteria | — | — | 38 |
| Keyboard Navigation | 35 scenarios | — | — | 35 |
| Screen Reader | 20 scenarios × 7 combos | — | — | 140 |
| Color Contrast | 20 elements | — | — | 20 |
| **Total** | **213 checks** | **—** | **—** | **213** |

---

## Sign-off

| Reviewer | Role | Date | Signature |
|---|---|---|---|
| | Frontend Lead Developer | | |
| | QA Accessibility Tester | | |
| | UX Designer | | |
| | Product Owner | | |

**Validation Result**: ☐ PASS &nbsp;&nbsp; ☐ FAIL &nbsp;&nbsp; ☐ CONDITIONAL PASS

**Notes**:

---

*Reference: Web Content Accessibility Guidelines (WCAG) 2.1 — W3C Recommendation*
*Reference: EN 301 549 v3.2.1 — EU Accessibility Act*
*Reference: ADA Title III — Web Accessibility Requirements*
