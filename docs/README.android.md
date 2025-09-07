```md
UBULITE — Build APK (overview)

Design decisions (concise)
- We bundle a Web UI (www/) and a Node backend. Two practical APK options:
  1) WebView + remote Node backend (fast to iterate): the APK contains only the frontend; backend runs on-device remotely or on the network.
  2) Full local backend inside the APK: embed Node runtime (nodejs-mobile) to run src/server.js inside the app (recommended for offline), but this requires native integration and extra build steps.

Which to pick:
- If you want fastest dev + ability to iterate on-device: use option (1) — build the web shell APK and point the app to local host (10.0.2.2 emu) or a device-hosted node server.
- If you need everything bundled and offline: option (2) — follow nodejs-mobile docs and integrate node assets into Android project; your Node server will listen on local port that WebView connects to.

Quick Start (Option 1 — recommended for initial test)
1) Install Node & npm on your dev machine (>=14).
2) Install Capacitor CLI: npm i -g @capacitor/cli
3) From repo root:
   npm ci
   npm run build:web
   npx cap add android   # only first time
   npx cap open android  # opens Android Studio
4) In Android Studio: build APK (Build -> Build Bundle(s) / APK(s) -> Build APK(s)).
5) Install APK on device and test. For development, run `npm start` (start node server on dev machine), then modify app to point API calls to your machine IP.

Full local Node runtime (Option 2 — nodejs-mobile)
- Read nodejs-mobile docs: https://github.com/JaneaSystems/nodejs-mobile
- Steps summary:
  - Add nodejs-mobile Android library to the native project.
  - Package src/ and node_modules used by server into the app assets.
  - Launch Node from Java/Kotlin using NodeBridge and run main script that starts Express server on 127.0.0.1:3000.
  - Point WebView inside the app to http://127.0.0.1:3000.
- This path yields a self-contained APK that hosts both UI and backend locally (ideal for your mobile-first workflow).

Secrets & tokens (device-safe)
- DO NOT hardcode tokens in repo.
- For local dev set env vars on dev machine. For device:
  - Option A: store keys in Android secure storage (Keystore) or set via build-time Gradle properties (CI picks secrets from GitHub Actions and injects during build).
  - Option B: load tokens into the app during first-run flow (user pastes into a secure UI) and store encrypted locally.

CI automation (recommended)
- Use GitHub Actions to build release APK and inject secrets via repository secrets (GITHUB secrets: OPENAI_API_KEY, HUGGINGFACE_API_KEY, etc) — do not commit tokens.
- The repo already contains `package.json` and `scripts/build_apk.sh` to standardize the web build step.

Testing on Android 10
- Use DRONE_SIMULATE=1 for any drone commands.
- Validate artifact output under `~/icedman/weave_artifacts/` inside the app sandbox (mapped to app storage).
- Start with provider=ollama (local) or huggingface (free-tier) for early testing.

```