---
name: browser-login-session
description: Use when the user wants to automate logging into a site and reuse the authenticated session across runs, or perform authenticated actions after login (scraping a dashboard, checking account state). Triggers on "log in and then...", "automate login", "reuse my session".
---

# Login Flow with Session Reuse

Logs into a site, saves the authenticated session to disk (`storageState`), and reuses it on subsequent runs so login only happens once.

## Prerequisites

```bash
npm install playwright
npx playwright install chromium
```

## Script

Adapt the login URL, field selectors/labels, success URL pattern, and the post-login action to the target site.

```javascript
const { chromium } = require('playwright');
const fs = require('fs');

const SESSION_FILE = 'session.json';

(async () => {
  const browser = await chromium.launch({ headless: true });

  // Reuse session if it exists
  const contextOptions = fs.existsSync(SESSION_FILE)
    ? { storageState: SESSION_FILE }
    : {};
  const context = await browser.newContext(contextOptions);
  const page = await context.newPage();

  // Check if already logged in
  await page.goto('https://example.com/dashboard', { waitUntil: 'networkidle' });

  if (page.url().includes('/login')) {
    // Need to log in
    await page.getByLabel('Email').fill('user@example.com');
    await page.getByLabel('Password').fill('password123');
    await page.getByRole('button', { name: 'Sign in' }).click();
    await page.waitForURL('**/dashboard**', { timeout: 10000 });

    // Save session for next run
    await context.storageState({ path: SESSION_FILE });
    console.log('Logged in and session saved');
  }

  // Now do authenticated actions
  const username = await page.locator('.user-display-name').innerText();
  console.log('Logged in as:', username);
  await page.screenshot({ path: 'dashboard.png', fullPage: true });

  await browser.close();
})();
```

## Notes

- Never commit `session.json` or hardcode real credentials in scripts checked into version control — read credentials from environment variables instead (`process.env.LOGIN_EMAIL`, etc.) and gitignore the session file.
- `page.url().includes('/login')` is a simple "am I logged in" check; some sites need a more specific selector check instead (e.g. presence of a logout button).
- Only automate logins for accounts the user owns or has explicit authorization to test.
