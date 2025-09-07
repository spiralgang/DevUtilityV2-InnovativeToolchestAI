#!/usr/bin/env bash
set -euo pipefail

start_ts=$(date -u +%s)

# Fast checks only; keep total under ~15s
command -v bash >/dev/null
command -v git >/dev/null
command -v sh >/dev/null

# Repo sanity
test -d .git
test -f configs/activation_rules.json

# Optional: verify critical paths exist
for p in ".github/workflows" "scripts" "logs"; do
  test -d "$p"
done

echo "All validation tests pass in ~$(( $(date -u +%s) - start_ts ))s"
