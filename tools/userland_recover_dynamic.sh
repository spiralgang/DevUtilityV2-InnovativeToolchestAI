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
# userland_recover_dynamic.sh
# Dynamic, defensive helper to locate & extract Android app-data (tech.ula) from
# inside a UserLAnd chroot. Preserves hidden (dot) files, reports accessibility,
# attempts run-as if requested, prints/executes host-root tar snippet if needed,
# and can serve the result over loopback HTTP for on-device download.
#
# Design notes:
# - Works inside chroot; does not bypass host protections. If /data is protected,
#   it will print the exact su -c tar command to run on the Android host.
# - To include dotfiles reliably, archives are created with: tar -C "$SRC" -czf "$OUT" .
# - Safe-by-default: dry-run mode, size estimate, confirmation for large archives.
# - Keeps logs & pids under /tmp/userland_recover_dynamic.
#
# Usage examples:
#   ./userland_recover_dynamic.sh --dry-run --source /data/data/tech.ula
#   ./userland_recover_dynamic.sh --source /data/data/tech.ula --out /tmp/techula.tgz --serve
#   ./userland_recover_dynamic.sh --host-tar-suggest
#   ./userland_recover_dynamic.sh --exec-host-tar --host-dest /sdcard/techula.tgz
#
set -euo pipefail
IFS=$'\n\t'

# Defaults - override via CLI
SOURCE=""                                 # path to directory or file (auto-resolve if empty)
OUT="/tmp/techula-backup-$(date +%Y%m%d_%H%M%S).tgz"
DRY_RUN=false
SERVE=false
HTTP_BIND="127.0.0.1"
HTTP_PORT=8000
FORCE=false
EXEC_HOST_TAR=false
HOST_DEST=""                              # where to place host tar when exec-host-tar
TRY_RUNAS=""                              # package name to try run-as
VERBOSE=false

PID_DIR="/tmp/userland_recover_dynamic"
LOG_FILE="$PID_DIR/run.log"
PID_FILE="$PID_DIR/http.pid"
TAR_ERR="$PID_DIR/tar.err"
mkdir -p "$PID_DIR"

log(){ printf '%s\n' "$*"; }
v(){ [ "$VERBOSE" = true ] && printf '[v] %s\n' "$*"; }
die(){ printf 'ERROR: %s\n' "$*" >&2; exit 1; }

usage(){
  cat <<USAGE
userland_recover_dynamic.sh - find & archive Android app data (tech.ula)
Options:
  --source PATH         Path to app-data dir or file (default: auto-resolve common mappings)
  --out PATH            Output tar.gz (default: $OUT)
  --dry-run             Inspect & report (no tar)
  --serve               After creating archive, start loopback HTTP server (background)
  --port N              HTTP port for serve (default: $HTTP_PORT)
  --bind ADDR           HTTP bind address (default: $HTTP_BIND)
  --host-tar-suggest    Print exact host 'su -c "tar ..."' snippet for host-root extraction
  --exec-host-tar       Attempt to run host tar via su (only if you truly have host root)
  --host-dest PATH      Host destination for exec-host-tar (default: /sdcard/<basename>)
  --run-as PKG          Try 'run-as PKG' to create package-scoped archive (works if debuggable)
  --force               Don't prompt for large archive confirmation
  --verbose             Verbose output
  -h, --help            Show this help
Examples:
  $0 --dry-run --source /data/data/tech.ula
  $0 --source /data/data/tech.ula --out /tmp/techula.tgz --serve
  $0 --host-tar-suggest
USAGE
  exit 1
}

# arg parse
while [[ $# -gt 0 ]]; do
  case "$1" in
    --source) SOURCE="$2"; shift 2;;
    --out) OUT="$2"; shift 2;;
    --dry-run) DRY_RUN=true; shift;;
    --serve) SERVE=true; shift;;
    --port) HTTP_PORT="$2"; shift 2;;
    --bind) HTTP_BIND="$2"; shift 2;;
    --host-tar-suggest) printf '%s\n' "su -c \"tar -C /data/user/0 -czf /sdcard/$(basename "$OUT") .\""; exit 0;;
    --exec-host-tar) EXEC_HOST_TAR=true; shift;;
    --host-dest) HOST_DEST="$2"; shift 2;;
    --run-as) TRY_RUNAS="$2"; shift 2;;
    --force) FORCE=true; shift;;
    --verbose) VERBOSE=true; shift;;
    -h|--help) usage;;
    *) echo "Unknown arg: $1"; usage;;
  esac
