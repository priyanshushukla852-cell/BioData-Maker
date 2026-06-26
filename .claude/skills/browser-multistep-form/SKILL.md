---
name: browser-multistep-form
description: Use when the user wants to automate filling out a wizard-style form that spans multiple screens/steps (signup flow, application form, checkout). Triggers on "fill out this multi-step form", "automate this signup wizard", "submit this form for me".
---

# Multi-Step Form Automation

Fills a wizard-style form across multiple screens, waiting for each step's container to appear before proceeding to the next, then confirms submission succeeded.

## Prerequisites

```bash
npm install playwright
npx playwright install chromium
```

## Script

Adapt the field labels/selectors, step-detection selector (`[data-step="N"]` here — swap for whatever the target site actually uses), and field values to the user's form and data.

```javascript
const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  await page.goto('https://example.com/signup');

  // Step 1 — Personal info
  await page.getByLabel('First name').fill('Priyanshu');
  await page.getByLabel('Last name').fill('Sharma');
  await page.getByLabel('Email').fill('priyanshu@example.com');
  await page.getByRole('button', { name: 'Continue' }).click();
  await page.waitForSelector('[data-step="2"]');

  // Step 2 — Account details
  await page.getByLabel('Username').fill('priyanshu_dev');
  await page.getByLabel('Password').fill('Secure@123');
  await page.getByLabel('Confirm password').fill('Secure@123');
  await page.getByRole('button', { name: 'Continue' }).click();
  await page.waitForSelector('[data-step="3"]');

  // Step 3 — Preferences
  await page.selectOption('select#country', { label: 'India' });
  await page.check('input[name="newsletter"]');
  await page.getByRole('button', { name: 'Submit' }).click();

  // Wait for confirmation
  await page.waitForSelector('.success-message', { timeout: 10000 });
  const msg = await page.locator('.success-message').innerText();
  console.log('Done:', msg);
  await page.screenshot({ path: 'confirmation.png' });

  await browser.close();
})();
```

## Notes

- Prefer `getByLabel`/`getByRole` over CSS selectors when the form has proper accessibility labels — they're more resilient to markup changes.
- If a step transition is animated rather than a hard navigation, `waitForSelector` on the next step's unique element is more reliable than a fixed `waitForTimeout`.
- Never hardcode real passwords or PII for accounts that aren't disposable test accounts; read sensitive values from environment variables when automating a real signup.
