#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# userland_transfer_dynamic.sh
# Comprehensive, dynamic, variable-driven helper for UserLAnd -> Android transfers.
# - installs minimal deps (optional)
# - starts/stops sshd (for SFTP) in the chroot
# - creates a tar.gz (or serves a directory) for single-file transfer
# - starts/stops an HTTP loopback server bound to 127.0.0.1
# - records PIDs and logs in /tmp/userland_transfer for safe cleanup
#
# Example usage:
#   ./userland_transfer_dynamic.sh --install --source /tmp/mydata
#   ./userland_transfer_dynamic.sh --source /tmp/userland-share-root-20250901_084824.tar.gz
#   ./userland_transfer_dynamic.sh --source /home/user/folder --no-tar --port 9000 --fg
#   ./userland_transfer_dynamic.sh --status
#   ./userland_transfer_dynamic.sh --stop
#
set -euo pipefail
IFS=$'\n\t'

# Defaults
SOURCE="."
HTTP_PORT=8000
HTTP_BIND="127.0.0.1"
ARCHIVE_PREFIX="userland-share"
USE_TAR=true
BACKGROUND=true
INSTALL_PKGS=false
STOP_HTTP=false
STOP_SSHD=false
STATUS_ONLY=false
QUIET=false
VERBOSE=false
PID_DIR="/tmp/userland_transfer"
SSH_PORTS_TO_SUGGEST=(8022 22)

log(){ [ "$QUIET" = false ] && printf '%s\n' "$*"; }
vlog(){ [ "$VERBOSE" = true ] && printf '[v] %s\n' "$*"; }
err(){ printf 'ERROR: %s\n' "$*" >&2; }

usage(){
  cat <<'USAGE'
Usage: userland_transfer_dynamic.sh [options]
Options:
  --source PATH        Path (file or directory) you want to share (default: .)
  --port PORT          HTTP port to serve archive/dir (default: 8000)
  --bind ADDR          HTTP bind address (default: 127.0.0.1)
  --no-tar             Serve directory over HTTP instead of creating a tar.gz
  --background / --fg  Background HTTP server (default background)
  --install            Attempt apt install of openssh-server python3 curl nano
  --stop               Stop background HTTP server
  --stop-sshd          Stop sshd (best-effort)
  --status             Print status: sshd listening ports, http server PID/log, archive path
  --verbose            Verbose logging
  --quiet              Minimal output
  -h, --help           Show this help
Examples:
  ./userland_transfer_dynamic.sh --install --source /tmp/mydata
  ./userland_transfer_dynamic.sh --source /tmp/mydata --no-tar --port 9000 --fg
USAGE
  exit 1
}

# Parse args
while [[ $# -gt 0 ]]; do
  case "$1" in
    --source) SOURCE="$2"; shift 2;;
    --port) HTTP_PORT="$2"; shift 2;;
    --bind) HTTP_BIND="$2"; shift 2;;
    --no-tar) USE_TAR=false; shift;;
    --background) BACKGROUND=true; shift;;
    --fg) BACKGROUND=false; shift;;
    --install) INSTALL_PKGS=true; shift;;
    --stop) STOP_HTTP=true; shift;;
    --stop-sshd) STOP_SSHD=true; shift;;
    --status) STATUS_ONLY=true; shift;;
    --verbose) VERBOSE=true; shift;;
    --quiet) QUIET=true; shift;;
    -h|--help) usage;;
    *) err "Unknown option: $1"; usage;;
  esac
done

mkdir -p "$PID_DIR"
PID_FILE="$PID_DIR/http.pid"
LOG_FILE="$PID_DIR/http.log"
ARCHIVE_PATH=""

detect_pkg_mgr(){
  if command -v apt >/dev/null 2>&1; then echo "apt"
  elif command -v apt-get >/dev/null 2>&1; then echo "apt-get"
  else echo ""; fi
}

install_packages(){
  PKG_MGR=$(detect_pkg_mgr)
  if [ -z "$PKG_MGR" ]; then
    err "No apt/apt-get found; skipping install step. Ensure openssh-server & python3 exist."
    return 1
  fi
  log "[+] Installing minimal packages via $PKG_MGR (may take time)"
  if [ "$PKG_MGR" = "apt" ]; then
    apt update
    DEBIAN_FRONTEND=noninteractive apt install -y --no-install-recommends openssh-server python3 curl nano || true
  else
    apt-get update
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends openssh-server python3 curl nano || true
  fi
  return 0
}