done

# candidate roots to probe (heuristic, ordered)
CANDIDATES=(
  "/data/user/0"
  "/data/data"
  "/host-rootfs/data/user/0"
  "/host-rootfs/data/data"
  "/storage/internal"
  "/storage/emulated/0"
  "/sdcard"
  "/data/media/0"
  "/tmp"
)

# Resolve source: if provided and exists, use it; else probe candidates for tech.ula
resolve_source(){
  if [ -n "$SOURCE" ]; then
    SOURCE="$(realpath -m "$SOURCE" 2>/dev/null || echo "$SOURCE")"
    if [ -e "$SOURCE" ]; then
      v "User-specified source resolved to: $SOURCE"
      return 0
    else
      die "Requested source does not exist inside chroot: $SOURCE"
    fi
  fi

  # try tech.ula first, then any readable data/user/0
  for root in "${CANDIDATES[@]}"; do
    # expand possible globs
    for cand in $(eval echo $root); do
      [ -n "$cand" ] || continue
      # prefer package path
      if [ -d "$cand/tech.ula" ]; then
        SOURCE="$(realpath -m "$cand/tech.ula")"
        v "Found tech.ula at: $SOURCE"
        return 0
      fi
    done
  done

  # fallback: find shallow for tech.ula (limit depth)
  found="$(find / -maxdepth 6 -type d -name 'tech.ula' -print 2>/dev/null | head -n1 || true)"
  if [ -n "$found" ]; then
    SOURCE="$(realpath -m "$found")"
    v "Found tech.ula via find: $SOURCE"
    return 0
  fi

  # if no tech.ula, try to find /data/user/0 readable
  for root in "${CANDIDATES[@]}"; do
    for cand in $(eval echo $root); do
      if [ -d "$cand" ]; then
        SOURCE="$(realpath -m "$cand")"
        v "Using candidate root: $SOURCE"
        return 0
      fi
    done
  done

  die "Could not resolve any candidate source paths inside chroot. If you have host root, run with --host-tar-suggest on the phone host."
}

# check readability/listability
is_listable(){
  local d="$1"
  [ -d "$d" ] && ls -A "$d" >/dev/null 2>&1
}

# count top-level dot entries
count_top_dotfiles(){
  local dir="$1"
  (
    shopt -s nullglob dotglob
    set -- "$dir"/.[!.]* "$dir"/..?*
    cnt=0
    for p in "$@"; do [ -e "$p" ] && ((cnt++)); done
    printf '%d' "$cnt"
  )
}

# recursive dot files count
count_recursive_dotfiles(){
  local dir="$1"
  find "$dir" -type f -name '.*' 2>/dev/null | wc -l || echo 0
}

# sample listing for dotfiles
list_sample_dotfiles(){
  local dir="$1"
  find "$dir" -maxdepth 3 -name '.*' 2>/dev/null | head -n 40 || true
}

# small-read test (non-destructive)
small_read_test(){
  local dir="$1"
  sample="$(find "$dir" -type f -size -64k -print 2>/dev/null | head -n1 || true)"
  if [ -n "$sample" ]; then
    v "Small file sample: $sample"
    head -c 256 "$sample" 2>/dev/null | sed -n '1,6p' || true
    echo "SMALL_READ_OK"
    return 0
  else
    echo "NO_SMALL_FILE"
    return 1
  fi
}

# show basic attrs
show_attrs(){
  local d="$1"
  echo "---- ATTRS: $d ----"
  ls -ld "$d" 2>/dev/null || true
  command -v stat >/dev/null 2>&1 && stat -c 'mode=%A uid=%u gid=%g mtime=%y %n' "$d" 2>/dev/null || true
  command -v ls >/dev/null 2>&1 && ls -ldZ "$d" 2>/dev/null || true
  echo "---------------------"
}

