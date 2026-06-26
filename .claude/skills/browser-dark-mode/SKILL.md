---
name: browser-dark-mode
description: Use when the user wants to test or screenshot a page under dark mode, reduced-motion, or forced-colors (high contrast) preferences. Triggers on "check this in dark mode", "test reduced motion", "test high contrast mode".
---

# Dark Mode / Reduced Motion / Forced Colors Emulation

Emulates OS-level user preferences (`prefers-color-scheme`, `prefers-reduced-motion`, `forced-colors`) so a page's CSS media-query-driven behavior can be tested without changing actual OS settings.

## Prerequisites

```bash
npm install playwright
npx playwright install chromium
```

## Script

```javascript
const context = await browser.newContext({
  colorScheme: 'dark',          // 'light' | 'dark'
  reducedMotion: 'reduce',      // 'reduce' | 'no-preference'
  forcedColors: 'none',
});
const page = await context.newPage();
await page.goto('https://example.com');
await page.screenshot({ path: 'dark-mode.png', fullPage: true });
```

## Notes

- These options only affect pages that actually respond to the corresponding CSS media query (`prefers-color-scheme`, `prefers-reduced-motion`, `forced-colors`) — a page with a JS-driven theme toggle stored in localStorage/cookies instead needs that toggle clicked or that storage value set, not this emulation.
- Combine with `browser-screenshot-batch` to capture a URL list in both light and dark mode for comparison.
