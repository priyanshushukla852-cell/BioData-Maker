---
name: browser-intercept-api
description: Use when the user wants the raw JSON data a page loads via XHR/fetch, rather than scraping the rendered HTML. Triggers on "get the API data behind this page", "intercept the network requests", "capture the JSON this page loads".
---

# Intercept & Capture API Responses

Attaches a response listener before navigation and captures every JSON API response the page makes while loading — useful when scraping rendered HTML would be fragile but the underlying data is cleaner JSON.

## Prerequisites

```bash
npm install playwright
npx playwright install chromium
```

## Script

Adapt the URL substring filter (`/api/`) to match the target site's actual API path pattern.

```javascript
const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();

  // Attach listener BEFORE navigation
  const captured = [];
  page.on('response', async resp => {
    if (resp.url().includes('/api/') && resp.headers()['content-type']?.includes('json')) {
      try {
        const json = await resp.json();
        captured.push({ url: resp.url(), data: json });
      } catch {}
    }
  });

  await page.goto('https://example.com/products', { waitUntil: 'networkidle' });

  console.log(`Captured ${captured.length} API responses`);
  require('fs').writeFileSync('api-data.json', JSON.stringify(captured, null, 2));

  await browser.close();
})();
```

## Notes

- The listener must be attached before `page.goto` — responses that fire before the listener exists are missed.
- If the data loads on scroll or interaction rather than initial page load, trigger that interaction (scroll, click) before reading `captured`, and keep the listener attached throughout.
- Check the target site's terms of service before relying on undocumented internal APIs for anything beyond ad-hoc, personal-use data retrieval.
