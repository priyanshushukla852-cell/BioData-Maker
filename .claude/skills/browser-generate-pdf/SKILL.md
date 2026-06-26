---
name: browser-generate-pdf
description: Use when the user wants to turn a live webpage into a downloadable PDF (a report page, an invoice, an article). Triggers on "convert this page to PDF", "save this webpage as a PDF", "generate a PDF from this URL".
---

# Generate PDF from Webpage

Renders a webpage to a paginated PDF via Chromium's print pipeline. Chromium-only — Firefox and WebKit don't support `page.pdf()`.

## Prerequisites

```bash
npm install playwright
npx playwright install chromium
```

Must launch in headless Chromium (`chromium.launch({ headless: true })`); PDF generation is unavailable in headed mode on some platforms and is not supported on Firefox/WebKit at all.

## Script

```javascript
await page.goto('https://example.com/report', { waitUntil: 'networkidle' });
await page.pdf({
  path: 'report.pdf',
  format: 'A4',
  printBackground: true,
  margin: { top: '20mm', bottom: '20mm', left: '15mm', right: '15mm' },
});
```

## Notes

- `printBackground: true` is required if the page relies on background colors/images for its layout — without it they're omitted, matching default browser print behavior.
- For content the page hides via `@media print` CSS, check that the printed result actually matches what's expected; some sites strip navigation/ads on print intentionally, which may or may not be what the user wants captured.
- Use `format` (`'A4'`, `'Letter'`, etc.) or explicit `width`/`height`, not both.
