#!/usr/bin/env bash
set -euo pipefail

mkdir -p .baseline

# Define what constitutes identity for your ops layer
TARGETS=(
  ".github/workflows"
  "scripts"
  "configs/activation_rules.json"
)

# Compute current state
tmp_current=$(mktemp)
for t in "${TARGETS[@]}"; do
  if [[ -d "$t" ]]; then
    find "$t" -type f -print0 | sort -z | xargs -0 sha256sum
  elif [[ -f "$t" ]]; then
    sha256sum "$t"
  fi
done > "$tmp_current"

# Bootstrap baseline if missing
if [[ ! -f .baseline/state.sha256 ]]; then
  cp "$tmp_current" .baseline/state.sha256
  echo "Baseline created."
  exit 0
fi

# Compare with baseline
if ! diff -u .baseline/state.sha256 "$tmp_current" >/dev/null; then
  echo "Drift detected against baseline."

  # Policy: block or roll back — choose one.
  # Hard block:
  echo "::error::Drift detected. Review changes or update baseline intentionally."
  exit 1

  # Or soft rollback (uncomment to enable):
  # git checkout -- .
  # echo "Rolled back to working tree baseline."
fi

echo "No drift."
