---
name: browser-screenshot-batch
description: Use when the user wants to screenshot a list of URLs (e.g. a site's pages, a set of competitor pages, multiple template previews) and save each to a named PNG file. Triggers on "screenshot these URLs", "capture all these pages", "batch screenshot".
---

# Batch Screenshot URLs

Screenshots a list of URLs with Playwright and saves each to a named PNG. Reuses one browser context across all pages for speed; continues past individual failures instead of aborting the whole batch.

## Prerequisites

```bash
npm install playwright
npx playwright install chromium
```

## Script

Adapt the `urls` array to the user's actual list (name + url pairs), then run with `node script.js`.

```javascript
const { chromium } = require('playwright');

const urls = [
  { name: 'home', url: 'https://example.com' },
  { name: 'about', url: 'https://example.com/about' },
  { name: 'pricing', url: 'https://example.com/pricing' },
];

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });

  for (const { name, url } of urls) {
    const page = await context.newPage();
    try {
      await page.goto(url, { waitUntil: 'networkidle', timeout: 15000 });
      await page.screenshot({ path: `${name}.png`, fullPage: true });
      console.log(`✓ ${name}`);
    } catch (e) {
      console.error(`✗ ${name}: ${e.message}`);
    } finally {
      await page.close();
    }
  }

  await browser.close();
})();
```

## Notes

- `fullPage: true` captures the entire scrollable page, not just the viewport — drop it if the user wants above-the-fold only.
- Failures on one URL don't stop the batch; the `try/catch` logs and moves on.
- For mobile screenshots, combine with the `browser-mobile-emulation` skill's device context options.
