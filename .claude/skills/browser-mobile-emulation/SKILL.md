---
name: browser-mobile-emulation
description: Use when the user wants to see or screenshot how a website looks/behaves on a phone or tablet without a physical device. Triggers on "test this on mobile", "show me how this looks on an iPhone", "check the responsive layout".
---

# Mobile Viewport Emulation

Emulates a specific phone/tablet (viewport size, device pixel ratio, user agent, touch support) using Playwright's built-in device profiles.

## Prerequisites

```bash
npm install playwright
npx playwright install chromium
```

## Script

Swap `devices['iPhone 14']` for any other supported device name.

```javascript
const { chromium, devices } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });

  // Use a predefined device profile
  const iPhone = devices['iPhone 14'];
  const context = await browser.newContext({ ...iPhone });
  const page = await context.newPage();

  await page.goto('https://example.com');
  await page.screenshot({ path: 'mobile.png', fullPage: true });

  await browser.close();
})();
```

Available devices: `iPhone 14`, `iPhone 14 Plus`, `Pixel 7`, `Galaxy S9+`, `iPad Pro`, `iPad Mini`, etc. List all of them with:

```bash
node -e "const {devices}=require('playwright'); console.log(Object.keys(devices).join('\n'))"
```

## Notes

- Combine with `browser-screenshot-batch` to capture the same URL list across several device profiles in one run (loop over both `urls` and a `deviceNames` array).
- Device profiles set touch emulation too, so tap-based interactions (not just clicks) will behave correctly for mobile-specific UI like swipe menus.
