#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# userland_recover_techula_fix.sh
# Purpose: Diagnose and (best-effort) extract tech.ula app-data from inside UserLAnd chroot.
# - Inspect permissions/SELinux info
# - Try a safe small-file read to test access
# - Attempt to create a tar.gz (capture errors)
# - If tar fails due to permissions/SELinux, print exact host-side 'su -c' command
# - Optionally start a loopback HTTP server to fetch the produced archive
#
# Usage examples:
#   ./userland_recover_techula_fix.sh --source /data/data/tech.ula --out /tmp/techula.tgz --serve
#   ./userland_recover_techula_fix.sh --dry-run --source /data/data/tech.ula
#
set -o pipefail
set -u

SOURCE_DEFAULT="/data/data/tech.ula"
OUT_DEFAULT="/tmp/techula-backup-$(date +%Y%m%d_%H%M%S).tgz"
PID_FILE="/tmp/userland_recover/http.pid"
LOG_FILE="/tmp/userland_recover/http.log"

SOURCE="${SOURCE:-$SOURCE_DEFAULT}"
OUT="${OUT:-$OUT_DEFAULT}"
DRY_RUN=false
SERVE=false
HTTP_BIND="127.0.0.1"
HTTP_PORT=8000
VERBOSE=false

usage(){ cat <<EOF
Usage: $(basename "$0") [options]
  --source PATH   source directory (default: $SOURCE_DEFAULT)
  --out PATH      output archive (default: $OUT_DEFAULT)
  --dry-run       only diagnose and report (no tar)
  --serve         after successful archive, start http server (background)
  --port N        HTTP port (default $HTTP_PORT)
  --verbose       verbose output
  -h,--help       show this help
EOF
exit 1
}

# parse
while [[ $# -gt 0 ]]; do
  case "$1" in
    --source) SOURCE="$2"; shift 2;;
    --out) OUT="$2"; shift 2;;
    --dry-run) DRY_RUN=true; shift;;
    --serve) SERVE=true; shift;;
    --port) HTTP_PORT="$2"; shift 2;;
    --verbose) VERBOSE=true; shift;;
    -h|--help) usage;;
    *) echo "Unknown arg: $1"; usage;;
  esac
done

mkdir -p "$(dirname "$OUT")" /tmp/userland_recover 2>/dev/null || true

vlog(){ $VERBOSE && printf '[v] %s\n' "$*"; }
log(){ printf '%s\n' "$*"; }
err(){ printf 'ERROR: %s\n' "$*" >&2; }

# 1) Resolve and basic existence
log "1) resolving source..."
SOURCE="$(realpath -m "$SOURCE" 2>/dev/null || echo "$SOURCE")"
if [ ! -e "$SOURCE" ]; then
  err "Source does not exist inside chroot: $SOURCE"
  exit 2
fi
log "Source exists: $SOURCE"

# 2) Permission & attribute checks
log "2) examining permissions, owner, and (if available) SELinux context"
if command -v stat >/dev/null 2>&1; then
  stat -c 'mode=%A uid=%u gid=%g size=%s mtime=%y %n' "$SOURCE" 2>/dev/null || true
fi
if ls -ld "$SOURCE" >/dev/null 2>&1; then
  ls -ld "$SOURCE"
fi
if command -v ls >/dev/null 2>&1 && ls -Z "$SOURCE" >/dev/null 2>&1; then
  # ls -Z may fail if not supported; ignore failure
  ls -ldZ "$SOURCE" 2>/dev/null || true
fi

# 3) Try a safe small read test (list a few files, head a small file)
log "3) attempting small-read tests (non-destructive)"
if [ -d "$SOURCE" ]; then
  # list top entries
  vlog "listing top 20 entries (ls -A):"
  ls -A "$SOURCE" | head -n 20 || true

  # find one small file and try to read first 512 bytes
  smallf="$(find "$SOURCE" -type f -size -64k -print 2>/dev/null | head -n1 || true)"
  if [ -n "$smallf" ]; then
    vlog "attempting to read small file: $smallf"
    head -c 512 "$smallf" | sed -n '1,10p' || true
    log "small-file read OK"
  else
    log "no small (<64k) file found quickly; that doesn't imply unreadable — could be permissions/SELinux blocking reads."
  fi
