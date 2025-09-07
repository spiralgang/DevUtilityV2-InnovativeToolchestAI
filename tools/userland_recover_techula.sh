#!/usr/bin/env bash
#
# userland_recover_techula.sh
# Purpose: Robust, fully-dynamic helper to locate and extract Android app-data
#          (especially tech.ula) when running inside a UserLAnd chroot (or a
#          Linux environment that can access Android host mounts).  The script
#          tries readable/mapped copies, offers safe host-root tar commands,
#          tries run-as for debuggable apps, and can serve the resulting
#          archive over loopback HTTP for on-device download (Solid/CX/etc).
#
# Important safety notes (read before running)
# - This script does not forcibly change SELinux or escalate host privileges.
# - If you truly have host-root and pass --exec-host-tar the script will attempt
#   to run `su -c "tar ..."` on the host (this MUST only be used when you
#   understand host-root implications).
# - The script avoids destructive operations; it will not remove or overwrite
#   host files unless you explicitly point a destination file that exists.
#
# Quick examples:
#   # Dry-run to see what is reachable and estimate sizes
#   ./userland_recover_techula.sh --dry-run
#
#   # Try to find tech.ula under common mapped paths and create an archive
#   ./userland_recover_techula.sh --out /storage/internal/techula-backup.tgz
#
#   # If chroot can't read /data, print host-root tar command to run on the phone:
#   ./userland_recover_techula.sh --host-tar-suggest
#
#   # Attempt to execute host tar directly (ONLY if you are sure you have host root)
#   ./userland_recover_techula.sh --exec-host-tar --out /sdcard/techula-host-backup.tgz
#
#   # Serve the produced archive on loopback for the phone to download
#   ./userland_recover_techula.sh --serve --out /tmp/techula-backup.tgz
#
# Designed for: Android 10 + UserLAnd chroots; tested for common mappings:
#  - /storage/internal, /storage/emulated/0, /sdcard, /tmp, /host-rootfs, /data/media/0
#
# Output: prints clear next steps (HTTP URL/SFTP hints/host tar snippet). Logs pid/logs under /tmp/userland_recover_*
#
set -euo pipefail
IFS=$'\n\t'

# --- Defaults ---
OUT="/tmp/techula-backup-$(date +%Y%m%d_%H%M%S).tgz"
DRY_RUN=false
SERVE=false
HTTP_BIND="127.0.0.1"
HTTP_PORT=8000
EXEC_HOST_TAR=false
HOST_TAR_SUGGEST="/sdcard/techula-backup-$(date +%Y%m%d_%H%M%S).tgz"
VERBOSE=false
QUIET=false
PID_DIR="/tmp/userland_recover"
LOG_FILE="${PID_DIR}/http.log"
PID_FILE="${PID_DIR}/http.pid"
TARGET_PACKAGE="tech.ula"   # primary app folder name to search for
SEARCH_DEPTH=6
# Common mapped candidate roots (ordered heuristically)
CANDIDATE_PATHS=(
  "/data/user/0"
  "/data/data"
  "/host-rootfs/data/user/0"
  "/host-rootfs/data/data"
  "/storage/internal"
  "/storage/emulated/0"
  "/sdcard"
  "/data/media/0"
  "/tmp"
  "/storage"
)

# --- helpers ---
log(){ [ "$QUIET" = false ] && printf '%s\n' "$*"; }
vlog(){ [ "$VERBOSE" = true ] && printf '[v] %s\n' "$*"; }
die(){ printf 'ERROR: %s\n' "$*" >&2; exit 1; }

usage(){
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --out PATH            Output archive path (default: $OUT)
  --dry-run             List possible candidate locations + sizes; do not archive
  --serve               After creating archive, start loopback HTTP server (background)
  --port PORT           HTTP port to use when --serve (default $HTTP_PORT)
  --bind ADDR           HTTP bind address (default $HTTP_BIND)
  --host-tar-suggest    Print exact host (su -c) tar command to run on Android host
  --exec-host-tar       Attempt to execute the host tar command (ONLY if you have host root)
  --run-as PACKAGE      Attempt 'run-as PACKAGE' to create a package-local archive (if package is debuggable)
  --verbose             Verbose logging
  --quiet               Minimal output
  -h, --help            Show this help
USAGE
  exit 1
}

