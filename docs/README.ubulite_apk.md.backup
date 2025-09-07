```md
UBULITE — local test workflow & recommended quick dev loop

1) Quick local test (dev machine + device on same Wi‑Fi)
   - On dev machine:
     npm ci
     npm run build:web
     npm start          # starts node src/server.js on PORT 3000
   - On device: install APK (Capacitor shell). Configure app UI to call your dev machine IP (e.g., 192.168.1.50:3000) for /api/ai etc.
   - Advantages: instant feedback, no native builds each change.

2) Bundle-in Node (offline)
   - Follow nodejs-mobile integration (docs linked in README.android.md)
   - Place server files in app assets and boot Node on app start
   - WebView points to http://127.0.0.1:3000

3) Secrets
   - Use Android Keystore or prompt-first-run to set tokens.
   - For CI releases use GitHub Secrets (GITHUB repo: Settings > Secrets) and inject via Gradle properties.

4) Safety note
   - Keep DRONE_SIMULATE=1 on-device until you validate everything.
   - Use provider=ollama (local) or HF (free). Avoid production provider tokens on device during testing.
```