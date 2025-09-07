#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# Extract the single APK from the app storage to shared storage.
# Run in the SAME shell that showed / as the app root (proc root -> /data/data/tech.ula/...).
set -euo pipefail
SRC="/data/data/tech.ula/files/storage/tech.ula_7438725.apk"
BNAME="$(basename "$SRC")"
LOG="/tmp/extract_apk.log"
DESTS=( "/storage/internal" "/storage/emulated/0" "/sdcard" "/data/media/0" "/tmp" )

echo "extract_and_copy_apk.sh starting: $(date)" >"$LOG"

if [ ! -f "$SRC" ]; then
  echo "ERROR: source not found: $SRC" | tee -a "$LOG"
  exit 2
fi

echo "Source:" | tee -a "$LOG"
stat -c '%n %s %A %u:%g' "$SRC" | tee -a "$LOG"

# Try direct copy -> test dest write by writing a small temp file
for D in "${DESTS[@]}"; do
  if [ ! -d "$D" ]; then
    echo "skip: dest missing $D" | tee -a "$LOG"
    continue
  fi
  # quick writable check: attempt touch
  TMPCHECK="$D/.proot_write_test.$$"
  if ( touch "$TMPCHECK" ) 2>/dev/null; then
    rm -f "$TMPCHECK"
    echo "attempting cp -> $D" | tee -a "$LOG"
    if cp -av "$SRC" "$D/" 2>>"$LOG"; then
      echo "OK: copied to $D/$BNAME" | tee -a "$LOG"
      ls -lh "$D/$BNAME" 2>/dev/null | tee -a "$LOG"
      exit 0
    else
      echo "WARN: cp to $D failed; continuing" | tee -a "$LOG"
    fi
  else
    echo "not writable: $D (skipping)" | tee -a "$LOG"
  fi

  # fallback single-file tar into this dest
  TARPATH="$D/${BNAME%.apk}.tgz"
  echo "attempting single-file tar -> $TARPATH" | tee -a "$LOG"
  if tar -C "$(dirname "$SRC")" -czf "$TARPATH" "$(basename "$SRC")" 2>>"$LOG"; then
    echo "OK: tar created $TARPATH" | tee -a "$LOG"
    ls -lh "$TARPATH" 2>/dev/null | tee -a "$LOG"
    exit 0
  else
    echo "tar -> $TARPATH failed; continuing" | tee -a "$LOG"
    rm -f "$TARPATH" 2>/dev/null || true
  fi
done

# Nothing worked
echo "FAILED: could not move APK to any candidate destination from this shell" | tee -a "$LOG"
echo "See $LOG for details" | tee -a "$LOG"
exit 3