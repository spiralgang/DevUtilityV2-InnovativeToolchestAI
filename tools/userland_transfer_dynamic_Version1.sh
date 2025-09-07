#!/usr/bin/env bash
# userland_transfer_dynamic.sh
# Comprehensive, dynamic, variable-driven helper for UserLAnd -> Android transfers.
# - installs minimal deps (optional)
# - starts/stops sshd (for SFTP) in the chroot
# - creates a tar.gz (or serves a directory) for single-file transfer
# - starts/stops an HTTP loopback server bound to 127.0.0.1
# - records PIDs and logs in /tmp for safe cleanup
#
# Usage (examples):
#   ./userland_transfer_dynamic.sh --source /home/user/data                # default: create tar, start sshd, start http on 8000 (background)
#   ./userland_transfer_dynamic.sh --source /home/user/data --port 9000    # use HTTP port 9000
#   ./userland_transfer_dynamic.sh --source /home/user/data --no-tar      # serve the directory instead of archiving
#   ./userland_transfer_dynamic.sh --status                                # check sshd/http status and artifact paths
#   ./userland_transfer_dynamic.sh --stop                                  # stop background HTTP server (and optionally sshd with --stop-sshd)
#   ./userland_transfer_dynamic.sh --install                               # only run install step (openssh-server, python3, curl, nano)
#
# Notes:
# - Script is safe to run in UserLAnd chroot as root (intended).
# - It binds HTTP server to 127.0.0.1 only (loopback). Use an Android file manager to connect to SFTP at 127.0.0.1:8022 (or 22).
# - All PIDs/logs stored under /tmp/userland_transfer_* for easy inspection and cleanup.
#
set -euo pipefail
IFS=$'\n\t'

# Default variables (dynamic & user-overridable)
SOURCE="."                 # path to file/dir to transfer
HTTP_PORT=8000
HTTP_BIND="127.0.0.1"
ARCHIVE_PREFIX="userland-share"
USE_TAR=true
BACKGROUND=true
INSTALL_PKGS=false
STOP_SSHD=false
QUIET=false
VERBOSE=false
PID_DIR="/tmp/userland_transfer"
SSH_PORTS_TO_SUGGEST=(8022 22)

# helpers
log(){ if [ "$QUIET" = false ]; then printf '%s\n' "$*"; fi }
vlog(){ if [ "$VERBOSE" = true ]; then printf '[v] %s\n' "$*"; fi }
err(){ printf 'ERROR: %s\n' "$*" >&2; }

usage(){
  cat <<'USAGE'
Usage: userland_transfer_dynamic.sh [options]

Options:
  --source PATH        Path (file or directory) you want to share (default: .)
  --port PORT          HTTP port to serve archive/dir (default: 8000)
  --bind ADDR          HTTP bind address (default: 127.0.0.1)
  --no-tar             Serve directory over HTTP instead of creating a tar.gz
  --background / --fg  Background HTTP server (default) or run in foreground
  --install            Attempt to install openssh-server python3 curl nano (uses apt/apt-get)
  --stop               Stop background HTTP server (if running). Use --stop-sshd to stop ssh too.
  --stop-sshd          Stop sshd (service ssh stop or pkill)
  --status             Print status: sshd listening ports, http server PID/log, archive path
  --verbose            More verbose logging
  --quiet              Minimal output (status still printed)
  -h, --help           Show this help

Examples:
  ./userland_transfer_dynamic.sh --source /home/user/data
  ./userland_transfer_dynamic.sh --source /home/user/data --no-tar --port 9000 --fg
  ./userland_transfer_dynamic.sh --install
  ./userland_transfer_dynamic.sh --stop --stop-sshd
USAGE
  exit 1
}

# parse args
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

# ensure PID_DIR exists
mkdir -p "$PID_DIR"

PID_FILE="$PID_DIR/http.pid"
LOG_FILE="$PID_DIR/http.log"
ARCHIVE_PATH=""

# Detect package manager
detect_pkg_mgr(){
  if command -v apt >/dev/null 2>&1; then
    echo "apt"
  elif command -v apt-get >/dev/null 2>&1; then
    echo "apt-get"
  else
    echo ""
  fi
}

# Install minimal packages (if requested)
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

# Start sshd (best-effort)
start_sshd(){
  vlog "attempting to start sshd"
  if command -v service >/dev/null 2>&1 && [ -f /etc/init.d/ssh ]; then
    service ssh start || /etc/init.d/ssh start || true
  elif command -v /usr/sbin/sshd >/dev/null 2>&1; then
    /usr/sbin/sshd || true
  else
    err "sshd not found. Try: apt install -y openssh-server (run with --install)"
    return 1
  fi
  sleep 0.6
  return 0
}

# Stop sshd (best-effort)
stop_sshd(){
  vlog "attempting to stop sshd"
  if command -v service >/dev/null 2>&1 && [ -f /etc/init.d/ssh ]; then
    service ssh stop || /etc/init.d/ssh stop || true
  else
    pkill -f sshd || true
  fi
}

# Find listening sshd ports (ss or netstat)
find_sshd_listeners(){
  if command -v ss >/dev/null 2>&1; then
    ss -tlnp 2>/dev/null | grep -E 'sshd|:22|:8022' || true
  elif command -v netstat >/dev/null 2>&1; then
    netstat -tlnp 2>/dev/null | grep -E 'sshd|:22|:8022' || true
  else
    ps aux | grep sshd | grep -v grep || true
  fi
}

