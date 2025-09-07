#!/usr/bin/env bash
#
# SCRIPTTHESE_FINAL.sh
# Single-file, resilient recovery helper for chrooted UserLAnd environments.
# - Attempts best-effort archival of chroot-readable locations (including /usr, /home, /tmp).
# - Always prints exact *host-shell* su -c tar fallback commands to run on the real device host
#   (Termux `su`, CX root terminal, or adb shell with root) when /data app sandbox is protected.
#
# Usage (inside chroot):
#   1) save to ~/SCRIPTTHESE_FINAL.sh
#   2) chmod +x ~/SCRIPTTHESE_FINAL.sh
#   3) sudo bash ~/SCRIPTTHESE_FINAL.sh --outdir /tmp/recover --serve --port 8000
#
set +e
IFS=$'\n\t'

OUT_DIR="${OUT_DIR:-/tmp/recovery_output}"
NAME_PREFIX="${NAME_PREFIX:-techula_recover}"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
ARCHIVE_NAME="${ARCHIVE_NAME:-${NAME_PREFIX}_${TIMESTAMP}.tgz}"
ARCHIVE_PATH="${OUT_DIR}/${ARCHIVE_NAME}"
LOG_DIR="${OUT_DIR}/logs"
PORT="${PORT:-8000}"
BIND_ADDR="${BIND_ADDR:-127.0.0.1}"
SERVE=false
FORCE=false
VERBOSE=false

# Candidate list — trimmed to highest-probability chroot-readable places
CANDIDATES=(
  "/usr/bin"
  "/usr"
  "/root"
  "/home"
  "/tmp"
  "/var"
)

usage(){
  cat <<USAGE
SCRIPTTHESE_FINAL.sh - best-effort chroot recovery (non-destructive)
Options:
  --outdir PATH      Output dir (default: $OUT_DIR)
  --serve            Start HTTP server to serve archive (bind $BIND_ADDR:$PORT)
  --port N           HTTP port (default: $PORT)
  --bind ADDR        Bind address (default: $BIND_ADDR)
  --force            Don't prompt
  --verbose          Verbose
  -h, --help         Show this help
USAGE
  exit 1
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --outdir) OUT_DIR="$2"; ARCHIVE_PATH="${OUT_DIR}/${ARCHIVE_NAME}"; LOG_DIR="${OUT_DIR}/logs"; shift 2;;
    --serve) SERVE=true; shift;;
    --port) PORT="$2"; shift 2;;
    --bind) BIND_ADDR="$2"; shift 2;;
    --force) FORCE=true; shift;;
    --verbose) VERBOSE=true; shift;;
    -h|--help) usage;;
    *) echo "Unknown arg: $1"; usage;;
  esac
done

mkdir -p "$OUT_DIR" "$LOG_DIR"

log(){ printf '%s\n' "$*"; }
v(){ $VERBOSE && printf '[v] %s\n' "$*"; }
err(){ printf 'ERR: %s\n' "$*" >&2; }

# detect sudo if not running as uid 0
SUDO=""
if [ "$(id -u)" -ne 0 ] && command -v sudo >/dev/null 2>&1; then
  SUDO="sudo"
fi

log "Starting SCRIPTTHESE_FINAL at $(date)"
log "Output dir: $OUT_DIR"
v "Candidate paths: ${CANDIDATES[*]}"

# quick diagnostics for mounting & common paths
log "=== mounts & common host-visible storage paths ==="
mount | egrep -i 'sdcard|emulated|storage|media|fuse' 2>/dev/null || true
ls -ld /storage /storage/emulated /sdcard /storage/internal /data/media/0 2>/dev/null || true
echo ""

# probe candidate list and build source set
SOURCES=()
for p in "${CANDIDATES[@]}"; do
  if [ -e "$p" ]; then
    SOURCES+=("$p")
    v "Found: $p"
  else
    v "Missing: $p"
  fi
done

if [ ${#SOURCES[@]} -eq 0 ]; then
  err "No chroot-readable candidate sources found. Exiting."
  exit 2
fi

# sample listing of tech.ula if present (non-destructive)
for must in /data/data/tech.ula /data/user/0/tech.ula; do
  if [ -e "$must" ]; then
    log "Note: host app path exists (but likely unreadable from chroot): $must"
    ls -ldZ "$must" 2>/dev/null || ls -ld "$must" 2>/dev/null || true
  fi
done

# prompt with estimated sample DU (best-effort)
est=$(du -sh "${SOURCES[@]}" 2>/dev/null | tail -n1 | awk '{print $1}' || echo "unknown")
log "Estimated sample size (du -sh last line): $est"
if [ "$FORCE" != true ]; then
  read -p "Proceed to create best-effort archive of chroot-readable locations? (y/N) " yn
  case "$yn" in [Yy]*) ;; *) log "Aborted."; exit 0;; esac
