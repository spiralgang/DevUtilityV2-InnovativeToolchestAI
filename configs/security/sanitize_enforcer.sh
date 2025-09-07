#!/usr/bin/env sh
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# sanitize_enforcer.sh â€” repository banlist scanner (no echoes of banned tokens)
# Purpose
# - Fail fast if any banned token appears anywhere in the repo (code, docs, examples).
# - Default banlist: ./.banned_tokens.txt (one token per line, # comments allowed).
# - Optional: env BANNED_TOKENS="token1,token2" overrides file.
# - Zero dependencies beyond POSIX sh + grep + find. Silent on success.
#
# Usage
#   tools/sanitize_enforcer.sh
#   BANNED_TOKENS="alpha,beta" tools/sanitize_enforcer.sh
#
# Rationale
# - Keeps multi-agent pipelines aligned by preventing trigger words/brands.
# - Avoids printing sensitive tokens; only counts and file paths are shown.

set -eu

repo_root() {
  if command -v git >/dev/null 2>&1 && git rev-parse --show-toplevel >/dev/null 2>&1; then
    git rev-parse --show-toplevel
  else
    pwd
  fi
}

ROOT="$(repo_root)"
TOKENS_FILE="${TOKENS_FILE:-$ROOT/.banned_tokens.txt}"
EXCLUDE_DIRS="${EXCLUDE_DIRS:-.git,node_modules,dist,build,.venv,.mypy_cache,.pytest_cache,.cache,.next,.turbo,coverage}"
CASE_INSENSITIVE="${CASE_INSENSITIVE:-1}"   # 1: use -i (case-insensitive), 0: exact case

# Resolve token list
TOKENS_RAW="${BANNED_TOKENS:-}"
TOKENS_TMP="$(mktemp)"
cleanup() { rm -f "$TOKENS_TMP"; }
trap cleanup EXIT

if [ -n "$TOKENS_RAW" ]; then
  # Split on commas into lines
  printf '%s' "$TOKENS_RAW" | tr ',' '\n' | sed '/^[[:space:]]*$/d' > "$TOKENS_TMP"
elif [ -r "$TOKENS_FILE" ]; then
  # From file: strip comments and blanks
  sed -e 's/[[:space:]]\+$//' -e '/^[[:space:]]*#/d' -e '/^[[:space:]]*$/d' "$TOKENS_FILE" > "$TOKENS_TMP"
else
  echo "[sanitize] No banlist set (BANNED_TOKENS or $TOKENS_FILE). Skipping." >&2
  exit 0
fi

# Build grep options
GREP_OPTS="-R -n -F -I"
[ "$CASE_INSENSITIVE" = "1" ] && GREP_OPTS="$GREP_OPTS -i"

# Build exclude-dir options
IFS=',' read -r -a EXARR <<EOF
$EXCLUDE_DIRS
EOF

for d in "${EXARR[@]}"; do
  GREP_OPTS="$GREP_OPTS --exclude-dir=$d"
done

FOUND=0

# Scan per-token to avoid echoing the token in output
# We only print file:line and a generic marker count summary.
while IFS= read -r token; do
  [ -z "$token" ] && continue
  # Count matches quietly
  if grep $GREP_OPTS -q -- "$token" "$ROOT"; then
    FOUND=1
    echo "[sanitize] Violations found for a banned token."
    # Show paths and line numbers without echoing the token
    # Use grep again but redact matched line content:
    # We print only 'path:line' using awk, platform-safe.
    grep $GREP_OPTS -- "$token" "$ROOT" | awk -F: '{print $1 ":" $2}' | sort -u
  fi
done < "$TOKENS_TMP"

if [ "$FOUND" -ne 0 ]; then
  echo "[sanitize] Fail: remove banned terms. See above file:line list." >&2
  exit 2
fi

# Quiet success
exit 0

# References
# - /reference/vault: CI gating, content policy, repo hygiene