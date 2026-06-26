# CLAUDE.md — BioData AI

Project rules derived from [biodata_app_prd_erd.md](biodata_app_prd_erd.md). This file governs how code should be written once implementation starts. Keep it in sync if the PRD changes.

## What this is

Android app that turns structured form input into an AI-polished marriage biodata, exported as PDF. Not a matchmaking platform — no browsing other users' profiles, ever.

## Tech stack (do not substitute without asking)

- **Android client:** Kotlin + Jetpack Compose, Room (local draft storage), Firebase Auth (Google + Phone OTP), Firebase Analytics + Crashlytics
- **Backend:** Spring Boot (Java), hosted on Railway/Render
- **AI:** Google Gemini API (Gemini 2.5 Flash), called **only from the backend**
- **Storage:** Firebase Storage for photos, user-scoped access rules
- **PDF:** Android `PdfDocument` API or server-side iText
- **Ads:** Google AdMob

## Hard architecture rules

1. **Never call the Gemini API (or any AI provider) directly from the Android client.** All AI calls go through the backend (`/api/ai/summary`, `/api/ai/suggest`). API keys live server-side only.
2. **No plain-text PII in logs**, on either client or backend. This includes name, phone, email, address, DOB.
3. Internet is required only for auth and AI calls. Form drafts must work fully offline via Room, syncing to backend on save.
4. Photos: max 5MB upload, client-side compression before upload, stored in Firebase Storage under user-scoped paths.
5. No NSFW/content moderation in v1 — don't add it speculatively.

## Scope discipline

- Build **MVP (v1.0) features only** (F1–F10 in the PRD) unless explicitly told to start v1.1 or v2 work. Don't build multi-biodata management, kundali section, shareable links, WhatsApp image export, or premium templates unless asked.
- Templates are static design assets (in-house or CDN-fetched), not user-customizable. Don't build a template editor.
- Income field is optional input and must stay excludable per-template — never hardcode it into every template's output.
- AI summary generation must never mention income, caste, or Manglik status, per the PRD's prompt spec (Section 7.1).

## Data model

Source of truth is the ERD in [biodata_app_prd_erd.md §10](biodata_app_prd_erd.md#10-erd--entity-relationship-diagram). Key entities: `USERS`, `BIODATAS` (1:N from user), and per-biodata 1:1 children `PERSONAL_DETAILS`, `FAMILY_DETAILS`, `EDUCATION_CAREER`, `LIFESTYLE`, `CONTACT_INFO`, plus optional 1:0..1 `ASTROLOGY`, 1:N `BIODATA_PHOTOS` / `AI_GENERATIONS` / `PDF_EXPORTS`, and N:1 `TEMPLATES`. When adding a field, find its correct owning entity in the ERD rather than bolting it onto `BIODATAS` directly.

## API conventions

Follow the contracts in [§11](biodata_app_prd_erd.md#11-api-contracts-key-endpoints) as the baseline shape (`/api/auth/verify-otp`, `/api/biodatas` CRUD, `/api/ai/summary`, `/api/ai/suggest`, `/api/biodatas/{id}/export-pdf`, `/api/templates`). Deletes are soft deletes, not hard deletes.

## UX rules that constrain implementation

- No ads on form input screens (Steps 1–7) or photo upload screens.
- Max 1 interstitial ad per session, shown at PDF export.
- Hindi and English are both first-class — don't ship a feature that only renders in English. Devanagari font rendering must be verified for any new template or PDF output path.
- AI field suggestions must return in under 3 seconds; if a feature can't realistically hit that, flag it rather than silently shipping slower.

## Open questions to flag, not silently resolve

If work touches these, surface it to the user instead of assuming an answer — see [§14](biodata_app_prd_erd.md#14-open-questions):
- Whether AI summary generation should be rate-limited per month (cost control).
- Minimum Android API level (26 vs 24).
- Whether a "partner preferences" section or joint family biodata format is in scope.
- Ask question whenever in doubt, don't assume anything