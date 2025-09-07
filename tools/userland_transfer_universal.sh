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
# userland_transfer_universal.sh
# A robust, dynamic helper to transfer files from a UserLAnd chroot to
# Android app storage (/storage/emulated/0/Android/data/tech.ula/files/storage).
#
# Goals:
# - handle Android-host paths that may or may not be visible inside chroot
# - create a single tar.gz (default) or serve a directory directly
# - best-effort start sshd for SFTP (UserLAnd commonly proxied on 127.0.0.1:8022)
# - start/stop a loopback-only HTTP server (python3) and write PID/logs to /tmp
# - provide a safe host-root copy option for true host-root systems (opt-in)
# - record and report status and troubleshooting hints for Android 10 + SAF workflows
#
# Usage examples:
#   ./userland_transfer_universal.sh --source /tmp/mypkg.tar.gz
#   ./userland_transfer_universal.sh --source /home/serverhustle/mydir --no-tar
#   ./userland_transfer_universal.sh --source /storage/emulated/0/... --install
#   ./userland_transfer_universal.sh --status
#   ./userland_transfer_universal.sh --stop
#
# IMPORTANT:
# - This script is intended to run INSIDE the UserLAnd chroot as root (or sudo).
# - It does NOT perform SAF-based writes itself; it prints the URL and SFTP hints.
# - For writing into Android/data (tech.ula) you must use an on-device file manager
#   that uses the Storage Access Framework (Solid Explorer, CX File Explorer, Total Cmd).
#
set -euo pipefail
IFS=$'\n\t'

# Default configurable variables (override via CLI)
SOURCE="."
HTTP_BIND="127.0.0.1"
HTTP_PORT=8000
USE_TAR=true
BACKGROUND=true
INSTALL_PKGS=false
STOP_HTTP=false
STOP_SSH=false
STATUS_ONLY=false
VERBOSE=false
QUIET=false
PID_DIR="/tmp/userland_transfer"
LOG_FILE="${PID_DIR}/http.log"
PID_FILE="${PID_DIR}/http.pid"
ARCHIVE_PATH=""
DEST_ANDROID_PATH="/storage/emulated/0/Android/data/tech.ula/files/storage"
SUGGEST_SSH_PORTS=(8022 22)    # common UserLAnd ssh proxy port then fallback
HOST_ROOT_ACTION=false         # dangerous; only if user explicitly passes --host-root
# End of defaults

log(){ [ "$QUIET" = false ] && printf '%s\n' "$*"; }
vlog(){ [ "$VERBOSE" = true ] && printf '[v] %s\n' "$*"; }
die(){ printf 'ERROR: %s\n' "$*" >&2; exit 1; }

usage(){
  cat <<'USAGE'
userland_transfer_universal.sh - dynamic UserLAnd -> Android transfer helper

Options:
  --source PATH        Path (file or directory) to share (default: .)
  --port PORT          HTTP port to serve on (default: 8000)
  --bind ADDR          HTTP bind address (default: 127.0.0.1)
  --no-tar             Serve directory instead of creating tar.gz
  --fg                 Run HTTP server in foreground (blocks)
  --install            Attempt to apt install python3 & openssh-server & curl & nano
  --stop               Stop background HTTP server
  --stop-sshd          Stop sshd (best-effort)
  --status             Print status and exit
  --verbose            Verbose output
  --quiet              Minimal output
  --host-root          (DANGEROUS) attempt direct copy into Android destination (only if you truly have host root)
  -h, --help           Show this help

Examples:
  ./userland_transfer_universal.sh --install --source /tmp/mydata
  ./userland_transfer_universal.sh --source /tmp/userland-share-20250901.tar.gz
  ./userland_transfer_universal.sh --source /storage/emulated/0/... --no-tar
  ./userland_transfer_universal.sh --stop
USAGE
  exit 1
}

