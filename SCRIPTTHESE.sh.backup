#!/usr/bin/env bash
# SCRIPTTHESE.sh
# Consolidated, resilient recovery tool implementing the diagnostic + archive steps
# described in "SCRIPTTHESE.txt" and the conversation.
#
# Goals:
# - Run non-destructive diagnostics (mounts, storage mapping, tech.ula presence)
# - Collect as much readable data as possible from /usr and from Android app data
#   locations (/data/user/0, /data/data), including dotfiles
# - Never abort on single-file errors; skip unreadable files and continue
# - Produce a combined archive and per-source archives in /tmp (or configurable outdir)
# - Try to copy archives to common host-visible paths and/or serve them over HTTP
# - Print exact host-side su -c tar snippets to run on phone host if chroot cannot read
#
# Usage examples:
#   bash SCRIPTTHESE.sh --dry-run --verbose
#   bash SCRIPTTHESE.sh --outdir /tmp/recovery --serve --port 8000 --verbose
#   bash SCRIPTTHESE.sh --exec-host-tar --host-dest /storage/emulated/0/techula-host.tgz
#
# Notes:
# - Run from a writable location (your home or /tmp). Avoid writing edited copies to /usr/bin.
# - If "sudo: /usr/bin/env bash: command not found" appears when using sudo,
#   run the script explicitly with: bash SCRIPTTHESE.sh ...
#
set +e
IFS=$'\n\t'

### Configurable defaults (override with env or CLI)
OUT_DIR="${OUT_DIR:-/tmp/recovery_output}"
NAME_PREFIX="${NAME_PREFIX:-recovery}"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
COMBINED_ARCHIVE="${ARCHIVE_NAME:-${NAME_PREFIX}_${TIMESTAMP}.tgz}"
COMBINED_PATH="${OUT_DIR}/${COMBINED_ARCHIVE}"
PORT="${PORT:-8000}"
BIND_ADDR="${BIND_ADDR:-127.0.0.1}"
SERVE=false
FORCE=false
EXEC_HOST_TAR=false
HOST_DEST="${HOST_DEST:-/storage/emulated/0/${COMBINED_ARCHIVE}}"
VERBOSE=false
DRY_RUN=false

# Candidate roots (ordered)
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

LOG_DIR="${OUT_DIR}/logs"
MANIFEST="${OUT_DIR}/${NAME_PREFIX}_${TIMESTAMP}.manifest.txt"
TMP_LIST="${LOG_DIR}/sources_list.null"

mkdir -p "$OUT_DIR" "$LOG_DIR"

# Helper logging
log(){ printf '%s\n' "$*"; }
v(){ $VERBOSE && printf '[v] %s\n' "$*"; }
err(){ printf 'ERR: %s\n' "$*" >&2; }

usage(){
  cat <<USAGE
SCRIPTTHESE.sh - best-effort recovery & serve script
Options:
  --outdir PATH         Output directory (default: $OUT_DIR)
  --archive NAME        Archive filename (default: $COMBINED_ARCHIVE)
  --roots PATHS         Comma-separated candidate roots (override defaults)
  --serve               Start background HTTP server after archive creation
  --port N              HTTP port (default: $PORT)
  --bind ADDR           HTTP bind address (default: $BIND_ADDR)
  --force               Don't prompt before large work
  --exec-host-tar       Attempt to run host 'su -c' tar (may fail inside chroot)
  --host-dest PATH      Destination for host tar (default: $HOST_DEST)
  --verbose             Verbose output
  --dry-run             Do diagnostics only (no archive)
  -h, --help            Show this help
Examples:
  bash SCRIPTTHESE.sh --serve --bind 127.0.0.1 --port 8000 --verbose
  bash SCRIPTTHESE.sh --roots "/data/data,/usr/bin" --outdir /tmp/recover
USAGE
  exit 1
}

# parse args
while [[ $# -gt 0 ]]; do
  case "$1" in
    --outdir) OUT_DIR="$2"; LOG_DIR="${OUT_DIR}/logs"; MANIFEST="${OUT_DIR}/${NAME_PREFIX}_${TIMESTAMP}.manifest.txt"; shift 2;;
    --archive) COMBINED_ARCHIVE="$2"; COMBINED_PATH="${OUT_DIR}/${COMBINED_ARCHIVE}"; shift 2;;
    --roots) IFS=',' read -r -a CANDIDATES <<< "$2"; shift 2;;
    --serve) SERVE=true; shift;;
    --port) PORT="$2"; shift 2;;
    --bind) BIND_ADDR="$2"; shift 2;;
    --force) FORCE=true; shift;;
    --exec-host-tar) EXEC_HOST_TAR=true; shift;;
    --host-dest) HOST_DEST="$2"; shift 2;;
    --verbose) VERBOSE=true; shift;;
    --dry-run) DRY_RUN=true; shift;;
    -h|--help) usage;;
    *) echo "Unknown arg: $1"; usage;;
  esac