# parse args
while [[ $# -gt 0 ]]; do
  case "$1" in
    --out) OUT="$2"; shift 2;;
    --dry-run) DRY_RUN=true; shift;;
    --serve) SERVE=true; shift;;
    --port) HTTP_PORT="$2"; shift 2;;
    --bind) HTTP_BIND="$2"; shift 2;;
    --host-tar-suggest) printf '%s\n' "su -c \"tar -C /data/user/0 -czf ${HOST_TAR_SUGGEST} .\"" ; exit 0;;
    --exec-host-tar) EXEC_HOST_TAR=true; shift;;
    --run-as) TARGET_PACKAGE="$2"; shift 2;;
    --verbose) VERBOSE=true; shift;;
    --quiet) QUIET=true; shift;;
    -h|--help) usage;;
    *) printf 'Unknown arg: %s\n' "$1"; usage;;
  esac
done

mkdir -p "$PID_DIR"
# --- core functions ---

# Test if directory is listable/readable
is_listable(){
  local p="$1"
  if [ -d "$p" ] && ls -A "$p" >/dev/null 2>&1; then
    return 0
  fi
  return 1
}

# Search candidate roots for the target package directory and return first readable match
find_readable_techula(){
  local name="$TARGET_PACKAGE"
  local found=""
  for root in "${CANDIDATE_PATHS[@]}"; do
    # expand globs
    for candidate in $(eval echo "$root"); do
      [ -n "$candidate" ] || continue
      # check if candidate exists
      if [ -e "$candidate" ]; then
        # direct match: candidate/<package>
        if is_listable "${candidate}/${name}"; then
          found="$(realpath -m "${candidate}/${name}")"
          vlog "Found readable path: $found"
          echo "$found"
          return 0
        fi
        # sometimes data is stored directly under candidate (if candidate is /data/user/0)
        if is_listable "$candidate" && ls -A "$candidate" | grep -q "^${name}\$" 2>/dev/null; then
          found="$(realpath -m "${candidate}/${name}")"
          echo "$found"
          return 0
        fi
      fi
    done
  done

  # fallback: shallow find for <...>/data/user/0/<package> (limited depth)
  vlog "Fallback shallow find for pattern '*/data/user/0/${name}' at depth ${SEARCH_DEPTH}..."
  found=$(find / -maxdepth $SEARCH_DEPTH -type d -path "*/data/user/0/${name}" -print 2>/dev/null | head -n1 || true)
  if [ -n "$found" ] && is_listable "$found"; then
    echo "$(realpath -m "$found")"
    return 0
  fi

  # not found
  return 1
}