# create archive (tar from inside dir to include dotfiles)
create_archive(){
  local src="$1" dest="$2"
  if [ -f "$dest" ]; then
    die "Destination $dest exists. Remove or choose another --out"
  fi
  mkdir -p "$(dirname "$dest")"
  if [ -d "$src" ]; then
    # create tar from inside to ensure dotfiles are included: tar -C "$src" -czf "$dest" .
    v "Running: tar -C '$src' -czf '$dest' ."
    if tar -C "$src" -czf "$dest" . 2> "$TAR_ERR"; then
      log "ARCHIVE_OK"
      return 0
    else
      return 2
    fi
  elif [ -f "$src" ]; then
    cp -a "$src" "$dest"
    return 0
  else
    return 3
  fi
}

# start python HTTP server background
start_http_bg(){
  local file="$1"
  if ! command -v python3 >/dev/null 2>&1; then
    die "python3 missing in chroot. Install or run --host-tar approach."
  fi
  local dir name pid
  dir="$(dirname "$file")"
  name="$(basename "$file")"
  if [ -f "$PID_FILE" ]; then
    oldpid="$(cat "$PID_FILE" 2>/dev/null || true)"
    if [ -n "$oldpid" ] && kill -0 "$oldpid" 2>/dev/null; then
      log "HTTP already running at http://${HTTP_BIND}:${HTTP_PORT}/${name} (PID $oldpid)"
      return 0
    else
      rm -f "$PID_FILE"
    fi
  fi
  cd "$dir" || die "cannot cd $dir"
  nohup python3 -m http.server "$HTTP_PORT" --bind "$HTTP_BIND" > "$LOG_FILE" 2>&1 &
  echo $! > "$PID_FILE"
  sleep 0.25
  pid="$(cat "$PID_FILE" 2>/dev/null || true)"
  if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
    log "HTTP server: http://${HTTP_BIND}:${HTTP_PORT}/${name}  PID:$pid  log:$LOG_FILE"
  else
    die "Failed to start HTTP server; check $LOG_FILE"
  fi
}

# stop http
stop_http(){
  if [ -f "$PID_FILE" ]; then
    pid="$(cat "$PID_FILE" 2>/dev/null || true)"
    [ -n "$pid" ] && kill "$pid" 2>/dev/null || true
    rm -f "$PID_FILE" || true
    log "Stopped HTTP (PID $pid)"
  else
    pkill -f "python3 -m http.server" || true
    log "Stopped python simple HTTP (pkill fallback)"
  fi
}

# print host-root tar snippet
print_host_snippet(){
  local dest="${HOST_DEST:-/sdcard/$(basename "$OUT")}"
  cat <<EOF
HOST-ROOT FALLBACK (run this on the ANDROID HOST with su):
  su -c "tar -C /data/user/0 -czf '${dest}' ."

After completion, check inside chroot for:
  ls -lh /storage/internal/$(basename "$dest")  || ls -lh /sdcard/$(basename "$dest") || ls -lh /storage/emulated/0/$(basename "$dest")
Then serve or copy from that path.

Note: This requires host/root (su) on the phone. Do NOT run inside the chroot unless you know the environment provides host 'su'.
EOF
}

# attempt exec host tar via su (may fail inside chroot unless host-provided)
exec_host_tar(){
  local dest="${HOST_DEST:-/sdcard/$(basename "$OUT")}"
  if ! command -v su >/dev/null 2>&1; then
    die "su not found in this environment; cannot exec host tar."
  fi
  log "Attempting: su -c \"tar -C /data/user/0 -czf '${dest}' .\"  (this runs where 'su' is available)"
  if su -c "tar -C /data/user/0 -czf '${dest}' ." 2> "$PID_DIR/exec_host_tar.err"; then
    log "Host tar executed; now check /sdcard/$(basename "$dest") inside chroot."
    return 0
  else
    log "exec-host-tar failed; stderr:"
    sed -n '1,200p' "$PID_DIR/exec_host_tar.err" || true
    return 2
  fi
}

