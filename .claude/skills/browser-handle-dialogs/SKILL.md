---
name: browser-handle-dialogs
description: Use when a page being automated triggers native browser dialogs (alert/confirm/prompt/beforeunload) that would otherwise block the script. Triggers on "this page has a confirm popup", "handle the alert", "the script hangs on a dialog".
---

# Handling Dialogs (alert / confirm / prompt)

Native browser dialogs block script execution until handled. Attach a `dialog` listener before triggering whatever action causes the dialog, so Playwright can accept/dismiss it automatically instead of hanging.

## Prerequisites

```bash
npm install playwright
npx playwright install chromium
```

## Recipes

```javascript
// Auto-accept all dialogs
page.on('dialog', dialog => dialog.accept());

// Auto-dismiss all dialogs
page.on('dialog', dialog => dialog.dismiss());

// Handle selectively
page.on('dialog', async dialog => {
  console.log('Dialog type:', dialog.type());   // alert | confirm | prompt | beforeunload
  console.log('Message:', dialog.message());
  if (dialog.type() === 'confirm') {
    await dialog.accept();
  } else {
    await dialog.dismiss();
  }
});
```

## Notes

- Attach the listener before the action that triggers the dialog (e.g. before clicking a delete button that shows a `confirm()`), not after — a dialog fired with no listener attached will hang the script until it times out.
- For `prompt()` dialogs needing text input, pass it to `accept`: `dialog.accept('some text')`.