# --- arg parsing ---
while [[ $# -gt 0 ]]; do
  case "$1" in
    --source) SOURCE="$2"; shift 2;;
    --port) HTTP_PORT="$2"; shift 2;;
    --bind) HTTP_BIND="$2"; shift 2;;
    --no-tar) USE_TAR=false; shift;;
    --fg) BACKGROUND=false; shift;;
    --install) INSTALL_PKGS=true; shift;;
    --stop) STOP_HTTP=true; shift;;
    --stop-sshd) STOP_SSH=true; shift;;
    --status) STATUS_ONLY=true; shift;;
    --verbose) VERBOSE=true; shift;;
    --quiet) QUIET=true; shift;;
    --host-root) HOST_ROOT_ACTION=true; shift;;
    -h|--help) usage;;
    *) echo "Unknown arg: $1"; usage;;
  esac
done

mkdir -p "$PID_DIR"

# --- helpers ---
detect_pkg_mgr(){
  if command -v apt >/dev/null 2>&1; then echo "apt"
  elif command -v apt-get >/dev/null 2>&1; then echo "apt-get"
  else echo ""; fi
}

install_packages(){
  PKG=$(detect_pkg_mgr)
  if [ -z "$PKG" ]; then
    err="No apt/apt-get found; cannot install packages automatically."
    printf 'WARN: %s\n' "$err"
    return 1
  fi
  log "[+] Installing minimal packages via $PKG (may take time)"
  if [ "$PKG" = "apt" ]; then
    apt update
    DEBIAN_FRONTEND=noninteractive apt install -y --no-install-recommends python3 openssh-server curl nano || true
  else
    apt-get update
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends python3 openssh-server curl nano || true
  fi
}

# Resolve SOURCE: if user passed an Android host path that doesn't exist inside chroot,
# try common mapped locations so we can still find it.
resolve_source(){
  # Make absolute-ish
  SOURCE_ABS="$(realpath -m "$SOURCE" 2>/dev/null || echo "$SOURCE")"
  if [ -e "$SOURCE_ABS" ]; then
    SOURCE="$SOURCE_ABS"
    vlog "Source exists in-chroot: $SOURCE"
    return 0
  fi

  vlog "Source not found at $SOURCE_ABS in chroot; attempting mapped-path discovery..."
  # Common mounts in various UserLAnd/Termux setups
  candidates=(
    "/storage/internal/$(basename "$SOURCE")"
    "/storage/emulated/0/$(echo "$SOURCE" | sed 's/^\/storage\/emulated\/0\///')"
    "/data/media/0/$(basename "$SOURCE")"
    "/mnt/sdcard/$(basename "$SOURCE")"
    "/sdcard/$(basename "$SOURCE")"
    "/storage/$(whoami)/$(basename "$SOURCE")"
    "/tmp/$(basename "$SOURCE")"
  )
  for c in "${candidates[@]}"; do
    if [ -e "$c" ]; then
      SOURCE="$(realpath -m "$c")"
      log "[+] Mapped path found: $SOURCE"
      return 0
    fi
  done

  # fallback: search entire chroot for a matching filename (limit depth)
  base="$(basename "$SOURCE")"
  log "[+] Full search for \"$base\" (this may be slow)..."
  found="$(find / -maxdepth 6 -type f -iname "$base" 2>/dev/null | head -n1 || true)"
  if [ -n "$found" ]; then
    SOURCE="$found"
    log "[+] Found candidate: $SOURCE"
    return 0
  fi

  # not found
  die "Source not found in chroot or common mapped paths: attempted: $SOURCE_ABS"
}

start_sshd(){
  vlog "Starting sshd (best-effort)..."
  if command -v service >/dev/null 2>&1 && [ -f /etc/init.d/ssh ]; then
    service ssh start 2>/dev/null || /etc/init.d/ssh start 2>/dev/null || true
  elif command -v /usr/sbin/sshd >/dev/null 2>&1; then
    /usr/sbin/sshd 2>/dev/null || true
  else
    printf 'WARN: sshd not available; run with --install to try installing openssh-server\n'
    return 1
  fi
  sleep 0.5
  return 0
}

find_sshd_info(){
  if command -v ss >/dev/null 2>&1; then
    ss -tlnp 2>/dev/null | grep -E 'sshd|:22|:8022' || true
  elif command -v netstat >/dev/null 2>&1; then
    netstat -tlnp 2>/dev/null | grep -E 'sshd|:22|:8022' || true
  else
    ps aux | grep sshd | grep -v grep || true
  fi
}