# attempt run-as packaging (rarely works unless debuggable)
attempt_run_as(){
  local pkg="$1"
  local dest="$2"
  if ! command -v run-as >/dev/null 2>&1; then
    log "run-as not available here."
    return 1
  fi
  tmp="/data/local/tmp/runas-${pkg}-$(date +%Y%m%d_%H%M%S).tgz"
  log "Trying: run-as $pkg sh -c 'cd /data/data/$pkg && tar -czf $tmp .'"
  if run-as "$pkg" sh -c "cd /data/data/$pkg && tar -czf $tmp ." 2> "$PID_DIR/runas.err"; then
    log "run-as produced host tmp archive: $tmp"
    # try to copy to /sdcard if possible
    if [ -f "$tmp" ]; then
      cp -a "$tmp" "$dest" 2>/dev/null || true
      [ -f "$dest" ] && log "Copied run-as-produced archive to $dest" && return 0
    fi
    log "run-as worked but archive not visible inside chroot; consider host-root approach."
    return 2
  else
    log "run-as failed; stderr:"
    sed -n '1,200p' "$PID_DIR/runas.err" || true
    return 3
  fi
}

# main flow
main(){
  resolve_source

  echo "Resolved SOURCE: $SOURCE"
  show_attrs "$SOURCE"

  if ! is_listable "$SOURCE"; then
    echo "Directory exists but not listable/readable from this chroot (likely host protections)."
    print_host_snippet
    exit 2
  fi

  # dotfile reports
  topd="$(count_top_dotfiles "$SOURCE")"
  recd="$(count_recursive_dotfiles "$SOURCE")"
  echo "Top-level hidden entries: $topd"
  echo "Recursive hidden-files count: $recd"
  if [ "$topd" -gt 0 ] || [ "$recd" -gt 0 ]; then
    echo "Sample hidden entries (depth 3):"
    list_sample_dotfiles "$SOURCE"
  else
    echo "(No dotfiles found quickly; could still exist deeper or be unreadable.)"
  fi

  # small-read test
  sr="$(small_read_test "$SOURCE" || true)"
  if [ "$sr" = "SMALL_READ_OK" ]; then
    echo "Small-file read test: OK"
  else
    echo "No small readable file found quickly (may still be readable or permission-blocked)."
  fi

  # dry-run stops here
  if [ "$DRY_RUN" = true ]; then
    echo "Dry-run completed. Re-run without --dry-run to create the archive."
    exit 0
  fi

  # show size estimate
  approx="$(du -sh "$SOURCE" 2>/dev/null | awk '{print $1}' || echo 'unknown')"
  echo "Estimated size (du -sh): $approx"
  if [ "$FORCE" != true ]; then
    printf "About to create archive of %s -> %s\n" "$SOURCE" "$OUT"
    read -p "Proceed? (y/N) " ans
    case "$ans" in
      [Yy]*) ;;
      *) echo "Aborted."; exit 0;;
    esac
  fi

  # attempt run-as if requested
  if [ -n "$TRY_RUNAS" ]; then
    attempt_run_as "$TRY_RUNAS" "$OUT" && { echo "run-as archive ready at $OUT"; [ "$SERVE" = true ] && start_http_bg "$OUT"; exit 0; } || true
  fi

  # try create archive
  if create_archive "$SOURCE" "$OUT"; then
    log "Archive created: $OUT"
    ls -lh "$OUT" || true
    if [ "$SERVE" = true ]; then
      start_http_bg "$OUT"
    else
      log "To serve: cd $(dirname "$OUT") && python3 -m http.server $HTTP_PORT --bind $HTTP_BIND"
    fi
    exit 0
  else
    echo "Archive creation failed. Tar stderr (head):"
    [ -f "$TAR_ERR" ] && sed -n '1,200p' "$TAR_ERR" || true
    echo ""
    echo "Likely cause: permission/SELinux/host protections blocking read of /data."
    print_host_snippet

    if [ "$EXEC_HOST_TAR" = true ]; then
      # set default HOST_DEST if empty
      HOST_DEST="${HOST_DEST:-/sdcard/$(basename "$OUT")}"
      exec_host_tar || die "exec-host-tar attempt failed."
      # After exec, try to locate it inside chroot
      for candidate in "/storage/internal/$(basename "$HOST_DEST")" "/sdcard/$(basename "$HOST_DEST")" "/storage/emulated/0/$(basename "$HOST_DEST")"; do
        if [ -f "$candidate" ]; then
          log "Found host-produced archive at: $candidate"
          cp -a "$candidate" "$OUT" 2>/dev/null || true
          [ -f "$OUT" ] && log "Copied to $OUT"
          [ "$SERVE" = true ] && start_http_bg "$OUT"
          exit 0
        fi
      done
      die "Host tar executed but produced archive not found in expected locations."
    fi

    exit 3
  fi
}

# run
main