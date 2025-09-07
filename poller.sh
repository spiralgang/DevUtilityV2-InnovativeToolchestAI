#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# Minimal poller to keep indexes fresh without inotify (Android/PRoot-safe).
# Run in screen/tmux: ./scripts/poller.sh
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
STATE="${STATE:-${ROOT}/state}"
SLEEP="${SLEEP:-900}"  # 15 min
while true; do
  ts="$(date -Iseconds)"
  echo "[$ts] Refresh metadata..."
  "${ROOT}/techula_caretaker.sh" index || true
  echo "[$ts] Content index..."
  "${ROOT}/techula_caretaker.sh" content-index || true
  echo "[$ts] CDC index..."
  "${ROOT}/techula_caretaker.sh" cdc-index || true
  echo "[$ts] Simhash..."
  "${ROOT}/techula_caretaker.sh" simhash-index || true
  echo "[$ts] Sleep ${SLEEP}s"
  sleep "${SLEEP}"
done