create_archive(){
  if [ ! -e "$SOURCE" ]; then die "create_archive: source does not exist: $SOURCE"; fi
  base="$(basename "$SOURCE")"
  ts="$(date +%Y%m%d_%H%M%S)"
  ARCHIVE_PATH="/tmp/${base%/}-${ts}.tar.gz"
  if [ -d "$SOURCE" ]; then
    if command -v pigz >/dev/null 2>&1; then
      vlog "Using pigz for parallel compression"
      tar -C "$(dirname "$SOURCE")" -cf - "$(basename "$SOURCE")" | pigz -9 > "$ARCHIVE_PATH"
    else
      tar -C "$(dirname "$SOURCE")" -czf "$ARCHIVE_PATH" "$(basename "$SOURCE")"
    fi
  else
    # if source is already a file and looks like an archive, copy into /tmp for serving
    cp -a "$SOURCE" "$ARCHIVE_PATH"
  fi
  log "[+] Archive ready: $ARCHIVE_PATH"
}

start_http(){
  if ! command -v python3 >/dev/null 2>&1; then
    die "python3 not present in chroot. Run with --install or install python3."
  fi

  if [ "$USE_TAR" = true ]; then
    [ -n "$ARCHIVE_PATH" ] || die "ARCHIVE_PATH not set"
    serve_dir="$(dirname "$ARCHIVE_PATH")"
    serve_name="$(basename "$ARCHIVE_PATH")"
  else
    [ -d "$SOURCE" ] || die "--no-tar requires source to be a directory"
    serve_dir="$SOURCE"
    serve_name=""
  fi

  # If a PID is present and running, report and exit
  if [ -f "$PID_FILE" ]; then
    oldpid="$(cat "$PID_FILE" 2>/dev/null || true)"
    if [ -n "$oldpid" ] && kill -0 "$oldpid" 2>/dev/null; then
      log "[+] HTTP server already running (PID $oldpid)  URL: http://${HTTP_BIND}:${HTTP_PORT}/${serve_name}"
      return 0
    else
      rm -f "$PID_FILE"
    fi
  fi

  # Start background or foreground
  if [ "$BACKGROUND" = true ]; then
    cd "$serve_dir" || die "cannot cd to $serve_dir"
    nohup python3 -m http.server "$HTTP_PORT" --bind "$HTTP_BIND" > "$LOG_FILE" 2>&1 &
    echo $! > "$PID_FILE"
    sleep 0.2
    pid="$(cat "$PID_FILE" 2>/dev/null || true)"
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      log "[+] HTTP server started (bg) at http://${HTTP_BIND}:${HTTP_PORT}/${serve_name}"
      log "    PID: $pid   LOG: $LOG_FILE"
      return 0
    else
      die "Failed to start HTTP server; see $LOG_FILE"
    fi
  else
    cd "$serve_dir" || die "cannot cd to $serve_dir"
    log "[+] Starting HTTP server (foreground) at http://${HTTP_BIND}:${HTTP_PORT}/ (Ctrl-C to stop)"
    python3 -m http.server "$HTTP_PORT" --bind "$HTTP_BIND"
  fi
}

stop_http(){
  if [ -f "$PID_FILE" ]; then
    pid="$(cat "$PID_FILE" 2>/dev/null || true)"
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      kill "$pid" && rm -f "$PID_FILE"
      log "[+] Stopped HTTP server (PID $pid)"
      return 0
    else
      rm -f "$PID_FILE"
    fi
  fi
  # fallback
  pkill -f "python3 -m http.server" || true
  log "[+] Stopped any python3 simple http.server (pkill fallback)"
}

stop_sshd_action(){
  if command -v service >/dev/null 2>&1 && [ -f /etc/init.d/ssh ]; then
    service ssh stop 2>/dev/null || /etc/init.d/ssh stop 2>/dev/null || true
  else
    pkill -f sshd || true
  fi
  log "[+] stop_sshd_action completed (best-effort)"
}

