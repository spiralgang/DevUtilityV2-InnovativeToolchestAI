#!/usr/bin/env bash
# Build helper (Capacitor/Android) — high-level automation.
# Two build paths:
#  A) Quick dev (web UI + remote backend): build web bundle and use Capacitor to build Android shell.
#  B) Full local backend (nodejs-mobile): requires nodejs-mobile build integration (advanced).
#
# This script automates path A. For fully-local backend (nodejs-mobile) see README.android.md.

set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "[1/6] Install node deps (local)"
npm ci

echo "[2/6] Build web bundle into www/"
npm run build:web

echo "[3/6] Ensure Capacitor installed and android platform added (requires capacitor cli & Android SDK)"
# You can skip these if you already added android
if ! command -v npx >/dev/null; then echo "npx not found; install Node tooling first"; exit 1; fi
npx cap sync android

echo "[4/6] Open Android Studio for final build (recommended). Alternatively run Gradle build via CLI."
echo "To continue automatically, uncomment and run the next lines if you have Android SDK & Gradle configured:"
# cd android && ./gradlew assembleRelease
echo "Done. If you prefer automated CI, use the provided GitHub Actions template in README.android.md"