```md
``` 
HOTSPOTS.md — quick manifest of "hottest thought" files and why to inspect them

Purpose
-------
A compact manifest describing high-value files and directories across your repo collection to prioritize inspection and upload.

How to use
----------
- Use scripts/collect_and_encrypt_upload.sh which references this list.
- Inspect artifacts in the uploaded archive for:
  - config and env patterns
  - AI provider adapters
  - EnergeticWeave artifacts & JSON outputs
  - Mobile build docs and BigModel code samples

Hotspot entries (ordered by priority)
- UBULITE/
  - README.ubulite_weave.md — EnergeticWeave description + runbook
  - .github/workflows/* — CI workflow templates
  - scripts/* — dispatch, zram_helper, collect scripts (runtime tooling)
  - src/, web/, www/ — server and web UI to test on device
- iceman_drone_tool/ (if present)
  - energetic_weave.py
  - whisper.py / providers.py — provider adapters & token handling
  - drone_controller.py — MAVLink logic (safety check)
- BigModel/ (Zhipu specs)
  - BigModelService.kt (spec) and README — mobile integration instructions
- DevUtilityV2-InnovativeToolchestAI/
  - toolchest modules & plugins — plugin discovery & dev tooling
- NeuronLabs/
  - scripts/* (train_agent.sh, preprocess.sh) — dataset & train pipeline for offline models
- SGNeuronLabs-CTC-Coder-Specialists/
  - tools/check_env.py and extract_all_zips.py — environment checks and zip extraction (important to run before tests)
- terminal-chat-robot-node/
  - package.json, bin/tcr — Node chat robot (quick local test candidate)
- README.android.md & README.ubulite_apk.md — APK build & packaging instructions
- scripts/find_and_backup.sh, scripts/zram_helper.sh — useful runtime utilities

Notes
-----
- Prioritize artifact types: artifacts/*.json, build/*.apk, *.zip, .env-like files (but DO NOT commit secrets).
- For any JSON artifacts produced by EnergeticWeave, look for model_request, model_response fields for provider usage and tokens (tokens should not be printed — if they are, scrub immediately).
```
```


```yaml name=.github/workflows/ci-artifacts-upload.yml
name: CI Build & Encrypted Artifact Upload (template)
on:
  push:
    branches:
      - main
      - dev
  workflow_dispatch:

permissions:
  contents: read
jobs:
  build-and-upload:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Node (for web builds)
        uses: actions/setup-node@v4
        with:
          node-version: "18"

      - name: Install deps & build (if package.json)
        if: exists('package.json')
        run: |
          npm ci
          if [ -f package.json ]; then
            npm run build:web || echo "no web build"
          fi

      - name: Gather artifacts
        run: |
          mkdir -p artifacts_ci
          # include common artifacts if present
          test -d www && tar -czf artifacts_ci/www_$(date +%s).tar.gz www || true
          test -d build && tar -czf artifacts_ci/build_$(date +%s).tar.gz build || true
          test -f app/build/outputs/apk/release/app-release.apk && cp app/build/outputs/apk/release/app-release.apk artifacts_ci/ || true
          find .github -maxdepth 2 -type f -name '*.yml' -print0 | xargs -0 -r tar -czf artifacts_ci/gh_workflows.tar.gz || true
          ls -lah artifacts_ci || true

      - name: Create encrypted archive
        env:
          ACTION_ENC_PASS: ${{ secrets.ACTION_ENC_PASS }}
        run: |
          if [ -z "$ACTION_ENC_PASS" ]; then
            echo "No ACTION_ENC_PASS secret; aborting safely."
            exit 1
          fi
          TAR=ci_artifacts_$(date +%Y%m%dT%H%M%SZ).tar.gz
          tar -czf "$TAR" artifacts_ci || true
          openssl enc -aes-256-cbc -pbkdf2 -iter 200000 -salt -in "$TAR" -out "$TAR.enc" -pass pass:"$ACTION_ENC_PASS"
          ls -lah "$TAR.enc"

      - name: Install rclone (if not present)
        run: |
          sudo apt-get update -qq
          sudo apt-get install -y -qq rclone

      - name: Configure rclone (from secret)
        env:
          RCLONE_CONF_B64: ${{ secrets.RCLONE_CONF_B64 }}
        run: |
          mkdir -p ~/.config/rclone
          echo "$RCLONE_CONF_B64" | base64 -d > ~/.config/rclone/rclone.conf
          rclone listremotes

      - name: Upload encrypted archive to remote
        env:
          UPLOAD_REMOTE: ${{ secrets.RCLONE_REMOTE }}
        run: |
          if [ -z "$UPLOAD_REMOTE" ]; then
            echo "No RCLONE remote configured; aborting upload."
            exit 1
          fi
          TAR=ci_artifacts_*.tar.gz.enc
          rclone copy $TAR "$UPLOAD_REMOTE" --progress
```

What to inspect first after upload
- artifacts_ci/* (web bundle, APK if present)
- EnergeticWeave artifacts/ (weave-*.json) — check model_request.meta and model_response for provider selection
- provider adapters: iceman_drone_tool/providers.py or src/providers.js — verify which env tokens are referenced (OPENAI, OLLAMA, HF, ZHIPU)
- Any accidentally committed secrets — if present, rotate immediately and remove from repo history.

Security / safety note (short)
- Do not commit tokens in repo or upload plain archives. The provided scripts encrypt archives locally before upload.
- Rotate tokens if any key ever appears in a scanned artifact.

What's next (I did it — and next steps you can run right now)
- I created the files above; drop them into the UBULITE repository (scripts/ and .github/workflows/).
- On device: run scripts/collect_and_encrypt_upload.sh to snapshot and upload (or run it from a nearby laptop that has rclone configured).
- In GitHub: copy the CI workflow into .github/workflows/ and set the secrets:
  - ACTION_ENC_PASS (a secure phrase to encrypt CI artifacts)
  - RCLONE_CONF_B64 (base64-encoded rclone config file contents)
  - RCLONE_REMOTE (e.g., b2:mybucket/ubulite_ci/)
- After one run, inspect your remote and the downloaded archive on your desktop (openssl enc -d ...) to review contents.

If you want I can (pick one or I’ll do the first automatically):
- A) produce a small systemd user unit + metrics writer so EnergeticWeave can ingest zramctl metrics into weave_artifacts periodically; or
- B) produce a nodejs-mobile glue script that will let you run the Node server inside an APK and point the WebView to http://127.0.0.1:3000; or
- C) scan the repos and produce a prioritized code review checklist listing immediate fixes, risky commits (search for tokens), and the top 10 files to audit for secrets or unsafe provider usage.

I’ll proceed with C (repo scan checklist) automatically if you say “run the scan” — otherwise tell me which of A/B/C you want first and I’ll generate that code/config next.