done

mkdir -p "$OUT_DIR" "$LOG_DIR"

# Determine if we can use sudo
SUDO=""
if [ "$(id -u)" -ne 0 ]; then
  if command -v sudo >/dev/null 2>&1; then
    SUDO="sudo"
  fi
fi

# --- Diagnostics functions ---
run_mount_check(){
  log "=== MOUNTS & STORAGE MAPPING ==="
  mount | egrep -i 'sdcard|emulated|storage|media|fuse' 2>/dev/null || mount | sed -n '1,120p' || true
  ls -ld /sdcard /storage /storage/emulated /storage/internal /data/media/0 2>/dev/null || true
  echo ""
}

find_techula_instances(){
  log "=== Finding tech.ula directories (limited depth) ==="
  $SUDO find / -maxdepth 6 -type d -iname 'tech.ula' -print 2>/dev/null | while read -r d; do
    echo "=== PATH: $d ==="
    # prefer SELinux-aware ls
    ls -ldZ "$d" 2>/dev/null || ls -ld "$d" 2>/dev/null || true
    du -sh "$d" 2>/dev/null || true
  done
  echo ""
}

list_top_contents(){
  local P="$1"
  [ -n "$P" ] || return
  echo "=== TOP (incl dotfiles) for: $P ==="
  # ensure dotfiles are shown
  shopt -s nullglob dotglob 2>/dev/null || true
  ls -al --color=never "$P" 2>/dev/null | sed -n '1,200p' || echo "ls failed or permission denied for $P"
  echo "--- sample hidden entries (depth 2) ---"
  find "$P" -maxdepth 2 -name '.*' -ls 2>/dev/null | head -n 40 || true
  echo ""
}

small_read_test(){
  local P="$1"
  [ -n "$P" ] || return
  echo "=== Small-file read test in: $P ==="
  smallf=$($SUDO find "$P" -type f -size -64k -print 2>/dev/null | head -n1 || true)
  if [ -n "$smallf" ]; then
    echo "SAMPLE_FILE=$smallf"
    $SUDO head -c 256 "$smallf" 2>/dev/null | sed -n '1,12p' || echo "read failed"
  else
    echo "no small file found or unreadable in $P"
  fi
  echo ""
}

# Build list of existing candidate sources
collect_candidates(){
  SOURCES=()
  for p in "${CANDIDATES[@]}"; do
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
}

# Write NUL-separated list of relative paths to read by tar -C /
write_sources_list(){
  : > "$TMP_LIST"
  for s in "${SOURCES[@]}"; do
    rel="${s#/}"
    rel="${rel%/}"
    if [ -z "$rel" ]; then rel="."; fi
    # write NUL-separated entries
    printf '%s\0' "$rel" >> "$TMP_LIST"
  done
  v "Wrote sources list to $TMP_LIST"
}

# Best-effort archive creation (skips unreadable files)
create_combined_archive(){
  if [ "$DRY_RUN" = true ]; then
    log "[dry-run] Would create combined archive: $COMBINED_PATH"
    return 0
  fi

  # ensure we run tar from /
  if [ -f "$TMP_LIST" ]; then
    log "Creating combined archive (best-effort, skipping unreadable files): $COMBINED_PATH"
    TAR_CMD=(tar -C / --null -T "$TMP_LIST" --warning=no-file-changed --ignore-failed-read -czf "$COMBINED_PATH")
    v "Running: ${TAR_CMD[*]}"
    $SUDO "${TAR_CMD[@]}" 2> "$LOG_DIR/tar.err"
    rc=$?
    if [ $rc -ne 0 ]; then
      err "tar exited $rc. See $LOG_DIR/tar.err (head):"
      sed -n '1,200p' "$LOG_DIR/tar.err" || true
    fi
    if [ -f "$COMBINED_PATH" ]; then
      log "Archive produced: $COMBINED_PATH"
      du -h "$COMBINED_PATH" || true
      tar -tzf "$COMBINED_PATH" | sed -n '1,200p' > "$MANIFEST" 2>/dev/null || true
      log "Manifest first lines:"
      sed -n '1,120p' "$MANIFEST" || true
    else
      err "Archive not created."
    fi
  else
    err "No sources list; nothing archived."
  fi
}

# Try copy to host-visible locations
copy_to_host_visible(){
  local copied=false
  for d in /storage/emulated/0 /sdcard /storage/internal; do
    if [ -d "$d" ]; then
      v "Trying copy to $d"
      $SUDO cp -av "$COMBINED_PATH" "$d/" 2> "$LOG_DIR/copy.err" || true
      if [ -f "$d/$(basename "$COMBINED_PATH")" ]; then
        log "Copied archive to: $d/$(basename "$COMBINED_PATH")"
        copied=true
      fi
    fi
  done
  if [ "$copied" = false ]; then
    log "No host-visible copy created (no common mount or permission)."
  fi
}

