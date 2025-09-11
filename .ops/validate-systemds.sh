#!/usr/bin/env bash
# Fast repository & environment sanity checks (aim: <15s).
# - Fails fast on missing tools/files.
# - Tolerant of being invoked from subdirs: resolves repo root.
# - Provides clearer error messages and optional JSON validation if jq present.

set -euo pipefail

start_ts=$(date -u +%s)

# Helpers
err() { echo "ERROR: $*" >&2; exit 2; }
have() { command -v "$1" >/dev/null 2>&1; }

# Resolve repo root: prefer git, fallback to script dir parent
if have git; then
  repo_root=$(git rev-parse --show-toplevel 2>/dev/null || true)
fi
repo_root=${repo_root:-}
if [ -z "$repo_root" ]; then
  # Fallback: directory containing this script, two levels up if placed in .ops/
  script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  repo_root="$(cd "$script_dir/.." && pwd)"
fi

cd "$repo_root" || err "cannot cd to repo root '$repo_root'"

# Required commands
required_cmds=(bash git sh)
for cmd in "${required_cmds[@]}"; do
  if ! have "$cmd"; then
    err "required command not found: $cmd"
  fi
done

# Required files/dirs (fail if absent)
test -d .git || err ".git directory missing; not a git repo or wrong working dir"
test -f configs/activation_rules.json || err "missing required file: configs/activation_rules.json"

# Optional critical paths (clear failure if absent)
for p in ".github/workflows" "scripts" "logs"; do
  test -d "$p" || err "missing required directory: $p"
done

# Optional: validate JSON if jq is present
if have jq; then
  if ! jq empty configs/activation_rules.json >/dev/null 2>&1; then
    err "configs/activation_rules.json is not valid JSON"
  fi
fi

echo "All validation tests pass in ~$(( $(date -u +%s) - start_ts ))s"
