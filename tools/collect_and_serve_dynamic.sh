#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

#
# collect_and_serve_dynamic.sh
# Dynamically collect as many readable filesystem slices as possible (non-destructive,
# keeps running on errors), package them (including dotfiles), and optionally serve
# a resulting archive over HTTP for on-device download.
#
# Key properties:
# - Resilient: continues on errors (uses --ignore-failed-read and non-fatal checks)
# - Dynamic variables: candidate roots are configurable via env / CLI
# - Includes dotfiles by using tar -C <parent> <path> or tar with relative-source paths
# - Produces: one combined archive, a per-source log, and a manifest (tar -tzf)
# - Optional: start background HTTP server and print URL
#
# Safety: non-destructive; does not attempt privileged host escallation by default.
# If you have host/root and want the script to attempt host tar, use --exec-host-tar.
#
set +e
IFS=$'\n\t'

### Defaults (can be overridden via environment or CLI)
OUT_DIR="${OUT_DIR:-/tmp/recovery_output}"
NAME_PREFIX="${NAME_PREFIX:-recovery}"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
ARCHIVE_NAME="${ARCHIVE_NAME:-${NAME_PREFIX}_${TIMESTAMP}.tgz}"
ARCHIVE_PATH="${OUT_DIR}/${ARCHIVE_NAME}"
LOG_DIR="${OUT_DIR}/logs"
MANIFEST="${OUT_DIR}/${NAME_PREFIX}_${TIMESTAMP}.manifest.txt"
PORT="${PORT:-8000}"
BIND_ADDR="${BIND_ADDR:-127.0.0.1}"
SERVE=false
FORCE=false
EXEC_HOST_TAR=false
HOST_DEST="${HOST_DEST:-/storage/emulated/0/${ARCHIVE_NAME}}"
VERBOSE=false

# Candidate roots (ordered heuristically). Can be overridden with ROOTS env (comma-separated).
if [ -n "${ROOTS:-}" ]; then
  IFS=',' read -r -a CANDIDATES <<< "$ROOTS"
else
  CANDIDATES=(
    "/usr/bin"
    "/usr"
    "/data/user/0"
    "/data/data"
    "/data/media/0"
    "/storage/emulated/0"
    "/sdcard"
    "/storage/internal"
    "/tmp"
    "/var"
    "/home"
  )
fi

mkdir -p "$OUT_DIR" "$LOG_DIR"

log(){ printf '%s\n' "$*"; }
v(){ $VERBOSE && printf '[v] %s\n' "$*"; }
err(){ printf 'ERR: %s\n' "$*" >&2; }

usage(){
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --outdir PATH         Output directory (default: $OUT_DIR)
  --archive NAME        Archive filename (default: $ARCHIVE_NAME)
  --roots PATHS         Comma-separated candidate roots (overrides built-in)
  --serve               Start background HTTP server after archive creation
  --port N              HTTP port (default: $PORT)
  --bind ADDR           HTTP bind address (default: $BIND_ADDR)
  --force               Don't prompt for big archives
  --exec-host-tar       Attempt host 'su -c' tar if chroot tar can't read data
  --host-dest PATH      Destination path on host for exec-host-tar (default: $HOST_DEST)
  --verbose             Verbose logging
  -h, --help            Show this help
Examples:
  # dry run listing (no archive):
    ./$(basename "$0") --roots "/data/data,/usr/bin" --verbose
  # create archive and serve:
    ./$(basename "$0") --serve --bind 0.0.0.0 --port 8000
USAGE
  exit 1
}

# --- parse args ---
while [[ $# -gt 0 ]]; do
  case "$1" in
    --outdir) OUT_DIR="$2"; shift 2;;
    --archive) ARCHIVE_NAME="$2"; ARCHIVE_PATH="${OUT_DIR}/${ARCHIVE_NAME}"; shift 2;;
    --roots) IFS=',' read -r -a CANDIDATES <<< "$2"; shift 2;;
    --serve) SERVE=true; shift;;
    --port) PORT="$2"; shift 2;;
    --bind) BIND_ADDR="$2"; shift 2;;
    --force) FORCE=true; shift;;
    --exec-host-tar) EXEC_HOST_TAR=true; shift;;
    --host-dest) HOST_DEST="$2"; shift 2;;
    --verbose) VERBOSE=true; shift;;
    -h|--help) usage;;
    *) echo "Unknown arg: $1"; usage;;
  esac
done

mkdir -p "$OUT_DIR" "$LOG_DIR"

# detect elevated command use
SUDO=""
if [ "$(id -u)" -ne 0 ]; then
  if command -v sudo >/dev/null 2>&1; then
    SUDO="sudo"
  fi
fi

# Compose list of sources that exist
declare -a SOURCES=()
for p in "${CANDIDATES[@]}"; do
  # expand globs if any
  for cand in $(eval echo "$p"); do
    [ -n "$cand" ] || continue
    if [ -e "$cand" ]; then
      SOURCES+=("$cand")
      v "Candidate exists: $cand"
    else
      v "Candidate missing: $cand"
    fi
  done
done

if [ ${#SOURCES[@]} -eq 0 ]; then
  err "No candidate sources exist. Re-run with --roots or ensure mounts available."
  exit 2
fi

log "Will attempt to archive the following sources (best-effort):"
for s in "${SOURCES[@]}"; do
  log " - $s"
done

# Prompt if archive might be large
if [ "$FORCE" != true ]; then
  est_size=$(du -sh "${SOURCES[@]}" 2>/dev/null | tail -n1 | awk '{print $1}')
  log "Estimated size (du -sh sample): $est_size"
  read -p "Proceed to create best-effort archive including dotfiles? (y/N) " yn
  case "$yn" in [Yy]*) ;; *) log "Aborted by user."; exit 0;; esac
