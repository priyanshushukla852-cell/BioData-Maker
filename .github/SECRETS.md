# GitHub Actions Secrets Setup

## Required Secrets for CI/CD Pipelines

### Backend (Railway Deployment)
Set these secrets in your GitHub repository settings under **Settings → Secrets and variables → Actions**:

| Secret | Description |
|--------|-------------|
| `RAILWAY_TOKEN` | Railway API token. [Get it here](https://railway.app/account/tokens) |
| `RAILWAY_PROJECT_ID` | Railway project ID (found in project settings) |
| `RAILWAY_SERVICE_ID` | Railway service ID for the backend (found in service settings) |

### Android (Play Console Release)
Set these secrets in your GitHub repository settings:

| Secret | Description |
|--------|-------------|
| `SIGNING_KEY_ALIAS` | Android keystore alias for signing release APKs |
| `SIGNING_KEY_PASSWORD` | Password for the signing key in the keystore |
| `SIGNING_STORE_PASSWORD` | Master password for the keystore file |
| `PLAY_CONSOLE_SERVICE_ACCOUNT` | Google Play Console service account JSON (full contents) |

### How to Set Up

1. Go to your GitHub repository
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add each secret by name and value

#### Android Signing Key
Generate a keystore if you don't have one:
```bash
keytool -genkey -v -keystore biodata-release.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias biodata-key
```

#### Play Console Service Account
1. Open [Google Play Console](https://play.google.com/console)
2. Go to **Settings** → **API access**
3. Create a service account in Google Cloud Console
4. Download the JSON key and paste its full contents as `PLAY_CONSOLE_SERVICE_ACCOUNT`

#### Railway Tokens
1. Go to [Railway Dashboard](https://railway.app)
2. Click your account in the bottom left
3. Go to **Tokens**
4. Create a new token
5. Copy and paste as `RAILWAY_TOKEN`

## Workflow Behavior

- **Android CI:** Runs on all PRs and main branch pushes that touch `android/`
  - Lint, unit tests, debug build on all PRs
  - Instrumented tests (API 26) on all PRs
  - Release build & Play Console upload **only on main branch**

- **Backend CI:** Runs on all PRs and main branch pushes that touch `backend/`
  - Build and test on all PRs
  - Auto-deploy to Railway **only on main branch** (requires secrets configured)

## Troubleshooting

- If Play Console upload fails: verify service account has the correct permissions in Google Play Console
- If Railway deployment fails: check that project/service IDs are correct and token is valid
- If instrumented tests timeout: the emulator-runner action can take 10+ minutes; adjust timeout if needed