fi

# Write NUL-separated list and run tar from / (best-effort)
TMP_LIST="${LOG_DIR}/sources.null"
: > "$TMP_LIST"
for s in "${SOURCES[@]}"; do
  rel="${s#/}"
  [ -n "$rel" ] || rel='.'
  printf '%s\0' "$rel" >> "$TMP_LIST"
done
v "sources list -> $TMP_LIST"

log "Creating combined archive (best-effort) -> $ARCHIVE_PATH"
tar_cmd=(tar -C / --null -T "$TMP_LIST" --warning=no-file-changed --ignore-failed-read -czf "$ARCHIVE_PATH")
v "Running: ${tar_cmd[*]}"
$SUDO "${tar_cmd[@]}" 2> "${LOG_DIR}/tar.err"
tar_rc=$?
if [ $tar_rc -ne 0 ]; then
  err "tar returned $tar_rc; head of $LOG_DIR/tar.err:"
  sed -n '1,200p' "${LOG_DIR}/tar.err" || true
fi

if [ -f "$ARCHIVE_PATH" ]; then
  log "Archive created: $ARCHIVE_PATH"
  du -h "$ARCHIVE_PATH" || true
  log "Sample contents (first 120 lines):"
  tar -tzf "$ARCHIVE_PATH" | sed -n '1,120p' > "${LOG_DIR}/manifest.txt" 2>/dev/null || true
  sed -n '1,120p' "${LOG_DIR}/manifest.txt" || true
else
  err "Archive was not created or too small. See ${LOG_DIR}/tar.err"
fi

# Attempt copy to common host-visible locations
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

# If archive absent or chroot couldn't read /data, print exact host commands
HOST_ARCHIVE_NAME="techula-backup-${TIMESTAMP}.tgz"
HOST_SNIPPET="${LOG_DIR}/host-tar-snippet.txt"
cat > "$HOST_SNIPPET" <<EOF
# Run THIS ON THE ANDROID HOST (Termux 'su', CX root shell, or adb shell with root).
# Preferred: create archive into shared storage so you can fetch it from chroot or file manager.
# If your device uses /data/user/0 layout:
su -c 'tar -C /data -czf /storage/emulated/0/${HOST_ARCHIVE_NAME} data/user/0'
# Or to target only tech.ula app directory (commonly):
su -c 'tar -C /data -czf /storage/emulated/0/${HOST_ARCHIVE_NAME} data/data/tech.ula'
# After running on the host, check from the chroot:
ls -lh /storage/emulated/0/${HOST_ARCHIVE_NAME} /sdcard/${HOST_ARCHIVE_NAME} /storage/internal/${HOST_ARCHIVE_NAME}
EOF

if [ ! -f "$ARCHIVE_PATH" ] || [ "$(stat -c%s "$ARCHIVE_PATH" 2>/dev/null || echo 0)" -lt 2048 ]; then
  err "Archive missing or tiny. The host fallback is likely required. Host snippet written to: $HOST_SNIPPET"
  sed -n '1,120p' "$HOST_SNIPPET"
else
  if [ "$copied_any" = false ]; then
    log "Archive exists locally but was not copied to host-visible paths; you can fetch via HTTP server below or copy manually."
  fi
fi

# Optionally start a local HTTP server to serve OUT_DIR for on-device download (loopback)
if [ "$SERVE" = true ] && [ -f "$ARCHIVE_PATH" ]; then
  if command -v python3 >/dev/null 2>&1; then
    log "Starting HTTP server: http://${BIND_ADDR}:${PORT}/$(basename "$ARCHIVE_PATH")"
    (cd "$(dirname "$ARCHIVE_PATH")" && nohup python3 -m http.server "$PORT" --bind "$BIND_ADDR" > "${LOG_DIR}/http.out" 2>&1 & echo $! > "${LOG_DIR}/http.pid")
    sleep 0.2
    if [ -f "${LOG_DIR}/http.pid" ]; then
      pid=$(cat "${LOG_DIR}/http.pid" 2>/dev/null || echo "")
      log "HTTP server PID: $pid"
    fi
  else
    err "python3 not found; cannot serve via HTTP."
  fi
fi

log "Done. Outputs: $OUT_DIR"
log "If host tar is required, open the host snippet: $HOST_SNIPPET"
exit 0