else
  log "Source is not a directory (it is a file). Trying a simple copy test."
  if cp -a "$SOURCE" /tmp/ 2>/tmp/userland_recover/copy.err; then
    log "file copy to /tmp succeeded (copied to /tmp/$(basename "$SOURCE"))"
  else
    err "copy failed; see /tmp/userland_recover/copy.err"
  fi
fi

# 4) If dry-run, stop after reporting
if [ "$DRY_RUN" = true ]; then
  log "Dry-run requested; stopping after diagnostics."
  exit 0
fi

# 5) Attempt to tar the source to OUT, capturing stderr
log "4) attempting to create archive (tar) -> $OUT"
tar_cmd=(tar -C "$(dirname "$SOURCE")" -czf "$OUT" "$(basename "$SOURCE")")
vlog "running: ${tar_cmd[*]}"
if "${tar_cmd[@]}" 2> /tmp/userland_recover/tar.err; then
  log "Archive created successfully: $OUT"
  ls -lh "$OUT" || true
  TAR_OK=true
else
  TAR_OK=false
  err "tar failed. Captured stderr (first 200 bytes):"
  head -c 200 /tmp/userland_recover/tar.err || true
  echo
fi

# 6) If tar failed, provide exact host-root commands + diagnostics to paste back
if [ "$TAR_OK" != true ]; then
  log "5) tar failed — diagnosing likely cause and printing host-root fallback commands."
  # show error lines
  log "---- tar stderr (tail 40) ----"
  tail -n 40 /tmp/userland_recover/tar.err 2>/dev/null || true
  log "-------------------------------"

  cat <<EOF

Diagnosis checklist:
 - If 'Permission denied' appears in the tar stderr, SELinux or Android host protections are blocking access.
 - If 'Input/output error' or 'Operation not permitted', mount/namespace protections likely apply.
 - Being 'root inside chroot' does not guarantee host-root; /data is commonly blocked.

Host-root fallback (run on Android host shell with su):
  su -c "tar -C /data/user/0 -czf /sdcard/$(basename "$OUT") ."

Notes:
 - That command creates an archive on /sdcard (or /storage/internal). After running it on the host,
   check inside the chroot for /sdcard/$(basename "$OUT") or /storage/internal/$(basename "$OUT"), then copy/serve it.
 - If you have CX File Explorer with root, use its Compress/Create Archive function to write the archive to /sdcard and then access it from the chroot.

If you want, I can:
 - 1) attempt run-as (if you tell me the package is debuggable) and create an archive via run-as, or
 - 2) produce a one-line su command you can paste into your phone's host shell, or
 - 3) attempt an alternate tar call that tries to skip unreadable files (creates partial archive) — I can create that now.

EOF
  exit 3
fi

# 7) If tar succeeded and user wanted serve, start HTTP server in background
if [ "$TAR_OK" = true ] && [ "$SERVE" = true ]; then
  if ! command -v python3 >/dev/null 2>&1; then
    err "python3 not found; install with: apt update && apt install -y python3"
    exit 4
  fi
  dir="$(dirname "$OUT")"
  name="$(basename "$OUT")"
  # background serve
  cd "$dir" || die "unable to cd to $dir"
  nohup python3 -m http.server "$HTTP_PORT" --bind "$HTTP_BIND" > "$LOG_FILE" 2>&1 &
  echo $! > "$PID_FILE"
  sleep 0.2
  pid="$(cat "$PID_FILE" 2>/dev/null || true)"
  if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
    log "HTTP server started: http://${HTTP_BIND}:${HTTP_PORT}/${name}   PID $pid"
    log "Open on phone and download; then use SAF (Open Document) to paste into Android/data/tech.ula/files/storage"
  else
    err "Failed to start HTTP server; check $LOG_FILE"
  fi
fi

exit 0