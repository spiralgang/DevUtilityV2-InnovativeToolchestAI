#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# Generate per-profile XDG env export files pinned to persistent storage.
# Usage: ./generate_profile_env.sh --state ./state
set -euo pipefail

STATE_DIR=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --state) STATE_DIR="$2"; shift 2;;
    *) echo "Unknown arg: $1"; exit 1;;
  esac
done
[[ -n "${STATE_DIR}" ]] || { echo "Missing --state"; exit 2; }

XDG_DIR="${STATE_DIR}/xdg"
mkdir -p "${XDG_DIR}"

DB="${STATE_DIR}/techula_index.db"
if [[ ! -f "${DB}" ]]; then
  echo "[!] Index DB not found. Run index first."
  exit 2
fi

profiles=$(sqlite3 "${DB}" "SELECT profile FROM profiles;")
for prof in ${profiles}; do
  envf="${XDG_DIR}/${prof}.env"
  # Use state as persistent base (customize as desired)
  base="${STATE_DIR}/persist/${prof}"
  mkdir -p "${base}/config" "${base}/data" "${base}/cache" "${base}/runtime"
  {
    echo "# XDG env for profile ${prof}"
    echo "export XDG_CONFIG_HOME='${base}/config'"
    echo "export XDG_DATA_HOME='${base}/data'"
    echo "export XDG_CACHE_HOME='${base}/cache'"
    echo "export XDG_RUNTIME_DIR='${base}/runtime'"
  } > "${envf}"
  echo "[i] Wrote ${envf}"
done