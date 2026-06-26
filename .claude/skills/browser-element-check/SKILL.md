---
name: browser-element-check
description: Use when the user wants to check whether an element exists, is visible, count matching elements, or wait for something to disappear — typically for assertions or as a gate before further automation steps. Triggers on "check if this element exists", "wait until the spinner disappears", "count the results on this page".
---

# Element Visibility / Existence Check

Quick recipes for the most common Playwright element-state checks, used as assertions or as gates before continuing an automation script.

## Prerequisites

```bash
npm install playwright
npx playwright install chromium
```

## Recipes

```javascript
// Check if element exists (even if hidden)
const exists = await page.locator('.feature-flag-banner').count() > 0;

// Check if visible
const visible = await page.locator('.cookie-banner').isVisible();

// Get count of matching elements
const count = await page.locator('.search-result').count();
console.log(`Found ${count} results`);

// Wait until an element disappears
await page.locator('.loading-spinner').waitFor({ state: 'detached', timeout: 15000 });
```

## Notes

- `count() > 0` checks presence in the DOM regardless of CSS visibility; `isVisible()` checks both DOM presence and actual visibility (not `display: none`, not zero-size, not off-screen).
- `waitFor({ state: 'detached' })` waits for full removal from the DOM; use `state: 'hidden'` instead if the element stays in the DOM but becomes invisible (e.g. opacity/visibility toggle rather than unmount).
- These are the right primitives to gate subsequent steps in any of the other browser skills — e.g. wait for a spinner to detach before screenshotting, or check a result count before looping through pagination.