fi

# Build tar source list relative to "/" to avoid double -C complexity
# We'll create tar from / and provide relative paths (strip leading /)
TMP_LIST="$LOG_DIR/sources_list.txt"
: > "$TMP_LIST"
for s in "${SOURCES[@]}"; do
  # if path is rooted at '/', convert to relative with no leading slash
  rel="${s#/}"
  # If path ends with '/', strip
  rel="${rel%/}"
  # If rel empty (means '/'), treat as '.' (rare)
  if [ -z "$rel" ]; then
    rel="."
  fi
  printf '%s\0' "$rel" >> "$TMP_LIST"
done

# Create archive using tar from / with --ignore-failed-read to skip unreadable content
log "Creating combined best-effort archive: $ARCHIVE_PATH"
# Use --null -T - to read NUL-separated list
tar_cmd=(tar -C / --null -T "$TMP_LIST" --warning=no-file-changed --ignore-failed-read -czf "$ARCHIVE_PATH")
v "Running: ${tar_cmd[*]}"
$SUDO "${tar_cmd[@]}" 2> "$LOG_DIR/tar.err"
tar_rc=$?

if [ $tar_rc -ne 0 ]; then
  err "tar returned non-zero ($tar_rc). Captured stderr head:"
  sed -n '1,200p' "$LOG_DIR/tar.err" || true
fi

# Make manifest (list of included entries)
if [ -f "$ARCHIVE_PATH" ]; then
  log "Archive created: $ARCHIVE_PATH"
  tar -tzf "$ARCHIVE_PATH" | sed -n '1,200p' > "$MANIFEST" 2>/dev/null || true
  log "Manifest written to: $MANIFEST (first 200 lines):"
  sed -n '1,200p' "$MANIFEST" || true
  du -h "$ARCHIVE_PATH" || true
else
  err "Archive not produced. See $LOG_DIR/tar.err"
fi

# If archive missing or tiny and user requested exec-host-tar, provide attempt or snippet
if [ ! -f "$ARCHIVE_PATH" ] || [ "$(stat -c%s "$ARCHIVE_PATH" 2>/dev/null || echo 0)" -lt 1024 ]; then
  err "Archive absent or very small. Likely host protections blocked read of /data."
  cat <<EOF > "$LOG_DIR/host-tar.snippet.txt"
# Run on Android host (CX/Termux/adb shell) with root (su):
su -c 'tar -C /data -czf "${HOST_DEST}" data/user/0 data/data'
# Alternative (if tech.ula location known):
su -c 'tar -C /data -czf "${HOST_DEST}" data/data/tech.ula'
# After running on host, check from chroot:
ls -lh /storage/emulated/0/$(basename "${HOST_DEST}") /sdcard/$(basename "${HOST_DEST}") /storage/internal/$(basename "${HOST_DEST}")
EOF
  log "Host-tar snippet written to: $LOG_DIR/host-tar.snippet.txt"
  if [ "$EXEC_HOST_TAR" = true ]; then
    log "Attempting to run host tar via su (may fail inside chroot):"
    $SUDO sh -c "su -c 'tar -C /data -czf \"${HOST_DEST}\" data/user/0 data/data' 2> \"$LOG_DIR/exec_host.err\""
    sed -n '1,200p' "$LOG_DIR/exec_host.err" || true
  fi
fi

# Offer to copy archive to common host-visible locations (/storage/emulated/0, /sdcard, /storage/internal)
copied_any=false
for dest in /storage/emulated/0 /sdcard /storage/internal; do
  if [ -d "$dest" ]; then
    v "Attempting copy to $dest"
    $SUDO cp -av "$ARCHIVE_PATH" "$dest/" 2> "$LOG_DIR/copy.err" || true
    if [ -f "$dest/$(basename "$ARCHIVE_PATH")" ]; then
      log "Copied archive to: $dest/$(basename "$ARCHIVE_PATH")"
      copied_any=true
    fi
  fi
done

# If serve requested and archive exists, start simple HTTP server in background
HTTP_PID_FILE="${LOG_DIR}/http.pid"
if [ "$SERVE" = true ] && [ -f "$ARCHIVE_PATH" ]; then
  SERVE_DIR="$(dirname "$ARCHIVE_PATH")"
  ARCH_NAME="$(basename "$ARCHIVE_PATH")"
  # use python3 if available
  if command -v python3 >/dev/null 2>&1; then
    log "Starting HTTP server in $SERVE_DIR -> http://${BIND_ADDR}:${PORT}/${ARCH_NAME}"
    cd "$SERVE_DIR" || true
    nohup python3 -m http.server "$PORT" --bind "$BIND_ADDR" > "$LOG_DIR/http.out" 2>&1 &
    echo $! > "$HTTP_PID_FILE"
    sleep 0.2
    pid="$(cat "$HTTP_PID_FILE" 2>/dev/null || echo '')"
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      log "HTTP server running: http://${BIND_ADDR}:${PORT}/${ARCH_NAME}  (PID $pid)"
    else
      err "Failed to start HTTP server; check $LOG_DIR/http.out"
    fi
  else
    err "python3 not found; cannot start HTTP server. Copy archive to /sdcard or use other transfer."
  fi
fi

log "Done. Output dir: $OUT_DIR"
log "Archive: $ARCHIVE_PATH"
[ -f "$MANIFEST" ] && log "Manifest: $MANIFEST"
log "Logs: $LOG_DIR"
if [ -f "$HTTP_PID_FILE" ]; then
  pid="$(cat "$HTTP_PID_FILE" || true)"
  log "HTTP PID: $pid -> http://${BIND_ADDR}:${PORT}/$(basename "$ARCHIVE_PATH")"
fi

exit 0