status_report(){
  echo "=== userland_transfer_universal status ==="
  printf "User: %s\n" "$(whoami)"
  printf "CWD: %s\n" "$(pwd)"
  printf "Source (resolved): %s\n" "$SOURCE"
  printf "Mode: %s\n" "$([ "$USE_TAR" = true ] && echo "archive (tar.gz)" || echo "serve directory")"
  [ -n "$ARCHIVE_PATH" ] && printf "Archive: %s\n" "$ARCHIVE_PATH" || true
  printf "HTTP: %s:%s\n" "$HTTP_BIND" "$HTTP_PORT"
  if [ -f "$PID_FILE" ]; then
    pid="$(cat "$PID_FILE" 2>/dev/null || true)"
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      printf "HTTP server: running (PID %s) log=%s\n" "$pid" "$LOG_FILE"
    else
      printf "HTTP server: PID file present but process not running. PID_FILE=%s\n" "$PID_FILE"
    fi
  else
    if command -v ss >/dev/null 2>&1; then ss -tlnp | grep ":${HTTP_PORT}" || true; else printf "HTTP: no PID file and no ss/netstat to check\n"; fi
  fi
  echo
  echo "sshd (chroot) listeners:"
  find_sshd_info || true
  echo
  echo "Suggested SFTP: connect from Android file manager to 127.0.0.1 on ports: ${SUGGEST_SSH_PORTS[*]}"
  echo "Target Android app-storage path (for SAF write): $DEST_ANDROID_PATH"
  echo "Logs/piddir: $PID_DIR"
  echo "==========================================="
}

# Host-root direct copy option (risky)
host_root_copy(){
  if [ "$HOST_ROOT_ACTION" != true ]; then die "host_root_copy called without --host-root"; fi
  if [ ! -e "$SOURCE" ]; then die "host_root_copy: source missing: $SOURCE"; fi
  if [ ! -d "$DEST_ANDROID_PATH" ]; then die "Destination path not present: $DEST_ANDROID_PATH"; fi
  log "[!] Attempting direct host copy (you claimed host/root). This may break SELinux/UID mappings. Proceeding..."
  cp -a "$SOURCE"/* "$DEST_ANDROID_PATH"/ || die "cp failed"
  log "[+] copy complete. You may need to chown/chmod to match app UID (dangerous)."
}

# --- main flow ---
# Resolve source (if not found, try mapped locations)
resolve_source

if [ "$STATUS_ONLY" = true ]; then
  status_report
  exit 0
fi

if [ "$STOP_HTTP" = true ] || [ "$STOP_SSH" = true ]; then
  [ "$STOP_HTTP" = true ] && stop_http
  [ "$STOP_SSH" = true ] && stop_sshd_action
  status_report
  exit 0
fi

if [ "$INSTALL_PKGS" = true ]; then
  install_packages || true
fi

# Host-root copy branch
if [ "$HOST_ROOT_ACTION" = true ]; then
  host_root_copy
  status_report
  exit 0
fi

# Ensure sshd available (best-effort) for SFTP clients
start_sshd || true

# Prepare artifact or serve directory
if [ "$USE_TAR" = true ]; then
  create_archive
else
  if [ ! -d "$SOURCE" ]; then die "--no-tar requires a directory source"; fi
  ARCHIVE_PATH=""
fi

# Start HTTP server
start_http

# Final user-facing instructions
echo
log "=== ACTION ==="
if [ "$USE_TAR" = true ]; then
  log "Download the archive on-device: http://${HTTP_BIND}:${HTTP_PORT}/$(basename "$ARCHIVE_PATH")"
else
  log "Browse directory on-device: http://${HTTP_BIND}:${HTTP_PORT}/"
fi
log "Preferred method to place into app storage on Android 10:"
log "  1) Use Solid Explorer / CX / Total Commander on your phone."
log "  2) Option A (download): open above URL in browser or Solid Explorer -> save to Downloads -> copy using 'Open document' (SAF) to:"
log "       $DEST_ANDROID_PATH"
log "  3) Option B (live): create SFTP connection to 127.0.0.1 port 8022 (try 8022 then 22). Login as linux user and copy target file -> Local -> 'Open document' -> choose $DEST_ANDROID_PATH"
log "  4) When finished, stop the HTTP server: $(basename "$0") --stop"
log ""
log "If anything fails, paste the outputs of these commands and I'll give the exact fix:"
log "  - ss -tlnp | grep -E ':${HTTP_PORT}|:22|:8022' || true"
log "  - ls -lh \"${ARCHIVE_PATH:-$SOURCE}\""
log "  - cat /tmp/userland_http.log  (if present)"
log ""
status_report