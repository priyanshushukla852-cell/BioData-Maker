---
name: browser-pagination-scroll
description: Use when the user wants to scrape data spread across multiple pages via a "Next" button, or content loaded via infinite scroll. Triggers on "scrape all pages", "go through pagination", "scroll and collect everything", "infinite scroll".
---

# Pagination & Infinite Scroll

Two patterns for collecting data spread across more content than a single page load: click-through pagination, and infinite-scroll-triggered lazy loading.

## Prerequisites

```bash
npm install playwright
npx playwright install chromium
```

## Click-through pagination (Next button)

Adapt the item selector, field extraction, and the "next page" locator to the target site.

```javascript
const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  await page.goto('https://example.com/listings');

  const allItems = [];
  let pageNum = 1;

  while (true) {
    console.log(`Scraping page ${pageNum}...`);

    // Extract items on current page
    const items = await page.locator('.listing-card').evaluateAll(cards =>
      cards.map(c => ({
        title: c.querySelector('.title')?.textContent?.trim(),
        price: c.querySelector('.price')?.textContent?.trim(),
      }))
    );
    allItems.push(...items);

    // Check for next page button
    const nextBtn = page.locator('a[aria-label="Next page"]');
    const hasNext = await nextBtn.count() > 0 && await nextBtn.isEnabled();
    if (!hasNext) break;

    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle' }),
      nextBtn.click(),
    ]);
    pageNum++;
  }

  console.log(`Total items: ${allItems.length}`);
  require('fs').writeFileSync('results.json', JSON.stringify(allItems, null, 2));
  await browser.close();
})();
```

## Infinite scroll

Scrolls to the bottom repeatedly until page height stops growing (no more lazy-loaded content), then extracts everything at once.

```javascript
async function scrollToBottom(page, maxScrolls = 20) {
  let scrollCount = 0;
  let previousHeight = 0;

  while (scrollCount < maxScrolls) {
    const currentHeight = await page.evaluate(() => document.body.scrollHeight);
    if (currentHeight === previousHeight) break; // No more content loaded

    await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
    await page.waitForTimeout(1500); // Wait for lazy-load
    previousHeight = currentHeight;
    scrollCount++;
    console.log(`Scroll ${scrollCount}, height: ${currentHeight}`);
  }
}

// Usage
await page.goto('https://example.com/feed');
await scrollToBottom(page);
const allPosts = await page.locator('.post').allInnerTexts();
```

## Notes

- Raise `maxScrolls` for very long feeds; the height-comparison loop already stops early once nothing new loads.
- If a site uses both (paginated infinite scroll, e.g. "load more" button instead of true infinite scroll), swap `window.scrollTo` for a click on the load-more button and otherwise reuse the same height-check loop.
