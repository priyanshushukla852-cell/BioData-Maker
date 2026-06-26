# BioData AI — Product Requirements Document (PRD) & Entity Relationship Document (ERD)

> **App Concept:** An Android app that helps users (candidates or parents) create polished, AI-enhanced marriage biodata in minutes — with multiple templates, bilingual output, and PDF export.

---

## Table of Contents
1. [Product Overview](#1-product-overview)
2. [Goals & Success Metrics](#2-goals--success-metrics)
3. [User Personas](#3-user-personas)
4. [Feature List & Prioritization](#4-feature-list--prioritization)
5. [User Flows](#5-user-flows)
6. [Screen Inventory](#6-screen-inventory)
7. [AI Feature Spec](#7-ai-feature-spec)
8. [Non-Functional Requirements](#8-non-functional-requirements)
9. [Tech Stack Recommendation](#9-tech-stack-recommendation)
10. [ERD — Entity Relationship Diagram](#10-erd--entity-relationship-diagram)
11. [API Contracts (Key Endpoints)](#11-api-contracts-key-endpoints)
12. [Monetization Strategy](#12-monetization-strategy)
13. [Constraints & Assumptions](#13-constraints--assumptions)
14. [Open Questions](#14-open-questions)

---

## 1. Product Overview

**App Name (working):** BioData AI  
**Platform:** Android (API 26+, i.e. Android 8.0 Oreo and above)  
**Primary Output:** Downloadable PDF biodata  
**Languages Supported:** Hindi & English (user-selectable per biodata)  
**Authentication:** Google Sign-In or Phone OTP  
**Monetization:** Free with Google AdMob banner/interstitial ads  

### Value Proposition
Traditionally, Indian families spend hours manually creating biodata in Word or on paper. BioData AI reduces that to under 5 minutes by:
- Guiding users through structured input forms
- Using AI to auto-write a polished personal summary paragraph
- Suggesting completions for vague or missing fields
- Rendering the data into one of several curated templates
- Exporting as a shareable PDF

---

## 2. Goals & Success Metrics

| Goal | Metric | Target (6 months) |
|---|---|---|
| Adoption | App installs | 50,000 |
| Engagement | % users who complete & export a biodata | ≥ 60% |
| Retention | DAU/MAU ratio | ≥ 20% |
| Ad Revenue | eCPM on interstitial ads | ≥ ₹60 |
| Quality | Avg. Play Store rating | ≥ 4.2 ★ |
| Trust | % users who log in vs. skip | ≥ 70% |

---

## 3. User Personas

### Persona A — Priya, 26, Software Engineer, Pune
- **Scenario:** Her parents are looking for a match. She wants to create her own biodata quickly without the old Word doc format.
- **Goals:** Professional-looking output, English language, modern template.
- **Pain points:** Traditional biodata looks outdated; doesn't want to involve a relative to design it.

### Persona B — Ramesh Ji, 54, Retired Teacher, Jaipur
- **Scenario:** Setting up biodata for his son. Not tech-savvy; needs a simple interface.
- **Goals:** Hindi language support, traditional template, easy sharing via WhatsApp or printing.
- **Pain points:** Confused by complex apps; wants a guided, step-by-step experience.

---

## 4. Feature List & Prioritization

### MVP (v1.0)
| # | Feature | Priority |
|---|---|---|
| F1 | Phone OTP + Google Sign-In auth | P0 |
| F2 | Multi-step input form (7 sections) | P0 |
| F3 | Photo upload (profile + family) | P0 |
| F4 | AI personal summary generation | P0 |
| F5 | AI field completion suggestions | P0 |
| F6 | 4–6 curated templates | P0 |
| F7 | Hindi / English language toggle | P0 |
| F8 | PDF export & download | P0 |
| F9 | Save & edit biodata (linked to account) | P0 |
| F10 | AdMob banner ads on non-sensitive screens | P0 |

### v1.1
| # | Feature | Priority |
|---|---|---|
| F11 | Multiple saved biodatas per account | P1 |
| F12 | Biodata preview before export | P1 |
| F13 | Interstitial ad on PDF export | P1 |
| F14 | Duplicate & edit existing biodata | P1 |
| F15 | Kundali/horoscope section (optional) | P1 |

### Future (v2.0)
| # | Feature | Priority |
|---|---|---|
| F16 | Shareable link (hosted HTML biodata) | P2 |
| F17 | WhatsApp-ready image export | P2 |
| F18 | Family biodata (joint format) | P2 |
| F19 | Regional language support (Gujarati, Marathi) | P2 |
| F20 | Premium templates (paid upgrade) | P2 |

---

## 5. User Flows

### 5.1 New User Onboarding
```
App Launch
    │
    ▼
Splash + Language Select (Hindi / English)
    │
    ▼
Auth Screen
    ├── Google Sign-In ──────────────────┐
    └── Phone OTP (enter → verify) ──────┤
                                         │
                                         ▼
                                    Home Screen
                                    (no saved biodatas)
                                         │
                                         ▼
                                    "Create Your Biodata" CTA
```

### 5.2 Biodata Creation Flow
```
Create Biodata
    │
    ▼
[Step 1] Personal Details
    Name, DOB, Religion, Caste, Gotra, Height, Complexion, Disability (optional)
    │
    ▼
[Step 2] Family Details
    Father's name/occupation, Mother's name/occupation,
    No. of siblings, Family type (nuclear/joint), Family values
    │
    ▼
[Step 3] Education & Career
    Highest qualification, College/University, Job title,
    Company name, Annual income (optional), Work location
    │
    ▼
[Step 4] Lifestyle & Preferences
    Diet (veg/non-veg), Drinking/Smoking (N/Y/occasionally),
    Hobbies, Languages spoken
    │
    ▼
[Step 5] Astrology (optional)
    Rashi, Nakshatra, Manglik status, Time of birth, Place of birth
    │
    ▼
[Step 6] Contact Details
    Phone, Email, Address (city/state)
    │
    ▼
[Step 7] Photos
    Profile photo (required) + optional additional photos
    │
    ▼
AI Processing Screen
    "Generating your personal summary..."
    "Suggesting completions..."
    │
    ▼
AI Summary Review
    User can edit the AI-generated paragraph
    │
    ▼
Template Selection
    User browses 4–6 template thumbnails, picks one
    │
    ▼
Live Preview (scrollable PDF preview)
    │
    ├── "Edit" → back to form
    └── "Export PDF" → AdMob interstitial → PDF saved to device
```

---

## 6. Screen Inventory

| Screen | Key Components |
|---|---|
| Splash | Logo, language toggle |
| Auth | Google button, Phone OTP flow, T&C link |
| Home | Saved biodatas list, Create button, AdMob banner |
| Create — Step 1–7 | Progress bar, form fields, Next/Back, AI suggestion chips |
| AI Processing | Lottie animation, progress message |
| AI Summary Review | Editable text card, Regenerate button |
| Template Picker | Horizontal scroll of thumbnail cards, language badge |
| Biodata Preview | Zoomable PDF-like preview, Edit / Export buttons |
| Export Success | Download path, Share button, AdMob interstitial |
| Profile / Settings | Account info, language preference, saved biodatas |

---

## 7. AI Feature Spec

### 7.1 Personal Summary Generation
- **Trigger:** After Step 7 (all inputs collected)
- **Input to AI:** All form fields (structured JSON)
- **Output:** A 4–6 line paragraph in the selected language
- **Tone:** Warm, formal, suitable for Indian arranged marriage context
- **Sample prompt structure:**
  ```
  You are helping create a marriage biodata for [Name].
  Here are their details: [JSON].
  Write a warm, respectful personal introduction paragraph in [Hindi/English]
  suitable for an Indian arranged marriage biodata. Keep it 4–6 sentences.
  Mention their family background, education, career, and personality briefly.
  Do NOT include income, caste, or Manglik status in the summary.
  ```

### 7.2 AI Field Suggestion
- **Trigger:** User leaves a field blank and taps "Next" OR taps a ✨ chip
- **Input:** Partial profile data + field context
- **Output:** 2–3 short suggestions shown as chips (tapable to autofill)
- **Example:** Hobbies field is empty → AI suggests: "Reading, Travelling, Cooking" based on profession/education
- **Latency target:** < 3 seconds per suggestion call

### 7.3 AI Provider
- Use **Google Gemini API (Gemini 2.5 Flash)** via backend proxy
- Never call AI APIs directly from the Android client (hide API keys server-side)
- Backend: thin REST endpoint `/api/ai/summary` and `/api/ai/suggest`

---

## 8. Non-Functional Requirements

| Category | Requirement |
|---|---|
| Performance | App cold start < 2.5s; PDF generation < 5s |
| Offline | Form data saved locally (Room DB); requires internet only for AI calls and auth |
| Security | No plain-text PII in logs; photos stored in Firebase Storage with user-scoped access rules |
| Privacy | No biodata data shared with third parties; GDPR + IT Act 2000 compliance |
| Accessibility | Font size scaling; talkback support on major screens |
| Ads | No ads on form input screens or photo upload screens (avoid intrusion during sensitive input) |
| Photo | Max upload size 5MB per photo; auto-compressed on client before upload |

---

## 9. Tech Stack Recommendation

| Layer | Technology | Reason |
|---|---|---|
| Android | Kotlin + Jetpack Compose | Modern, declarative UI |
| Local DB | Room (SQLite) | Offline draft saving |
| Auth | Firebase Auth (Google + Phone OTP) | Handles OTP infra cheaply |
| Cloud Storage | Firebase Storage | Photo uploads; user-scoped |
| Backend API | Spring Boot (Java) on Railway/Render | Your existing expertise |
| AI Proxy | Spring Boot → Google Gemini API | Keeps API keys server-side |
| PDF Generation | Android PdfDocument API or iText (server-side) | Template rendering |
| Ads | Google AdMob | Standard for free Android apps |
| Analytics | Firebase Analytics | Usage funnels |
| Crash Reporting | Firebase Crashlytics | |

---

## 10. ERD — Entity Relationship Diagram

### Entities & Attributes

```
┌──────────────────────────────────────┐
│               USERS                  │
├──────────────────────────────────────┤
│ PK  user_id         UUID             │
│     auth_provider   ENUM(google,otp) │
│     firebase_uid    VARCHAR(128)      │
│     phone           VARCHAR(15) NULL  │
│     email           VARCHAR(255) NULL │
│     display_name    VARCHAR(100)      │
│     language_pref   ENUM(hi, en)      │
│     created_at      TIMESTAMP         │
│     updated_at      TIMESTAMP         │
└────────────────┬─────────────────────┘
                 │ 1
                 │
                 │ N
┌────────────────▼─────────────────────┐
│             BIODATAS                 │
├──────────────────────────────────────┤
│ PK  biodata_id      UUID             │
│ FK  user_id         UUID → USERS     │
│ FK  template_id     UUID → TEMPLATES │
│     title           VARCHAR(100)      │  ← user-given label e.g. "My Biodata"
│     language        ENUM(hi, en)      │
│     status          ENUM(draft,done)  │
│     ai_summary      TEXT              │  ← AI-generated paragraph (editable)
│     created_at      TIMESTAMP         │
│     updated_at      TIMESTAMP         │
└──┬───────┬──────┬────────────────────┘
   │       │      │
   │ 1     │ 1    │ 1
   │       │      │
   │N      │N     │N
┌──▼───┐ ┌─▼────┐ ┌▼──────────────────┐
│PERS. │ │FAMILY│ │  BIODATA_PHOTOS    │
│DETAIL│ │DETAIL│ ├───────────────────┤
├──────┤ ├──────┤ │PK photo_id   UUID │
│PK id │ │PK id │ │FK biodata_id UUID │
│FK    │ │FK    │ │   photo_type       │
│biod..│ │biod..│ │   ENUM(profile,   │
│      │ │      │ │   family,extra)    │
│name  │ │father│ │   storage_url TEXT│
│dob   │ │_name │ │   sort_order  INT │
│gender│ │father│ │   uploaded_at      │
│relig.│ │_occ  │ └───────────────────┘
│caste │ │mother│
│gotra │ │_name │
│height│ │mother│
│cmplx.│ │_occ  │
│disab.│ │siblgs│
└──────┘ │fam_  │
         │type  │
         │fam_  │
         │values│
         └──────┘

┌──────────────────────────────────────┐
│          EDUCATION_CAREER            │
├──────────────────────────────────────┤
│ PK  id              UUID             │
│ FK  biodata_id      UUID → BIODATAS  │
│     highest_qual    VARCHAR(100)      │
│     college         VARCHAR(200)      │
│     job_title       VARCHAR(100) NULL │
│     company         VARCHAR(200) NULL │
│     annual_income   VARCHAR(50) NULL  │  ← stored as string (e.g. "10-15 LPA")
│     work_location   VARCHAR(100) NULL │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│             LIFESTYLE                │
├──────────────────────────────────────┤
│ PK  id              UUID             │
│ FK  biodata_id      UUID → BIODATAS  │
│     diet            ENUM(veg,nonveg) │
│     drinking        ENUM(no,yes,occ) │
│     smoking         ENUM(no,yes,occ) │
│     hobbies         TEXT             │  ← comma-separated or JSON array
│     languages_spoken TEXT            │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│             ASTROLOGY                │
├──────────────────────────────────────┤
│ PK  id              UUID             │
│ FK  biodata_id      UUID → BIODATAS  │
│     rashi           VARCHAR(50) NULL  │
│     nakshatra       VARCHAR(50) NULL  │
│     manglik         ENUM(yes,no,      │
│                     partial) NULL    │
│     birth_time      TIME NULL         │
│     birth_place     VARCHAR(100) NULL │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│            CONTACT_INFO              │
├──────────────────────────────────────┤
│ PK  id              UUID             │
│ FK  biodata_id      UUID → BIODATAS  │
│     contact_phone   VARCHAR(15)       │
│     contact_email   VARCHAR(255) NULL │
│     city            VARCHAR(100)      │
│     state           VARCHAR(100)      │
│     country         VARCHAR(50)       │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│             TEMPLATES                │
├──────────────────────────────────────┤
│ PK  template_id     UUID             │
│     name            VARCHAR(100)      │
│     preview_url     TEXT              │  ← thumbnail image URL
│     style           ENUM(traditional,│
│                     modern,floral,   │
│                     minimal,royal,   │
│                     festive)         │
│     is_premium      BOOLEAN (false)   │
│     supports_hindi  BOOLEAN           │
│     sort_order      INT               │
│     created_at      TIMESTAMP         │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│           AI_GENERATIONS             │
├──────────────────────────────────────┤
│ PK  id              UUID             │
│ FK  biodata_id      UUID → BIODATAS  │
│     generation_type ENUM(summary,    │
│                     field_suggest)   │
│     input_snapshot  JSONB            │  ← snapshot of inputs sent to AI
│     ai_response     TEXT             │
│     field_name      VARCHAR(50) NULL  │  ← which field was being suggested
│     accepted        BOOLEAN          │  ← did user accept this output?
│     generated_at    TIMESTAMP         │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│            PDF_EXPORTS               │
├──────────────────────────────────────┤
│ PK  id              UUID             │
│ FK  biodata_id      UUID → BIODATAS  │
│     storage_url     TEXT             │  ← Firebase Storage path
│     language        ENUM(hi, en)      │
│     template_id     UUID             │
│     exported_at     TIMESTAMP         │
└──────────────────────────────────────┘
```

### Relationships Summary

| Relationship | Cardinality |
|---|---|
| USER → BIODATAS | 1 : N (one user can have multiple saved biodatas) |
| BIODATA → PERSONAL_DETAILS | 1 : 1 |
| BIODATA → FAMILY_DETAILS | 1 : 1 |
| BIODATA → EDUCATION_CAREER | 1 : 1 |
| BIODATA → LIFESTYLE | 1 : 1 |
| BIODATA → ASTROLOGY | 1 : 0..1 (optional section) |
| BIODATA → CONTACT_INFO | 1 : 1 |
| BIODATA → BIODATA_PHOTOS | 1 : N (profile + optional extras) |
| BIODATA → TEMPLATES | N : 1 (many biodatas can use the same template) |
| BIODATA → AI_GENERATIONS | 1 : N (multiple AI calls per biodata lifecycle) |
| BIODATA → PDF_EXPORTS | 1 : N (user can re-export with different template/language) |

---

## 11. API Contracts (Key Endpoints)

### Auth
```
POST /api/auth/verify-otp
  Body: { phone, otp }
  Response: { jwt_token, user_id, is_new_user }
```

### Biodata CRUD
```
POST   /api/biodatas              → create draft
GET    /api/biodatas              → list user's biodatas
GET    /api/biodatas/{id}         → get full biodata
PUT    /api/biodatas/{id}         → update any section
DELETE /api/biodatas/{id}         → soft delete
```

### AI Endpoints
```
POST /api/ai/summary
  Body: { biodata_id, language }
  Response: { summary_text, generation_id }

POST /api/ai/suggest
  Body: { biodata_id, field_name, current_value }
  Response: { suggestions: [string, string, string] }
```

### PDF Export
```
POST /api/biodatas/{id}/export-pdf
  Body: { template_id, language }
  Response: { pdf_url, export_id }
  Note: triggers interstitial ad gate on client
```

### Templates
```
GET /api/templates
  Response: [{ template_id, name, preview_url, style, supports_hindi }]
```

---

## 12. Monetization Strategy

| Ad Placement | Type | Trigger |
|---|---|---|
| Home screen (bottom) | Banner | Always visible on home |
| Template picker screen (bottom) | Banner | While browsing templates |
| PDF Export | Interstitial (fullscreen) | Shown once before download begins |
| After AI summary generation | Native/banner | Below the AI-generated paragraph card |

**Rules:**
- Never show ads on form input screens (Steps 1–7) — users are in flow
- Never show ads on photo upload screens
- Frequency cap: max 1 interstitial per session until export action
- Use AdMob test IDs during development

---

## 13. Constraints & Assumptions

- **No matchmaking:** This app only creates biodata; it is NOT a matrimonial platform (no browsing of others' profiles)
- **PDF is king:** WhatsApp image and shareable link are v2 features; v1 is PDF-only
- **Templates are static:** Templates are designed once by a designer and shipped as app assets or fetched from CDN; they are not user-customizable
- **Backend is mandatory:** AI calls and PDF rendering must go through a backend (not direct from Android client)
- **Photo moderation:** No automatic NSFW moderation in v1; rely on user trust + report mechanism
- **Income field is optional and not shown on all templates:** Sensitive field — user controls visibility

---

## 14. Open Questions

| # | Question | Stakeholder |
|---|---|---|
| Q1 | Should users be able to preview the biodata in Hindi before export, rendered with Devanagari fonts? | Design + Dev |
| Q2 | Do we need a "partner preferences" section (age range, education, location preference)? | Product |
| Q3 | Should AI summary generation be limited to N free generations per month (to control API cost)? | Business |
| Q4 | Who designs the 4–6 templates — in-house or freelancer? | Design |
| Q5 | Do we want a "family biodata" format where both sides fill one document? | Product |
| Q6 | What's the minimum Android API level — 26 (8.0) or 24 (7.0)? Affects ~5% of user base. | Dev |
