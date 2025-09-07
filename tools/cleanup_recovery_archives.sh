#!/usr/bin/env bash
# Remove large recovery archives created earlier to free space.
set -euo pipefail
BASEDIR="/tmp/recovery_output"
LOG="/tmp/cleanup_recovery.log"
echo "cleanup_recovery_archives.sh starting: $(date)" >"$LOG"
echo "Listing candidate files in $BASEDIR:" | tee -a "$LOG"
ls -lh "${BASEDIR}" 2>/dev/null | tee -a "$LOG" || true
echo
echo "Also check /storage/internal and /storage/emulated/0 for large recovery_*.tgz" | tee -a "$LOG"
ls -lh /storage/internal/recovery_*.tgz 2>/dev/null | tee -a "$LOG" || true
ls -lh /storage/emulated/0/recovery_*.tgz 2>/dev/null | tee -a "$LOG" || true
echo
read -p "Type YES to delete *.tgz in ${BASEDIR} and /storage/internal/recovery_*.tgz : " confirm
if [ "$confirm" = "YES" ]; then
  find "${BASEDIR}" -maxdepth 1 -type f -name 'recovery_*.tgz' -print -exec rm -v {} \; 2>&1 | tee -a "$LOG" || true
  rm -vf /storage/internal/recovery_*.tgz 2>>"$LOG" || true
  rm -vf /storage/emulated/0/recovery_*.tgz 2>>"$LOG" || true
  echo "Removed listed archives. Running sync and df -h:" | tee -a "$LOG"
  sync
  df -h /tmp /storage /data 2>/dev/null | tee -a "$LOG" || df -h | tee -a "$LOG"
else
  echo "Aborted by user. No files deleted." | tee -a "$LOG"
fi
echo "done" | tee -a "$LOG"