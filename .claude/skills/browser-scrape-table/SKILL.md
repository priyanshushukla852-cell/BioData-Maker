---
name: browser-scrape-table
description: Use when the user wants to extract all rows from a JS-rendered HTML data table into structured JSON. Triggers on "scrape this table", "extract the data from this table", "get all rows from this page".
---

# Scrape a Dynamic Table

Extracts headers and all rows from a JavaScript-rendered `<table>` into an array of objects keyed by column header, and writes the result to JSON.

## Prerequisites

```bash
npm install playwright
npx playwright install chromium
```

## Script

Adapt the URL and table selector if the page has multiple tables or a non-standard structure.

```javascript
const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  await page.goto('https://example.com/data-table', { waitUntil: 'networkidle' });

  // Wait for table to render
  await page.waitForSelector('table tbody tr');

  // Get headers
  const headers = await page.locator('table thead th').allInnerTexts();

  // Get all rows
  const rows = await page.locator('table tbody tr').evaluateAll(rows =>
    rows.map(row =>
      Array.from(row.querySelectorAll('td')).map(td => td.textContent?.trim())
    )
  );

  // Map to objects
  const data = rows.map(row =>
    Object.fromEntries(headers.map((h, i) => [h, row[i]]))
  );

  console.log(JSON.stringify(data, null, 2));
  require('fs').writeFileSync('table-data.json', JSON.stringify(data, null, 2));

  await browser.close();
})();
```

## Notes

- If the table paginates rather than loading all rows at once, combine this with the `browser-pagination-scroll` skill — extract on each page and concatenate.
- For multiple tables on one page, scope the locator with a more specific selector (e.g. `'#results-table tbody tr'`) instead of the bare `table` tag.