start_sshd(){
  vlog "attempting to start sshd"
  if command -v service >/dev/null 2>&1 && [ -f /etc/init.d/ssh ]; then
    service ssh start || /etc/init.d/ssh start || true
  elif command -v /usr/sbin/sshd >/dev/null 2>&1; then
    /usr/sbin/sshd || true
  else
    err "sshd not found. Run with --install or apt install openssh-server."
    return 1
  fi
  sleep 0.6
  return 0
}

stop_sshd(){
  vlog "attempting to stop sshd"
  if command -v service >/dev/null 2>&1 && [ -f /etc/init.d/ssh ]; then
    service ssh stop || /etc/init.d/ssh stop || true
  else
    pkill -f sshd || true
  fi
}

find_sshd_listeners(){
  if command -v ss >/dev/null 2>&1; then
    ss -tlnp 2>/dev/null | grep -E 'sshd|:22|:8022' || true
  elif command -v netstat >/dev/null 2>&1; then
    netstat -tlnp 2>/dev/null | grep -E 'sshd|:22|:8022' || true
  else
    ps aux | grep sshd | grep -v grep || true
  fi
}

create_archive(){
  if [ ! -e "$SOURCE" ]; then err "Source does not exist: $SOURCE"; return 2; fi
  base=$(basename "$SOURCE")
  ts=$(date +%Y%m%d_%H%M%S)
  if command -v pigz >/dev/null 2>&1; then
    ARCHIVE_PATH="/tmp/${ARCHIVE_PREFIX}-${base}-${ts}.tar.gz"
    tar -C "$(dirname "$SOURCE")" -cf - "$(basename "$SOURCE")" | pigz -9 > "$ARCHIVE_PATH"
  else
    ARCHIVE_PATH="/tmp/${ARCHIVE_PREFIX}-${base}-${ts}.tar.gz"
    tar -C "$(dirname "$SOURCE")" -czf "$ARCHIVE_PATH" "$(basename "$SOURCE")"
  fi
  log "[+] Archive created: $ARCHIVE_PATH"
  return 0
}

start_http_server(){
  cd /tmp || true
  if [ "$USE_TAR" = true ]; then
    serve_target="$(basename "$ARCHIVE_PATH")"
    serve_dir="/tmp"
    [ -n "$ARCHIVE_PATH" ] || { err "Archive not created"; return 3; }
  else
    serve_dir="$(realpath "$SOURCE" 2>/dev/null || echo "$SOURCE")"
    serve_target="(directory) $(basename "$serve_dir")"
    if [ ! -d "$serve_dir" ]; then err "Source must be directory to --no-tar"; return 4; fi
  fi

  if [ "$BACKGROUND" = true ]; then
    if [ -f "$PID_FILE" ]; then
      oldpid=$(cat "$PID_FILE" 2>/dev/null || true)
      if [ -n "$oldpid" ] && kill -0 "$oldpid" 2>/dev/null; then
        log "[+] HTTP server already running with PID $oldpid (log: $LOG_FILE)"; return 0
      else
        rm -f "$PID_FILE"
      fi
    fi
    (
      cd "$serve_dir" || exit 0
      nohup python3 -m http.server "$HTTP_PORT" --bind "$HTTP_BIND" >"$LOG_FILE" 2>&1 &
      echo $! > "$PID_FILE"
    )
    sleep 0.2
    pid=$(cat "$PID_FILE" 2>/dev/null || "")
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      log "[+] HTTP server started (background) at http://${HTTP_BIND}:${HTTP_PORT}/${serve_target}"
      log "    PID: $pid  LOG: $LOG_FILE  PID_FILE: $PID_FILE"
      return 0
    else
      err "Failed to start background HTTP server; check $LOG_FILE"; return 3
    fi
  else
    cd "$serve_dir" || return 1
    log "[+] Starting HTTP server (foreground) at http://${HTTP_BIND}:${HTTP_PORT}/ (CTRL-C to stop)"
    python3 -m http.server "$HTTP_PORT" --bind "$HTTP_BIND"
  fi
}

