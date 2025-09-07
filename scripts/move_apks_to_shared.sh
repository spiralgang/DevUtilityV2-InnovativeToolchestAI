#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# move_apks_to_shared.sh
# Move APK files from inside the app storage tree to a writable shared storage destination.
# Usage:
#   bash move_apks_to_shared.sh          # interactive listing
#   bash move_apks_to_shared.sh --yes    # move without asking
#
set -euo pipefail

SRC_BASE="/data/data/tech.ula/files"
DESTS=( "/storage/internal" "/storage/emulated/0" "/sdcard" "/data/media/0" "/tmp" )
AUTO_YES=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --yes) AUTO_YES=true; shift ;;
    -h|--help) echo "Usage: $0 [--yes]"; exit 0 ;;
    *) echo "Unknown arg: $1"; exit 1 ;;
  esac
done

echo "Searching for APKs under: $SRC_BASE"
mapfile -t APKS < <(find "$SRC_BASE" -type f -iname '*.apk' 2>/dev/null || true)

if [ "${#APKS[@]}" -eq 0 ]; then
  echo "No APKs found under $SRC_BASE"
  exit 0
fi

echo "Found APKs:"
for a in "${APKS[@]}"; do
  ls -lh "$a" || true
done

# Pick first writable destination
DEST=""
for d in "${DESTS[@]}"; do
  if [ -d "$d" ]; then
    # quick write test
    testfile="$d/.proot_move_test.$$"
    if touch "$testfile" 2>/dev/null; then
      rm -f "$testfile"
      DEST="$d"
      break
    fi
  fi
done

if [ -z "$DEST" ]; then
  echo "No writable shared destination found among: ${DESTS[*]}"
  exit 2
fi

echo "Chosen destination: $DEST"
if ! $AUTO_YES; then
  read -p "Move ${#APKS[@]} APK(s) to $DEST ? Type YES to proceed: " c
  if [ "$c" != "YES" ]; then
    echo "Aborted by user."
    exit 0
  fi
fi

for a in "${APKS[@]}"; do
  echo "mv -v \"$a\" \"$DEST/\""
  mv -v "$a" "$DEST/" || { echo "mv failed for $a"; }
done

sync
echo "Moved APKs. Verify:"
ls -lh "$DEST"/*.apk 2>/dev/null || true
echo "Done."