# Provide host-side tar snippet
write_host_snippet(){
  cat <<EOF > "$LOG_DIR/host-tar.snippet.txt"
# Run on Android host (CX root shell, Termux with su, or adb shell with root)
# Preferred: create archive of app data and user data in accessible storage
su -c 'tar -C /data -czf "${HOST_DEST}" data/user/0 data/data'
# If tech.ula known location:
su -c 'tar -C /data -czf "${HOST_DEST}" data/data/tech.ula'
# After running on host, check inside chroot:
ls -lh /storage/emulated/0/$(basename "${HOST_DEST}") /sdcard/$(basename "${HOST_DEST}") /storage/internal/$(basename "${HOST_DEST}")
EOF
  log "Wrote host-tar snippet to: $LOG_DIR/host-tar.snippet.txt"
}

# Attempt to run host tar (may fail inside chroot)
attempt_exec_host_tar(){
  if [ "$EXEC_HOST_TAR" != true ]; then return; fi
  log "Attempting to run host 'su -c' tar from this environment (may fail)"
  $SUDO sh -c "su -c 'tar -C /data -czf \"${HOST_DEST}\" data/user/0 data/data'" 2> "$LOG_DIR/exec_host.err" || true
  sed -n '1,200p' "$LOG_DIR/exec_host.err" || true
}

# Start HTTP server to serve OUT_DIR (background)
start_http_server(){
  if ! command -v python3 >/dev/null 2>&1; then
    err "python3 not found; cannot start HTTP server."
    return 1
  fi
  local serve_dir
  serve_dir="$(dirname "$COMBINED_PATH")"
  (cd "$serve_dir" && nohup python3 -m http.server "$PORT" --bind "$BIND_ADDR" > "$LOG_DIR/http.out" 2>&1 & echo $! > "$LOG_DIR/http.pid")
  sleep 0.2
  if [ -f "$LOG_DIR/http.pid" ]; then
    pid=$(cat "$LOG_DIR/http.pid" 2>/dev/null || echo "")
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      log "HTTP server started: http://${BIND_ADDR}:${PORT}/$(basename "$COMBINED_PATH")  (PID $pid)"
      return 0
    fi
  fi
  err "Failed to start HTTP server; check $LOG_DIR/http.out"
  return 1
}

# ---------- Main flow ----------
log "Starting SCRIPTTHESE.sh diagnostics at $(date)"

run_mount_check
find_techula_instances

# show top-level for commonly expected paths
for p in /data/data/tech.ula /data/user/0/tech.ula /data/user/0 /usr/bin; do
  if [ -e "$p" ]; then
    list_top_contents "$p"
    small_read_test "$p"
  fi
done

collect_candidates

# If no candidates found, bail
if [ ${#SOURCES[@]} -eq 0 ]; then
  err "No candidate sources found. Exiting."
  write_host_snippet
  exit 2
fi

# If dry-run, stop after diagnostics and show seeds
if [ "$DRY_RUN" = true ]; then
  log "[dry-run] candidate sources:"
  for s in "${SOURCES[@]}"; do log " - $s"; done
  write_host_snippet
  exit 0
fi

# prompt if likely large and not forced
if [ "$FORCE" != true ]; then
  est=$(du -sh "${SOURCES[@]}" 2>/dev/null | tail -n1 | awk '{print $1}' || echo "unknown")
  log "Estimated sample size (du): $est"
  read -p "Proceed to create best-effort archive of the listed sources? (y/N) " yn
  case "$yn" in [Yy]*) ;; *) log "Aborted by user."; exit 0;; esac
fi

# Build sources list and archive
write_sources_list
create_combined_archive

# If archive created, try to copy to host-visible storage
if [ -f "$COMBINED_PATH" ] && [ "$(stat -c%s "$COMBINED_PATH" 2>/dev/null || echo 0)" -ge 1024 ]; then
  copy_to_host_visible
else
  err "Combined archive missing or too small â€” likely host protections. Creating host snippet."
  write_host_snippet
  attempt_exec_host_tar
fi

# If serve requested and archive exists, start server
if [ "$SERVE" = true ] && [ -f "$COMBINED_PATH" ]; then
  start_http_server || true
fi

log "Finished. Outputs in: $OUT_DIR"
log "Archive (if created): $COMBINED_PATH"
log "Logs: $LOG_DIR"
[ -f "$LOG_DIR/host-tar.snippet.txt" ] && log "Host tar snippet: $LOG_DIR/host-tar.snippet.txt"
log "Run: tar -tzf $COMBINED_PATH | head -n 200   (to list archive contents)"
exit 0