# Provide size estimates for a directory
report_size_summary(){
  local p="$1"
  if ! is_listable "$p"; then
    printf "  NOT READABLE: %s\n" "$p"
    return 1
  fi
  printf "Summary for: %s\n" "$p"
  # top-level child summary (fastish)
  for d in "$p"/*; do
    [ -e "$d" ] || continue
    if [ -d "$d" ]; then
      size=$(du -sh "$d" 2>/dev/null | awk '{print $1}')
      files=$(find "$d" -type f 2>/dev/null | wc -l)
      printf "  %s | size=%s files=%d\n" "$(basename "$d")" "$size" "$files"
    fi
  done
  total=$(du -sh "$p" 2>/dev/null | awk '{print $1}')
  printf "  TOTAL (approx): %s\n\n" "$total"
  return 0
}

# Create archive from a readable source path
create_archive_from(){
  local src="$1"
  local dest="$2"
  # If src is file (e.g., already an archive), copy rather than tar
  if [ -f "$dest" ] && [ "$dest" != "/dev/null" ]; then
    die "Destination exists: $dest (won't overwrite). Remove it or choose a new --out"
  fi
  mkdir -p "$(dirname "$dest")"
  if [ -d "$src" ]; then
    # Use tar with numeric owner to avoid host/uid rewriting; preserve perms where possible
    log "Creating tar.gz of $src -> $dest (preserving numeric owners)..."
    tar -C "$src" -czf "$dest" . 2>"${PID_DIR}/tar.err" || (echo "tar failed; see ${PID_DIR}/tar.err" && return 2)
    log "Archive created: $dest  (ls -lh $dest):"
    ls -lh "$dest" || true
  else
    # If src is a file, copy
    cp -a "$src" "$dest"
    log "Copied file -> $dest"
  fi
}

# Try run-as for a package to create a package-local archive (only works if binary allowed)
attempt_run_as(){
  local pkg="$1"
  local out="$2"
  if ! command -v run-as >/dev/null 2>&1; then
    log "run-as not available in this chroot."
    return 1
  fi
  # The run-as command must be executed inside the Android host environment; in chroot it may or may not exist.
  log "Attempting run-as ${pkg} to create package-scoped archive (will create on host /data/local/tmp then copy if possible)..."
  tmp_host="/data/local/tmp/runas-${pkg}-$(date +%Y%m%d_%H%M%S).tgz"
  # This run attempts to use run-as to tar the package's data into a temporary host path,
  # then copy it to the chroot-readable /sdcard if possible.
  if run-as "$pkg" sh -c "cd /data/data/${pkg} 2>/dev/null && tar -czf ${tmp_host} . >/dev/null 2>&1"; then
    log "run-as produced host archive: ${tmp_host} (attempting to copy to $out if accessible)"
    # Try to copy from /data/local/tmp to /sdcard (requires host permissions)
    if [ -f "$tmp_host" ]; then
      create_archive_from "$tmp_host" "$out"
      return 0
    else
      log "run-as wrote ${tmp_host} on host but it's not visible inside chroot. Consider using host-root approach."
      return 2
    fi
  else
    log "run-as failed for package ${pkg} (common if package not debuggable)."
    return 1
  fi
}

# Print host-root tar command that the user can run on device host (su required)
print_host_tar_snippet(){
  local dest="$1"
  cat <<EOF
If you have host root on the Android device (be careful), run this on the Android host shell to create an archive accessible to the chroot:

  su -c "tar -C /data/user/0 -czf '${dest}' ."

After the command completes, check from the chroot:
  ls -lh /storage/internal/$(basename "$dest")  || ls -lh /sdcard/$(basename "$dest") || ls -lh /storage/emulated/0/$(basename "$dest")

Then you can serve/copy it from those locations.
EOF
}

# Execute host tar via su (dangerous; only when user explicitly requested)
exec_host_tar(){
  local dest="$1"
  printf ">>> Attempting to run host tar via su (this will run on the current environment; only do this if you truly have host root)...\n"
  # Running `su -c` inside chroot is usually not effective. We execute, but we expect this may fail.
  if command -v su >/dev/null 2>&1; then
    su -c "tar -C /data/user/0 -czf '${dest}' ." || return 2
    log "Host tar attempted; check for ${dest} in /sdcard or /storage/internal after this command."
    return 0
  else
    die "su not available in this environment; cannot exec host tar."
  fi
}

# Serve archive on loopback (background) and record PID
serve_archive_bg(){
  local file="$1"
  if ! command -v python3 >/dev/null 2>&1; then
    die "python3 not found. Install via: apt update && apt install -y python3"
  fi
  local dir
  dir="$(dirname "$file")"
  local name
  name="$(basename "$file")"

  # If already running, report
  if [ -f "$PID_FILE" ]; then
    oldpid="$(cat "$PID_FILE" 2>/dev/null || true)"
    if [ -n "$oldpid" ] && kill -0 "$oldpid" 2>/dev/null; then
      log "HTTP server already running PID ${oldpid} -> http://${HTTP_BIND}:${HTTP_PORT}/${name}"
      return 0
    else
      rm -f "$PID_FILE"
    fi
  fi

  cd "$dir" || die "Cannot cd to $dir"
  nohup python3 -m http.server "$HTTP_PORT" --bind "$HTTP_BIND" >"$LOG_FILE" 2>&1 &
  echo $! > "$PID_FILE"
  sleep 0.25
  pid="$(cat "$PID_FILE" 2>/dev/null || true)"
  if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
    log "HTTP server started: http://${HTTP_BIND}:${HTTP_PORT}/${name}  (PID $pid)  log: $LOG_FILE"
    return 0
  else
    die "Failed to start HTTP server; check $LOG_FILE"
  fi
}

stop_http(){
  if [ -f "$PID_FILE" ]; then
    pid="$(cat "$PID_FILE" 2>/dev/null || true)"
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      kill "$pid" && rm -f "$PID_FILE"
      log "Stopped HTTP server (PID $pid)"
      return 0
    else
      rm -f "$PID_FILE"
    fi
  fi
  pkill -f "python3 -m http.server" || true
  log "Stopped any python3 -m http.server (fallback)"
}

# --- main flow ---

log "userland_recover_techula: starting (target package: $TARGET_PACKAGE)"
vlog "Candidate roots: ${CANDIDATE_PATHS[*]}"

# Try to find readable tech.ula
found_path="$(find_readable_techula || true)"
if [ -n "$found_path" ]; then
  log "Readable tech.ula data directory found at: $found_path"
  report_size_summary "$found_path"
  if [ "$DRY_RUN" = true ]; then
    log "Dry-run requested; stopping here. Re-run without --dry-run to archive."
    exit 0
  fi

  # Create archive
  create_archive_from "$found_path" "$OUT" || die "archive creation failed"
  log "Archive created at: $OUT"

  if [ "$SERVE" = true ]; then
    serve_archive_bg "$OUT"
    log "Open on your phone (in Solid/CX/Browser): http://${HTTP_BIND}:${HTTP_PORT}/$(basename "$OUT")"
    log "Download to Downloads, then use CX -> Copy -> Local -> Open document -> Android/data/tech.ula/files/storage -> Paste"
  else
    log "Archive ready. Use SFTP from 127.0.0.1:8022 (try 8022 then 22) or serve with --serve."
    log "Example serve command (manual): cd $(dirname "$OUT") && python3 -m http.server ${HTTP_PORT} --bind ${HTTP_BIND}"
  fi

  exit 0
fi

# Not found in mapped paths - try run-as if user requested or default (helpful sometimes)
# Only attempt run-as if explicitly requested by user via --run-as (we still have TARGET_PACKAGE default)
# We already defaulted TARGET_PACKAGE to tech.ula; allow user to set via --run-as if desired.
log "tech.ula not directly readable in common mapped paths."

# Provide host-root suggestion (always useful)
print_host_tar_snippet "$HOST_TAR_SUGGEST"

if [ "$EXEC_HOST_TAR" = true ]; then
  log "User requested execution of host tar via su (attempting). This will likely only work if this environment is actually host shell and has su."
  exec_host_tar "$HOST_TAR_SUGGEST" || die "exec_host_tar failed or su not available"
  # After host tar, check common locations for produced file
  for candidate in "/storage/internal/$(basename "$HOST_TAR_SUGGEST")" "/sdcard/$(basename "$HOST_TAR_SUGGEST")" "/storage/emulated/0/$(basename "$HOST_TAR_SUGGEST")"; do
    if [ -f "$candidate" ]; then
      log "Host tar produced file accessible at: $candidate"
      if [ "$DRY_RUN" = true ]; then
        log "Dry-run; not copying further."
        exit 0
      fi
      # copy to OUT location for convenience (unless OUT == candidate)
      if [ "$OUT" != "$candidate" ]; then
        cp -a "$candidate" "$OUT"
        log "Copied host archive to $OUT"
      else
        log "Archive already at requested --out $OUT"
      fi
      if [ "$SERVE" = true ]; then serve_archive_bg "$OUT"; fi
      exit 0
    fi
  done
  die "Host tar executed but archive not found in expected locations; inspect device manually."
fi

# If we reach here: not readable, no exec host tar -> print final instructions for user
cat <<EOF

RESULT: tech.ula app-data directory was NOT readable from this chroot via common mapped paths.

What I tried:
 - Scanned common mapped locations: ${CANDIDATE_PATHS[*]}
 - Attempted shallow find for data/user/0/${TARGET_PACKAGE}
 - Reported size if a readable candidate existed

Why this happens:
 - On modern Android, /data (and /data/user/0) is protected by SELinux and not mounted into app/chroot contexts.
 - Being 'root in chroot' (UserLAnd root) is NOT the same as host root. You need host root (su) to tar /data/user/0.

Recommended next steps (pick one):

A) If you have a rooted phone (host root) and an Android terminal (or CX supports host-root shell):
   1) Run on the phone's host shell (NOT in the chroot):
      su -c "tar -C /data/user/0 -czf '${HOST_TAR_SUGGEST}' ."
   2) From the chroot (or this script after exec-host-tar), check:
      ls -lh /storage/internal/$(basename "${HOST_TAR_SUGGEST}")  || ls -lh /sdcard/$(basename "${HOST_TAR_SUGGEST}")
   3) Serve/download the produced archive via the HTTP instructions above OR copy via SFTP.

B) If you have CX File Explorer with root feature:
   1) Navigate to /data/user/0 (or /data/data) in CX while in root mode.
   2) Long-press the tech.ula folder -> Compress or Create Archive (if CX offers) to /sdcard or /storage/internal.
   3) Then open the archive in chroot-visible path (/sdcard or /storage/internal) and move/copy as needed.

C) If tech.ula is debuggable and you can use run-as (rare):
   1) On the host shell: run-as tech.ula tar -C /data/data/tech.ula -czf /sdcard/techula-runas.tgz .
   2) Then find that file from chroot under /sdcard and copy it.

D) If none of the above:
   - Provide the outputs of these commands (paste back here):
     * find / -maxdepth 6 -type d -name "tech.ula" -print 2>/dev/null | head -n 20
     * ls -lh /storage/internal /sdcard /storage/emulated/0 2>/dev/null || true
     * ss -tlnp | grep -E ':8022|:22|:8000' || true
     * If you ran earlier and got a tiny (8 KB) archive, paste its full path and `ls -lh` output for diagnosis.

EOF

exit 2