stop_http_server(){
  if [ -f "$PID_FILE" ]; then
    pid=$(cat "$PID_FILE" 2>/dev/null || true)
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      kill "$pid" && rm -f "$PID_FILE"
      log "[+] Stopped HTTP server PID $pid"; return 0
    else
      rm -f "$PID_FILE"
      log "[+] No running HTTP server found (cleaned PID file)"; return 0
    fi
  else
    pkill -f "python3 -m http.server" || true
    log "[+] Stopped HTTP server (pkill fallback)."; return 0
  fi
}

status_report(){
  echo "=== userland_transfer_dynamic status ==="
  echo "User: $(whoami)   CWD: $(pwd)"
  echo
  echo "Source: $SOURCE"
  if [ "$USE_TAR" = true ]; then
    echo "Mode: archive (tar.gz)"
    echo "Archive path: ${ARCHIVE_PATH:-(not created)}"
  else
    echo "Mode: directory serve"
    echo "Serving path: $(realpath "$SOURCE" 2>/dev/null || echo '(unknown)')"
  fi
  echo
  echo "HTTP server bind: ${HTTP_BIND}:${HTTP_PORT}"
  if [ -f "$PID_FILE" ]; then
    pid=$(cat "$PID_FILE" 2>/dev/null || echo "")
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      echo "HTTP: running (PID $pid)  Log: $LOG_FILE"
    else
      echo "HTTP: PID file present but process not running. PID_FILE: $PID_FILE"
    fi
  else
    if command -v ss >/dev/null 2>&1; then ss -tlnp | grep ":${HTTP_PORT}" || true
    elif command -v netstat >/dev/null 2>&1; then netstat -tlnp | grep ":${HTTP_PORT}" || true
    else echo "HTTP: no PID file and no ss/netstat to check."; fi
  fi
  echo
  echo "sshd listeners (chroot):"
  find_sshd_listeners || true
  echo
  echo "Suggested SFTP connect points on-device (try in file manager SFTP):"
  for p in "${SSH_PORTS_TO_SUGGEST[@]}"; do
    printf "  sftp://127.0.0.1:%s   (user: %s, password: your linux password)\n" "$p" "$(whoami)"
  done
  echo
  echo "Logs and pid files under: $PID_DIR"
  echo "========================================="
}

# Normalize flags
STOP_HTTP=${STOP_HTTP:-false}
STATUS_ONLY=${STATUS_ONLY:-false}

# Make SOURCE absolute if possible
if [ -n "${SOURCE:-}" ]; then
  SOURCE="$(realpath -m "$SOURCE" 2>/dev/null || echo "$SOURCE")"
fi

main(){
  if [ "$STATUS_ONLY" = true ]; then status_report; exit 0; fi
  if [ "$STOP_HTTP" = true ] || [ "$STOP_SSHD" = true ]; then
    [ "$STOP_HTTP" = true ] && stop_http_server || true
    [ "$STOP_SSHD" = true ] && stop_sshd || true
    status_report; exit 0
  fi
  if [ "$INSTALL_PKGS" = true ]; then install_packages || true; fi
  start_sshd || true
  sleep 0.3
  if [ "$USE_TAR" = true ]; then create_archive || true; else [ -d "$SOURCE" ] || { err "Source must be an existing directory when using --no-tar"; exit 4; }; fi
  start_http_server || { err "HTTP server did not start; aborting"; status_report; exit 5; }
  status_report
  if [ "$BACKGROUND" = false ]; then exit 0; fi
  echo
  log "[ACTION]"
  if [ "$USE_TAR" = true ]; then
    log "Download archive on-device: http://${HTTP_BIND}:${HTTP_PORT}/$(basename "$ARCHIVE_PATH")"
  else
    log "Access directory listing at: http://${HTTP_BIND}:${HTTP_PORT}/"
  fi
  log "Or use SFTP in a file manager: host=127.0.0.1, port=8022 (or 22), user=$(whoami)"
  log "Once downloaded, use a SAF-capable file manager (Solid Explorer, CX) -> Copy -> Local -> 'Open document' -> choose Android/data/tech.ula/files/storage -> Paste"
}

main "$@"