# Create tar archive (uses pigz if present)
create_archive(){
  if [ ! -e "$SOURCE" ]; then
    err "Source does not exist: $SOURCE"
    return 2
  fi
  base=$(basename "$SOURCE")
  ts=$(date +%Y%m%d_%H%M%S)
  if command -v pigz >/dev/null 2>&1; then
    ARCHIVE_PATH="/tmp/${ARCHIVE_PREFIX}-${base}-${ts}.tar.gz"
    vlog "Using pigz for faster compression"
    tar -C "$(dirname "$SOURCE")" -cf - "$(basename "$SOURCE")" | pigz -9 > "$ARCHIVE_PATH"
  else
    ARCHIVE_PATH="/tmp/${ARCHIVE_PREFIX}-${base}-${ts}.tar.gz"
    tar -C "$(dirname "$SOURCE")" -czf "$ARCHIVE_PATH" "$(basename "$SOURCE")"
  fi
  log "[+] Archive created: $ARCHIVE_PATH"
  return 0
}

# Start HTTP server (background/foreground)
start_http_server(){
  # If background requested, ensure PID file doesn't already exist or is stale
  cd /tmp || true
  if [ "$USE_TAR" = true ]; then
    serve_target="$(basename "$ARCHIVE_PATH")"
    serve_dir="/tmp"
  else
    # serve directory in-place
    serve_target="(directory) $(realpath "$SOURCE")"
    serve_dir="$(realpath "$SOURCE")"
  fi

  if [ "$BACKGROUND" = true ]; then
    if [ -f "$PID_FILE" ]; then
      oldpid=$(cat "$PID_FILE" 2>/dev/null || true)
      if [ -n "$oldpid" ] && kill -0 "$oldpid" 2>/dev/null; then
        log "[+] HTTP server already running with PID $oldpid (log: $LOG_FILE)"
        return 0
      else
        rm -f "$PID_FILE"
      fi
    fi
    # start server
    (
      cd "$serve_dir" || exit 0
      # nohup + redirect to log file
      nohup python3 -m http.server "$HTTP_PORT" --bind "$HTTP_BIND" >"$LOG_FILE" 2>&1 &
      echo $! > "$PID_FILE"
    )
    sleep 0.2
    pid=$(cat "$PID_FILE" 2>/dev/null || echo "")
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      log "[+] HTTP server started (background) at http://${HTTP_BIND}:${HTTP_PORT}/${serve_target}"
      log "    PID: $pid  LOG: $LOG_FILE  PID_FILE: $PID_FILE"
      return 0
    else
      err "Failed to start background HTTP server; check $LOG_FILE"
      return 3
    fi
  else
    # foreground mode: run in current shell (blocking)
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
      log "[+] Stopped HTTP server PID $pid"
      return 0
    else
      rm -f "$PID_FILE"
      log "[+] No running HTTP server found (cleaned PID file)"
      return 0
    fi
  else
    # try pkill fallback
    pkill -f "python3 -m http.server" || true
    log "[+] Stopped HTTP server (pkill fallback)."
    return 0
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
    # check for listening socket
    if command -v ss >/dev/null 2>&1; then
      ss -tlnp | grep ":${HTTP_PORT}" || true
    elif command -v netstat >/dev/null 2>&1; then
      netstat -tlnp | grep ":${HTTP_PORT}" || true
    else
      echo "HTTP: no PID file and no ss/netstat on system to check."
    fi
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

# main orchestrator
main(){
  # STATUS_ONLY handling
  if [ "${STATUS_ONLY:-false}" = true ]; then
    status_report
    exit 0
  fi

  # STOP handling
  if [ "${STOP_HTTP:-false}" = true ] || [ "${STOP_SSHD:-false}" = true ]; then
    if [ "${STOP_HTTP:-false}" = true ]; then
      stop_http_server || true
    fi
    if [ "${STOP_SSHD:-false}" = true ]; then
      stop_sshd || true
    fi
    status_report
    exit 0
  fi

  # INSTALL handling
  if [ "$INSTALL_PKGS" = true ]; then
    install_packages || true
  fi

  # Start sshd (best-effort)
  start_sshd || true
  sleep 0.4

  # prepare artifact
  if [ "$USE_TAR" = true ]; then
    create_archive || true
  else
    # validate source exists and is a dir
    if [ ! -d "$SOURCE" ]; then
      err "Source must be an existing directory when using --no-tar"
      exit 4
    fi
    ARCHIVE_PATH=""
  fi

  # start http server
  start_http_server || {
    err "HTTP server did not start; aborting with status report."
    status_report
    exit 5
  }

  # final status
  status_report

  if [ "$BACKGROUND" = false ]; then
    # foreground server will block; nothing else
    exit 0
  fi

  # background mode: print short instructions
  echo
  log "[ACTION]"
  if [ "$USE_TAR" = true ]; then
    log "Download archive on-device via browser: http://${HTTP_BIND}:${HTTP_PORT}/$(basename "$ARCHIVE_PATH")"
  else
    log "Access directory listing at: http://${HTTP_BIND}:${HTTP_PORT}/"
  fi
  log "Or use SFTP in a file manager: host=127.0.0.1, port=8022 (or 22), user=$(whoami), password=your linux password"
  log "Once downloaded to Downloads, use a SAF-capable file manager (Solid Explorer, CX, Total Commander) -> Copy -> Local -> 'Open document' -> choose Android/data/tech.ula/files/storage -> Paste"
}

# If status-only flag set? detect variable presence earlier.
# Normalize optional flags that might be undefined (Bash collapse)
STOP_HTTP=${STOP_HTTP:-false}
STATUS_ONLY=${STATUS_ONLY:-false}

# Validate source path to absolute if possible
if [ -n "${SOURCE:-}" ]; then
  SOURCE="$(realpath -m "$SOURCE" 2>/dev/null || echo "$SOURCE")"
fi

main "$@"